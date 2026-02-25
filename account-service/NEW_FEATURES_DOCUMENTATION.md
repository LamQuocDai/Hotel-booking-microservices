# Hotel Booking Account Service - New Features Implementation

## Overview

This document describes the newly implemented features for the Hotel Booking Account Service, including logout token blacklisting, email confirmation, and reusable email service.

## Implemented Features

### 1. Logout with Token Blacklisting

#### Description

When a user logs out, their access token is added to a blacklist to prevent further use, even if the token hasn't expired yet.

#### Implementation Details

- **Entity**: `TokenBlacklist` - Stores blacklisted JWT tokens with expiration dates
- **Service**: `BlacklistTokenService` - Manages token blacklisting operations
- **Cleanup**: Scheduled task runs every hour to remove expired blacklisted tokens
- **Security Filter**: Updated `JwtAuthenticationFilter` to check blacklist before authentication

#### Endpoint

```http
POST /auth/logout
Authorization: Bearer <jwt-token>
```

#### Response Format

```json
{
  "success": true,
  "message": "Logout successful",
  "data": null,
  "statusCode": 200
}
```

### 2. Email Confirmation Feature

#### Description

Users must verify their email address after registration before their account becomes active.

#### Implementation Details

- **Database**: Added `isActive`, `verificationToken`, and `verificationTokenExpiresAt` to Account entity
- **Registration**: Sets `isActive = false` and generates verification token (24-hour expiry)
- **Security**: Login is blocked until email is verified
- **Tokens**: UUID-based verification tokens with 24-hour expiration

#### Endpoints

##### Email Verification

```http
GET /auth/verify-email?token=<verification-token>
```

##### Resend Verification Email

```http
POST /auth/resend-verification
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### Response Format

```json
{
  "success": true,
  "message": "Email verified successfully. Account is now active.",
  "data": {
    "id": "uuid",
    "username": "user123",
    "email": "user@example.com",
    "isActive": true
  },
  "statusCode": 200
}
```

### 3. Reusable Email Service

#### Description

Common email service that handles verification emails, welcome emails, and password reset emails with consistent templates using **SendGrid API**.

#### Implementation Details

- **Service**: `EmailService` - Centralized email management using SendGrid
- **Templates**: Professional HTML email templates for different purposes
- **Configuration**: SendGrid API Key in `application.yml`
- **Error Handling**: Graceful error handling with logging
- **Delivery**: Uses SendGrid API v3 for reliable email delivery

#### Email Types

1. **Verification Email**: Sent during registration
2. **Welcome Email**: Sent after successful email verification
3. **Password Reset Email**: For future forgot password functionality

#### Configuration (application.yml)

```yaml
# SendGrid Configuration
sendgrid:
  api-key: ${SENDGRID_API_KEY:your-sendgrid-api-key-here}
  from-email: ${SENDGRID_FROM_EMAIL:noreply@hotelbooking.com}
  from-name: ${SENDGRID_FROM_NAME:Hotel Booking}

