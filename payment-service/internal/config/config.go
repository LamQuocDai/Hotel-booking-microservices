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
	// MoMo Configuration
	MoMo MoMoConfig
}

type MoMoConfig struct {
	PartnerCode string
	AccessKey   string
	SecretKey   string
	Endpoint    string
	IpnURL      string
	PartnerName string
	StoreID     string
}

func LoadConfig() *Config {
	if err := godotenv.Load(); err != nil {
		log.Println("Warning: .env file not found, using environment variables")
	}

	return &Config{
		DatabaseURL:  getEnv("URL_DB", "mongodb://localhost:27017"),
		DatabaseName: getEnv("NAME_DB", "payment_service"),
		Port:         getEnv("PORT", "8081"),
		MoMo: MoMoConfig{
			PartnerCode: getEnv("MOMO_PARTNER_CODE", ""),
			AccessKey:   getEnv("MOMO_ACCESS_KEY", ""),
			SecretKey:   getEnv("MOMO_SECRET_KEY", ""),
			Endpoint:    getEnv("MOMO_ENDPOINT", "https://test-payment.momo.vn/v2/gateway/api/create"),
			IpnURL:      getEnv("MOMO_IPN_URL", "https://your-domain.com/api/v1/payments/momo/webhook"),
			PartnerName: getEnv("MOMO_PARTNER_NAME", "Test Partner"),
			StoreID:     getEnv("MOMO_STORE_ID", "MomoTestStore"),
		},
	}
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}