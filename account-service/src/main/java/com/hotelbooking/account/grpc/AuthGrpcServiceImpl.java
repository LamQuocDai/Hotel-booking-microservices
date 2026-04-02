package com.hotelbooking.account.grpc;

import com.hotelbooking.account.dto.*;
import com.hotelbooking.account.grpc.*;
import com.hotelbooking.account.service.AuthService;
import com.hotelbooking.account.service.BlacklistTokenService;
import com.hotelbooking.account.service.EmailService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@GrpcService
public class AuthGrpcServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AuthGrpcServiceImpl.class);

    private final AuthService authService;
    private final BlacklistTokenService blacklistTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthGrpcServiceImpl(AuthService authService,
                              BlacklistTokenService blacklistTokenService,
                              EmailService emailService,
                              PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.blacklistTokenService = blacklistTokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            if (request.getEmail().isEmpty() || request.getPassword().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Email and password are required")
                    .asException());
                return;
            }

            AuthResponse authResponse = authService.login(request.getEmail(), request.getPassword());
            
            LoginResponse response = LoginResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Login successful")
                    .setStatusCode(200)
                    .build())
                .setToken(authResponse.getToken())
                .setRefreshToken(authResponse.getRefreshToken())
                .setUser(mapAccountDTOToUser(authResponse.getUser()))
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error during login", e);
            responseObserver.onError(Status.UNAUTHENTICATED
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        try {
            if (request.getUsername().isEmpty() || request.getEmail().isEmpty() || 
                request.getPassword().isEmpty() || request.getPhone().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("All fields are required")
                    .asException());
                return;
            }

            if (!request.getPassword().equals(request.getPasswordConfirmation())) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Passwords do not match")
                    .asException());
                return;
            }

            RegisterAccountDTO dto = new RegisterAccountDTO();
            dto.setUsername(request.getUsername());
            dto.setEmail(request.getEmail());
            dto.setPassword(request.getPassword());
            dto.setPasswordConfirmation(request.getPasswordConfirmation());
            dto.setPhone(request.getPhone());

            AccountDTO account = authService.registerAccount(dto);

            RegisterResponse response = RegisterResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Account registered successfully")
                    .setStatusCode(200)
                    .build())
                .setUser(mapAccountDTOToUser(account))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error during registration", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<RefreshTokenResponse> responseObserver) {
        try {
            if (request.getRefreshToken().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Refresh token is required")
                    .asException());
                return;
            }

            AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());

            RefreshTokenResponse response = RefreshTokenResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Token refreshed successfully")
                    .setStatusCode(200)
                    .build())
                .setToken(authResponse.getToken())
                .setRefreshToken(authResponse.getRefreshToken())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error during token refresh", e);
            responseObserver.onError(Status.UNAUTHENTICATED
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        try {
            if (request.getToken().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Token is required")
                    .asException());
                return;
            }

            blacklistTokenService.blacklistToken(request.getToken(), null);

            LogoutResponse response = LogoutResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Logout successful")
                    .setStatusCode(200)
                    .build())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error during logout", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request, StreamObserver<VerifyEmailResponse> responseObserver) {
        try {
            if (request.getToken().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Verification token is required")
                    .asException());
                return;
            }

            AccountDTO account = authService.verifyEmail(request.getToken());
            emailService.sendWelcomeEmail(account.getEmail(), account.getUsername());

            VerifyEmailResponse response = VerifyEmailResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Email verified successfully. Account is now active.")
                    .setStatusCode(200)
                    .build())
                .setUser(mapAccountDTOToUser(account))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error during email verification", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void resendVerificationEmail(ResendVerificationRequest request, StreamObserver<ResendVerificationResponse> responseObserver) {
        try {
            if (request.getEmail().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Email is required")
                    .asException());
                return;
            }

            authService.resendVerificationEmail(request.getEmail());

            ResendVerificationResponse response = ResendVerificationResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Verification email sent successfully")
                    .setStatusCode(200)
                    .build())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error resending verification email", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void getProfile(GetProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
        try {
            if (request.getUserId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("User ID is required")
                    .asException());
                return;
            }

            AccountDTO profile = authService.getCurrentUserProfile(UUID.fromString(request.getUserId()));

            GetProfileResponse response = GetProfileResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Profile retrieved successfully")
                    .setStatusCode(200)
                    .build())
                .setUser(mapAccountDTOToUser(profile))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error retrieving profile", e);
            responseObserver.onError(Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void updateProfile(UpdateProfileRequest request, StreamObserver<UpdateProfileResponse> responseObserver) {
        try {
            if (request.getUserId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("User ID is required")
                    .asException());
                return;
            }

            UpdateProfileDTO dto = new UpdateProfileDTO();
            dto.setPhone(request.getPhone());
            dto.setImageUrl(request.getImageUrl());

            AccountDTO updatedProfile = authService.updateCurrentUserProfile(UUID.fromString(request.getUserId()), dto);

            UpdateProfileResponse response = UpdateProfileResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Profile updated successfully")
                    .setStatusCode(200)
                    .build())
                .setUser(mapAccountDTOToUser(updatedProfile))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void changePassword(ChangePasswordRequest request, StreamObserver<ChangePasswordResponse> responseObserver) {
        try {
            if (request.getUserId().isEmpty() || request.getOldPassword().isEmpty() || 
                request.getNewPassword().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("All fields are required")
                    .asException());
                return;
            }

            if (!request.getNewPassword().equals(request.getPasswordConfirmation())) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Passwords do not match")
                    .asException());
                return;
            }

            ChangePasswordRequest.Builder passwordRequestBuilder = ChangePasswordRequest.newBuilder(request);
            com.hotelbooking.account.dto.ChangePasswordRequest dto = new com.hotelbooking.account.dto.ChangePasswordRequest();
            dto.setOldPassword(request.getOldPassword());
            dto.setNewPassword(request.getNewPassword());
            dto.setPasswordConfirmation(request.getPasswordConfirmation());

            authService.changePassword(UUID.fromString(request.getUserId()), dto);

            ChangePasswordResponse response = ChangePasswordResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Password changed successfully")
                    .setStatusCode(200)
                    .build())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error changing password", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request, StreamObserver<ForgotPasswordResponse> responseObserver) {
        try {
            if (request.getEmail().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Email is required")
                    .asException());
                return;
            }

            com.hotelbooking.account.dto.ForgotPasswordRequest dto = new com.hotelbooking.account.dto.ForgotPasswordRequest();
            dto.setEmail(request.getEmail());

            authService.forgotPassword(dto);

            ForgotPasswordResponse response = ForgotPasswordResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("If the email exists in our system, a password reset link has been sent.")
                    .setStatusCode(200)
                    .build())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error processing forgot password", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, StreamObserver<ResetPasswordResponse> responseObserver) {
        try {
            if (request.getToken().isEmpty() || request.getNewPassword().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Token and new password are required")
                    .asException());
                return;
            }

            if (!request.getNewPassword().equals(request.getPasswordConfirmation())) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Passwords do not match")
                    .asException());
                return;
            }

            com.hotelbooking.account.dto.ResetPasswordRequest dto = new com.hotelbooking.account.dto.ResetPasswordRequest();
            dto.setToken(request.getToken());
            dto.setNewPassword(request.getNewPassword());
            dto.setPasswordConfirmation(request.getPasswordConfirmation());

            authService.resetPassword(dto);

            ResetPasswordResponse response = ResetPasswordResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Password reset successfully")
                    .setStatusCode(200)
                    .build())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error resetting password", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    private User mapAccountDTOToUser(AccountDTO account) {
        return User.newBuilder()
            .setId(account.getId().toString())
            .setUsername(account.getUsername())
            .setEmail(account.getEmail())
            .setPhone(account.getPhone() != null ? account.getPhone() : "")
            .setImageUrl(account.getImageUrl() != null ? account.getImageUrl() : "")
            .setRole(account.getRole() != null ? account.getRole() : "USER")
            .setIsActive(account.isActive())
            .setCreatedAt(account.getCreatedAt() != null ? account.getCreatedAt().toString() : "")
            .setUpdatedAt(account.getUpdatedAt() != null ? account.getUpdatedAt().toString() : "")
            .build();
    }
}
