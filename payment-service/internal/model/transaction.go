package model

import (
	"time"
	"github.com/google/uuid"
	"github.com/go-playground/validator/v10"
)

type TransactionStatus string

const (
	TransactionStatusSuccess TransactionStatus = "success"
	TransactionStatusPending   TransactionStatus = "pending"
	TransactionStatusFailed    TransactionStatus = "failed"
)

type Transaction struct {
	ID        uuid.UUID            `bson:"id"`
	PaymentId    uuid.UUID       `bson:"payment_id"`
	Status		TransactionStatus   `bson:"status" validate:"required,oneof=success pending failed"`
	CreatedAt time.Time          `bson:"created_at"`
	UpdatedAt time.Time          `bson:"updated_at"`
}

func (t *Transaction) Validate() error {
	validate := validator.New()
	return validate.Struct(t)
}