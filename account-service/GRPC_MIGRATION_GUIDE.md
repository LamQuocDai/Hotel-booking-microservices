# gRPC Migration Guide - Account Service

This document provides comprehensive information about the gRPC migration of the hotel booking account service.

## Overview

The Account Service has been successfully migrated from REST API to gRPC for improved performance, lower latency, and better compatibility with microservices architecture.

### Key Changes

- **REST API**: Disabled (REST server port set to 0)
- **gRPC Server**: Listening on port **50051** (configurable via `grpc.server.port`)
- **Authentication**: JWT-based via gRPC metadata header
- **Transport**: HTTP/2 using gRPC protocol

## Architecture

### Services Provided

The Account Service provides three gRPC services:

1. **AuthService** - Authentication, profile, and password management
2. **AdminService** - Administrative user management
3. **HealthService** - Health checks

### Authentication

All gRPC calls require JWT authentication except for public endpoints:

```
Public Endpoints (No Auth Required):
- AuthService.Login
- AuthService.Register
- AuthService.RefreshToken
- AuthService.VerifyEmail
- AuthService.ResendVerificationEmail
- AuthService.ForgotPassword
- AuthService.ResetPassword
- HealthService.Check
```

**Authenticated calls must include JWT token in metadata:**

```
metadata headers:
  authorization: Bearer <your_jwt_token>
```

## Service Definitions

### AuthService

#### 1. Login

```protobuf
rpc Login(LoginRequest) returns (LoginResponse);

message LoginRequest {
  string email = 1;
  string password = 2;
}

message LoginResponse {
  ApiResponse api_response = 1;
  string token = 2;
  string refresh_token = 3;
  User user = 4;
}
```

**Usage**: Authenticate user with email and password

---

#### 2. Register

```protobuf
rpc Register(RegisterRequest) returns (RegisterResponse);

message RegisterRequest {
  string username = 1;
  string email = 2;
  string password = 3;
  string password_confirmation = 4;
  string phone = 5;
}

message RegisterResponse {
  ApiResponse api_response = 1;
  User user = 2;
}
```

**Usage**: Create new user account (email verification required)

---

#### 3. RefreshToken

```protobuf
rpc RefreshToken(RefreshTokenRequest) returns (RefreshTokenResponse);

message RefreshTokenRequest {
  string refresh_token = 1;
}

message RefreshTokenResponse {
  ApiResponse api_response = 1;
  string token = 2;
  string refresh_token = 3;
}
```

**Usage**: Get new access token using refresh token

---

#### 4. Logout

```protobuf
rpc Logout(LogoutRequest) returns (LogoutResponse);

message LogoutRequest {
  string token = 1;
}

message LogoutResponse {
  ApiResponse api_response = 1;
}
```

**Usage**: Invalidate JWT token (adds to blacklist)

**Requires**: Authentication header

---

#### 5. VerifyEmail

```protobuf
rpc VerifyEmail(VerifyEmailRequest) returns (VerifyEmailResponse);

message VerifyEmailRequest {
  string token = 1;
}

message VerifyEmailResponse {
  ApiResponse api_response = 1;
  User user = 2;
}
```

**Usage**: Verify email with token from registration

---

#### 6. ResendVerificationEmail

```protobuf
rpc ResendVerificationEmail(ResendVerificationRequest) returns (ResendVerificationResponse);

message ResendVerificationRequest {
  string email = 1;
}

message ResendVerificationResponse {
  ApiResponse api_response = 1;
}
```

**Usage**: Resend verification email if initial email was lost

---

#### 7. GetProfile

```protobuf
rpc GetProfile(GetProfileRequest) returns (GetProfileResponse);

message GetProfileRequest {
  string user_id = 1;
}

message GetProfileResponse {
  ApiResponse api_response = 1;
  User user = 2;
}
```

**Usage**: Retrieve user profile

**Requires**: Authentication header

---

#### 8. UpdateProfile

