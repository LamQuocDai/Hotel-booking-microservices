package service

import (
	"errors"
	"payment-service/internal/model"
	"payment-service/internal/store"
	"payment-service/pkg/pagination"
	"time"

	"github.com/google/uuid"
)

type TransactionService struct {
	transactionStore store.TransactionStore
	paymentStore     store.PaymentStore
}

func NewTransactionService(transactionStore store.TransactionStore, paymentStore store.PaymentStore) *TransactionService {
	return &TransactionService{
		transactionStore: transactionStore,
		paymentStore:     paymentStore,
	}
}

func (s *TransactionService) GetTransactions() ([]model.Transaction, error) {
	return s.transactionStore.GetTransactions()
}

func (s *TransactionService) GetTransactionsPaginated(query *pagination.PaginationQuery, filter *model.TransactionFilterQuery) (*pagination.PaginatedResponse, error) {
	transactions, total, err := s.transactionStore.GetTransactionsPaginated(query, filter)
	if err != nil {
		return nil, err
	}

	return pagination.NewPaginatedResponse(transactions, total, query), nil
}

func (s *TransactionService) GetTransactionByID(id uuid.UUID) (*model.Transaction, error) {
	return s.transactionStore.GetTransactionByID(id)
}

func (s *TransactionService) GetTransactionsByPaymentID(paymentID uuid.UUID) ([]model.Transaction, error) {
	return s.transactionStore.GetTransactionsByPaymentID(paymentID)
}

func (s *TransactionService) CreateTransaction(req *model.CreateTransactionRequest) (*model.Transaction, error) {
	// Verify payment exists
	payment, err := s.paymentStore.GetPaymentByID(req.PaymentID)
	if err != nil {
		return nil, err
	}
	if payment == nil {
		return nil, errors.New("payment not found")
	}

	// Validate transaction amount against payment amount
	if req.Type == model.TransactionTypePayment && req.Amount != payment.TotalPrice {
		return nil, errors.New("transaction amount must match payment total price")
	}

	transaction := &model.Transaction{
		PaymentID:      req.PaymentID,
		Type:           req.Type,
		Amount:         req.Amount,
		Currency:       req.Currency,
		PaymentGateway: req.PaymentGateway,
		ExternalID:     req.ExternalID,
		Status:         model.TransactionStatusPending,
	}

	err = s.transactionStore.CreateTransaction(transaction)
	if err != nil {
		return nil, err
	}

	return transaction, nil
}

func (s *TransactionService) UpdateTransaction(id uuid.UUID, req *model.UpdateTransactionRequest) (*model.Transaction, error) {
	// Get existing transaction
	existingTransaction, err := s.transactionStore.GetTransactionByID(id)
	if err != nil {
		return nil, err
	}
	if existingTransaction == nil {
		return nil, errors.New("transaction not found")
	}

	// Update only provided fields
	if req.Status != nil {
		// Validate status transitions
		if !s.isValidStatusTransition(existingTransaction.Status, *req.Status) {
			return nil, errors.New("invalid status transition")
		}
		existingTransaction.Status = *req.Status
		
		// Mark as processed if status changed to success or failed
		if *req.Status == model.TransactionStatusSuccess || *req.Status == model.TransactionStatusFailed {
			existingTransaction.MarkAsProcessed()
		}
	}

	if req.ExternalID != nil {
		existingTransaction.ExternalID = *req.ExternalID
	}

	if req.GatewayResponse != nil {
		existingTransaction.GatewayResponse = *req.GatewayResponse
	}

	if req.FailureReason != nil {
		existingTransaction.FailureReason = *req.FailureReason
	}

	existingTransaction.UpdatedAt = time.Now()

	err = s.transactionStore.UpdateTransaction(id, existingTransaction)
	if err != nil {
		return nil, err
	}

	// Update payment status based on transaction status
	if req.Status != nil {
		err = s.updatePaymentStatusFromTransaction(existingTransaction)
		if err != nil {
			// Log error but don't fail the transaction update
			// This could be handled with event-driven architecture
		}
	}

	return existingTransaction, nil
}

func (s *TransactionService) DeleteTransaction(id uuid.UUID) error {
	// Check if transaction exists
	existingTransaction, err := s.transactionStore.GetTransactionByID(id)
	if err != nil {
		return err
	}
	if existingTransaction == nil {
		return errors.New("transaction not found")
	}

	// Only allow deletion of pending or failed transactions
	if existingTransaction.Status != model.TransactionStatusPending && existingTransaction.Status != model.TransactionStatusFailed {
		return errors.New("cannot delete successful transaction")
	}

	return s.transactionStore.DeleteTransaction(id)
}

func (s *TransactionService) isValidStatusTransition(from, to model.TransactionStatus) bool {
	validTransitions := map[model.TransactionStatus][]model.TransactionStatus{
		model.TransactionStatusPending: {
			model.TransactionStatusSuccess,
			model.TransactionStatusFailed,
			model.TransactionStatusTimeout,
		},
		model.TransactionStatusSuccess: {},
		model.TransactionStatusFailed:  {},
		model.TransactionStatusTimeout: {},
	}

	allowedTransitions, exists := validTransitions[from]
	if !exists {
		return false
	}

	for _, allowed := range allowedTransitions {
		if allowed == to {
			return true
		}
	}

	return false
}

func (s *TransactionService) updatePaymentStatusFromTransaction(transaction *model.Transaction) error {
	payment, err := s.paymentStore.GetPaymentByID(transaction.PaymentID)
	if err != nil {
		return err
	}
	if payment == nil {
		return errors.New("payment not found")
	}

	var newPaymentStatus model.PaymentStatus

	switch transaction.Status {
	case model.TransactionStatusSuccess:
		if transaction.Type == model.TransactionTypePayment {
			newPaymentStatus = model.PaymentStatusPaid
		} else if transaction.Type == model.TransactionTypeRefund || transaction.Type == model.TransactionTypePartialRefund {
			newPaymentStatus = model.PaymentStatusRefunded
		}
	case model.TransactionStatusFailed, model.TransactionStatusTimeout:
		if transaction.Type == model.TransactionTypePayment {
			newPaymentStatus = model.PaymentStatusFailed
		}
	default:
		return nil // No status change needed
	}

	if newPaymentStatus != "" && payment.Status != newPaymentStatus {
		payment.Status = newPaymentStatus
		payment.UpdatedAt = time.Now()
		return s.paymentStore.UpdatePayment(payment.ID, payment)
	}

	return nil
}