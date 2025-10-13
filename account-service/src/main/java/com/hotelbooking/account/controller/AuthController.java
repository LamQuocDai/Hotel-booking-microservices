package com.hotelbooking.account.controller;

import com.hotelbooking.account.dto.*;
import com.hotelbooking.account.interfaces.CreateAccountForm;
import com.hotelbooking.account.response.ApiResponse;
import com.hotelbooking.account.service.AuthService;
import com.hotelbooking.account.service.JwtService;
import com.hotelbooking.account.service.RefreshTokenService;
import com.hotelbooking.account.validation.Validation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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

    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Validated(CreateAccountForm.Register.class) @ModelAttribute CreateAccountDTO accountDTO, BindingResult bindingResult) {
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

//    @PostMapping("/refresh")
//    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
//        String refreshToken = request.get("refreshToken");
//        if (refreshToken == null) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
//        }
//
//        String userId = refreshTokenService.validateRefreshToken(refreshToken);
//        if (userId == null) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Invalid refresh token"));
//        }
//
//        Optional<Account> userOpt = accountRepository.findById(java.util.UUID.fromString(userId));
//        if (userOpt.isEmpty()) {
//            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
//        }
//
//        Account user = userOpt.get();
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("username", user.getUsername());
//        claims.put("email", user.getEmail());
//        claims.put("role", user.getRoles().stream().findFirst().map(r -> r.getName()).orElse("user"));
//
//        String token = jwtService.generateToken(String.valueOf(user.getId()), claims);
//        String newRefreshToken = refreshTokenService.issueRefreshToken(String.valueOf(user.getId()));
//
//        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
//            user.getId(),
//            user.getUsername(),
//            user.getEmail(),
//            user.getPhone(),
//            user.getRoles().stream().findFirst().map(r -> r.getName()).orElse("user"),
//            user.getCreatedAt().toString()
//        );
//
//        AuthResponse response = new AuthResponse(token, newRefreshToken, userDto);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "Account service is running"));
    }
}
