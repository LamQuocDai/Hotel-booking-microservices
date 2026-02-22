package model

import (
	"time"

	"github.com/google/uuid"
)

type PaymentStatus string

const (
	PaymentStatusPending   PaymentStatus = "pending"
	PaymentStatusSuccess   PaymentStatus = "success"  // MoMo uses 'success' instead of 'paid'
	PaymentStatusPaid      PaymentStatus = "paid"
	PaymentStatusFailed    PaymentStatus = "failed"
	PaymentStatusCancelled PaymentStatus = "cancelled"
	PaymentStatusRefunded  PaymentStatus = "refunded"
)

// Payment represents a payment in the system
type Payment struct {
	ID                      uuid.UUID     `bson:"_id" json:"id"`
	PromotionID             *uuid.UUID    `bson:"promotion_id,omitempty" json:"promotionId,omitempty"`
	Status                  PaymentStatus `bson:"status" json:"status" binding:"required,oneof=pending success paid failed cancelled refunded" example:"pending"`
	Total                   float64       `bson:"total" json:"total" binding:"required,gte=0" example:"100.00"`
	Tax                     float64       `bson:"tax" json:"tax" binding:"gte=0" example:"10.00"`
	Discount                float64       `bson:"discount" json:"discount" binding:"gte=0" example:"20.00"`
	TotalPrice              float64       `bson:"total_price" json:"totalPrice" binding:"required,gte=0" example:"90.00"`
	Currency                string        `bson:"currency" json:"currency" binding:"required,len=3" example:"USD"`
	PaymentMethod           string        `bson:"payment_method" json:"paymentMethod" example:"credit_card"`
	CustomerID              uuid.UUID     `bson:"customer_id" json:"customerId" binding:"required"`
	RoomBookingIDs          []uuid.UUID   `bson:"room_booking_ids" json:"roomBookingIds"`
	Description             string        `bson:"description" json:"description" example:"Hotel booking payment"`
	// MoMo specific fields
	Provider                string        `bson:"provider,omitempty" json:"provider,omitempty" example:"MOMO"`
	ProviderTransactionID   string        `bson:"provider_transaction_id,omitempty" json:"providerTransactionId,omitempty"`
	RequestID               string        `bson:"request_id,omitempty" json:"requestId,omitempty"`
	OrderID                 string        `bson:"order_id,omitempty" json:"orderId,omitempty"`
	CheckoutURL             string        `bson:"checkout_url,omitempty" json:"checkoutUrl,omitempty"`
	ExpiredAt               *time.Time    `bson:"expired_at,omitempty" json:"expiredAt,omitempty"`
	PaidAt                  *time.Time    `bson:"paid_at,omitempty" json:"paidAt,omitempty"`
	FailureReason           string        `bson:"failure_reason,omitempty" json:"failureReason,omitempty"`
	CreatedAt               time.Time     `bson:"created_at" json:"createdAt"`
	UpdatedAt               time.Time     `bson:"updated_at" json:"updatedAt"`
}

// CreatePaymentRequest represents the request body for creating a payment
type CreatePaymentRequest struct {
	PromotionID    *uuid.UUID    `json:"promotionId,omitempty"`
	Total          float64       `json:"total" binding:"required,gte=0" example:"100.00"`
	Tax            float64       `json:"tax" binding:"gte=0" example:"10.00"`
	Currency       string        `json:"currency" binding:"required,len=3" example:"USD"`
	PaymentMethod  string        `json:"paymentMethod" binding:"required" example:"credit_card"`
	CustomerID     uuid.UUID     `json:"customerId" binding:"required"`
	RoomBookingIDs []uuid.UUID   `json:"roomBookingIds"`
	Description    string        `json:"description" example:"Hotel booking payment"`
}

// UpdatePaymentRequest represents the request body for updating a payment
type UpdatePaymentRequest struct {
	Status        *PaymentStatus `json:"status,omitempty" example:"paid"`
	PaymentMethod *string        `json:"paymentMethod,omitempty" example:"credit_card"`
	Description   *string        `json:"description,omitempty" example:"Hotel booking payment"`
}

