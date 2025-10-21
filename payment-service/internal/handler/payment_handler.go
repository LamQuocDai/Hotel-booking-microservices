package handler

import (
	"net/http"
	"payment-service/internal/model"
	"payment-service/internal/service"
	"payment-service/pkg/pagination"
	"payment-service/pkg/response"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type PaymentHandler struct {
	paymentService *service.PaymentService
}

func NewPaymentHandler(paymentService *service.PaymentService) *PaymentHandler {
	return &PaymentHandler{
		paymentService: paymentService,
	}
}

// GetPayments godoc
// @Summary Get all payments with pagination
// @Description Get a paginated list of payments with optional filters
// @Tags payments
// @Accept json
// @Produce json
// @Param pageNumber query int false "Page number" default(1)
// @Param pageSize query int false "Page size" default(10)
// @Param search query string false "Search term"
// @Param sortBy query string false "Sort by field" default("created_at")
// @Param sortDirection query string false "Sort direction (asc/desc)" default("desc")
// @Param status query string false "Filter by payment status" Enums(pending, paid, failed, cancelled, refunded)
// @Param customerId query string false "Filter by customer ID"
// @Param paymentMethod query string false "Filter by payment method"
// @Param minAmount query number false "Minimum payment amount"
// @Param maxAmount query number false "Maximum payment amount"
// @Param currency query string false "Filter by currency"
// @Success 200 {object} response.APIResponse{data=pagination.PaginatedResponse}
// @Failure 500 {object} response.APIResponse
// @Router /api/v1/payments [get]
func (h *PaymentHandler) GetPayments(c *gin.Context) {
	// Parse pagination query
	paginationQuery := pagination.ParsePaginationQuery(c)
	
	// Parse filter query
	var filter model.PaymentFilterQuery
	if err := c.ShouldBindQuery(&filter); err != nil {
		response.ValidationErrorResponse(c, err.Error())
		return
	}

	result, err := h.paymentService.GetPaymentsPaginated(paginationQuery, &filter)
	if err != nil {
		response.InternalErrorResponse(c, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, result, "Payments retrieved successfully")
}

// GetPaymentByID godoc
// @Summary Get payment by ID
// @Description Get a single payment by its ID
// @Tags payments
// @Accept json
// @Produce json
// @Param id path string true "Payment ID"
// @Success 200 {object} response.APIResponse{data=model.Payment}
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /api/v1/payments/{id} [get]
func (h *PaymentHandler) GetPaymentByID(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid payment ID format")
		return
	}

	payment, err := h.paymentService.GetPaymentByID(id)
	if err != nil {
		response.InternalErrorResponse(c, err.Error())
		return
	}

	if payment == nil {
		response.NotFoundResponse(c, "Payment not found")
		return
	}

	response.SuccessResponse(c, http.StatusOK, payment, "Payment retrieved successfully")
}

// GetPaymentsByCustomerID godoc
// @Summary Get payments by customer ID
// @Description Get all payments for a specific customer
// @Tags payments
// @Accept json
// @Produce json
// @Param customerId path string true "Customer ID"
// @Success 200 {object} response.APIResponse{data=[]model.Payment}
// @Failure 400 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /api/v1/payments/customer/{customerId} [get]
func (h *PaymentHandler) GetPaymentsByCustomerID(c *gin.Context) {
	customerIDStr := c.Param("customerId")
	customerID, err := uuid.Parse(customerIDStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid customer ID format")
		return
	}

	payments, err := h.paymentService.GetPaymentsByCustomerID(customerID)
	if err != nil {
		response.InternalErrorResponse(c, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, payments, "Customer payments retrieved successfully")
}

// CreatePayment godoc
// @Summary Create a new payment
// @Description Create a new payment with the provided details
// @Tags payments
// @Accept json
// @Produce json
// @Param payment body model.CreatePaymentRequest true "Payment details"
// @Success 201 {object} response.APIResponse{data=model.Payment}
// @Failure 400 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /api/v1/payments [post]
func (h *PaymentHandler) CreatePayment(c *gin.Context) {
	var req model.CreatePaymentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.ValidationErrorResponse(c, err.Error())
		return
	}

	payment, err := h.paymentService.CreatePayment(&req)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusCreated, payment, "Payment created successfully")
}

// UpdatePayment godoc
// @Summary Update a payment
// @Description Update a payment with the provided details
// @Tags payments
// @Accept json
// @Produce json
// @Param id path string true "Payment ID"
// @Param payment body model.UpdatePaymentRequest true "Updated payment details"
// @Success 200 {object} response.APIResponse{data=model.Payment}
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /api/v1/payments/{id} [put]
func (h *PaymentHandler) UpdatePayment(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid payment ID format")
		return
	}

	var req model.UpdatePaymentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.ValidationErrorResponse(c, err.Error())
		return
	}

	payment, err := h.paymentService.UpdatePayment(id, &req)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, payment, "Payment updated successfully")
}

// DeletePayment godoc
// @Summary Delete a payment
// @Description Delete a payment by its ID
// @Tags payments
// @Accept json
// @Produce json
// @Param id path string true "Payment ID"
// @Success 200 {object} response.APIResponse
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /api/v1/payments/{id} [delete]
func (h *PaymentHandler) DeletePayment(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid payment ID format")
		return
	}

	err = h.paymentService.DeletePayment(id)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, nil, "Payment deleted successfully")
}