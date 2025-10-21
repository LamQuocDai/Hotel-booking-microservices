package router

import (
	"payment-service/internal/handler"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
)

type Router struct {
	engine *gin.Engine
}

type Handlers struct {
	PromotionHandler   *handler.PromotionHandler
	PaymentHandler     *handler.PaymentHandler
	TransactionHandler *handler.TransactionHandler
}

func NewRouter() *Router {
	return &Router{
		engine: gin.Default(),
	}
}

func (r *Router) SetupRoutes(handlers *Handlers) {
	// Swagger documentation
	r.engine.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	// Health check
	r.engine.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{"status": "healthy", "service": "payment-service"})
	})

	// API versioning
	v1 := r.engine.Group("/api/v1")
	{
		// Promotion routes
		promotions := v1.Group("/promotions")
		{
			promotions.GET("", handlers.PromotionHandler.GetPromotions)
			promotions.GET("/:id", handlers.PromotionHandler.GetPromotionByID)
			promotions.POST("", handlers.PromotionHandler.CreatePromotion)
			promotions.PUT("/:id", handlers.PromotionHandler.UpdatePromotion)
			promotions.DELETE("/:id", handlers.PromotionHandler.DeletePromotion)
		}

		// Payment routes
		payments := v1.Group("/payments")
		{
			payments.GET("", handlers.PaymentHandler.GetPayments)
			payments.GET("/:id", handlers.PaymentHandler.GetPaymentByID)
			payments.GET("/customer/:customerId", handlers.PaymentHandler.GetPaymentsByCustomerID)
			payments.POST("", handlers.PaymentHandler.CreatePayment)
			payments.PUT("/:id", handlers.PaymentHandler.UpdatePayment)
			payments.DELETE("/:id", handlers.PaymentHandler.DeletePayment)
		}

		// Transaction routes
		transactions := v1.Group("/transactions")
		{
			transactions.GET("", handlers.TransactionHandler.GetTransactions)
			transactions.GET("/:id", handlers.TransactionHandler.GetTransactionByID)
			transactions.GET("/payment/:paymentId", handlers.TransactionHandler.GetTransactionsByPaymentID)
			transactions.POST("", handlers.TransactionHandler.CreateTransaction)
			transactions.PUT("/:id", handlers.TransactionHandler.UpdateTransaction)
			transactions.DELETE("/:id", handlers.TransactionHandler.DeleteTransaction)
		}
	}
}

func (r *Router) Start(port string) error {
	return r.engine.Run(":" + port)
}

func (r *Router) GetEngine() *gin.Engine {
	return r.engine
}