// PaymentFilterQuery represents filter parameters for payment queries
type PaymentFilterQuery struct {
	Status         PaymentStatus `form:"status" json:"status" example:"paid"`
	CustomerID     *uuid.UUID    `form:"customerId" json:"customerId"`
	PaymentMethod  string        `form:"paymentMethod" json:"paymentMethod" example:"credit_card"`
	MinAmount      *float64      `form:"minAmount" json:"minAmount" example:"50.00"`
	MaxAmount      *float64      `form:"maxAmount" json:"maxAmount" example:"500.00"`
	Currency       string        `form:"currency" json:"currency" example:"USD"`
	DateFrom       *time.Time    `form:"dateFrom" json:"dateFrom"`
	DateTo         *time.Time    `form:"dateTo" json:"dateTo"`
}

// CalculateTotalPrice calculates the final price after tax and discount
func (p *Payment) CalculateTotalPrice() {
	p.TotalPrice = p.Total + p.Tax - p.Discount
	if p.TotalPrice < 0 {
		p.TotalPrice = 0
	}
}

// IsCompleted checks if the payment is in a completed state
func (p *Payment) IsCompleted() bool {
	return p.Status == PaymentStatusPaid || p.Status == PaymentStatusRefunded
}

// CanBeRefunded checks if the payment can be refunded
func (p *Payment) CanBeRefunded() bool {
	return p.Status == PaymentStatusPaid
}

// CanBeCancelled checks if the payment can be cancelled
func (p *Payment) CanBeCancelled() bool {
	return p.Status == PaymentStatusPending
}

// IsExpired checks if the payment has expired
func (p *Payment) IsExpired() bool {
	return p.ExpiredAt != nil && time.Now().After(*p.ExpiredAt)
}

// MoMoCheckoutRequest represents the request body for MoMo checkout
type MoMoCheckoutRequest struct {
	OrderID         string     `json:"orderId" binding:"required" example:"ORDER_123456"`
	Amount          int64      `json:"amount" binding:"required,min=1000" example:"100000"`   // VND, minimum 1000
	CustomerID      uuid.UUID  `json:"customerId" binding:"required"`
	OrderInfo       string     `json:"orderInfo" binding:"required" example:"Hotel booking payment"`
	RedirectURL     string     `json:"redirectUrl" binding:"required,url" example:"https://yourapp.com/payment/result"`
	PromotionID     *uuid.UUID `json:"promotionId,omitempty"`
	RoomBookingIDs  []uuid.UUID `json:"roomBookingIds"`
	ExtraData       string     `json:"extraData,omitempty" example:"{}"`
}

// MoMoCheckoutResponse represents the response from MoMo checkout
type MoMoCheckoutResponse struct {
	PaymentID   uuid.UUID `json:"paymentId"`
	CheckoutURL string    `json:"checkoutUrl"`
	Status      string    `json:"status"`
	OrderID     string    `json:"orderId"`
	RequestID   string    `json:"requestId"`
	ExpiredAt   time.Time `json:"expiredAt"`
}

// MoMoWebhookRequest represents the webhook request from MoMo
type MoMoWebhookRequest struct {
	PartnerCode     string `json:"partnerCode" binding:"required"`
	OrderID         string `json:"orderId" binding:"required"`
	RequestID       string `json:"requestId" binding:"required"`
	Amount          int64  `json:"amount" binding:"required"`
	OrderInfo       string `json:"orderInfo" binding:"required"`
	OrderType       string `json:"orderType" binding:"required"`
	TransID         int64  `json:"transId" binding:"required"`
	ResultCode      int    `json:"resultCode" binding:"required"`
	Message         string `json:"message" binding:"required"`
	PayType         string `json:"payType" binding:"required"`
	ResponseTime    int64  `json:"responseTime" binding:"required"`
	ExtraData       string `json:"extraData"`
	Signature       string `json:"signature" binding:"required"`
}

// MoMoWebhookResponse represents the response to MoMo webhook
type MoMoWebhookResponse struct {
	PartnerCode string `json:"partnerCode"`
	RequestID   string `json:"requestId"`
	OrderID     string `json:"orderId"`
	ResultCode  int    `json:"resultCode"`
	Message     string `json:"message"`
	ResponseTime int64 `json:"responseTime"`
	ExtraData   string `json:"extraData"`
}

