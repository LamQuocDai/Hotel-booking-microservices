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

type TransactionStore interface {
	GetTransactions() ([]model.Transaction, error)
	GetTransactionsPaginated(query *pagination.PaginationQuery, filter *model.TransactionFilterQuery) ([]model.Transaction, int64, error)
	GetTransactionByID(id uuid.UUID) (*model.Transaction, error)
	GetTransactionsByPaymentID(paymentID uuid.UUID) ([]model.Transaction, error)
	CreateTransaction(transaction *model.Transaction) error
	UpdateTransaction(id uuid.UUID, transaction *model.Transaction) error
	DeleteTransaction(id uuid.UUID) error
}

type mongoStoreTransaction struct {
	collection *mongo.Collection
}

func NewMongoStoreTransaction(collection *mongo.Collection) TransactionStore {
	return &mongoStoreTransaction{
		collection: collection,
	}
}

func (s *mongoStoreTransaction) GetTransactions() ([]model.Transaction, error) {
	var transactions []model.Transaction
	cursor, err := s.collection.Find(context.TODO(), bson.M{})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(context.TODO())
	
	if err = cursor.All(context.TODO(), &transactions); err != nil {
		return nil, err
	}
	return transactions, nil
}

func (s *mongoStoreTransaction) GetTransactionsPaginated(query *pagination.PaginationQuery, filter *model.TransactionFilterQuery) ([]model.Transaction, int64, error) {
	ctx := context.TODO()
	
	// Build filter
	mongoFilter := s.buildTransactionFilter(query.Search, filter)
	
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
	
	var transactions []model.Transaction
	if err = cursor.All(ctx, &transactions); err != nil {
		return nil, 0, err
	}
	
	return transactions, total, nil
}

func (s *mongoStoreTransaction) GetTransactionByID(id uuid.UUID) (*model.Transaction, error) {
	var transaction model.Transaction
	err := s.collection.FindOne(context.TODO(), bson.M{"_id": id}).Decode(&transaction)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}
	return &transaction, nil
}

func (s *mongoStoreTransaction) GetTransactionsByPaymentID(paymentID uuid.UUID) ([]model.Transaction, error) {
	var transactions []model.Transaction
	cursor, err := s.collection.Find(context.TODO(), bson.M{"payment_id": paymentID})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(context.TODO())
	
	if err = cursor.All(context.TODO(), &transactions); err != nil {
		return nil, err
	}
	return transactions, nil
}

func (s *mongoStoreTransaction) CreateTransaction(transaction *model.Transaction) error {
	transaction.ID = uuid.New()
	transaction.CreatedAt = time.Now()
	transaction.UpdatedAt = time.Now()
	
	_, err := s.collection.InsertOne(context.TODO(), transaction)
	return err
}

func (s *mongoStoreTransaction) UpdateTransaction(id uuid.UUID, transaction *model.Transaction) error {
	transaction.UpdatedAt = time.Now()
	
	update := bson.M{"$set": transaction}
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

func (s *mongoStoreTransaction) DeleteTransaction(id uuid.UUID) error {
	result, err := s.collection.DeleteOne(context.TODO(), bson.M{"_id": id})
	if err != nil {
		return err
	}
	
	if result.DeletedCount == 0 {
		return mongo.ErrNoDocuments
	}
	
	return nil
}

func (s *mongoStoreTransaction) buildTransactionFilter(search string, filter *model.TransactionFilterQuery) bson.M {
	mongoFilter := bson.M{}
	
	// Search filter
	if search != "" {
		mongoFilter["$or"] = []bson.M{
			{"external_id": bson.M{"$regex": search, "$options": "i"}},
			{"payment_gateway": bson.M{"$regex": search, "$options": "i"}},
			{"failure_reason": bson.M{"$regex": search, "$options": "i"}},
		}
	}
	
	if filter == nil {
		return mongoFilter
	}
	
	// Payment ID filter
	if filter.PaymentID != nil {
		mongoFilter["payment_id"] = *filter.PaymentID
	}
	
	// Status filter
	if filter.Status != "" {
		mongoFilter["status"] = filter.Status
	}
	
	// Type filter
	if filter.Type != "" {
		mongoFilter["type"] = filter.Type
	}
	
	// Payment gateway filter
	if filter.PaymentGateway != "" {
		mongoFilter["payment_gateway"] = bson.M{"$regex": filter.PaymentGateway, "$options": "i"}
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
		mongoFilter["amount"] = amountFilter
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