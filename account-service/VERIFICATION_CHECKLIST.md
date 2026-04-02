# gRPC Configuration Verification Checklist

Use this checklist to verify that the gRPC migration is complete and working correctly.

## Pre-Build Verification

### 1. Proto File ✅

- [ ] `src/main/proto/account.proto` exists
- [ ] Contains `AuthService` with 11 RPC methods
- [ ] Contains `AdminService` with 6 RPC methods
- [ ] Contains `HealthService` with 1 RPC method
- [ ] All message definitions present
- [ ] Java package: `com.hotelbooking.account`

**Check**:

```bash
grep "service AuthService\|service AdminService\|service HealthService" src/main/proto/account.proto
```

### 2. Maven Configuration ✅

- [ ] `pom.xml` contains gRPC dependencies
- [ ] Check for:
  - `grpcserver-spring-boot-starter`
  - `grpc-netty-shaded`
  - `grpc-protobuf`
  - `grpc-stub`
  - `protobuf-java`
- [ ] Maven plugins defined:
  - `protoc-maven-plugin`
  - `os-maven-plugin`

**Check**:

```bash
grep -c "grpc\|protobuf" pom.xml
# Should show: multiple matches
```

### 3. gRPC Service Implementations ✅

- [ ] `AuthGrpcServiceImpl.java` exists in `src/main/java/.../grpc/`
- [ ] Implements `AuthServiceGrpc.AuthServiceImplBase`
- [ ] Has 11 RPC method overrides
- [ ] `AdminGrpcServiceImpl.java` exists
- [ ] Implements `AdminServiceGrpc.AdminServiceImplBase`
- [ ] Has 6 RPC method overrides
- [ ] `HealthCheckServiceImpl.java` exists
- [ ] Implements `HealthServiceGrpc.HealthServiceImplBase`

**Check**:

```bash
ls -la src/main/java/com/hotelbooking/account/grpc/
# Should show: AuthGrpcServiceImpl.java, AdminGrpcServiceImpl.java, HealthCheckServiceImpl.java
```

### 4. Authentication Interceptor ✅

- [ ] `JwtAuthenticationInterceptor.java` exists
- [ ] Implements `ServerInterceptor`
- [ ] Has `interceptCall` method
- [ ] Defines public endpoints list
- [ ] Validates JWT from metadata

**Check**:

```bash
grep "implements ServerInterceptor" src/main/java/com/hotelbooking/account/grpc/JwtAuthenticationInterceptor.java
```

### 5. gRPC Configuration ✅

- [ ] `GrpcConfig.java` exists
- [ ] Has `@Configuration` annotation
- [ ] Registers `JwtAuthenticationInterceptor`
- [ ] Creates `GrpcServerConfigurer` bean

**Check**:

```bash
grep "@Configuration\|GrpcServerConfigurer" src/main/java/com/hotelbooking/account/config/GrpcConfig.java
```

### 6. Security Configuration ✅

- [ ] `SecurityConfig.java` updated
- [ ] No `SecurityFilterChain` bean (or disabled)
- [ ] No `JwtAuthenticationFilter` reference
- [ ] Keeps `passwordEncoder` bean
- [ ] Keeps `authenticationProvider` bean
- [ ] Has `@Configuration` annotation

**Check**:

```bash
grep -c "SecurityFilterChain\|JwtAuthenticationFilter" src/main/java/com/hotelbooking/account/config/SecurityConfig.java
# Should return: 0
```

### 7. Application Configuration ✅

- [ ] `application.yml` has `server.port: 0` (REST disabled)
- [ ] Has `grpc.server.port: 50051`
- [ ] Database configuration unchanged
- [ ] JWT configuration unchanged
- [ ] SendGrid configuration unchanged

**Check**:

```bash
grep "server:\|port:\|grpc:" src/main/resources/application.yml
```

---

## Build Verification

### 8. Maven Build ✅

