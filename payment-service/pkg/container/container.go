package container

import (
	"payment-service/internal/config"
	"payment-service/internal/handler"
	"payment-service/internal/service"
	"payment-service/internal/store"
	"payment-service/pkg/database"
)

type Container struct {
	DB     *database.MongoDB
	Config *config.Config

	// Stores
	PromotionStore   store.PromotionStore
	PaymentStore     store.PaymentStore
	TransactionStore store.TransactionStore

	// Services
	PromotionService   *service.PromotionService
	PaymentService     *service.PaymentService
	TransactionService *service.TransactionService
	MoMoService        service.MoMoService

	// Handlers
	PromotionHandler   *handler.PromotionHandler
	PaymentHandler     *handler.PaymentHandler
	TransactionHandler *handler.TransactionHandler
	MoMoHandler        *handler.MoMoHandler
}

func NewContainer(db *database.MongoDB, cfg *config.Config) *Container {
	container := &Container{
		DB:     db,
		Config: cfg,
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
	c.MoMoService = service.NewMoMoService(c.PaymentStore, &c.Config.MoMo)
}

func (c *Container) initHandlers() {
	c.PromotionHandler = handler.NewPromotionHandler(c.PromotionService)
	c.PaymentHandler = handler.NewPaymentHandler(c.PaymentService)
	c.TransactionHandler = handler.NewTransactionHandler(c.TransactionService)
	c.MoMoHandler = handler.NewMoMoHandler(c.MoMoService, c.PaymentService)
}