// MoMoAPIRequest represents the request to MoMo API
type MoMoAPIRequest struct {
	PartnerCode string `json:"partnerCode"`
	PartnerName string `json:"partnerName"`
	StoreId     string `json:"storeId"`
	RequestType string `json:"requestType"`
	IpnURL      string `json:"ipnUrl"`
	RedirectURL string `json:"redirectUrl"`
	OrderID     string `json:"orderId"`
	Amount      int64  `json:"amount"`
	Lang        string `json:"lang"`
	OrderInfo   string `json:"orderInfo"`
	RequestID   string `json:"requestId"`
	ExtraData   string `json:"extraData"`
	Signature   string `json:"signature"`
	AutoCapture bool   `json:"autoCapture"`
}

// MoMoAPIResponse represents the response from MoMo API
type MoMoAPIResponse struct {
	PartnerCode     string `json:"partnerCode"`
	RequestID       string `json:"requestId"`
	OrderID         string `json:"orderId"`
	Amount          int64  `json:"amount"`
	ResponseTime    int64  `json:"responseTime"`
	Message         string `json:"message"`
	ResultCode      int    `json:"resultCode"`
	PayURL          string `json:"payUrl"`
	Deeplink        string `json:"deeplink"`
	QRCodeURL       string `json:"qrCodeUrl"`
}

// PaymentStatusResponse represents the response for payment status check
type PaymentStatusResponse struct {
	PaymentID         uuid.UUID      `json:"paymentId"`
	Status            PaymentStatus  `json:"status"`
	OrderID           string         `json:"orderId,omitempty"`
	Amount            float64        `json:"amount"`
	Currency          string         `json:"currency"`
	Provider          string         `json:"provider,omitempty"`
	FailureReason     string         `json:"failureReason,omitempty"`
	CreatedAt         time.Time      `json:"createdAt"`
	PaidAt            *time.Time     `json:"paidAt,omitempty"`
	ExpiredAt         *time.Time     `json:"expiredAt,omitempty"`
}

// CancelPaymentRequest represents the request body for cancelling a payment
type CancelPaymentRequest struct {
	Reason string `json:"reason,omitempty" example:"Customer requested cancellation"`
}

// MoMoPaymentRequest represents the request body for creating a MoMo payment
type MoMoPaymentRequest struct {
	CustomerID     uuid.UUID   `json:"customerId" binding:"required"`
	Amount         int64       `json:"amount" binding:"required,min=1000" example:"100000"` // VND, minimum 1000  
	OrderInfo      string      `json:"orderInfo" binding:"required" example:"Hotel booking payment"`
	ReturnURL      string      `json:"returnUrl" binding:"required,url" example:"https://yourapp.com/payment/result"`
	NotifyURL      string      `json:"notifyUrl" binding:"required,url" example:"https://yourapp.com/api/v1/payments/momo/webhook"`
	PromotionID    *uuid.UUID  `json:"promotionId,omitempty"`
	RoomBookingIDs []uuid.UUID `json:"roomBookingIds"`
	ExtraData      string      `json:"extraData,omitempty" example:"{}"`
}

// MoMoPaymentResponse represents the response after creating a MoMo payment
type MoMoPaymentResponse struct {
	PaymentID   uuid.UUID `json:"paymentId"`
	OrderID     string    `json:"orderId"`
	CheckoutURL string    `json:"checkoutUrl"`
	QRCodeURL   string    `json:"qrCodeUrl,omitempty"`
	Status      string    `json:"status"`
	RequestID   string    `json:"requestId"`
	Amount      int64     `json:"amount"`
	ExpiredAt   time.Time `json:"expiredAt"`
}

// MoMoStatusResponse represents the response from MoMo status query
type MoMoStatusResponse struct {
	PartnerCode   string `json:"partnerCode"`
	RequestID     string `json:"requestId"`
	OrderID       string `json:"orderId"`
	TransID       string `json:"transId"`
	Amount        int64  `json:"amount"`
	ResultCode    int    `json:"resultCode"`
	Message       string `json:"message"`
	LocalMessage  string `json:"localMessage"`
	PayType       string `json:"payType"`
	OrderInfo     string `json:"orderInfo"`
	ResponseTime  int64  `json:"responseTime"`
	ExtraData     string `json:"extraData"`
	Signature     string `json:"signature"`
}