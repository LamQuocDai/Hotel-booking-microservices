package model

import (
	"time"

	"github.com/go-playground/validator/v10"
	"github.com/google/uuid"
)

type PaymentStatus string

const (
	PaymentStatusPaid   PaymentStatus = "paid"
	PaymentStatusYet    PaymentStatus = "yet"
)

type Payment struct {
	ID        uuid.UUID     `bson:"id" validate:"required,uuid4"`
	PromotionId uuid.UUID     `bson:"promotion_id" validate:"uuid4"`
	Status    PaymentStatus `bson:"status" validate:"required,oneof=paid yet"`
	Total 	 float64       `bson:"total" validate:"required,gte=0"`
	Tax 		 float64       `bson:"tax" validate:"required,gte=0"`
	Discount  float64       `bson:"discount" validate:"required,gte=0"`
	TotalPrice float64       `bson:"total_price" validate:"required,gte=0"`
	RoomBookingIDs []uuid.UUID   `bson:"room_booking_ids" validate:"dive,uuid4"`
	CreatedAt time.Time     `bson:"created_at" validate:"required"`
	UpdatedAt time.Time     `bson:"updated_at" validate:"required"`
}

func (p *Payment) Validate() error {
	validate := validator.New()
	return validate.Struct(p)
}