- [ ] Run: `mvn clean package`
- [ ] Build succeeds without errors
- [ ] No compilation errors
- [ ] Proto files compile successfully
- [ ] Generated sources created

**Expected Output**:

```
BUILD SUCCESS

Generated Files:
- target/generated-sources/protobuf/java/
- target/generated-sources/protobuf/grpc-java/
```

### 9. Generated Classes ✅

After build, verify generated files:

**Check**:

```bash
find target/generated-sources -name "*account_pb2*" -o -name "*ServiceGrpc*"
```

**Should exist**:

- `*ServiceGrpc.java` (service stubs)
- `*ServiceGrpc$*ServiceBlockingStub.java`
- `*ServiceGrpc$*ServiceFutureStub.java`
- `*ServiceGrpc$*ServiceStub.java`
- All message classes (e.g., `LoginRequest.java`, `User.java`, etc.)

---

## Runtime Verification

### 10. Server Startup ✅

- [ ] Run: `mvn spring-boot:run` or `java -jar target/account-service-0.0.1-SNAPSHOT.jar`
- [ ] Server starts without errors
- [ ] No port conflicts
- [ ] Logs show gRPC server initialized

**Expected Logs**:

```
Tomcat initialized with port(s): 0 (http)
Server started on 0 port(s): 0 (http)
Server started on port(s): 50051 (grpc)
```

### 11. Health Check ✅

- [ ] Install gRPCurl: https://github.com/fullstorydev/grpcurl
- [ ] Run health check:
  ```bash
  grpcurl -plaintext localhost:50051 com.hotelbooking.account.HealthService/Check
  ```
- [ ] Response shows: `"status":1` or `"status":"SERVING"`

**Expected Response**:

```json
{
  "status": "SERVING"
}
```

### 12. Authentication Test ✅

- [ ] Run login:
  ```bash
  grpcurl -plaintext -d '{"email":"admin@example.com","password":"password"}' \
    localhost:50051 com.hotelbooking.account.AuthService/Login
  ```
- [ ] Response includes: `token`, `refreshToken`, `user` object
- [ ] User has: `id`, `username`, `email`, `role`, `isActive`

**Expected Response Structure**:

```json
{
  "apiResponse": {
    "success": true,
    "message": "Login successful",
    "statusCode": 200
  },
  "token": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "...",
  "user": {
    "id": "...",
    "username": "...",
    "email": "...",
    "role": "ADMIN",
    "isActive": true
  }
}
```

### 13. Authenticated Call Test ✅

- [ ] Extract token from login response
- [ ] Call GetProfile with token:
  ```bash
  grpcurl -plaintext -H "authorization: Bearer <TOKEN>" \
    -d '{"user_id":"<USER_ID>"}' \
    localhost:50051 com.hotelbooking.account.AuthService/GetProfile
  ```
- [ ] Response returns user profile successfully

### 14. Error Handling ✅

- [ ] Call protected endpoint without auth:
  ```bash
  grpcurl -plaintext -d '{"user_id":"dummy"}' \
    localhost:50051 com.hotelbooking.account.AuthService/GetProfile
  ```
- [ ] Returns gRPC error: `UNAUTHENTICATED`
- [ ] Error message: "Authorization header missing or invalid"

### 15. Admin Service Test ✅

- [ ] With valid token, call ListUsers:
  ```bash
  grpcurl -plaintext -H "authorization: Bearer <TOKEN>" \
    -d '{"page_number":1, "page_size":10}' \
    localhost:50051 com.hotelbooking.account.AdminService/ListUsers
  ```
- [ ] Response includes: `users[]`, `totalCount`, `pageNumber`, `totalPages`

---

## Documentation Verification

### 16. Migration Documentation ✅

- [ ] `GRPC_MIGRATION_GUIDE.md` exists (400+ lines)
- [ ] Contains all 18 RPC method descriptions
- [ ] Has client examples (Java, Node.js, Python)
- [ ] Includes troubleshooting section
- [ ] Has error codes documentation

