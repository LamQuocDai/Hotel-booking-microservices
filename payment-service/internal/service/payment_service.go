package service

import (
	"errors"
	"payment-service/internal/model"
	"payment-service/internal/store"
	"payment-service/pkg/pagination"
	"time"

	"github.com/google/uuid"
)

type PaymentService struct {
	paymentStore   store.PaymentStore
	promotionStore store.PromotionStore
}

func NewPaymentService(paymentStore store.PaymentStore, promotionStore store.PromotionStore) *PaymentService {
	return &PaymentService{
		paymentStore:   paymentStore,
		promotionStore: promotionStore,
	}
}

func (s *PaymentService) GetPayments() ([]model.Payment, error) {
	return s.paymentStore.GetPayments()
}

func (s *PaymentService) GetPaymentsPaginated(query *pagination.PaginationQuery, filter *model.PaymentFilterQuery) (*pagination.PaginatedResponse, error) {
	payments, total, err := s.paymentStore.GetPaymentsPaginated(query, filter)
	if err != nil {
		return nil, err
	}

	return pagination.NewPaginatedResponse(payments, total, query), nil
}

func (s *PaymentService) GetPaymentByID(id uuid.UUID) (*model.Payment, error) {
	return s.paymentStore.GetPaymentByID(id)
}

func (s *PaymentService) GetPaymentsByCustomerID(customerID uuid.UUID) ([]model.Payment, error) {
	return s.paymentStore.GetPaymentsByCustomerID(customerID)
}

func (s *PaymentService) CreatePayment(req *model.CreatePaymentRequest) (*model.Payment, error) {
	payment := &model.Payment{
		Total:          req.Total,
		Tax:            req.Tax,
		Currency:       req.Currency,
		PaymentMethod:  req.PaymentMethod,
		CustomerID:     req.CustomerID,
		RoomBookingIDs: req.RoomBookingIDs,
		Description:    req.Description,
		Status:         model.PaymentStatusPending,
	}

	// Apply promotion if provided
	if req.PromotionID != nil {
		promotion, err := s.promotionStore.GetPromotionByID(*req.PromotionID)
		if err != nil {
			return nil, err
		}
		if promotion == nil {
			return nil, errors.New("promotion not found")
		}
		if !promotion.IsCurrentlyActive() {
			return nil, errors.New("promotion is not active")
		}

		payment.PromotionID = req.PromotionID
		payment.Discount = (req.Total * promotion.Discount) / 100
	}

	// Calculate total price
	payment.CalculateTotalPrice()

	err := s.paymentStore.CreatePayment(payment)
	if err != nil {
		return nil, err
	}

	return payment, nil
}

func (s *PaymentService) UpdatePayment(id uuid.UUID, req *model.UpdatePaymentRequest) (*model.Payment, error) {
	// Get existing payment
	existingPayment, err := s.paymentStore.GetPaymentByID(id)
	if err != nil {
		return nil, err
	}
	if existingPayment == nil {
		return nil, errors.New("payment not found")
	}

	// Update only provided fields
	if req.Status != nil {
		// Validate status transitions
		if !s.isValidStatusTransition(existingPayment.Status, *req.Status) {
			return nil, errors.New("invalid status transition")
		}
		existingPayment.Status = *req.Status
	}

	if req.PaymentMethod != nil {
		existingPayment.PaymentMethod = *req.PaymentMethod
	}

	if req.Description != nil {
		existingPayment.Description = *req.Description
	}

	existingPayment.UpdatedAt = time.Now()

	err = s.paymentStore.UpdatePayment(id, existingPayment)
	if err != nil {
		return nil, err
	}

	return existingPayment, nil
}

func (s *PaymentService) DeletePayment(id uuid.UUID) error {
	// Check if payment exists
	existingPayment, err := s.paymentStore.GetPaymentByID(id)
	if err != nil {
		return err
	}
	if existingPayment == nil {
		return errors.New("payment not found")
	}

	// Only allow deletion of pending or failed payments
	if existingPayment.Status != model.PaymentStatusPending && existingPayment.Status != model.PaymentStatusFailed {
		return errors.New("cannot delete completed payment")
	}

	return s.paymentStore.DeletePayment(id)
}

func (s *PaymentService) isValidStatusTransition(from, to model.PaymentStatus) bool {
	validTransitions := map[model.PaymentStatus][]model.PaymentStatus{
		model.PaymentStatusPending: {
			model.PaymentStatusPaid,
			model.PaymentStatusFailed,
			model.PaymentStatusCancelled,
		},
		model.PaymentStatusPaid: {
			model.PaymentStatusRefunded,
		},
		model.PaymentStatusFailed:    {},
		model.PaymentStatusCancelled: {},
		model.PaymentStatusRefunded:  {},
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