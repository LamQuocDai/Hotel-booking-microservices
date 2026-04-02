# Account Service - gRPC Quick Start

## Overview

The Account Service is now a gRPC-based microservice for hotel booking authentication and account management.

**gRPC Server**: `localhost:50051`

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- SendGrid API Key

### Build

```bash
mvn clean package
```

This will:

- Compile Java source code
- Compile Protocol Buffer files (.proto)
- Generate gRPC stubs
- Package the application

### Run

```bash
mvn spring-boot:run
```

Server starts on port **50051**

### Configuration

Set environment variables:

```bash
MYSQL_URL=jdbc:mysql://localhost:3306/account_db?useSSL=false&serverTimezone=UTC
MYSQL_USER=root
MYSQL_PASSWORD=your_password
SENDGRID_API_KEY=your_sendgrid_key
SENDGRID_FROM_EMAIL=noreply@example.com
SENDGRID_FROM_NAME=Hotel Booking
GRPC_SERVER_PORT=50051
```

Or update `src/main/resources/application.yml`

## Basic Usage

### Using gRPCurl

Install gRPCurl first: https://github.com/fullstorydev/grpcurl

#### Login

```bash
grpcurl -plaintext \
  -d '{"email":"user@example.com","password":"password123"}' \
  localhost:50051 \
  com.hotelbooking.account.AuthService/Login
```

#### Register

```bash
grpcurl -plaintext \
  -d '{
    "username":"newuser",
    "email":"new@example.com",
    "password":"pass123",
    "password_confirmation":"pass123",
    "phone":"0123456789"
  }' \
  localhost:50051 \
  com.hotelbooking.account.AuthService/Register
```

#### Get Profile (with JWT)

```bash
grpcurl -plaintext \
  -H "authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"user_id":"user-uuid"}' \
  localhost:50051 \
  com.hotelbooking.account.AuthService/GetProfile
```

#### Health Check

```bash
grpcurl -plaintext \
  localhost:50051 \
  com.hotelbooking.account.HealthService/Check
```

## Java Client Example

```java
import com.hotelbooking.account.proto.*;
import io.grpc.*;

// Create channel
ManagedChannel channel = ManagedChannelBuilder
    .forAddress("localhost", 50051)
    .usePlaintext()
    .build();

// Create stub
AuthServiceGrpc.AuthServiceBlockingStub stub =
    AuthServiceGrpc.newBlockingStub(channel);

// Login
LoginResponse response = stub.login(LoginRequest.newBuilder()
    .setEmail("user@example.com")
    .setPassword("password123")
    .build());

System.out.println("Token: " + response.getToken());
System.out.println("User: " + response.getUser().getUsername());

// Clean up
channel.shutdown();
```

## Project Structure

```
src/main/
├── java/com/hotelbooking/account/
│   ├── grpc/
│   │   ├── AuthGrpcServiceImpl.java          # Authentication service
│   │   ├── AdminGrpcServiceImpl.java         # Admin service
│   │   ├── HealthCheckServiceImpl.java       # Health checks
│   │   ├── JwtAuthenticationInterceptor.java # JWT validation
│   │   └── ...
│   ├── service/
│   │   ├── AuthService.java                 # Auth business logic
│   │   ├── AdminUserService.java            # Admin business logic
│   │   ├── JwtService.java                  # JWT token generation/validation
│   │   └── ...
│   ├── config/
│   │   ├── GrpcConfig.java                  # gRPC configuration
│   │   └── SecurityConfig.java              # Security configuration
│   └── ...
├── proto/
│   └── account.proto                        # gRPC service definitions
└── resources/
    └── application.yml                      # Application configuration
```

## Key Services

### AuthService

- `Login` - Authenticate with email/password
- `Register` - Create new account
- `RefreshToken` - Get new JWT
- `Logout` - Invalidate token
- `VerifyEmail` - Confirm email
- `ResendVerificationEmail` - Resend verification
- `GetProfile` - Get user profile
- `UpdateProfile` - Update phone/image
- `ChangePassword` - Change password
- `ForgotPassword` - Request password reset
- `ResetPassword` - Reset password with token

### AdminService

- `ListUsers` - List all users with pagination
- `GetUsersByRole` - Filter by role
- `GetAvailableRoles` - Get list of roles
- `CreateUser` - Create new user
- `UpdateUser` - Update user
- `DeleteUser` - Delete user

### HealthService

- `Check` - Health status

## Authentication

All protected endpoints require JWT in metadata:

```
authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

Public endpoints (no JWT required):

- `AuthService/Login`
- `AuthService/Register`
- `AuthService/RefreshToken`
- `AuthService/VerifyEmail`
- `AuthService/ResendVerificationEmail`
- `AuthService/ForgotPassword`
- `AuthService/ResetPassword`
- `HealthService/Check`

## Testing

### Run Tests

```bash
mvn test
```

### Integration Tests

Tests are located in `src/test/java`

## Documentation

See [GRPC_MIGRATION_GUIDE.md](./GRPC_MIGRATION_GUIDE.md) for comprehensive service documentation.

## Troubleshooting

### Port in Use

```bash
# Change port in application.yml
grpc:
  server:
    port: 50052  # Use different port
```

### Database Connection Error

Check `application.yml` database configuration:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/account_db?useSSL=false&serverTimezone=UTC
    username: root
    password: # Set password
```

### SendGrid API Key Invalid

Generate new key from SendGrid dashboard and set:

```bash
SENDGRID_API_KEY=SG.your_new_key...
```

## Next Steps

1. Deploy to development environment
2. Update client applications to use gRPC
3. Generate client libraries for other languages
4. Integrate with other microservices
5. Set up monitoring and logging

## Support

For issues or questions, refer to:

- [GRPC_MIGRATION_GUIDE.md](./GRPC_MIGRATION_GUIDE.md)
- [gRPC Documentation](https://grpc.io/)
- [Account Service Wiki](https://github.com/your-org/account-service/wiki)
