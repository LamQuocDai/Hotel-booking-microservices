package model

import "time"

import (
	"time"
	"github.com/google/uuid"
)

type Promotion struct {
	ID          uuid.UUID    `bson:"id"`
	Code 			string    `bson:"code"`
	StartDate   time.Time `bson:"start_date"`
	EndDate     time.Time `bson:"end_date"`
	Discount    float64   `bson:"discount"`
	CreatedAt   time.Time `bson:"created_at"`
	UpdatedAt   time.Time `bson:"updated_at"`
}

func (p *Promotion) IsActive(at time.Time) bool {
	return at.After(p.StartDate) && at.Before(p.EndDate)
}