app:
  frontend:
    url: ${FRONTEND_URL:http://localhost:3000}
```

## Security Updates

### Authentication Flow

1. **Registration**: Account created with `isActive = false`
2. **Email Verification**: User clicks verification link to activate account
3. **Login**: Only active accounts can log in
4. **Token Usage**: JwtAuthenticationFilter checks token blacklist
5. **Logout**: Token added to blacklist

### Token Blacklist Security

- Persistent storage in database
- Automatic cleanup of expired tokens
- Fast lookup with database indexes
- User association for audit trails

## Database Changes

### New Table: `token_blacklist`

```sql
CREATE TABLE token_blacklist (
    id UUID PRIMARY KEY,
    jwt_token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    blacklisted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    user_email VARCHAR(150)
);
```

### Updated Table: `accounts`

```sql
ALTER TABLE accounts
ADD COLUMN is_active BOOLEAN DEFAULT FALSE NOT NULL,
ADD COLUMN verification_token VARCHAR(255),
ADD COLUMN verification_token_expires_at TIMESTAMP WITH TIME ZONE;
```

## Error Handling

### Common Error Scenarios

1. **Login with unverified email**: Returns "Account not activated" message
2. **Invalid verification token**: Returns "Invalid verification token" message
3. **Expired verification token**: Returns "Token has expired" message
4. **Blacklisted JWT**: Returns 401 Unauthorized
5. **Email sending failure**: Logs error but doesn't fail registration

### Response Format

All errors follow the consistent `ApiResponse` format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "statusCode": 400
}
```

## Environment Variables

Required environment variables for production:

```bash
# SendGrid Email Configuration
SENDGRID_API_KEY=SG.your-sendgrid-api-key-here
SENDGRID_FROM_EMAIL=noreply@hotelbooking.com
SENDGRID_FROM_NAME=Hotel Booking

# Frontend URL for email links
FRONTEND_URL=https://your-frontend-domain.com
```

**How to get SendGrid API Key:**

1. Sign up at [sendgrid.com](https://sendgrid.com)
2. Go to Settings → API Keys
3. Create new API Key with Mail Send permission
4. Copy the key and set it as environment variable

See [SENDGRID_SETUP.md](SENDGRID_SETUP.md) for detailed setup instructions.

## Testing

### Manual Testing Steps

1. **Registration Flow**:
   - Register new account
   - Check that `isActive = false`
   - Verify email is sent
   - Click verification link
   - Confirm account is activated

2. **Login Flow**:
   - Try login before verification (should fail)
   - Verify email
   - Try login after verification (should succeed)

3. **Logout Flow**:
   - Login successfully
   - Use JWT for authenticated request (should work)
   - Logout
   - Use same JWT again (should fail with 401)

### Postman Collection

A Postman collection can be created with the following requests:

- `POST /auth/register`
- `GET /auth/verify-email?token=<token>`
- `POST /auth/login`
- `POST /auth/logout`
- `POST /auth/resend-verification`

## Code Quality & Architecture

### Design Patterns Used

- **Repository Pattern**: For data access
- **Service Layer Pattern**: For business logic
- **DTO Pattern**: For data transfer
- **Dependency Injection**: For loose coupling

### SOLID Principles

- **Single Responsibility**: Each service has one clear purpose
- **Open/Closed**: Services are extensible without modification
- **Dependency Inversion**: Services depend on abstractions

### Error Handling Strategy

- Consistent error response format
- Proper HTTP status codes
- Detailed logging for debugging
- User-friendly error messages

## Production Deployment Notes

1. **Database Migration**: Run the provided SQL migration script
2. **Environment Variables**: Set up SendGrid API key and frontend URL configurations
3. **SendGrid Setup**:
   - Create SendGrid account
   - Generate API key with Mail Send permission
   - Verify sender email/domain
   - See [SENDGRID_SETUP.md](SENDGRID_SETUP.md) for details
4. **Email Provider**: Configure SendGrid API key in environment variables
5. **Monitoring**: Monitor email sending success/failure via SendGrid dashboard
6. **Cleanup Job**: The scheduled token cleanup runs automatically

## Future Enhancements

1. **Password Reset**: Extend EmailService for forgot password functionality
2. **Rate Limiting**: Add rate limiting for email sending
3. **Email Templates**: HTML templates with better styling
4. **Admin Panel**: Admin interface to manage blacklisted tokens
5. **Metrics**: Email delivery metrics and monitoring
6. **Redis Support**: Optional Redis backend for token blacklist

## Dependencies Added

```xml
<!-- SendGrid for email delivery -->
<dependency>
    <groupId>com.sendgrid</groupId>
    <artifactId>sendgrid-java</artifactId>
    <version>4.9.3</version>
</dependency>
```

The implementation maintains backward compatibility while adding the new security and verification features as requested.
