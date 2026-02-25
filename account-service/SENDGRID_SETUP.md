# SendGrid Email Integration Guide

## Overview

This project uses **SendGrid API** to send transactional emails (email verification, password reset, welcome emails) using an API Key instead of SMTP.

## SendGrid Setup

### 1. Create SendGrid Account

1. Go to [https://sendgrid.com/](https://sendgrid.com/)
2. Sign up for a free account (100 emails/day free tier)
3. Verify your email address

### 2. Generate API Key

1. Log in to SendGrid Dashboard
2. Navigate to **Settings** → **API Keys**
3. Click **Create API Key**
4. Give it a name (e.g., "Hotel Booking Service")
5. Select **Full Access** or at minimum **Mail Send** permission
6. Click **Create & View**
7. **Copy the API Key immediately** (you won't be able to see it again!)

### 3. Verify Sender Identity

#### Option A: Single Sender Verification (Quickest for Development)

1. Go to **Settings** → **Sender Authentication**
2. Click **Verify a Single Sender**
3. Fill in the form:
   - From Name: `Hotel Booking`
   - From Email Address: `noreply@yourdomain.com`
   - Reply To: Your support email
   - Company details
4. Check your email and click the verification link
5. Wait for approval (usually instant)

#### Option B: Domain Authentication (Recommended for Production)

1. Go to **Settings** → **Sender Authentication**
2. Click **Authenticate Your Domain**
3. Follow DNS setup instructions for your domain
4. Verify DNS records

## Configuration

### Environment Variables

Set these environment variables in your system or `.env` file:

```bash
# SendGrid API Key (Required)
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Sender Email (must match verified sender)
SENDGRID_FROM_EMAIL=noreply@hotelbooking.com

# Sender Name (displayed in email client)
SENDGRID_FROM_NAME=Hotel Booking

# Frontend URL for email links
FRONTEND_URL=http://localhost:3000
```

### Windows PowerShell

```powershell
$env:SENDGRID_API_KEY="SG.your-api-key-here"
$env:SENDGRID_FROM_EMAIL="noreply@hotelbooking.com"
$env:SENDGRID_FROM_NAME="Hotel Booking"
$env:FRONTEND_URL="http://localhost:3000"
```

### Linux/Mac Terminal

```bash
export SENDGRID_API_KEY="SG.your-api-key-here"
export SENDGRID_FROM_EMAIL="noreply@hotelbooking.com"
export SENDGRID_FROM_NAME="Hotel Booking"
export FRONTEND_URL="http://localhost:3000"
```

### application.yml (Default Values)

```yaml
sendgrid:
  api-key: ${SENDGRID_API_KEY:your-sendgrid-api-key-here}
  from-email: ${SENDGRID_FROM_EMAIL:noreply@hotelbooking.com}
  from-name: ${SENDGRID_FROM_NAME:Hotel Booking}

app:
  frontend:
    url: ${FRONTEND_URL:http://localhost:3000}
```

## Implementation Details

### Dependency

```xml
<dependency>
    <groupId>com.sendgrid</groupId>
    <artifactId>sendgrid-java</artifactId>
    <version>4.9.3</version>
</dependency>
```

### EmailService Methods

1. **sendVerificationEmail(toEmail, username, verificationToken)**
   - Sends HTML email with verification link
   - Token expires in 24 hours
   - Professional HTML template with styling

2. **sendPasswordResetEmail(toEmail, username, resetToken)**
   - Sends HTML email with password reset link
   - Token expires in 1 hour
   - Includes security warning

3. **sendWelcomeEmail(toEmail, username)**
   - Sends welcome email after successful verification
   - Lists features and benefits
   - Non-critical (doesn't throw exceptions)

### SendGrid API Integration

```java
Email from = new Email(fromEmail, fromName);
Email to = new Email(toEmail);
Content content = new Content("text/html", htmlContent);
Mail mail = new Mail(from, subject, to, content);

SendGrid sg = new SendGrid(sendGridApiKey);
Request request = new Request();
request.setMethod(Method.POST);
request.setEndpoint("mail/send");
request.setBody(mail.build());

Response response = sg.api(request);
```

## Email Templates

All emails use **professional HTML templates** with:

- Responsive design (mobile-friendly)
- Inline CSS styling
- Brand colors and styling
- Clear call-to-action buttons
- Security notices where appropriate

### Example Email Preview

**Verification Email:**

```
Subject: Hotel Booking - Verify Your Email Address

[Hotel Booking Header - Green]

Hello John,

Thank you for registering with Hotel Booking!

To complete your registration and activate your account,
please verify your email address:

[Verify Email Address Button]

This verification link will expire in 24 hours.

Best regards,
Hotel Booking Team
```

## Testing

### Test Email Sending

```java
// In your test or controller
@Autowired
private EmailService emailService;

public void testEmail() {
    emailService.sendVerificationEmail(
        "test@example.com",
        "John Doe",
        "test-token-123"
    );
}
```

### Check SendGrid Activity

1. Log in to SendGrid Dashboard
2. Go to **Activity** (Activity Feed)
3. View sent emails, delivery status, opens, clicks

### Test Endpoints

```bash
# Register (triggers verification email)
POST http://localhost:3002/auth/register
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "phone": "+84901234567"
}

# Forgot Password (triggers reset email)
POST http://localhost:3002/auth/forgot-password
{
  "email": "test@example.com"
}
```

## Troubleshooting

### Common Issues

#### 1. "Forbidden" or "Unauthorized" Error

**Solution:** Check your API key is correct and has Mail Send permission

#### 2. "The from email does not contain a valid address"

**Solution:** Verify your sender email in SendGrid dashboard

#### 3. Email not received

**Possible causes:**

- Check spam/junk folder
- Verify sender email is authenticated
- Check SendGrid Activity Feed for delivery status
- Ensure API key has correct permissions

#### 4. "Invalid API Key" Error

**Solution:**

- Regenerate API key in SendGrid
- Update environment variable
- Restart application

### Debug Logging

Enable debug logging in `application.yml`:

```yaml
logging:
  level:
    com.sendgrid: DEBUG
    com.hotelbooking.account.service.EmailService: DEBUG
```

## Production Best Practices

1. **Never commit API keys** to version control
2. **Use environment variables** for all sensitive configuration
3. **Monitor email sending** via SendGrid dashboard
4. **Set up alerts** for delivery failures
5. **Implement retry logic** for failed sends
6. **Use templates** in SendGrid for easier maintenance
7. **Track email metrics** (opens, clicks, bounces)
8. **Maintain sender reputation** by avoiding spam complaints

## Rate Limits

### Free Tier

- 100 emails/day forever free
- 2,000 contacts

### Paid Plans

- Essentials: $19.95/month (50k emails)
- Pro: $89.95/month (100k emails)
- Higher volume options available

## Support & Resources

- **SendGrid Documentation:** https://docs.sendgrid.com/
- **SendGrid Java SDK:** https://github.com/sendgrid/sendgrid-java
- **API Reference:** https://docs.sendgrid.com/api-reference/
- **Support:** https://support.sendgrid.com/

## Security Notes

⚠️ **Important Security Practices:**

- Store API keys in environment variables, never in code
- Use different API keys for dev/staging/production
- Rotate API keys periodically
- Use least-privilege permissions (only Mail Send if that's all you need)
- Monitor for unauthorized API usage
- Enable notification for suspicious activity

## Migration from SMTP

If migrating from SMTP to SendGrid API:

**Advantages:**
✅ Better deliverability  
✅ Real-time analytics  
✅ No SMTP port restrictions  
✅ Easier debugging  
✅ Better scalability  
✅ Advanced features (templates, A/B testing, etc.)

**No Code Changes Required:**
The EmailService abstraction remains the same, only the implementation changed from JavaMailSender to SendGrid API.
