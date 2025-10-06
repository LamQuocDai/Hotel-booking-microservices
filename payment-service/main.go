package main

import (
	"log"
	"payment-service/internal/config"
	"payment-service/internal/handlers"
	"payment-service/internal/repository"
	"payment-service/internal/services"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/mongo"
)

func main() {
	cfg := config.Load()

	// Connect to MongoDB
	client, err := repository.ConnectMongoDB(cfg.MongoURI)
	if err != nil {
		log.Fatal("Failed to connect to MongoDB:", err)
	}
	defer func() {
		if err = client.Disconnect(nil); err != nil {
			log.Fatal("Failed to disconnect from MongoDB:", err)
		}
	}()

	// Initialize repository
	paymentRepo := repository.NewPaymentRepository(client, cfg.DatabaseName)

	// Initialize services
	paymentService := services.NewPaymentService(paymentRepo)

	// Initialize handlers
	paymentHandler := handlers.NewPaymentHandler(paymentService)

	// Setup routes
	router := gin.Default()
	router.Use(gin.Logger())
	router.Use(gin.Recovery())

	// API routes
	api := router.Group("/api/v1")
	{
		api.POST("/payments", paymentHandler.CreatePayment)
		api.GET("/payments", paymentHandler.GetPayments)
		api.GET("/payments/:id", paymentHandler.GetPayment)
		api.GET("/promotions", paymentHandler.GetPromotions)
	}

	// Start server
	log.Printf("Payment service starting on port %s", cfg.Port)
	if err := router.Run(":" + cfg.Port); err != nil {
		log.Fatal("Failed to start server:", err)
	}
}
