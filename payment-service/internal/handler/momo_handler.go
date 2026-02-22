package handler

import (
	"fmt"
	"net/http"
	"payment-service/internal/model"
	"payment-service/internal/service"
	"payment-service/pkg/pagination"
	"payment-service/pkg/response"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type MoMoHandler struct {
	momoService    service.MoMoService
	paymentService service.PaymentService
}

func NewMoMoHandler(momoService service.MoMoService, paymentService service.PaymentService) *MoMoHandler {
	return &MoMoHandler{
		momoService:    momoService,
		paymentService: paymentService,
	}
}

// CreateMoMoPayment creates a new MoMo payment and returns checkout URL
// @Summary Create MoMo payment
// @Description Create a new payment and get MoMo checkout URL
// @Tags MoMo
// @Accept json
// @Produce json
// @Param payment body model.MoMoPaymentRequest true "Payment data"
// @Success 200 {object} response.Response{data=model.MoMoPaymentResponse}
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/payments/checkout [post]
func (h *MoMoHandler) CreateMoMoPayment(c *gin.Context) {
	var req model.MoMoPaymentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request body", err.Error())
		return
	}

	// Validate required fields
	if req.Amount <= 0 {
		response.Error(c, http.StatusBadRequest, "Invalid amount", "Amount must be greater than 0")
		return
	}

	if req.OrderInfo == "" {
		response.Error(c, http.StatusBadRequest, "Invalid order info", "Order info is required")
		return
	}

	if req.ReturnURL == "" {
		response.Error(c, http.StatusBadRequest, "Invalid return URL", "Return URL is required")
		return
	}

	if req.NotifyURL == "" {
		response.Error(c, http.StatusBadRequest, "Invalid notify URL", "Notify URL is required")
		return
	}

	// Create MoMo payment
	paymentRes, err := h.momoService.CreateCheckout(&model.MoMoCheckoutRequest{
		OrderID:         fmt.Sprintf("ORDER_%d_%s", time.Now().Unix(), req.CustomerID.String()[:8]),
		Amount:          req.Amount,
		CustomerID:      req.CustomerID,
		OrderInfo:       req.OrderInfo,
		RedirectURL:     req.ReturnURL,
		PromotionID:     req.PromotionID,
		RoomBookingIDs:  req.RoomBookingIDs,
		ExtraData:       req.ExtraData,
	})
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to create payment", err.Error())
		return
	}

	// Convert to expected response format
	momoResponse := &model.MoMoPaymentResponse{
		PaymentID:   paymentRes.PaymentID,
		OrderID:     paymentRes.OrderID,
		CheckoutURL: paymentRes.CheckoutURL,
		Status:      paymentRes.Status,
		RequestID:   paymentRes.RequestID,
		Amount:      req.Amount,
		ExpiredAt:   paymentRes.ExpiredAt,
	}

	response.Success(c, momoResponse, "Payment created successfully")
}

// HandleMoMoWebhook handles MoMo webhook notifications
// @Summary MoMo webhook handler
// @Description Handle payment notifications from MoMo
// @Tags MoMo
// @Accept json
// @Produce json
// @Param webhook body model.MoMoWebhookRequest true "Webhook data"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/payments/momo/webhook [post]
func (h *MoMoHandler) HandleMoMoWebhook(c *gin.Context) {
	var webhook model.MoMoWebhookRequest
	if err := c.ShouldBindJSON(&webhook); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid webhook body", err.Error())
		return
	}

	// Process webhook
	_, err := h.momoService.ProcessWebhook(&webhook)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to process webhook", err.Error())
		return
	}

	response.Success(c, nil, "Webhook processed successfully")
}

// GetPaymentStatus gets payment status by ID
// @Summary Get payment status
// @Description Get current payment status
// @Tags MoMo
// @Accept json
// @Produce json
// @Param id path string true "Payment ID"
// @Success 200 {object} response.Response{data=model.MoMoStatusResponse}
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/payments/{id}/status [get]
func (h *MoMoHandler) GetPaymentStatus(c *gin.Context) {
	idParam := c.Param("id")
	paymentID, err := uuid.Parse(idParam)
	if err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid payment ID", err.Error())
		return
	}

	// Get payment from database
	payment, err := h.paymentService.GetPaymentByID(paymentID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to get payment", err.Error())
		return
	}

	if payment == nil {
		response.Error(c, http.StatusNotFound, "Payment not found", "")
		return
	}

	// Query MoMo for latest status if needed
	var statusRes *model.MoMoStatusResponse
	if payment.Provider == "momo" && payment.ProviderTransactionID != "" {
		statusRes, err = h.momoService.QueryPaymentStatus(c.Request.Context(), payment.ProviderTransactionID, payment.OrderID)
		if err != nil {
			// If query fails, return current database status
			statusRes = &model.MoMoStatusResponse{
				OrderID:       payment.OrderID,
				TransID:       payment.ProviderTransactionID,
				ResultCode:    0,
				Message:       "Retrieved from database",
				LocalMessage:  "Retrieved from database",
				PayType:       "momo",
				Amount:        int64(payment.TotalPrice),
				OrderInfo:     payment.Description,
				ResponseTime:  payment.UpdatedAt.Unix() * 1000,
				ExtraData:     "",
				Signature:     "",
			}
		}
	} else {
		// Non-MoMo payment or missing transaction ID
		statusRes = &model.MoMoStatusResponse{
			OrderID:       payment.OrderID,
			TransID:       payment.ProviderTransactionID,
			ResultCode:    0,
			Message:       "Payment status retrieved",
			LocalMessage:  "Payment status retrieved",
			PayType:       payment.Provider,
			Amount:        int64(payment.TotalPrice),
			OrderInfo:     payment.Description,
			ResponseTime:  payment.UpdatedAt.Unix() * 1000,
			ExtraData:     "",
			Signature:     "",
		}
	}

	response.Success(c, statusRes, "Payment status retrieved successfully")
}

