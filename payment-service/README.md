# Payment Service API

A Go-based payment service with promotion management capabilities, built with Gin framework and MongoDB.

## Features

- ✅ **Clean Architecture**: Separation of concerns with layered architecture
- ✅ **CRUD Operations**: Complete CRUD for promotions management
- ✅ **Custom Response Format**: Standardized API responses
- ✅ **Swagger Documentation**: Auto-generated API documentation
- ✅ **Dependency Injection**: Centralized dependency management
- ✅ **MongoDB Integration**: Robust database operations
- ✅ **Input Validation**: Request validation with proper error handling

## Project Structure

```
payment-service/
├── cmd/server/         # Application entry point
├── internal/
│   ├── config/         # Configuration management
│   ├── handler/        # HTTP handlers (controllers)
│   ├── model/          # Data models and DTOs
│   ├── service/        # Business logic
│   └── store/          # Data access layer
├── pkg/
│   ├── container/      # Dependency injection container
│   ├── database/       # Database connection management
│   ├── response/       # Standardized API responses
│   └── router/         # HTTP routing
├── docs/               # Swagger documentation (auto-generated)
└── .env.example        # Environment variables template
```

## API Response Format

All API responses follow this standardized format:

```json
{
  "isSuccess": true,
  "data": {},
  "message": "Success message",
  "statusCode": 200
}
```

## API Endpoints

### Promotions API

| Method | Endpoint                 | Description          |
| ------ | ------------------------ | -------------------- |
| GET    | `/api/v1/promotions`     | Get all promotions   |
| GET    | `/api/v1/promotions/:id` | Get promotion by ID  |
| POST   | `/api/v1/promotions`     | Create new promotion |
| PUT    | `/api/v1/promotions/:id` | Update promotion     |
| DELETE | `/api/v1/promotions/:id` | Delete promotion     |

## Quick Start

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd payment-service
   ```

2. **Set up environment variables**

   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Install dependencies**

   ```bash
   go mod tidy
   ```

4. **Start MongoDB**

   ```bash
   # Using Docker
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   ```

5. **Generate Swagger docs**

   ```bash
   swag init -g cmd/server/main.go
   ```

6. **Run the application**

   ```bash
   go run cmd/server/main.go
   ```

7. **Access the application**
   - API: http://localhost:8081
   - Swagger UI: http://localhost:8081/swagger/index.html

## Environment Variables

| Variable  | Description               | Default                     |
| --------- | ------------------------- | --------------------------- |
| `URL_DB`  | MongoDB connection string | `mongodb://localhost:27017` |
| `NAME_DB` | Database name             | `payment_service`           |
| `PORT`    | Server port               | `8081`                      |

## Example Usage

### Create a Promotion

```bash
curl -X POST http://localhost:8081/api/v1/promotions \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SAVE20",
    "description": "Save 20% on your booking",
    "startDate": "2024-01-01T00:00:00Z",
    "endDate": "2024-12-31T23:59:59Z",
    "discount": 20.0,
    "isActive": true
  }'
```

### Get All Promotions

```bash
curl http://localhost:8081/api/v1/promotions
```

## Development

### Project Architecture

This project follows Clean Architecture principles:

- **Handler Layer**: HTTP request/response handling
- **Service Layer**: Business logic implementation
- **Store Layer**: Data persistence abstraction
- **Model Layer**: Data structures and validation

### Adding New Features

1. Define models in `internal/model/`
2. Create store interface and implementation in `internal/store/`
3. Implement business logic in `internal/service/`
4. Add HTTP handlers in `internal/handler/`
5. Register routes in `pkg/router/`
6. Update dependency injection in `pkg/container/`

### Running Tests

```bash
go test ./...
```

## Technologies Used

- **Go 1.24**: Programming language
- **Gin**: HTTP web framework
- **MongoDB**: Database
- **Swagger**: API documentation
- **UUID**: Unique identifier generation

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.
