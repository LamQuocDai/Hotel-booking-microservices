package com.hotelbooking.account.controller;

import com.hotelbooking.account.dto.*;
import com.hotelbooking.account.response.ApiResponse;
import com.hotelbooking.account.security.CustomUserPrincipal;import com.hotelbooking.account.security.CustomUserPrincipal;import com.hotelbooking.account.service.AuthService;
import com.hotelbooking.account.service.BlacklistTokenService;
import com.hotelbooking.account.service.EmailService;
import com.hotelbooking.account.service.JwtService;
import com.hotelbooking.account.service.RefreshTokenService;
import com.hotelbooking.account.validation.Validation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistTokenService blacklistTokenService;
    private final EmailService emailService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService,
                          BlacklistTokenService blacklistTokenService,
                          EmailService emailService) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.blacklistTokenService = blacklistTokenService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }
            AuthResponse authResponse = authService.login(request.getEmail(), request.getPassword());

            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", authResponse, 200));
        }
        catch (Exception e) {
            logger.error("Error during login", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null, 400));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterAccountDTO accountDTO, BindingResult bindingResult) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            AccountDTO account = authService.registerAccount(accountDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Account registered successfully", account, 200));
        }
        catch(Exception e) {
            logger.error("Error while registering account", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null, 400));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request, BindingResult bindingResult) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
            }

            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok().body(ApiResponse.success(response, "Token refreshed successfully"));
        }
        catch (Exception e) {
            logger.error("Error during token refresh", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, Authentication authentication) {
        try {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Authorization header missing or invalid", null, 400)
                );
            }

            final String jwt = authHeader.substring(7);
            String userEmail = null;
            
            // Get user email from authentication if available
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal userPrincipal) {
                userEmail = userPrincipal.getEmail();
            }

            // Blacklist the token
            blacklistTokenService.blacklistToken(jwt, userEmail);

            // Optionally revoke refresh token if available
            // This would require additional implementation in RefreshTokenService

            logger.info("User logged out successfully: {}", userEmail);
            return ResponseEntity.ok(new ApiResponse<>(true, "Logout successful", null, 200));
        } catch (Exception e) {
            logger.error("Error during logout", e);
            return ResponseEntity.internalServerError().body(
                new ApiResponse<>(false, "Logout failed: " + e.getMessage(), null, 500)
            );
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Verification token is required", null, 400)
                );
            }

            AccountDTO account = authService.verifyEmail(token);
            
            // Send welcome email
            emailService.sendWelcomeEmail(account.getEmail(), account.getUsername());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Email verified successfully. Account is now active.", account, 200)
            );
        } catch (RuntimeException e) {
            logger.error("Email verification failed", e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, e.getMessage(), null, 400)
            );
        } catch (Exception e) {
            logger.error("Unexpected error during email verification", e);
            return ResponseEntity.internalServerError().body(
                new ApiResponse<>(false, "Email verification failed: " + e.getMessage(), null, 500)
            );
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Email is required", null, 400)
                );
            }

            authService.resendVerificationEmail(email);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Verification email sent successfully", null, 200)
            );
        } catch (RuntimeException e) {
            logger.error("Failed to resend verification email", e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, e.getMessage(), null, 400)
            );
        } catch (Exception e) {
            logger.error("Unexpected error while resending verification email", e);
            return ResponseEntity.internalServerError().body(
                new ApiResponse<>(false, "Failed to send verification email: " + e.getMessage(), null, 500)
            );
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal userPrincipal)) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Authentication required", null, 400)
                );
            }

            AccountDTO profile = authService.getCurrentUserProfile(userPrincipal.getUserId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile retrieved successfully", profile, 200)
            );
        } catch (RuntimeException e) {
            logger.error("Failed to get user profile", e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, e.getMessage(), null, 400)
            );
        } catch (Exception e) {
            logger.error("Unexpected error while getting user profile", e);
            return ResponseEntity.internalServerError().body(
                new ApiResponse<>(false, "Failed to retrieve profile: " + e.getMessage(), null, 500)
            );
        }
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO, 
            BindingResult bindingResult,
            Authentication authentication) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal userPrincipal)) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Authentication required", null, 400)
                );
            }

            AccountDTO updatedProfile = authService.updateCurrentUserProfile(userPrincipal.getUserId(), updateProfileDTO);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile updated successfully", updatedProfile, 200)
            );
        } catch (RuntimeException e) {
            logger.error("Failed to update user profile", e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, e.getMessage(), null, 400)
            );
        } catch (Exception e) {
            logger.error("Unexpected error while updating user profile", e);
            return ResponseEntity.internalServerError().body(
                new ApiResponse<>(false, "Failed to update profile: " + e.getMessage(), null, 500)
            );
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            BindingResult bindingResult,
            Authentication authentication) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal userPrincipal)) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Authentication required", null, 400)
                );
            }

            authService.changePassword(userPrincipal.getUserId(), request);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Password changed successfully", null, 200)
            );
        } catch (RuntimeException e) {
            logger.error("Failed to change password", e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, e.getMessage(), null, 400)
            );
        } catch (Exception e) {
            logger.error("Unexpected error while changing password", e);
            return ResponseEntity.internalServerError().body(
                new ApiResponse<>(false, "Failed to change password: " + e.getMessage(), null, 500)
            );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, BindingResult bindingResult) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            authService.forgotPassword(request);
            
            // Always return success for security (don't reveal if email exists)
            return ResponseEntity.ok(
                new ApiResponse<>(true, "If the email exists in our system, a password reset link has been sent.", null, 200)
            );
        } catch (Exception e) {
            logger.error("Error during forgot password process", e);
            // Still return success for security
            return ResponseEntity.ok(
                new ApiResponse<>(true, "If the email exists in our system, a password reset link has been sent.", null, 200)
            );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request, BindingResult bindingResult) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            authService.resetPassword(request);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Password reset successfully. You can now login with your new password.", null, 200)
            );
        } catch (RuntimeException e) {
            logger.error("Password reset failed", e);
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, e.getMessage(), null, 400)
            );
        } catch (Exception e) {
            logger.error("Unexpected error during password reset", e);
            return ResponseEntity.internalServerError().body(
                new ApiResponse<>(false, "Password reset failed: " + e.getMessage(), null, 500)
            );
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "Account service is running"));
    }
}
