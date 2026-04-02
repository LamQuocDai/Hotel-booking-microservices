package com.hotelbooking.account.service;

import com.hotelbooking.account.dto.AccountDTO;
import com.hotelbooking.account.dto.AuthResponse;
import com.hotelbooking.account.dto.ChangePasswordRequest;
import com.hotelbooking.account.dto.CreateAccountDTO;
import com.hotelbooking.account.dto.ForgotPasswordRequest;
import com.hotelbooking.account.dto.RegisterAccountDTO;
import com.hotelbooking.account.dto.ResetPasswordRequest;
import com.hotelbooking.account.dto.UpdateProfileDTO;
import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.enums.RoleType;
import com.hotelbooking.account.repository.AccountRepository;
import com.hotelbooking.account.security.CustomUserDetailsService;
import com.hotelbooking.account.security.CustomUserPrincipal;
import com.hotelbooking.account.validation.PhoneValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;

    public AuthService(AccountRepository accountRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtService jwtService, 
                      RefreshTokenService refreshTokenService, 
                      CustomUserDetailsService userDetailsService,
                      EmailService emailService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
    }

    public AuthResponse login(String email, String password) {
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Check if account is active (email verified)
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new RuntimeException("Account not activated. Please verify your email address.");
        }

        // Load user principal properly using injected service
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) userDetailsService.loadUserByUsername(account.getUsername());

        // Generate token with permissions
        String token = jwtService.generateTokenForUser(userPrincipal);
        String refreshToken = refreshTokenService.issueRefreshToken(userPrincipal.getUserId().toString());

        AccountDTO accountDTO = new AccountDTO(account);
        return new AuthResponse(token, refreshToken, accountDTO);
    }

    public AccountDTO registerAccount(RegisterAccountDTO accountDTO) throws Exception {
        if (accountRepository.findByEmailAndDeletedAtIsNull(accountDTO.getEmail()).isPresent()) {
            throw new Exception("Email already in use");
        }

        if (PhoneValidator.isValidVietnamesePhone(accountDTO.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number");
        }

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        OffsetDateTime tokenExpiry = OffsetDateTime.now().plusHours(24); // Token expires in 24 hours

        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());
        account.setPhone(accountDTO.getPhone());
        account.setImageUrl(accountDTO.getImageUrl());
        account.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        account.setRole(RoleType.USER); // Set enum directly
        account.setIsActive(false); // Account is not active until email is verified
        account.setVerificationToken(verificationToken);
        account.setVerificationTokenExpiresAt(tokenExpiry);

        Account savedAccount = accountRepository.save(account);

        // Send verification email
        try {
            emailService.sendVerificationEmail(
                savedAccount.getEmail(),
                savedAccount.getUsername(),
                verificationToken
            );
        } catch (Exception e) {
            // Log the error but don't fail the registration
            // The user can request to resend the email later
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return new AccountDTO(savedAccount);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String userId = refreshTokenService.validateRefreshToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        Account account = accountRepository.findByIdAndDeletedAtIsNull(java.util.UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("Not found user"));

        // Load user principal properly using injected service
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) userDetailsService.loadUserByUsername(account.getUsername());

        // Generate token with permissions
        String token = jwtService.generateTokenForUser(userPrincipal);
        String newRefreshToken = refreshTokenService.issueRefreshToken(userPrincipal.getUserId().toString());

        return new AuthResponse(token, newRefreshToken, new AccountDTO(account));
    }

    /**
     * Verify email using verification token
     */
    public AccountDTO verifyEmail(String verificationToken) {
        Account account = accountRepository.findByVerificationTokenAndDeletedAtIsNull(verificationToken)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        // Check if token is expired
        if (account.getVerificationTokenExpiresAt() == null || 
            account.getVerificationTokenExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Verification token has expired. Please request a new verification email.");
        }

        // Check if already verified
        if (Boolean.TRUE.equals(account.getIsActive())) {
            throw new RuntimeException("Account is already verified");
        }

        // Activate account and clear verification token
        account.setIsActive(true);
        account.setVerificationToken(null);
        account.setVerificationTokenExpiresAt(null);

        Account savedAccount = accountRepository.save(account);
        return new AccountDTO(savedAccount);
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email) {
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Check if already verified
        if (Boolean.TRUE.equals(account.getIsActive())) {
            throw new RuntimeException("Account is already verified");
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        OffsetDateTime tokenExpiry = OffsetDateTime.now().plusHours(24);

        account.setVerificationToken(verificationToken);
        account.setVerificationTokenExpiresAt(tokenExpiry);

        accountRepository.save(account);

        // Send verification email
        emailService.sendVerificationEmail(
            account.getEmail(),
            account.getUsername(),
            verificationToken
        );
    }

    /**
     * Get current user profile by user ID
     */
    public AccountDTO getCurrentUserProfile(UUID userId) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        return new AccountDTO(account);
    }

    /**
     * Update current user profile
     */
    public AccountDTO updateCurrentUserProfile(UUID userId, UpdateProfileDTO updateProfileDTO) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Validate phone number
        if (PhoneValidator.isValidVietnamesePhone(updateProfileDTO.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number");
        }

        // Update allowed fields only (username is immutable)
        account.setPhone(updateProfileDTO.getPhone());
        account.setImageUrl(updateProfileDTO.getImageUrl());

        Account savedAccount = accountRepository.save(account);
        return new AccountDTO(savedAccount);
    }

    /**
     * Change password for current user
     */
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        Account account = accountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    /**
     * Forgot password - generate reset token and send email
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        // Find account by email - don't reveal if email exists or not
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElse(null);

        // Always return success for security (don't reveal if email exists)
        if (account == null) {
            return;
        }

        // Check if account is active
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            // Don't send reset email for inactive accounts, but don't reveal this
            return;
        }

        // Generate password reset token
        String resetToken = UUID.randomUUID().toString();
        OffsetDateTime tokenExpiry = OffsetDateTime.now().plusHours(1); // Token expires in 1 hour

        account.setPasswordResetToken(resetToken);
        account.setPasswordResetTokenExpiresAt(tokenExpiry);

        accountRepository.save(account);

        // Send password reset email
        try {
            emailService.sendPasswordResetEmail(
                account.getEmail(),
                account.getUsername(),
                resetToken
            );
        } catch (Exception e) {
            // Log the error but don't fail the operation
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    /**
     * Reset password using reset token
     */
    public void resetPassword(ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        Account account = accountRepository.findByPasswordResetTokenAndDeletedAtIsNull(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if token is expired
        if (account.getPasswordResetTokenExpiresAt() == null || 
            account.getPasswordResetTokenExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new password reset.");
        }

        // Update password and clear reset token
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setPasswordResetToken(null);
        account.setPasswordResetTokenExpiresAt(null);

        accountRepository.save(account);
    }
}
