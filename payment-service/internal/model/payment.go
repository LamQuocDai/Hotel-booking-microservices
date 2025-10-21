package model

import (
	"time"

	"github.com/google/uuid"
)

type PaymentStatus string

const (
	PaymentStatusPending   PaymentStatus = "pending"
	PaymentStatusPaid      PaymentStatus = "paid"
	PaymentStatusFailed    PaymentStatus = "failed"
	PaymentStatusCancelled PaymentStatus = "cancelled"
	PaymentStatusRefunded  PaymentStatus = "refunded"
)

// Payment represents a payment in the system
type Payment struct {
	ID             uuid.UUID     `bson:"_id" json:"id"`
	PromotionID    *uuid.UUID    `bson:"promotion_id,omitempty" json:"promotionId,omitempty"`
	Status         PaymentStatus `bson:"status" json:"status" binding:"required,oneof=pending paid failed cancelled refunded" example:"pending"`
	Total          float64       `bson:"total" json:"total" binding:"required,gte=0" example:"100.00"`
	Tax            float64       `bson:"tax" json:"tax" binding:"gte=0" example:"10.00"`
	Discount       float64       `bson:"discount" json:"discount" binding:"gte=0" example:"20.00"`
	TotalPrice     float64       `bson:"total_price" json:"totalPrice" binding:"required,gte=0" example:"90.00"`
	Currency       string        `bson:"currency" json:"currency" binding:"required,len=3" example:"USD"`
	PaymentMethod  string        `bson:"payment_method" json:"paymentMethod" example:"credit_card"`
	CustomerID     uuid.UUID     `bson:"customer_id" json:"customerId" binding:"required"`
	RoomBookingIDs []uuid.UUID   `bson:"room_booking_ids" json:"roomBookingIds"`
	Description    string        `bson:"description" json:"description" example:"Hotel booking payment"`
	CreatedAt      time.Time     `bson:"created_at" json:"createdAt"`
	UpdatedAt      time.Time     `bson:"updated_at" json:"updatedAt"`
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