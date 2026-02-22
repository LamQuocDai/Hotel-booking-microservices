package service

import (
	"bytes"
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log"
	"net/http"
	"payment-service/internal/config"
	"payment-service/internal/model"
	"payment-service/internal/store"
	"strconv"
	"time"

	"github.com/google/uuid"
)

type MoMoService struct {
	paymentStore store.PaymentStore
	config       *config.MoMoConfig
}

func NewMoMoService(paymentStore store.PaymentStore, cfg *config.MoMoConfig) *MoMoService {
	return &MoMoService{
		paymentStore: paymentStore,
		config:       cfg,
	}
}

// CreateCheckout creates a MoMo payment checkout
func (s *MoMoService) CreateCheckout(req *model.MoMoCheckoutRequest) (*model.MoMoCheckoutResponse, error) {
	// Validate configuration
	if err := s.validateConfig(); err != nil {
		return nil, fmt.Errorf("invalid MoMo configuration: %w", err)
	}

	// Generate unique IDs
	requestID := uuid.New().String()
	
	// Create payment record first
	payment := &model.Payment{
		ID:                    uuid.New(),
		Status:                model.PaymentStatusPending,
		Total:                 float64(req.Amount),
		TotalPrice:            float64(req.Amount), // Will be updated if promotion applied
		Currency:              "VND", // MoMo only supports VND
		PaymentMethod:         "MOMO",
		CustomerID:            req.CustomerID,
		RoomBookingIDs:        req.RoomBookingIDs,
		Description:           req.OrderInfo,
		Provider:              "MOMO",
		RequestID:             requestID,
		OrderID:               req.OrderID,
		ExpiredAt:             &[]time.Time{time.Now().Add(15 * time.Minute)}[0], // MoMo payments expire in 15 minutes
	}

	// Apply promotion if provided
	if req.PromotionID != nil {
		// This would typically be handled by payment service with promotion logic
		// For now, just store the promotion ID
		payment.PromotionID = req.PromotionID
	}

	// Save payment to database
	if err := s.paymentStore.CreatePayment(payment); err != nil {
		return nil, fmt.Errorf("failed to create payment record: %w", err)
	}

	// Create MoMo API request
	momoReq := &model.MoMoAPIRequest{
		PartnerCode: s.config.PartnerCode,
		PartnerName: s.config.PartnerName,
		StoreId:     s.config.StoreID,
		RequestType: "payWithATM",
		IpnURL:      s.config.IpnURL,
		RedirectURL: req.RedirectURL,
		OrderID:     req.OrderID,
		Amount:      req.Amount,
		Lang:        "vi",
		OrderInfo:   req.OrderInfo,
		RequestID:   requestID,
		ExtraData:   req.ExtraData,
		AutoCapture: true,
	}

	// Generate signature
	signature, err := s.generateSignature(momoReq)
	if err != nil {
		return nil, fmt.Errorf("failed to generate signature: %w", err)
	}
	momoReq.Signature = signature

	// Call MoMo API
	momoResp, err := s.callMoMoAPI(momoReq)
	if err != nil {
		// Update payment status to failed
		payment.Status = model.PaymentStatusFailed
		payment.FailureReason = err.Error()
		if updateErr := s.paymentStore.UpdatePayment(payment.ID, payment); updateErr != nil {
			log.Printf("Failed to update payment status: %v", updateErr)
		}
		return nil, fmt.Errorf("MoMo API call failed: %w", err)
	}

	// Check MoMo response
	if momoResp.ResultCode != 0 {
		// Update payment status to failed
		payment.Status = model.PaymentStatusFailed
		payment.FailureReason = momoResp.Message
		if updateErr := s.paymentStore.UpdatePayment(payment.ID, payment); updateErr != nil {
			log.Printf("Failed to update payment status: %v", updateErr)
		}
		return nil, fmt.Errorf("MoMo payment creation failed: %s (code: %d)", momoResp.Message, momoResp.ResultCode)
	}

	// Update payment with checkout URL
	payment.CheckoutURL = momoResp.PayURL
	if err := s.paymentStore.UpdatePayment(payment.ID, payment); err != nil {
		log.Printf("Failed to update payment with checkout URL: %v", err)
	}

	return &model.MoMoCheckoutResponse{
		PaymentID:   payment.ID,
		CheckoutURL: momoResp.PayURL,
		Status:      string(payment.Status),
		OrderID:     req.OrderID,
		RequestID:   requestID,
		ExpiredAt:   *payment.ExpiredAt,
	}, nil
}

