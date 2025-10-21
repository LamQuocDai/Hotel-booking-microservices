package model

import (
	"time"

	"github.com/google/uuid"
)

// Promotion represents a promotion in the system
type Promotion struct {
	ID          uuid.UUID `bson:"_id" json:"id"`
	Code        string    `bson:"code" json:"code" binding:"required" example:"SAVE20"`
	Description string    `bson:"description" json:"description" example:"Save 20% on your booking"`
	StartDate   time.Time `bson:"start_date" json:"startDate" binding:"required"`
	EndDate     time.Time `bson:"end_date" json:"endDate" binding:"required"`
	Discount    float64   `bson:"discount" json:"discount" binding:"required,min=0,max=100" example:"20.5"`
	IsActive    bool      `bson:"is_active" json:"isActive" example:"true"`
	CreatedAt   time.Time `bson:"created_at" json:"createdAt"`
	UpdatedAt   time.Time `bson:"updated_at" json:"updatedAt"`
}

// CreatePromotionRequest represents the request body for creating a promotion
type CreatePromotionRequest struct {
	Code        string    `json:"code" binding:"required" example:"SAVE20"`
	Description string    `json:"description" example:"Save 20% on your booking"`
	StartDate   time.Time `json:"startDate" binding:"required"`
	EndDate     time.Time `json:"endDate" binding:"required"`
	Discount    float64   `json:"discount" binding:"required,min=0,max=100" example:"20.5"`
	IsActive    bool      `json:"isActive" example:"true"`
}

// UpdatePromotionRequest represents the request body for updating a promotion
type UpdatePromotionRequest struct {
	Code        *string    `json:"code,omitempty" example:"SAVE20"`
	Description *string    `json:"description,omitempty" example:"Save 20% on your booking"`
	StartDate   *time.Time `json:"startDate,omitempty"`
	EndDate     *time.Time `json:"endDate,omitempty"`
	Discount    *float64   `json:"discount,omitempty" example:"20.5"`
	IsActive    *bool      `json:"isActive,omitempty" example:"true"`
}

// IsValidTimeRange checks if the promotion time range is valid
func (p *Promotion) IsValidTimeRange() bool {
	return p.EndDate.After(p.StartDate)
}

// IsCurrentlyActive checks if the promotion is currently active
func (p *Promotion) IsCurrentlyActive() bool {
	now := time.Now()
	return p.IsActive && p.IsValidTimeRange() && now.After(p.StartDate) && now.Before(p.EndDate)
}

// PromotionFilterQuery represents filter parameters for promotion queries
type PromotionFilterQuery struct {
	Code        string     `form:"code" json:"code" example:"SAVE20"`
	IsActive    *bool      `form:"isActive" json:"isActive" example:"true"`
	MinDiscount *float64   `form:"minDiscount" json:"minDiscount" example:"10.0"`
	MaxDiscount *float64   `form:"maxDiscount" json:"maxDiscount" example:"50.0"`
	DateFrom    *time.Time `form:"dateFrom" json:"dateFrom"`
	DateTo      *time.Time `form:"dateTo" json:"dateTo"`
	IsExpired   *bool      `form:"isExpired" json:"isExpired" example:"false"`
}