```protobuf
rpc UpdateProfile(UpdateProfileRequest) returns (UpdateProfileResponse);

message UpdateProfileRequest {
  string user_id = 1;
  string phone = 2;
  string image_url = 3;
}

message UpdateProfileResponse {
  ApiResponse api_response = 1;
  User user = 2;
}
```

**Usage**: Update user profile (phone, image only; username is immutable)

**Requires**: Authentication header

---

#### 9. ChangePassword

```protobuf
rpc ChangePassword(ChangePasswordRequest) returns (ChangePasswordResponse);

message ChangePasswordRequest {
  string user_id = 1;
  string old_password = 2;
  string new_password = 3;
  string password_confirmation = 4;
}

message ChangePasswordResponse {
  ApiResponse api_response = 1;
}
```

**Usage**: Change password (requires current password verification)

**Requires**: Authentication header

---

#### 10. ForgotPassword

```protobuf
rpc ForgotPassword(ForgotPasswordRequest) returns (ForgotPasswordResponse);

message ForgotPasswordRequest {
  string email = 1;
}

message ForgotPasswordResponse {
  ApiResponse api_response = 1;
}
```

**Usage**: Request password reset email

---

#### 11. ResetPassword

```protobuf
rpc ResetPassword(ResetPasswordRequest) returns (ResetPasswordResponse);

message ResetPasswordRequest {
  string token = 1;
  string new_password = 2;
  string password_confirmation = 3;
}

message ResetPasswordResponse {
  ApiResponse api_response = 1;
}
```

**Usage**: Reset password using token from forgot password email

---

### AdminService

#### 1. ListUsers

```protobuf
rpc ListUsers(ListUsersRequest) returns (ListUsersResponse);

message ListUsersRequest {
  int32 page_number = 1;
  int32 page_size = 2;
  string search = 3;
  string sort_by = 4;
  string sort_direction = 5;
}

message ListUsersResponse {
  ApiResponse api_response = 1;
  repeated User users = 2;
  int32 total_count = 3;
  int32 page_number = 4;
  int32 page_size = 5;
  int32 total_pages = 6;
}
```

**Usage**: List all users with pagination

**Requires**: Authentication + MANAGE_ACCOUNTS or VIEW_ALL_ACCOUNTS permission

---

#### 2. GetUsersByRole

```protobuf
rpc GetUsersByRole(GetUsersByRoleRequest) returns (ListUsersResponse);

message GetUsersByRoleRequest {
  string role_name = 1;
  int32 page_number = 2;
  int32 page_size = 3;
  string search = 4;
  string sort_by = 5;
  string sort_direction = 6;
}
```

**Usage**: Filter users by role

**Requires**: Authentication + MANAGE_ACCOUNTS or VIEW_ALL_ACCOUNTS permission

---

#### 3. GetAvailableRoles

```protobuf
rpc GetAvailableRoles(GetAvailableRolesRequest) returns (GetAvailableRolesResponse);

message GetAvailableRolesResponse {
  ApiResponse api_response = 1;
  repeated string roles = 2;
}
```

**Usage**: List all available roles (ADMIN, STAFF, USER)

**Requires**: Authentication

---

#### 4. CreateUser

```protobuf
rpc CreateUser(CreateUserRequest) returns (CreateUserResponse);

message CreateUserRequest {
  string username = 1;
  string email = 2;
  string password = 3;
  string phone = 4;
  string role = 5;
}

message CreateUserResponse {
  ApiResponse api_response = 1;
  User user = 2;
}
```

**Usage**: Create new user account (admin)

**Requires**: Authentication + MANAGE_ACCOUNTS permission

---

#### 5. UpdateUser

```protobuf
rpc UpdateUser(UpdateUserRequest) returns (UpdateUserResponse);

message UpdateUserRequest {
  string user_id = 1;
  string phone = 2;
  string image_url = 3;
}

message UpdateUserResponse {
  ApiResponse api_response = 1;
  User user = 2;
}
```