// ProcessWebhook processes MoMo webhook notification
func (s *MoMoService) ProcessWebhook(webhook *model.MoMoWebhookRequest) (*model.MoMoWebhookResponse, error) {
	log.Printf("Processing MoMo webhook for order: %s, result: %d", webhook.OrderID, webhook.ResultCode)

	// Verify signature
	if !s.verifyWebhookSignature(webhook) {
		return nil, errors.New("invalid webhook signature")
	}

	// Find payment by order ID
	payment, err := s.findPaymentByOrderID(webhook.OrderID)
	if err != nil {
		return nil, fmt.Errorf("payment not found: %w", err)
	}

	// Prevent duplicate processing
	if payment.Status != model.PaymentStatusPending {
		log.Printf("Payment %s already processed with status: %s", payment.ID, payment.Status)
		return s.createWebhookResponse(webhook), nil
	}

	// Validate amount
	if int64(payment.TotalPrice) != webhook.Amount {
		log.Printf("Amount mismatch: expected %f, got %d", payment.TotalPrice, webhook.Amount)
		return nil, errors.New("amount mismatch")
	}

	// Update payment status based on MoMo result
	now := time.Now()
	if webhook.ResultCode == 0 {
		payment.Status = model.PaymentStatusSuccess
		payment.PaidAt = &now
		payment.ProviderTransactionID = strconv.FormatInt(webhook.TransID, 10)
	} else {
		payment.Status = model.PaymentStatusFailed
		payment.FailureReason = webhook.Message
	}

	payment.UpdatedAt = now

	// Update payment in database
	if err := s.paymentStore.UpdatePayment(payment.ID, payment); err != nil {
		return nil, fmt.Errorf("failed to update payment: %w", err)
	}

	log.Printf("Payment %s updated to status: %s", payment.ID, payment.Status)

	return s.createWebhookResponse(webhook), nil
}

// QueryPaymentStatus queries MoMo for the current payment status
func (s *MoMoService) QueryPaymentStatus(ctx context.Context, transactionID, orderID string) (*model.MoMoStatusResponse, error) {
	// Create query request
	requestID := fmt.Sprintf("QUERY_%d", time.Now().Unix())
	
	req := &model.MoMoAPIRequest{
		PartnerCode: s.config.PartnerCode,
		RequestID:   requestID,
		OrderID:     orderID,
		RequestType: "transactionStatus",
		Lang:        "vi",
		Signature:   "",
	}

	// Generate signature for query request
	signature, err := s.generateSignature(req)
	if err != nil {
		return nil, fmt.Errorf("failed to generate signature: %v", err)
	}
	req.Signature = signature

	// Call MoMo query API (using different endpoint for query)
	// Note: You may need to implement a separate query endpoint call
	resp, err := s.callMoMoAPI(req)
	if err != nil {
		return nil, fmt.Errorf("failed to query MoMo API: %v", err)
	}

	// Convert to status response format
	statusResp := &model.MoMoStatusResponse{
		PartnerCode:   resp.PartnerCode,
		RequestID:     resp.RequestID,
		OrderID:       resp.OrderID,
		TransID:       transactionID,
		Amount:        resp.Amount,
		ResultCode:    resp.ResultCode,
		Message:       resp.Message,
		LocalMessage:  resp.Message,
		PayType:       "momo",
		OrderInfo:     "",
		ResponseTime:  resp.ResponseTime,
		ExtraData:     "",
		Signature:     "",
	}

	return statusResp, nil
}

// generateSignature generates HMAC SHA256 signature for MoMo request
func (s *MoMoService) generateSignature(req *model.MoMoAPIRequest) (string, error) {
	// MoMo signature format: accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
	rawSignature := fmt.Sprintf("accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
		s.config.AccessKey,
		req.Amount,
		req.ExtraData,
		req.IpnURL,
		req.OrderID,
		req.OrderInfo,
		req.PartnerCode,
		req.RedirectURL,
		req.RequestID,
		req.RequestType,
	)

	// Create HMAC SHA256 hash
	h := hmac.New(sha256.New, []byte(s.config.SecretKey))
	h.Write([]byte(rawSignature))
	signature := hex.EncodeToString(h.Sum(nil))

	log.Printf("Generated signature for order %s", req.OrderID)
	return signature, nil
}

