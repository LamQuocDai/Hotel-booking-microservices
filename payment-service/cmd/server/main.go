package main

import (
    "os"
    "github.com/joho/godotenv"
    "log"
    "context"
    "time"
    "go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)


import (
	"fmt"
	"net/http"
)

func main() {
    if err := godotenv.Load(); err != nil {
        log.Println("Error loading .env file")
    }
    client, err := mongo.NewClient(options.Client().ApplyURI(os.Getenv("URL_DB")))
    if err != nil {
        log.Println("Error connecting to MongoDB", err)
        return
    }
    ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	err = client.Connect(ctx)
	if err != nil {
		log.Fatal(err)
	}
	defer client.Disconnect(ctx)
	log.Println("Successfully connected to MongoDB!")


	fmt.Println("🚀 Payment Service running on port 8081")
    http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
        w.Write([]byte("Payment service OK"))
    })
    http.ListenAndServe(":8081", nil)
}