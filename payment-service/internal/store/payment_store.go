package store

import (
	"payment-service/internal/model"
	"payment-service/pkg/pagination"

	"context"
	"time"

	"github.com/google/uuid"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type PaymentStore interface {
	GetPayments() ([]model.Payment, error)
	GetPaymentsPaginated(query *pagination.PaginationQuery, filter *model.PaymentFilterQuery) ([]model.Payment, int64, error)
	GetPaymentByID(id uuid.UUID) (*model.Payment, error)
	GetPaymentsByCustomerID(customerID uuid.UUID) ([]model.Payment, error)
	CreatePayment(payment *model.Payment) error
	UpdatePayment(id uuid.UUID, payment *model.Payment) error
	DeletePayment(id uuid.UUID) error
}

type mongoStorePayment struct {
	collection *mongo.Collection
}

func NewMongoStorePayment(collection *mongo.Collection) PaymentStore {
	return &mongoStorePayment{
		collection: collection,
	}
}

func (s *mongoStorePayment) GetPayments() ([]model.Payment, error) {
	var payments []model.Payment
	cursor, err := s.collection.Find(context.TODO(), bson.M{})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(context.TODO())
	
	if err = cursor.All(context.TODO(), &payments); err != nil {
		return nil, err
	}
	return payments, nil
}

func (s *mongoStorePayment) GetPaymentsPaginated(query *pagination.PaginationQuery, filter *model.PaymentFilterQuery) ([]model.Payment, int64, error) {
	ctx := context.TODO()
	
	// Build filter
	mongoFilter := s.buildPaymentFilter(query.Search, filter)
	
	// Count total documents
	total, err := s.collection.CountDocuments(ctx, mongoFilter)
	if err != nil {
		return nil, 0, err
	}
	
	// Build sort option
	sortOrder := 1
	if query.SortDirection == "desc" {
		sortOrder = -1
	}
	
	findOptions := options.Find().
		SetSort(bson.D{{Key: query.SortBy, Value: sortOrder}}).
		SetSkip(int64(query.GetOffset())).
		SetLimit(int64(query.GetLimit()))
	
	// Execute query
	cursor, err := s.collection.Find(ctx, mongoFilter, findOptions)
	if err != nil {
		return nil, 0, err
	}
	defer cursor.Close(ctx)
	
	var payments []model.Payment
	if err = cursor.All(ctx, &payments); err != nil {
		return nil, 0, err
	}
	
	return payments, total, nil
}

func (s *mongoStorePayment) GetPaymentByID(id uuid.UUID) (*model.Payment, error) {
	var payment model.Payment
	err := s.collection.FindOne(context.TODO(), bson.M{"_id": id}).Decode(&payment)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}
	return &payment, nil
}

func (s *mongoStorePayment) GetPaymentsByCustomerID(customerID uuid.UUID) ([]model.Payment, error) {
	var payments []model.Payment
	cursor, err := s.collection.Find(context.TODO(), bson.M{"customer_id": customerID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(context.TODO())
	
	if err = cursor.All(context.TODO(), &payments); err != nil {
		return nil, err
	}
	return payments, nil
}

func (s *mongoStorePayment) CreatePayment(payment *model.Payment) error {
	payment.ID = uuid.New()
	payment.CreatedAt = time.Now()
	payment.UpdatedAt = time.Now()
	
	_, err := s.collection.InsertOne(context.TODO(), payment)
	return err
}

func (s *mongoStorePayment) UpdatePayment(id uuid.UUID, payment *model.Payment) error {
	payment.UpdatedAt = time.Now()
	
	update := bson.M{"$set": payment}
	result, err := s.collection.UpdateOne(
		context.TODO(),
		bson.M{"_id": id},
		update,
	)
	
	if err != nil {
		return err
	}
	
	if result.MatchedCount == 0 {
		return mongo.ErrNoDocuments
	}
	
	return nil
}

func (s *mongoStorePayment) DeletePayment(id uuid.UUID) error {
	result, err := s.collection.DeleteOne(context.TODO(), bson.M{"_id": id})
	if err != nil {
		return err
	}
	
	if result.DeletedCount == 0 {
		return mongo.ErrNoDocuments
	}
	
	return nil
}

func (s *mongoStorePayment) buildPaymentFilter(search string, filter *model.PaymentFilterQuery) bson.M {
	mongoFilter := bson.M{}
	
	// Search filter
	if search != "" {
		mongoFilter["$or"] = []bson.M{
			{"description": bson.M{"$regex": search, "$options": "i"}},
			{"payment_method": bson.M{"$regex": search, "$options": "i"}},
		}
	}
	
	if filter == nil {
		return mongoFilter
	}
	
	// Status filter
	if filter.Status != "" {
		mongoFilter["status"] = filter.Status
	}
	
	// Customer ID filter
	if filter.CustomerID != nil {
		mongoFilter["customer_id"] = *filter.CustomerID
	}
	
	// Payment method filter
	if filter.PaymentMethod != "" {
		mongoFilter["payment_method"] = bson.M{"$regex": filter.PaymentMethod, "$options": "i"}
	}
	
	// Amount range filter
	if filter.MinAmount != nil || filter.MaxAmount != nil {
		amountFilter := bson.M{}
		if filter.MinAmount != nil {
			amountFilter["$gte"] = *filter.MinAmount
		}
		if filter.MaxAmount != nil {
			amountFilter["$lte"] = *filter.MaxAmount
		}
		mongoFilter["total_price"] = amountFilter
	}
	
	// Currency filter
	if filter.Currency != "" {
		mongoFilter["currency"] = filter.Currency
	}
	
	// Date range filter
	if filter.DateFrom != nil || filter.DateTo != nil {
		dateFilter := bson.M{}
		if filter.DateFrom != nil {
			dateFilter["$gte"] = *filter.DateFrom
		}
		if filter.DateTo != nil {
			dateFilter["$lte"] = *filter.DateTo
		}
		mongoFilter["created_at"] = dateFilter
	}
	
	return mongoFilter
}