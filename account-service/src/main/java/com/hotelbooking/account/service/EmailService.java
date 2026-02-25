package com.hotelbooking.account.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:noreply@hotelbooking.com}")
    private String fromEmail;

    @Value("${sendgrid.from-name:Hotel Booking}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send email verification message
     */
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        try {
            String subject = "Hotel Booking - Verify Your Email Address";
            String verificationUrl = frontendUrl + "/auth/verify-email?token=" + verificationToken;
            
            String htmlContent = buildEmailVerificationTemplate(username, verificationUrl);
            
            sendEmail(toEmail, subject, htmlContent);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            String subject = "Hotel Booking - Password Reset Request";
            String resetUrl = frontendUrl + "/auth/reset-password?token=" + resetToken;
            
            String htmlContent = buildPasswordResetTemplate(username, resetUrl);
            
            sendEmail(toEmail, subject, htmlContent);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }

    /**
     * Send welcome email after successful verification
     */
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            String subject = "Welcome to Hotel Booking!";
            String htmlContent = buildWelcomeTemplate(username);
            
            sendEmail(toEmail, subject, htmlContent);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            // Don't throw exception for welcome email failure
        }
    }

    /**
     * Generic method to send email using SendGrid API
     */
    private void sendEmail(String toEmail, String subject, String htmlContent) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.debug("Email sent successfully. Status code: {}", response.getStatusCode());
            } else {
                log.error("SendGrid API returned error. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("SendGrid API error: " + response.getStatusCode());
            }
        } catch (IOException ex) {
            log.error("IOException while sending email via SendGrid", ex);
            throw ex;
        }
    }

    /**
     * Build email verification template
     */
    private String buildEmailVerificationTemplate(String username, String verificationUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 30px; }
                    .button { display: inline-block; padding: 12px 30px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Hotel Booking!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for registering with Hotel Booking!</p>
                        <p>To complete your registration and activate your account, please verify your email address by clicking the button below:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Verify Email Address</a>
                        </p>
                        <p>Or copy and paste this link in your browser:</p>
                        <p style="word-break: break-all; color: #4CAF50;">%s</p>
                        <p><strong>This verification link will expire in 24 hours.</strong></p>
                        <p>If you didn't create an account with Hotel Booking, you can safely ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>Hotel Booking Team</p>
                        <p>&copy; 2026 Hotel Booking. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, verificationUrl, verificationUrl);
    }

    /**
     * Build password reset template
     */
    private String buildPasswordResetTemplate(String username, String resetUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 30px; }
                    .button { display: inline-block; padding: 12px 30px; background-color: #FF9800; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>We received a request to reset your password for your Hotel Booking account.</p>
                        <p>To reset your password, please click the button below:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </p>
                        <p>Or copy and paste this link in your browser:</p>
                        <p style="word-break: break-all; color: #FF9800;">%s</p>
                        <p><strong>This password reset link will expire in 1 hour.</strong></p>
                        <div class="warning">
                            <strong>Security Notice:</strong> If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.
                        </div>
                        <p>For security reasons, please do not share this link with anyone.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>Hotel Booking Team</p>
                        <p>&copy; 2026 Hotel Booking. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, resetUrl, resetUrl);
    }

    /**
     * Build welcome email template
     */
    private String buildWelcomeTemplate(String username) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 30px; }
                    .button { display: inline-block; padding: 12px 30px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    .feature-list { list-style: none; padding: 0; }
                    .feature-list li { padding: 10px 0; padding-left: 30px; position: relative; }
                    .feature-list li:before { content: "✓"; position: absolute; left: 0; color: #2196F3; font-weight: bold; font-size: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Hotel Booking! 🎉</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p><strong>Congratulations!</strong> Your email has been successfully verified and your account is now active.</p>
                        <p>You can now enjoy all the features of Hotel Booking:</p>
                        <ul class="feature-list">
                            <li>Search and book hotels worldwide</li>
                            <li>Manage your bookings online</li>
                            <li>Update your profile and preferences</li>
                            <li>Access exclusive deals and offers</li>
                            <li>24/7 customer support</li>
                        </ul>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Start Exploring Hotels</a>
                        </p>
                        <p>If you have any questions or need assistance, please don't hesitate to contact our support team.</p>
                        <p><strong>Happy booking!</strong></p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>Hotel Booking Team</p>
                        <p>&copy; 2026 Hotel Booking. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, frontendUrl);
    }
}