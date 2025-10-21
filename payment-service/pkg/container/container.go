package container

import (
	"payment-service/internal/handler"
	"payment-service/internal/service"
	"payment-service/internal/store"
	"payment-service/pkg/database"
)

type Container struct {
	DB *database.MongoDB

	// Stores
	PromotionStore   store.PromotionStore
	PaymentStore     store.PaymentStore
	TransactionStore store.TransactionStore

	// Services
	PromotionService   *service.PromotionService
	PaymentService     *service.PaymentService
	TransactionService *service.TransactionService

	// Handlers
	PromotionHandler   *handler.PromotionHandler
	PaymentHandler     *handler.PaymentHandler
	TransactionHandler *handler.TransactionHandler
}

func NewContainer(db *database.MongoDB) *Container {
	container := &Container{
		DB: db,
	}

	container.initStores()
	container.initServices()
	container.initHandlers()

	return container
}

func (c *Container) initStores() {
	c.PromotionStore = store.NewMongoStorePromotion(c.DB.GetCollection("promotions"))
	c.PaymentStore = store.NewMongoStorePayment(c.DB.GetCollection("payments"))
	c.TransactionStore = store.NewMongoStoreTransaction(c.DB.GetCollection("transactions"))
}

func (c *Container) initServices() {
	c.PromotionService = service.NewPromotionService(c.PromotionStore)
	c.PaymentService = service.NewPaymentService(c.PaymentStore, c.PromotionStore)
	c.TransactionService = service.NewTransactionService(c.TransactionStore, c.PaymentStore)
}

func (c *Container) initHandlers() {
	c.PromotionHandler = handler.NewPromotionHandler(c.PromotionService)
	c.PaymentHandler = handler.NewPaymentHandler(c.PaymentService)
	c.TransactionHandler = handler.NewTransactionHandler(c.TransactionService)
}