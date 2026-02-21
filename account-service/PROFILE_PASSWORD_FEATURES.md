# Hotel Booking Account Service - Profile & Password Management Features

## Overview

This document describes the newly implemented profile and password management endpoints for authenticated users, following the same architecture patterns as the existing codebase.

## New Endpoints

### 1. Get Current User Profile

#### Description

Retrieve the profile information of the currently authenticated user.

#### Endpoint

```http
GET /auth/profile
Authorization: Bearer <jwt-token>
```

#### Response Format

```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "phone": "+84901234567",
    "imageUrl": "https://example.com/avatar.jpg",
    "role": "USER",
    "isActive": true,
    "createdAt": "2026-02-21T10:30:00Z"
  },
  "statusCode": 200
}
```

#### Security

- Requires valid JWT authentication
- Users can only access their own profile
- Profile information extracted from security context

### 2. Update Current User Profile

#### Description

Update the profile information of the currently authenticated user. Only allows updating non-sensitive fields.

#### Endpoint

```http
PUT /auth/profile
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

#### Request Body

```json
{
  "username": "new_username",
  "phone": "+84901234567",
  "imageUrl": "https://example.com/new-avatar.jpg"
}
```

#### Response Format

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "new_username",
    "email": "john@example.com",
    "phone": "+84901234567",
    "imageUrl": "https://example.com/new-avatar.jpg",
    "role": "USER",
    "isActive": true,
    "createdAt": "2026-02-21T10:30:00Z"
  },
  "statusCode": 200
}
```

#### Validation Rules

- `username`: Required, 3-50 characters
- `phone`: Required, valid Vietnamese phone number format
- `imageUrl`: Optional, valid URL format

#### Security

- Cannot update email, role, password, or verification status
- Phone number format validation applied
- Only authenticated users can update their own profile

### 3. Change Password (Authenticated User)

#### Description

Change the password for the currently authenticated user. Requires current password verification.

#### Endpoint

```http
POST /auth/change-password
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

#### Request Body

```json
{
  "currentPassword": "old_password123",
  "newPassword": "new_password456",
  "confirmPassword": "new_password456"
}
```

#### Response Format

```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null,
  "statusCode": 200
}
```

#### Validation Rules

- `currentPassword`: Required
- `newPassword`: Required, minimum 6 characters
- `confirmPassword`: Required, must match newPassword

#### Security Features

- Verifies current password before allowing change
- New password is bcrypt hashed
- Requires authentication
- Validates password confirmation

### 4. Forgot Password

#### Description

Request a password reset email. For security, always returns success regardless of email existence.

#### Endpoint

```http
POST /auth/forgot-password
Content-Type: application/json
```

#### Request Body

```json
{
  "email": "user@example.com"
}
```

#### Response Format

```json
{
  "success": true,
  "message": "If the email exists in our system, a password reset link has been sent.",
  "data": null,
  "statusCode": 200
}
```

#### Security Features

- No authentication required
- Always returns success (doesn't reveal if email exists)
- Only sends email to verified active accounts
- Reset token expires in 1 hour
- Uses reusable EmailService

#### Email Template

The system sends a password reset email with:

- Personalized greeting with username
- Secure reset link with token
- Clear expiration time (1 hour)
- Security advisory about not sharing the link

### 5. Reset Password (via Reset Token)

#### Description

Reset password using the token received via email.

#### Endpoint

```http
POST /auth/reset-password
Content-Type: application/json
```

#### Request Body

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "new_secure_password123",
  "confirmPassword": "new_secure_password123"
}
```

#### Response Format

```json
{
  "success": true,
  "message": "Password reset successfully. You can now login with your new password.",
  "data": null,
  "statusCode": 200
}
```

#### Validation Rules

- `token`: Required, valid UUID format
- `newPassword`: Required, minimum 6 characters
- `confirmPassword`: Required, must match newPassword

#### Security Features

- Token validation (existence, expiration)
- Token is single-use (invalidated after successful reset)
- Password confirmation required
- New password is bcrypt hashed
- Token expires after 1 hour

## Database Changes

### Updated Account Entity

```sql
ALTER TABLE accounts
ADD COLUMN password_reset_token VARCHAR(255),
ADD COLUMN password_reset_token_expires_at TIMESTAMP WITH TIME ZONE;
```

### New Indexes

```sql
CREATE INDEX idx_accounts_password_reset_token ON accounts(password_reset_token)
WHERE password_reset_token IS NOT NULL AND deleted_at IS NULL;
```

### Constraints

