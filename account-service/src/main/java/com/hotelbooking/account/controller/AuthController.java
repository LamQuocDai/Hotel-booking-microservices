package com.hotelbooking.account.controller;

import com.hotelbooking.account.dto.*;
import com.hotelbooking.account.response.ApiResponse;
import com.hotelbooking.account.service.AuthService;
import com.hotelbooking.account.service.JwtService;
import com.hotelbooking.account.service.RefreshTokenService;
import com.hotelbooking.account.validation.Validation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
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

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "Account service is running"));
    }
}