**Usage**: Update user profile

**Requires**: Authentication + MANAGE_ACCOUNTS permission

---

#### 6. DeleteUser

```protobuf
rpc DeleteUser(DeleteUserRequest) returns (DeleteUserResponse);

message DeleteUserRequest {
  string user_id = 1;
}

message DeleteUserResponse {
  ApiResponse api_response = 1;
}
```

**Usage**: Delete user (soft delete)

**Requires**: Authentication + MANAGE_ACCOUNTS permission

---

### HealthService

#### 1. Check

```protobuf
rpc Check(HealthCheckRequest) returns (HealthCheckResponse);

message HealthCheckRequest {}

message HealthCheckResponse {
  enum ServingStatus {
    UNKNOWN = 0;
    SERVING = 1;
    NOT_SERVING = 2;
  }
  ServingStatus status = 1;
}
```

**Usage**: Health check for service availability

---

## Common Data Types

### User

```protobuf
message User {
  string id = 1;
  string username = 2;
  string email = 3;
  string phone = 4;
  string image_url = 5;
  string role = 6;
  bool is_active = 7;
  string created_at = 8;
  string updated_at = 9;
}
```

### ApiResponse

```protobuf
message ApiResponse {
  bool success = 1;
  string message = 2;
  int32 status_code = 3;
}
```

## Error Handling

gRPC errors are returned as gRPC Status codes:

| HTTP Status | gRPC Status       | Usage                                      |
| ----------- | ----------------- | ------------------------------------------ |
| 400         | INVALID_ARGUMENT  | Validation errors, missing required fields |
| 401         | UNAUTHENTICATED   | Missing/invalid JWT token, token expired   |
| 403         | PERMISSION_DENIED | Insufficient permissions                   |
| 404         | NOT_FOUND         | Resource not found                         |
| 500         | INTERNAL          | Server error                               |

**Error details are included in the error message parameter.**

## Configuration

### application.yml

```yaml
server:
  port: 0 # REST disabled

grpc:
  server:
    port: 50051 # gRPC server port
    enable-keep-alive: true
    keep-alive-time: 30s
    keep-alive-timeout: 5s

jwt:
  expiration: 3600000 # 1 hour
  private-key: |
    # RSA private key
  public-key: |
    # RSA public key
```

### Environment Variables

```bash
# Database
MYSQL_URL=jdbc:mysql://localhost:3306/account_db?useSSL=false&serverTimezone=UTC
MYSQL_USER=root
MYSQL_PASSWORD=

# SendGrid Email
SENDGRID_API_KEY=your_sendgrid_api_key
SENDGRID_FROM_EMAIL=noreply@yourdomai.com
SENDGRID_FROM_NAME=Hotel Booking

# Frontend
FRONTEND_URL=http://localhost:3000

# gRPC Server Port
GRPC_SERVER_PORT=50051
```

## Client Implementation

### Java Client Example

```java
import com.hotelbooking.account.proto.*;

// Create channel
ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
    .usePlaintext()
    .build();

// Create stub
AuthServiceGrpc.AuthServiceBlockingStub stub = AuthServiceGrpc.newBlockingStub(channel);

// Add JWT to metadata
Metadata headers = new Metadata();
Metadata.Key<String> authHeader = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
headers.put(authHeader, "Bearer " + jwtToken);

// Call with metadata
AuthServiceGrpc.AuthServiceBlockingStub secureStub =
    MetadataUtils.attachHeaders(stub, headers);

GetProfileResponse response = secureStub.getProfile(
    GetProfileRequest.newBuilder()
        .setUserId(userId)
        .build()
);
```

### Node.js Client Example

```javascript
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

// Load proto
const packageDef = protoLoader.loadSync("account.proto");
const proto = grpc.loadPackageDefinition(packageDef).com.hotelbooking.account;

// Create client
const client = new proto.AuthService(
  "localhost:50051",
  grpc.credentials.createInsecure(),
);

// Call with metadata
const metadata = new grpc.Metadata();
metadata.add("authorization", `Bearer ${jwtToken}`);

client.getProfile({ user_id: userId }, metadata, (error, response) => {
  if (error) console.error(error);
  else console.log(response);
});
```

