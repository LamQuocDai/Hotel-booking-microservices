# 🏨 Hotel Booking Microservices System

Hệ thống đặt phòng khách sạn triển khai theo mô hình **Microservices** với các công nghệ hiện đại.

## 📦 Kiến trúc hệ thống

### Services

- **Frontend** (Next.js + TypeScript) - Port 3000
- **API Gateway** (NestJS) - Port 3001
- **Account-service** (Java Spring Boot + MySQL) - Port 3002
- **Booking-service** (.NET 8 + PostgreSQL + Redis) - Port 3003
- **Payment-service** (Golang + MongoDB) - Port 3004

### Infrastructure

- **Consul** (Service Discovery) - Port 8500
- **Prometheus** (Monitoring) - Port 9090
- **Grafana** (Dashboard) - Port 3005
- **MySQL** (Account data) - Port 3306
- **PostgreSQL** (Booking data) - Port 5432
- **MongoDB** (Payment data) - Port 27017
- **Redis** (Caching & Lock) - Port 6379

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- Node.js 18+ (for local development)
- Java 17+ (for local development)
- .NET 8 SDK (for local development)
- Go 1.22+ (for local development)

### 1. Clone repository

```bash
git clone <repository-url>
cd hotel-booking
```

### 2. Generate JWT Keys

```bash
# Generate private key
openssl genrsa -out private.pem 2048

# Generate public key
openssl rsa -in private.pem -pubout -out public.pem

# Copy keys to .env file
cp env.example .env
# Edit .env with your generated keys
```

### 3. Start all services

```bash
docker-compose up -d
```

### 4. Access services

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:3001
- **Consul UI**: http://localhost:8500
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3005 (admin/admin)

## 🔧 Development

### Frontend Development

```bash
cd frontend
npm install
npm run dev
```

### API Gateway Development

```bash
cd api-gateway
npm install
npm run start:dev
```

### Account Service Development

```bash
cd account-service
mvn spring-boot:run
```

### Booking Service Development

```bash
cd booking-service
dotnet run
```

### Payment Service Development

```bash
cd payment-service
go run main.go
```

## 📊 Monitoring

### Prometheus Metrics

- **API Gateway**: http://localhost:3001/metrics
- **Account Service**: http://localhost:3002/actuator/prometheus
- **Booking Service**: http://localhost:3003/metrics
- **Payment Service**: http://localhost:3004/metrics

### Grafana Dashboards

1. Access Grafana at http://localhost:3005
2. Login with admin/admin
3. Import dashboards for microservices monitoring

## 🔐 Authentication

Hệ thống sử dụng JWT với RSA key pair:

- **Private Key**: Ký JWT (Account Service)
- **Public Key**: Xác minh JWT (API Gateway & Services)

## 🗄️ Database Schema

### MySQL (Account Service)

- `Account` - Thông tin người dùng
- `Role` - Vai trò người dùng
- `Permission` - Quyền hạn
- `RolePermission` - Liên kết role-permission

### PostgreSQL (Booking Service)

- `Room` - Thông tin phòng
- `TypeRoom` - Loại phòng
- `Image` - Hình ảnh phòng
- `Review` - Đánh giá phòng
- `Location` - Địa điểm
- `RoomBooking` - Đặt phòng

### MongoDB (Payment Service)

- `Promotion` - Mã khuyến mãi
- `Payment` - Thanh toán
- `Transaction` - Giao dịch

## 🔄 API Endpoints

### Authentication

- `POST /auth/login` - Đăng nhập
- `POST /auth/register` - Đăng ký
- `POST /auth/refresh` - Làm mới token

### Account

- `GET /account/profile` - Thông tin cá nhân

### Booking

- `GET /booking/rooms` - Danh sách phòng
- `GET /booking/rooms/:id` - Chi tiết phòng
- `POST /booking/book` - Đặt phòng
- `GET /booking/my-bookings` - Lịch sử đặt phòng

### Payment

- `POST /payment/create` - Tạo thanh toán
- `GET /payment/promotions` - Danh sách khuyến mãi
- `GET /payment/my-payments` - Lịch sử thanh toán
- `GET /payment/:id` - Chi tiết thanh toán

## 🐳 Docker Commands

### Build all services

```bash
docker-compose build
```

### Start specific service

```bash
docker-compose up frontend
```

### View logs

```bash
docker-compose logs -f api-gateway
```

### Stop all services

```bash
docker-compose down
```

### Clean up

```bash
docker-compose down -v
docker system prune -a
```

## 🧪 Testing

### Run all tests

```bash
# Frontend
cd frontend && npm test

# API Gateway
cd api-gateway && npm test

# Account Service
cd account-service && mvn test

# Booking Service
cd booking-service && dotnet test

# Payment Service
cd payment-service && go test ./...
```

## 📝 Environment Variables

Copy `env.example` to `.env` and configure:

```env
JWT_PRIVATE_KEY=your_private_key
JWT_PUBLIC_KEY=your_public_key
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 3000-3005, 3306, 5432, 6379, 8500, 9090, 27017 are available
2. **JWT keys**: Ensure JWT keys are properly generated and configured
3. **Database connections**: Check database containers are running
4. **Service discovery**: Verify Consul is running and services are registered

### Logs

```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs api-gateway
```

## 📞 Support

For support, email support@hotelbooking.com or create an issue in the repository.