// verifyWebhookSignature verifies the webhook signature from MoMo
func (s *MoMoService) verifyWebhookSignature(webhook *model.MoMoWebhookRequest) bool {
	// MoMo webhook signature format: accessKey=$accessKey&amount=$amount&extraData=$extraData&message=$message&orderId=$orderId&orderInfo=$orderInfo&orderType=$orderType&partnerCode=$partnerCode&payType=$payType&requestId=$requestId&responseTime=$responseTime&resultCode=$resultCode&transId=$transId
	rawSignature := fmt.Sprintf("accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%d",
		s.config.AccessKey,
		webhook.Amount,
		webhook.ExtraData,
		webhook.Message,
		webhook.OrderID,
		webhook.OrderInfo,
		webhook.OrderType,
		webhook.PartnerCode,
		webhook.PayType,
		webhook.RequestID,
		webhook.ResponseTime,
		webhook.ResultCode,
		webhook.TransID,
	)

	// Create HMAC SHA256 hash
	h := hmac.New(sha256.New, []byte(s.config.SecretKey))
	h.Write([]byte(rawSignature))
	expectedSignature := hex.EncodeToString(h.Sum(nil))

	valid := hmac.Equal([]byte(expectedSignature), []byte(webhook.Signature))
	if !valid {
		log.Printf("Webhook signature verification failed for order: %s", webhook.OrderID)
	}
	return valid
}

// callMoMoAPI makes HTTP request to MoMo API
func (s *MoMoService) callMoMoAPI(req *model.MoMoAPIRequest) (*model.MoMoAPIResponse, error) {
	// Convert request to JSON
	jsonData, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	// Create HTTP request
	httpReq, err := http.NewRequest("POST", s.config.Endpoint, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create HTTP request: %w", err)
	}

	httpReq.Header.Set("Content-Type", "application/json")

	// Make HTTP call
	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("HTTP request failed: %w", err)
	}
	defer resp.Body.Close()

	// Read response
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	// Check HTTP status
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("MoMo API returned status %d: %s", resp.StatusCode, string(body))
	}

	// Parse response
	var momoResp model.MoMoAPIResponse
	if err := json.Unmarshal(body, &momoResp); err != nil {
		return nil, fmt.Errorf("failed to parse MoMo response: %w", err)
	}

	log.Printf("MoMo API response for order %s: resultCode=%d", req.OrderID, momoResp.ResultCode)
	return &momoResp, nil
}

// findPaymentByOrderID finds payment by order ID
func (s *MoMoService) findPaymentByOrderID(orderID string) (*model.Payment, error) {
	// This is a simplified implementation. In a real app, you'd implement this in the store layer
	// For now, we'll need to add this method to the payment store interface
	payments, err := s.paymentStore.GetPayments()
	if err != nil {
		return nil, err
	}

	for _, payment := range payments {
		if payment.OrderID == orderID {
			return &payment, nil
		}
	}

	return nil, errors.New("payment not found")
}

// createWebhookResponse creates a response for MoMo webhook
func (s *MoMoService) createWebhookResponse(webhook *model.MoMoWebhookRequest) *model.MoMoWebhookResponse {
	return &model.MoMoWebhookResponse{
		PartnerCode:  webhook.PartnerCode,
		RequestID:    webhook.RequestID,
		OrderID:      webhook.OrderID,
		ResultCode:   0, // Always return 0 to acknowledge receipt
		Message:      "success",
		ResponseTime: time.Now().Unix(),
		ExtraData:    webhook.ExtraData,
	}
}

// validateConfig validates MoMo configuration
func (s *MoMoService) validateConfig() error {
	if s.config.PartnerCode == "" {
		return errors.New("MOMO_PARTNER_CODE is required")
	}
	if s.config.AccessKey == "" {
		return errors.New("MOMO_ACCESS_KEY is required")
	}
	if s.config.SecretKey == "" {
		return errors.New("MOMO_SECRET_KEY is required")
	}
	if s.config.Endpoint == "" {
		return errors.New("MOMO_ENDPOINT is required")
	}
	if s.config.IpnURL == "" {
		return errors.New("MOMO_IPN_URL is required")
	}
	return nil
}