### Python Client Example

```python
import grpc
from com.hotelbooking.account import account_pb2, account_pb2_grpc

# Create channel
channel = grpc.insecure_channel('localhost:50051')
stub = account_pb2_grpc.AuthServiceStub(channel)

# Add JWT metadata
metadata = [('authorization', f'Bearer {jwt_token}')]

# Call
response = stub.GetProfile(
    account_pb2.GetProfileRequest(user_id=user_id),
    metadata=metadata
)
```

## Building & Running

### Build

```bash
# Build with protobuf compilation
mvn clean package

# The protobuf compiler will automatically:
# 1. Compile .proto files
# 2. Generate Java gRPC stubs
# 3. Generate Protocol Buffer message classes
```

### Run

```bash
# Run the gRPC server
mvn spring-boot:run

# Server will start on port 50051
```

### Testing with gRPCurl

```bash
# Login
grpcurl -plaintext -d '{"email":"user@example.com","password":"pass123"}' \
  localhost:50051 com.hotelbooking.account.AuthService/Login

# Get profile (requires token)
grpcurl -plaintext -H "authorization: Bearer <your_token>" \
  -d '{"user_id":"<user_uuid>"}' \
  localhost:50051 com.hotelbooking.account.AuthService/GetProfile

# Health check
grpcurl -plaintext localhost:50051 com.hotelbooking.account.HealthService/Check
```

## Migration Checklist

- [x] Proto file definitions created
- [x] Maven dependencies added (gRPC, Protocol Buffers)
- [x] Protobuf compiler plugin configured
- [x] gRPC service implementations created
- [x] JWT authentication interceptor implemented
- [x] gRPC configuration bean added
- [x] Security config updated for gRPC
- [x] Disabled REST API (server.port = 0)
- [x] Application configured for gRPC (port 50051)
- [ ] Client libraries generated
- [ ] Client applications updated to use gRPC
- [ ] Integration tests created
- [ ] Load testing performed
- [ ] Production deployment

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

```
Error: binding port 50051
Solution: Change grpc.server.port in application.yml
```

#### 2. JWT Token Invalid

```
Error: UNAUTHENTICATED - Authentication failed
Solution: Ensure Authorization header format is "Bearer <token>"
```

#### 3. Proto Compilation Fails

```
Error: Could not execute goal org.xolstice.maven.plugins
Solution: Run mvn clean, check protoc version compatibility
```

#### 4. Service Not Found

```
Error: UNIMPLEMENTED
Solution: Verify service package name matches proto definition
```

## Performance Benefits

- **Lower Latency**: HTTP/2 multiplexing reduces overhead
- **Smaller Payloads**: Protocol Buffers binary encoding vs JSON
- **Better Streaming Support**: Native bidirectional streaming
- **Improved Scalability**: Efficient connection handling
- **Type Safety**: Strongly-typed message definitions

## Security Considerations

1. **JWT Validation**: All requests validated via interceptor
2. **Token Blacklisting**: Logout invalidates tokens immediately
3. **Role-Based Access**: Admin services check permissions
4. **Password Hashing**: BCrypt for all stored passwords
5. **Email Verification**: Required before account activation

## Future Enhancements

1. Add gRPC service reflection for dynamic discovery
2. Implement request validation interceptor
3. Add caching layer (Redis) for JWT validation
4. Implement rate limiting per client
5. Add distributed tracing (Jaeger/Zipkin)
6. Create gRPC gateway for REST client fallback
7. Implement client certificate mTLS authentication

## References

- [gRPC Documentation](https://grpc.io/docs/)
- [Protocol Buffers](https://developers.google.com/protocol-buffers)
- [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter)
- [gRPC Java](https://grpc.io/docs/languages/java/)
