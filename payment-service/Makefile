# Variables
BINARY_NAME=payment-service
BINARY_PATH=bin/$(BINARY_NAME).exe
MAIN_PATH=cmd/server/main.go

# Build commands
.PHONY: build
build:
	go build -o $(BINARY_PATH) $(MAIN_PATH)

.PHONY: run
run:
	go run $(MAIN_PATH)

.PHONY: clean
clean:
	go clean
	rm -f $(BINARY_PATH)

# Development commands
.PHONY: dev
dev:
	go run $(MAIN_PATH)

.PHONY: test
test:
	go test ./...

.PHONY: test-verbose
test-verbose:
	go test -v ./...

# Dependencies
.PHONY: deps
deps:
	go mod download
	go mod tidy

# Swagger
.PHONY: swagger
swagger:
	swag init -g $(MAIN_PATH)

# Docker commands
.PHONY: docker-build
docker-build:
	docker build -t $(BINARY_NAME) .

.PHONY: docker-run
docker-run:
	docker run -p 8081:8081 --env-file .env $(BINARY_NAME)

# Help
.PHONY: help
help:
	@echo "Available commands:"
	@echo "  build         - Build the application"
	@echo "  run/dev       - Run the application"
	@echo "  clean         - Clean build artifacts"
	@echo "  test          - Run tests"
	@echo "  test-verbose  - Run tests with verbose output"
	@echo "  deps          - Download and tidy dependencies"
	@echo "  swagger       - Generate swagger documentation"
	@echo "  docker-build  - Build Docker image"
	@echo "  docker-run    - Run Docker container"
	@echo "  help          - Show this help message"