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

type PromotionStore interface {
	GetPromotions() ([]model.Promotion, error)
	GetPromotionsPaginated(query *pagination.PaginationQuery, filter *model.PromotionFilterQuery) ([]model.Promotion, int64, error)
	GetPromotionByID(id uuid.UUID) (*model.Promotion, error)
	GetPromotionByCode(code string) (*model.Promotion, error)
	CreatePromotion(promotion *model.Promotion) error
	UpdatePromotion(id uuid.UUID, promotion *model.Promotion) error
	DeletePromotion(id uuid.UUID) error
}

type mongoStorePromotion struct {
	collection *mongo.Collection
}

func NewMongoStorePromotion(collection *mongo.Collection) PromotionStore {
	return &mongoStorePromotion{
		collection: collection,
	}
}

func (s *mongoStorePromotion) GetPromotions() ([]model.Promotion, error) {
	var promotions []model.Promotion
	cursor, err := s.collection.Find(context.TODO(), bson.M{})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(context.TODO())
	
	if err = cursor.All(context.TODO(), &promotions); err != nil {
		return nil, err
	}
	return promotions, nil
}

func (s *mongoStorePromotion) GetPromotionByID(id uuid.UUID) (*model.Promotion, error) {
	var promotion model.Promotion
	err := s.collection.FindOne(context.TODO(), bson.M{"_id": id}).Decode(&promotion)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}
	return &promotion, nil
}

func (s *mongoStorePromotion) GetPromotionByCode(code string) (*model.Promotion, error) {
	var promotion model.Promotion
	err := s.collection.FindOne(context.TODO(), bson.M{"code": code}).Decode(&promotion)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}
	return &promotion, nil
}

func (s *mongoStorePromotion) CreatePromotion(promotion *model.Promotion) error {
	promotion.ID = uuid.New()
	promotion.CreatedAt = time.Now()
	promotion.UpdatedAt = time.Now()
	
	_, err := s.collection.InsertOne(context.TODO(), promotion)
	return err
}

func (s *mongoStorePromotion) UpdatePromotion(id uuid.UUID, promotion *model.Promotion) error {
	promotion.UpdatedAt = time.Now()
	
	update := bson.M{"$set": promotion}
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

func (s *mongoStorePromotion) DeletePromotion(id uuid.UUID) error {
	result, err := s.collection.DeleteOne(context.TODO(), bson.M{"_id": id})
	if err != nil {
		return err
	}
	
	if result.DeletedCount == 0 {
		return mongo.ErrNoDocuments
	}
	
	return nil
}

func (s *mongoStorePromotion) GetPromotionsPaginated(query *pagination.PaginationQuery, filter *model.PromotionFilterQuery) ([]model.Promotion, int64, error) {
	ctx := context.TODO()
	
	// Build filter
	mongoFilter := s.buildPromotionFilter(query.Search, filter)
	
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
	
	var promotions []model.Promotion
	if err = cursor.All(ctx, &promotions); err != nil {
		return nil, 0, err
	}
	
	return promotions, total, nil
}

func (s *mongoStorePromotion) buildPromotionFilter(search string, filter *model.PromotionFilterQuery) bson.M {
	mongoFilter := bson.M{}
	
	// Search filter
	if search != "" {
		mongoFilter["$or"] = []bson.M{
			{"code": bson.M{"$regex": search, "$options": "i"}},
			{"description": bson.M{"$regex": search, "$options": "i"}},
		}
	}
	
	if filter == nil {
		return mongoFilter
	}
	
	// Specific filters
	if filter.Code != "" {
		mongoFilter["code"] = bson.M{"$regex": filter.Code, "$options": "i"}
	}
	
	if filter.IsActive != nil {
		mongoFilter["is_active"] = *filter.IsActive
	}
	
	if filter.MinDiscount != nil || filter.MaxDiscount != nil {
		discountFilter := bson.M{}
		if filter.MinDiscount != nil {
			discountFilter["$gte"] = *filter.MinDiscount
		}
		if filter.MaxDiscount != nil {
			discountFilter["$lte"] = *filter.MaxDiscount
		}
		mongoFilter["discount"] = discountFilter
	}
	
	if filter.DateFrom != nil || filter.DateTo != nil {
		dateFilter := bson.M{}
		if filter.DateFrom != nil {
			dateFilter["$gte"] = *filter.DateFrom
		}
		if filter.DateTo != nil {
			dateFilter["$lte"] = *filter.DateTo
		}
		mongoFilter["start_date"] = dateFilter
	}
	
	if filter.IsExpired != nil {
		now := time.Now()
		if *filter.IsExpired {
			mongoFilter["end_date"] = bson.M{"$lt": now}
		} else {
			mongoFilter["end_date"] = bson.M{"$gte": now}
		}
	}
	
	return mongoFilter
}