// CancelPayment cancels a payment
// @Summary Cancel payment
// @Description Cancel a pending payment
// @Tags MoMo
// @Accept json
// @Produce json
// @Param id path string true "Payment ID"
// @Param request body model.CancelPaymentRequest true "Cancel request"
// @Success 200 {object} response.Response{data=model.Payment}
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/payments/{id}/cancel [post]
func (h *MoMoHandler) CancelPayment(c *gin.Context) {
	idParam := c.Param("id")
	paymentID, err := uuid.Parse(idParam)
	if err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid payment ID", err.Error())
		return
	}

	var req model.CancelPaymentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "Invalid request body", err.Error())
		return
	}

	// Get payment from database
	payment, err := h.paymentService.GetPaymentByID(paymentID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to get payment", err.Error())
		return
	}

	if payment == nil {
		response.Error(c, http.StatusNotFound, "Payment not found", "")
		return
	}

	// Check if payment can be cancelled
	if payment.Status != model.PaymentStatusPending {
		response.Error(c, http.StatusBadRequest, "Payment cannot be cancelled", "Only pending payments can be cancelled")
		return
	}

	// Update payment status to cancelled
	payment.Status = model.PaymentStatusCancelled
	if req.Reason != "" {
		payment.FailureReason = &req.Reason
	}

	err = h.paymentService.UpdatePayment(paymentID, payment)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to cancel payment", err.Error())
		return
	}

	response.Success(c, payment, "Payment cancelled successfully")
}

// GetMoMoPaymentByOrderID gets payment by MoMo order ID (internal endpoint)
// @Summary Get payment by order ID
// @Description Get payment details by MoMo order ID
// @Tags MoMo
// @Accept json
// @Produce json
// @Param orderID path string true "Order ID"
// @Success 200 {object} response.Response{data=model.Payment}
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/payments/order/{orderID} [get]
func (h *MoMoHandler) GetMoMoPaymentByOrderID(c *gin.Context) {
	orderID := c.Param("orderID")
	if orderID == "" {
		response.Error(c, http.StatusBadRequest, "Invalid order ID", "Order ID is required")
		return
	}

	payment, err := h.paymentService.GetPaymentByOrderID(orderID)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to get payment", err.Error())
		return
	}

	if payment == nil {
		response.Error(c, http.StatusNotFound, "Payment not found", "")
		return
	}

	response.Success(c, payment, "Payment retrieved successfully")
}

// ListMoMoTransactions lists transactions with optional filtering
// @Summary List MoMo transactions  
// @Description Get list of MoMo transactions with pagination and filtering
// @Tags MoMo
// @Accept json
// @Produce json
// @Param page query int false "Page number" default(1)
// @Param limit query int false "Items per page" default(10)
// @Param status query string false "Payment status filter"
// @Param date_from query string false "Start date (YYYY-MM-DD)"
// @Param date_to query string false "End date (YYYY-MM-DD)"
// @Param amount_min query float64 false "Minimum amount"
// @Param amount_max query float64 false "Maximum amount"
// @Success 200 {object} response.Response{data=pagination.PaginatedResponse}
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/payments/momo/transactions [get]
func (h *MoMoHandler) ListMoMoTransactions(c *gin.Context) {
	// Parse pagination parameters
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "10"))
	
	// Create pagination query
	paginationQuery := &pagination.PaginationQuery{
		PageNumber:    page,
		PageSize:      limit,
		SortBy:        c.DefaultQuery("sort_by", "created_at"),
		SortDirection: c.DefaultQuery("sort_direction", "desc"),
	}

	// Create filter for MoMo payments only
	filter := &model.PaymentFilterQuery{
		Status: c.Query("status"),
	}

	// Parse date filters
	if dateFrom := c.Query("date_from"); dateFrom != "" {
		// Parse date string (implement date parsing logic)
		filter.DateFrom = nil // TODO: implement date parsing
	}

	if dateTo := c.Query("date_to"); dateTo != "" {
		// Parse date string (implement date parsing logic)  
		filter.DateTo = nil // TODO: implement date parsing
	}

	// Parse amount filters
	if amountMin := c.Query("amount_min"); amountMin != "" {
		if val, err := strconv.ParseFloat(amountMin, 64); err == nil {
			filter.MinAmount = &val
		}
	}

	if amountMax := c.Query("amount_max"); amountMax != "" {
		if val, err := strconv.ParseFloat(amountMax, 64); err == nil {
			filter.MaxAmount = &val
		}
	}

	// Get paginated payments
	paginatedResult, err := h.paymentService.GetPaymentsPaginated(paginationQuery, filter)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "Failed to get payments", err.Error())
		return
	}

	// Filter only MoMo payments from the items
	if payments, ok := paginatedResult.Items.([]model.Payment); ok {
		var momoPayments []model.Payment
		for _, payment := range payments {
			if payment.Provider == "momo" {
				momoPayments = append(momoPayments, payment)
			}
		}
		paginatedResult.Items = momoPayments
	}

	response.Success(c, paginatedResult, "MoMo transactions retrieved successfully")
}