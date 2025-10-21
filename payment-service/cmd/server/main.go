// Package main Payment Service API
// @title Payment Service API
// @version 1.0
// @description This is a payment service API with promotion management
// @termsOfService http://swagger.io/terms/

// @contact.name API Support
// @contact.url http://www.swagger.io/support
// @contact.email support@swagger.io

// @license.name Apache 2.0
// @license.url http://www.apache.org/licenses/LICENSE-2.0.html

// @host localhost:8081
// @BasePath /api/v1
// @schemes http https

package main

import (
	"fmt"
	"log"
	"payment-service/internal/config"
	"payment-service/pkg/container"
	"payment-service/pkg/database"
	"payment-service/pkg/router"

	_ "payment-service/docs"
)

func main() {
	// Load configuration
	cfg := config.LoadConfig()

	// Initialize database
	db, err := database.NewMongoDB()
	if err != nil {
		log.Fatal("Failed to connect to database: ", err)
	}
	defer db.Close()

	// Initialize container with dependency injection
	container := container.NewContainer(db)

	// Setup router
	r := router.NewRouter()
	handlers := &router.Handlers{
		PromotionHandler:   container.PromotionHandler,
		PaymentHandler:     container.PaymentHandler,
		TransactionHandler: container.TransactionHandler,
	}
	r.SetupRoutes(handlers)

	// Start server
	fmt.Printf("🚀 Payment Service running on port %s\n", cfg.Port)
	fmt.Printf("📚 Swagger documentation available at: http://localhost:%s/swagger/index.html\n", cfg.Port)
	if err := r.Start(cfg.Port); err != nil {
		log.Fatal("Failed to run server: ", err)
	}
}