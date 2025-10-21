package config

import (
	"log"
	"os"

	"github.com/joho/godotenv"
)

type Config struct {
	DatabaseURL string
	DatabaseName string
	Port        string
}

func LoadConfig() *Config {
	if err := godotenv.Load(); err != nil {
		log.Println("Warning: .env file not found, using environment variables")
	}

	return &Config{
		DatabaseURL:  getEnv("URL_DB", "mongodb://localhost:27017"),
		DatabaseName: getEnv("NAME_DB", "payment_service"),
		Port:         getEnv("PORT", "8081"),
	}
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}