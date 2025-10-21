package service

import (
	"errors"
	"payment-service/internal/model"
	"payment-service/internal/store"
	"payment-service/pkg/pagination"
	"time"

	"github.com/google/uuid"
)

type PromotionService struct {
	promotionStore store.PromotionStore
}

func NewPromotionService(promotionStore store.PromotionStore) *PromotionService {
	return &PromotionService{
		promotionStore: promotionStore,
	}
}

func (s *PromotionService) GetPromotions() ([]model.Promotion, error) {
	return s.promotionStore.GetPromotions()
}

func (s *PromotionService) GetPromotionsPaginated(query *pagination.PaginationQuery, filter *model.PromotionFilterQuery) (*pagination.PaginatedResponse, error) {
	promotions, total, err := s.promotionStore.GetPromotionsPaginated(query, filter)
	if err != nil {
		return nil, err
	}

	return pagination.NewPaginatedResponse(promotions, total, query), nil
}

func (s *PromotionService) GetPromotionByID(id uuid.UUID) (*model.Promotion, error) {
	return s.promotionStore.GetPromotionByID(id)
}

func (s *PromotionService) CreatePromotion(req *model.CreatePromotionRequest) (*model.Promotion, error) {
	// Validate end date is after start date
	if !req.EndDate.After(req.StartDate) {
		return nil, errors.New("end date must be after start date")
	}

	// Check if promotion code already exists
	existingPromotion, err := s.promotionStore.GetPromotionByCode(req.Code)
	if err != nil {
		return nil, err
	}
	if existingPromotion != nil {
		return nil, errors.New("promotion code already exists")
	}

	promotion := &model.Promotion{
		Code:        req.Code,
		Description: req.Description,
		StartDate:   req.StartDate,
		EndDate:     req.EndDate,
		Discount:    req.Discount,
		IsActive:    req.IsActive,
	}

	err = s.promotionStore.CreatePromotion(promotion)
	if err != nil {
		return nil, err
	}

	return promotion, nil
}

func (s *PromotionService) UpdatePromotion(id uuid.UUID, req *model.UpdatePromotionRequest) (*model.Promotion, error) {
	// Get existing promotion
	existingPromotion, err := s.promotionStore.GetPromotionByID(id)
	if err != nil {
		return nil, err
	}
	if existingPromotion == nil {
		return nil, errors.New("promotion not found")
	}

	// Update only provided fields
	if req.Code != nil {
		// Check if new code conflicts with existing promotion (excluding current one)
		if *req.Code != existingPromotion.Code {
			conflictPromotion, err := s.promotionStore.GetPromotionByCode(*req.Code)
			if err != nil {
				return nil, err
			}
			if conflictPromotion != nil {
				return nil, errors.New("promotion code already exists")
			}
		}
		existingPromotion.Code = *req.Code
	}

	if req.Description != nil {
		existingPromotion.Description = *req.Description
	}

	if req.StartDate != nil {
		existingPromotion.StartDate = *req.StartDate
	}

	if req.EndDate != nil {
		existingPromotion.EndDate = *req.EndDate
	}

	if req.Discount != nil {
		existingPromotion.Discount = *req.Discount
	}

	if req.IsActive != nil {
		existingPromotion.IsActive = *req.IsActive
	}

	// Validate the updated promotion
	if !existingPromotion.IsValidTimeRange() {
		return nil, errors.New("end date must be after start date")
	}

	existingPromotion.UpdatedAt = time.Now()

	err = s.promotionStore.UpdatePromotion(id, existingPromotion)
	if err != nil {
		return nil, err
	}

	return existingPromotion, nil
}

func (s *PromotionService) DeletePromotion(id uuid.UUID) error {
	// Check if promotion exists
	existingPromotion, err := s.promotionStore.GetPromotionByID(id)
	if err != nil {
		return err
	}
	if existingPromotion == nil {
		return errors.New("promotion not found")
	}

	return s.promotionStore.DeletePromotion(id)
}