### 17. Quick Start Guide ✅

- [ ] `GRPC_QUICKSTART.md` exists
- [ ] Shows basic usage examples
- [ ] Includes configuration section
- [ ] Has gRPCurl examples

### 18. Setup Documentation ✅

- [ ] `README_GRPC_SETUP.md` exists
- [ ] Summarizes all changes
- [ ] Shows architecture diagrams
- [ ] Lists all new/updated files

### 19. Client Library Guide ✅

- [ ] `CLIENT_LIBRARY_GENERATION.md` exists
- [ ] Covers multiple languages
- [ ] Shows generation steps
- [ ] Includes code examples
- [ ] Has publishing guides

### 20. Migration Summary ✅

- [ ] `GRPC_MIGRATION_SUMMARY.md` exists
- [ ] Lists all file changes
- [ ] Shows before/after comparison
- [ ] Includes rollback plan

---

## Database Verification

### 21. Database Operations ✅

- [ ] Database connection works (check logs)
- [ ] Tables created/migrated successfully
- [ ] Can login with existing users
- [ ] Can create new users via gRPC
- [ ] Can update profiles via gRPC

**Check Logs for**:

```
Hibernate: creating table account
HikariPool-1 - Starting
HikariPool-1 - Pool is ready to accept connections
```

---

## Security Verification

### 22. JWT Validation ✅

- [ ] Expired token rejected
- [ ] Invalid token rejected
- [ ] Blacklisted token rejected (after logout)
- [ ] Valid token accepted
- [ ] User info extracted from token

### 23. Permission Control ✅

- [ ] Admin endpoints require permissions
- [ ] Normal user can't call admin endpoints
- [ ] Proper errors for permission denied

---

## Performance Verification

### 24. Response Times ✅

- [ ] gRPC calls complete in <100ms (local)
- [ ] No timeout errors
- [ ] Multiple concurrent requests work
- [ ] Memory usage stable

---

## Integration Verification

### 25. Service Integration ✅

- [ ] gRPC interceptor loads
- [ ] Security context set properly
- [ ] Business services called correctly
- [ ] Database operations work
- [ ] Email service works (if tested)

---

## Final Checklist

### All Checks Passed? ✅

- [ ] Sections 1-7: Pre-Build (mandatory)
- [ ] Section 8-9: Build (mandatory)
- [ ] Sections 10-15: Runtime (mandatory)
- [ ] Sections 16-20: Documentation (recommended)
- [ ] Sections 21-25: Integration (recommended)

---

## Troubleshooting Guide

### Issue: Build fails with "protoc not found"

**Solution**: Install protoc compiler

```bash
# macOS
brew install protobuf

# Ubuntu
sudo apt-get install protobuf-compiler

# Windows
choco install protoc
```

### Issue: Port 50051 already in use

**Solution**: Change port in `application.yml`

```yaml
grpc:
  server:
    port: 50052 # Change to different port
```

### Issue: NoClassDefFoundError for gRPC classes

**Solution**: Run Maven clean install

```bash
mvn clean install
```

### Issue: Authentication header format incorrect

**Solution**: Use correct format with space after "Bearer"

```bash
# Correct
-H "authorization: Bearer eyJhbGciOiJSUzI1NiJ9..."

# Incorrect (missing space)
-H "authorization:Bearer eyJhbGciOiJSUzI1NiJ9..."
```

### Issue: gRPCurl: "Unknown method"

**Solution**: Ensure service names match exactly

```bash
# Correct full path
com.hotelbooking.account.AuthService/Login

# NOT (missing package)
AuthService/Login
```

---

## Success Criteria

✅ **All checks passed** = Ready for:

- Integration with other microservices
- Client library generation
- Production deployment
- Performance testing

---

## Notes

- Keep this checklist for future deployments
- Run before each production release
- Update checklist if requirements change
- Share with team members

**Version**: 1.0  
**Last Updated**: 2026-04-01  
**Status**: Ready for verification
