package com.hotelbooking.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@hotelbooking.com}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send email verification message
     */
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        try {
            String subject = "Hotel Booking - Verify Your Email Address";
            String verificationUrl = frontendUrl + "/auth/verify-email?token=" + verificationToken;
            
            String body = buildEmailVerificationTemplate(username, verificationUrl);
            
            sendEmail(toEmail, subject, body);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (MailException e) {
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
            
            String body = buildPasswordResetTemplate(username, resetUrl);
            
            sendEmail(toEmail, subject, body);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MailException e) {
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
            String body = buildWelcomeTemplate(username);
            
            sendEmail(toEmail, subject, body);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            // Don't throw exception for welcome email failure
        }
    }

    /**
     * Generic method to send email
     */
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        
        mailSender.send(message);
    }

    /**
     * Build email verification template
     */
    private String buildEmailVerificationTemplate(String username, String verificationUrl) {
        return String.format("""
            Dear %s,
            
            Thank you for registering with Hotel Booking!
            
            To complete your registration and activate your account, please verify your email address by clicking the link below:
            
            %s
            
            This verification link will expire in 24 hours.
            
            If you didn't create an account with Hotel Booking, you can safely ignore this email.
            
            Best regards,
            Hotel Booking Team
            """, username, verificationUrl);
    }

    /**
     * Build password reset template
     */
    private String buildPasswordResetTemplate(String username, String resetUrl) {
        return String.format("""
            Dear %s,
            
            We received a request to reset your password for your Hotel Booking account.
            
            To reset your password, please click the link below:
            
            %s
            
            This password reset link will expire in 1 hour.
            
            If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.
            
            For security reasons, please do not share this link with anyone.
            
            Best regards,
            Hotel Booking Team
            """, username, resetUrl);
    }

    /**
     * Build welcome email template
     */
    private String buildWelcomeTemplate(String username) {
        return String.format("""
            Dear %s,
            
            Welcome to Hotel Booking!
            
            Your email has been successfully verified and your account is now active.
            
            You can now:
            - Search and book hotels
            - Manage your bookings
            - Update your profile
            - And much more!
            
            Start exploring amazing hotels at: %s
            
            If you have any questions or need assistance, please don't hesitate to contact our support team.
            
            Happy booking!
            
            Best regards,
            Hotel Booking Team
            """, username, frontendUrl);
    }
}