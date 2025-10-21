package model

import (
	"time"

	"github.com/google/uuid"
)

type TransactionStatus string
type TransactionType string

const (
	TransactionStatusPending TransactionStatus = "pending"
	TransactionStatusSuccess TransactionStatus = "success"
	TransactionStatusFailed  TransactionStatus = "failed"
	TransactionStatusTimeout TransactionStatus = "timeout"
)

const (
	TransactionTypePayment TransactionType = "payment"
	TransactionTypeRefund  TransactionType = "refund"
	TransactionTypePartialRefund TransactionType = "partial_refund"
)

// Transaction represents a transaction in the system
type Transaction struct {
	ID                uuid.UUID         `bson:"_id" json:"id"`
	PaymentID         uuid.UUID         `bson:"payment_id" json:"paymentId" binding:"required"`
	Status            TransactionStatus `bson:"status" json:"status" binding:"required,oneof=pending success failed timeout" example:"pending"`
	Type              TransactionType   `bson:"type" json:"type" binding:"required,oneof=payment refund partial_refund" example:"payment"`
	Amount            float64           `bson:"amount" json:"amount" binding:"required,gte=0" example:"100.00"`
	Currency          string            `bson:"currency" json:"currency" binding:"required,len=3" example:"USD"`
	ExternalID        string            `bson:"external_id" json:"externalId" example:"stripe_pi_1234"`
	PaymentGateway    string            `bson:"payment_gateway" json:"paymentGateway" example:"stripe"`
	GatewayResponse   map[string]interface{} `bson:"gateway_response,omitempty" json:"gatewayResponse,omitempty"`
	FailureReason     string            `bson:"failure_reason,omitempty" json:"failureReason,omitempty"`
	ProcessedAt       *time.Time        `bson:"processed_at,omitempty" json:"processedAt,omitempty"`
	CreatedAt         time.Time         `bson:"created_at" json:"createdAt"`
	UpdatedAt         time.Time         `bson:"updated_at" json:"updatedAt"`
}

// CreateTransactionRequest represents the request body for creating a transaction
type CreateTransactionRequest struct {
	PaymentID      uuid.UUID       `json:"paymentId" binding:"required"`
	Type           TransactionType `json:"type" binding:"required,oneof=payment refund partial_refund" example:"payment"`
	Amount         float64         `json:"amount" binding:"required,gte=0" example:"100.00"`
	Currency       string          `json:"currency" binding:"required,len=3" example:"USD"`
	PaymentGateway string          `json:"paymentGateway" binding:"required" example:"stripe"`
	ExternalID     string          `json:"externalId,omitempty" example:"stripe_pi_1234"`
}

// UpdateTransactionRequest represents the request body for updating a transaction
type UpdateTransactionRequest struct {
	Status          *TransactionStatus         `json:"status,omitempty" example:"success"`
	ExternalID      *string                    `json:"externalId,omitempty" example:"stripe_pi_1234"`
	GatewayResponse *map[string]interface{}    `json:"gatewayResponse,omitempty"`
	FailureReason   *string                    `json:"failureReason,omitempty"`
}

// TransactionFilterQuery represents filter parameters for transaction queries
type TransactionFilterQuery struct {
	PaymentID      *uuid.UUID        `form:"paymentId" json:"paymentId"`
	Status         TransactionStatus `form:"status" json:"status" example:"success"`
	Type           TransactionType   `form:"type" json:"type" example:"payment"`
	PaymentGateway string            `form:"paymentGateway" json:"paymentGateway" example:"stripe"`
	MinAmount      *float64          `form:"minAmount" json:"minAmount" example:"50.00"`
	MaxAmount      *float64          `form:"maxAmount" json:"maxAmount" example:"500.00"`
	Currency       string            `form:"currency" json:"currency" example:"USD"`
	DateFrom       *time.Time        `form:"dateFrom" json:"dateFrom"`
	DateTo         *time.Time        `form:"dateTo" json:"dateTo"`
}

// IsSuccessful checks if the transaction completed successfully
func (t *Transaction) IsSuccessful() bool {
	return t.Status == TransactionStatusSuccess
}

// IsFailed checks if the transaction failed
func (t *Transaction) IsFailed() bool {
	return t.Status == TransactionStatusFailed || t.Status == TransactionStatusTimeout
}

// IsPending checks if the transaction is still pending
func (t *Transaction) IsPending() bool {
	return t.Status == TransactionStatusPending
}

// MarkAsProcessed marks the transaction as processed with current timestamp
func (t *Transaction) MarkAsProcessed() {
	now := time.Now()
	t.ProcessedAt = &now
	t.UpdatedAt = now
}

// IsRefund checks if this is a refund transaction
func (t *Transaction) IsRefund() bool {
	return t.Type == TransactionTypeRefund || t.Type == TransactionTypePartialRefund
}