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

type TransactionHandler struct {
	transactionService *service.TransactionService
}

func NewTransactionHandler(transactionService *service.TransactionService) *TransactionHandler {
	return &TransactionHandler{
		transactionService: transactionService,
	}
}

// GetTransactions godoc
// @Summary Get all transactions with pagination
// @Description Get a paginated list of transactions with optional filters
// @Tags transactions
// @Accept json
// @Produce json
// @Param pageNumber query int false "Page number" default(1)
// @Param pageSize query int false "Page size" default(10)
// @Param search query string false "Search term"
// @Param sortBy query string false "Sort by field" default("created_at")
// @Param sortDirection query string false "Sort direction (asc/desc)" default("desc")
// @Param paymentId query string false "Filter by payment ID"
// @Param status query string false "Filter by transaction status" Enums(pending, success, failed, timeout)
// @Param type query string false "Filter by transaction type" Enums(payment, refund, partial_refund)
// @Param paymentGateway query string false "Filter by payment gateway"
// @Param minAmount query number false "Minimum transaction amount"
// @Param maxAmount query number false "Maximum transaction amount"
// @Param currency query string false "Filter by currency"
// @Success 200 {object} response.APIResponse{data=pagination.PaginatedResponse}
// @Failure 500 {object} response.APIResponse
// @Router /transactions [get]
func (h *TransactionHandler) GetTransactions(c *gin.Context) {
	// Parse pagination query
	paginationQuery := pagination.ParsePaginationQuery(c)
	
	// Parse filter query
	var filter model.TransactionFilterQuery
	if err := c.ShouldBindQuery(&filter); err != nil {
		response.ValidationErrorResponse(c, err.Error())
		return
	}

	result, err := h.transactionService.GetTransactionsPaginated(paginationQuery, &filter)
	if err != nil {
		response.InternalErrorResponse(c, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, result, "Transactions retrieved successfully")
}

// GetTransactionByID godoc
// @Summary Get transaction by ID
// @Description Get a single transaction by its ID
// @Tags transactions
// @Accept json
// @Produce json
// @Param id path string true "Transaction ID"
// @Success 200 {object} response.APIResponse{data=model.Transaction}
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /transactions/{id} [get]
func (h *TransactionHandler) GetTransactionByID(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid transaction ID format")
		return
	}

	transaction, err := h.transactionService.GetTransactionByID(id)
	if err != nil {
		response.InternalErrorResponse(c, err.Error())
		return
	}

	if transaction == nil {
		response.NotFoundResponse(c, "Transaction not found")
		return
	}

	response.SuccessResponse(c, http.StatusOK, transaction, "Transaction retrieved successfully")
}

// GetTransactionsByPaymentID godoc
// @Summary Get transactions by payment ID
// @Description Get all transactions for a specific payment
// @Tags transactions
// @Accept json
// @Produce json
// @Param paymentId path string true "Payment ID"
// @Success 200 {object} response.APIResponse{data=[]model.Transaction}
// @Failure 400 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /transactions/payment/{paymentId} [get]
func (h *TransactionHandler) GetTransactionsByPaymentID(c *gin.Context) {
	paymentIDStr := c.Param("paymentId")
	paymentID, err := uuid.Parse(paymentIDStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid payment ID format")
		return
	}

	transactions, err := h.transactionService.GetTransactionsByPaymentID(paymentID)
	if err != nil {
		response.InternalErrorResponse(c, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, transactions, "Payment transactions retrieved successfully")
}

// CreateTransaction godoc
// @Summary Create a new transaction
// @Description Create a new transaction with the provided details
// @Tags transactions
// @Accept json
// @Produce json
// @Param transaction body model.CreateTransactionRequest true "Transaction details"
// @Success 201 {object} response.APIResponse{data=model.Transaction}
// @Failure 400 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /transactions [post]
func (h *TransactionHandler) CreateTransaction(c *gin.Context) {
	var req model.CreateTransactionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.ValidationErrorResponse(c, err.Error())
		return
	}

	transaction, err := h.transactionService.CreateTransaction(&req)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusCreated, transaction, "Transaction created successfully")
}

// UpdateTransaction godoc
// @Summary Update a transaction
// @Description Update a transaction with the provided details
// @Tags transactions
// @Accept json
// @Produce json
// @Param id path string true "Transaction ID"
// @Param transaction body model.UpdateTransactionRequest true "Updated transaction details"
// @Success 200 {object} response.APIResponse{data=model.Transaction}
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /transactions/{id} [patch]
func (h *TransactionHandler) UpdateTransaction(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid transaction ID format")
		return
	}

	var req model.UpdateTransactionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.ValidationErrorResponse(c, err.Error())
		return
	}

	transaction, err := h.transactionService.UpdateTransaction(id, &req)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, transaction, "Transaction updated successfully")
}

// DeleteTransaction godoc
// @Summary Delete a transaction
// @Description Delete a transaction by its ID
// @Tags transactions
// @Accept json
// @Produce json
// @Param id path string true "Transaction ID"
// @Success 200 {object} response.APIResponse
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /transactions/{id} [delete]
func (h *TransactionHandler) DeleteTransaction(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid transaction ID format")
		return
	}

	err = h.transactionService.DeleteTransaction(id)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, nil, "Transaction deleted successfully")
}