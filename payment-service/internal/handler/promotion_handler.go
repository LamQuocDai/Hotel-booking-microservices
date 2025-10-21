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

type PromotionHandler struct {
	promotionService *service.PromotionService
}

func NewPromotionHandler(promotionService *service.PromotionService) *PromotionHandler {
	return &PromotionHandler{
		promotionService: promotionService,
	}
}

// GetPromotions godoc
// @Summary Get all promotions with pagination
// @Description Get a paginated list of promotions with optional filters
// @Tags promotions
// @Accept json
// @Produce json
// @Param pageNumber query int false "Page number" default(1)
// @Param pageSize query int false "Page size" default(10)
// @Param search query string false "Search term"
// @Param sortBy query string false "Sort by field" default("created_at")
// @Param sortDirection query string false "Sort direction (asc/desc)" default("desc")
// @Param code query string false "Filter by promotion code"
// @Param isActive query bool false "Filter by active status"
// @Param minDiscount query number false "Minimum discount percentage"
// @Param maxDiscount query number false "Maximum discount percentage"
// @Success 200 {object} response.APIResponse{data=pagination.PaginatedResponse}
// @Failure 500 {object} response.APIResponse
// @Router /promotions [get]
func (h *PromotionHandler) GetPromotions(c *gin.Context) {
	// Parse pagination query
	paginationQuery := pagination.ParsePaginationQuery(c)
	
	// Parse filter query
	var filter model.PromotionFilterQuery
	if err := c.ShouldBindQuery(&filter); err != nil {
		response.ValidationErrorResponse(c, err.Error())
		return
	}

	result, err := h.promotionService.GetPromotionsPaginated(paginationQuery, &filter)
	if err != nil {
		response.InternalErrorResponse(c, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, result, "Promotions retrieved successfully")
}

// GetPromotionByID godoc
// @Summary Get promotion by ID
// @Description Get a single promotion by its ID
// @Tags promotions
// @Accept json
// @Produce json
// @Param id path string true "Promotion ID"
// @Success 200 {object} response.APIResponse{data=model.Promotion}
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /promotions/{id} [get]
func (h *PromotionHandler) GetPromotionByID(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid promotion ID format")
		return
	}

	promotion, err := h.promotionService.GetPromotionByID(id)
	if err != nil {
		response.InternalErrorResponse(c, "Failed to get promotion: "+err.Error())
		return
	}

	if promotion == nil {
		response.NotFoundResponse(c, "Promotion not found")
		return
	}

	response.SuccessResponse(c, http.StatusOK, promotion, "Promotion retrieved successfully")
}

// CreatePromotion godoc
// @Summary Create a new promotion
// @Description Create a new promotion with the provided data
// @Tags promotions
// @Accept json
// @Produce json
// @Param promotion body model.CreatePromotionRequest true "Promotion data"
// @Success 201 {object} response.APIResponse{data=model.Promotion}
// @Failure 400 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /promotions [post]
func (h *PromotionHandler) CreatePromotion(c *gin.Context) {
	var req model.CreatePromotionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.ValidationErrorResponse(c, "Invalid request data: "+err.Error())
		return
	}

	promotion, err := h.promotionService.CreatePromotion(&req)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusCreated, promotion, "Promotion created successfully")
}

// UpdatePromotion godoc
// @Summary Update a promotion
// @Description Update an existing promotion with the provided data
// @Tags promotions
// @Accept json
// @Produce json
// @Param id path string true "Promotion ID"
// @Param promotion body model.UpdatePromotionRequest true "Promotion update data"
// @Success 200 {object} response.APIResponse{data=model.Promotion}
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /promotions/{id} [patch]
func (h *PromotionHandler) UpdatePromotion(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid promotion ID format")
		return
	}

	var req model.UpdatePromotionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.ValidationErrorResponse(c, "Invalid request data: "+err.Error())
		return
	}

	promotion, err := h.promotionService.UpdatePromotion(id, &req)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, promotion, "Promotion updated successfully")
}

// DeletePromotion godoc
// @Summary Delete a promotion
// @Description Delete an existing promotion by its ID
// @Tags promotions
// @Accept json
// @Produce json
// @Param id path string true "Promotion ID"
// @Success 200 {object} response.APIResponse
// @Failure 400 {object} response.APIResponse
// @Failure 404 {object} response.APIResponse
// @Failure 500 {object} response.APIResponse
// @Router /promotions/{id} [delete]
func (h *PromotionHandler) DeletePromotion(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		response.ValidationErrorResponse(c, "Invalid promotion ID format")
		return
	}

	err = h.promotionService.DeletePromotion(id)
	if err != nil {
		response.ErrorResponse(c, http.StatusBadRequest, err.Error())
		return
	}

	response.SuccessResponse(c, http.StatusOK, nil, "Promotion deleted successfully")
}