```sql
ALTER TABLE accounts
ADD CONSTRAINT chk_password_reset_token_expiry
CHECK (
    (password_reset_token IS NULL AND password_reset_token_expires_at IS NULL) OR
    (password_reset_token IS NOT NULL AND password_reset_token_expires_at IS NOT NULL)
);
```

## Error Handling

### Common Error Scenarios

#### Authentication Errors

```json
{
  "success": false,
  "message": "Authentication required",
  "data": null,
  "statusCode": 400
}
```

#### Validation Errors

```json
{
  "success": false,
  "message": "Username must be between 3 and 50 characters",
  "data": null,
  "statusCode": 400
}
```

#### Password Change Errors

```json
{
  "success": false,
  "message": "Current password is incorrect",
  "data": null,
  "statusCode": 400
}
```

#### Password Reset Errors

```json
{
  "success": false,
  "message": "Invalid or expired reset token",
  "data": null,
  "statusCode": 400
}
```

#### Profile Update Errors

```json
{
  "success": false,
  "message": "Invalid phone number",
  "data": null,
  "statusCode": 400
}
```

## DTOs (Data Transfer Objects)

### UpdateProfileDTO

```java
public class UpdateProfileDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String imageUrl;
}
```

### ChangePasswordRequest

```java
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
```

### ForgotPasswordRequest

```java
public class ForgotPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
```

### ResetPasswordRequest

```java
public class ResetPasswordRequest {
    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
```

## Architecture Consistency

### Followed Patterns

✅ **Controller → Service → Repository** layering  
✅ **ApiResponse<T>** wrapper for all responses  
✅ **@Valid and BindingResult** for validation  
✅ **Validation.validateBody()** for error handling  
✅ **Logger.error()** for exception logging  
✅ **Try-catch blocks** with proper error responses  
✅ **CustomUserPrincipal** for authentication context  
✅ **Same naming conventions** as existing code  
✅ **Same validation patterns** with Jakarta validation

### Security Integration

- JWT authentication via existing filter
- Spring Security Authentication object
- CustomUserPrincipal for user context
- Blacklist token checking (existing feature)
- Password encoding with existing BCryptPasswordEncoder

## Testing with Postman

### Test Flow

1. **Register** → Get verification email → **Verify email**
2. **Login** → Get JWT token
3. **Get Profile** → Use JWT token
4. **Update Profile** → Modify username/phone/avatar
5. **Change Password** → Verify old password required
6. **Logout** → Token blacklisted
7. **Forgot Password** → Email sent (if account exists)
8. **Reset Password** → Use token from email

### Sample Postman Collection

```json
{
  "info": {
    "name": "Hotel Booking - Profile & Password Management"
  },
  "item": [
    {
      "name": "Get Profile",
      "request": {
        "method": "GET",
        "header": [{ "key": "Authorization", "value": "Bearer {{jwt_token}}" }],
        "url": "{{base_url}}/auth/profile"
      }
    },
    {
      "name": "Update Profile",
      "request": {
        "method": "PUT",
        "header": [
          { "key": "Authorization", "value": "Bearer {{jwt_token}}" },
          { "key": "Content-Type", "value": "application/json" }
        ],
        "body": {
          "raw": "{\n  \"username\": \"new_username\",\n  \"phone\": \"+84901234567\",\n  \"imageUrl\": \"https://example.com/avatar.jpg\"\n}"
        },
        "url": "{{base_url}}/auth/profile"
      }
    }
  ]
}
```

## Production Considerations

### Email Configuration

Ensure proper SMTP configuration in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    from: ${MAIL_FROM}

app:
  frontend:
    url: ${FRONTEND_URL}
```

### Security Best Practices Implemented

1. **Password reset tokens expire in 1 hour**
2. **Always return success for forgot password (don't reveal if email exists)**
3. **Validate current password before allowing changes**
4. **Single-use reset tokens**
5. **Only allow updating non-sensitive profile fields**
6. **Proper validation and sanitization**
7. **Consistent error handling**

### Monitoring & Logging

- All operations are logged with appropriate levels
- Email sending failures are logged but don't break flows
- Authentication and authorization failures are properly logged
- Error responses don't reveal sensitive information

## Future Enhancements

1. **Rate Limiting**: Add rate limiting for password reset requests
2. **Password History**: Prevent reusing recent passwords
3. **Two-Factor Authentication**: Add 2FA support
4. **Email Templates**: Enhanced HTML email templates
5. **Profile Picture Upload**: Direct file upload capabilities
6. **Account Lockout**: Lock accounts after multiple failed attempts

The implementation maintains full backward compatibility while extending the API with comprehensive profile and password management features following the same high-quality patterns established in the existing codebase.
