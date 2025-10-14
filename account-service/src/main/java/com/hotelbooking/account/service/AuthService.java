package com.hotelbooking.account.service;

import com.hotelbooking.account.dto.AccountDTO;
import com.hotelbooking.account.dto.AuthResponse;
import com.hotelbooking.account.dto.CreateAccountDTO;
import com.hotelbooking.account.dto.RegisterAccountDTO;
import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.enums.RoleType;
import com.hotelbooking.account.repository.AccountRepository;
import com.hotelbooking.account.security.CustomUserDetailsService;
import com.hotelbooking.account.security.CustomUserPrincipal;
import com.hotelbooking.account.validation.PhoneValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService, CustomUserDetailsService userDetailsService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse login(String email, String password) {
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new RuntimeException("Invalid credentials");
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

        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());
        account.setPhone(accountDTO.getPhone());
        account.setImageUrl(accountDTO.getImageUrl());
        account.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        account.setRole(RoleType.USER); // Set enum directly

        return new AccountDTO(accountRepository.save(account));
    }

    public AuthResponse refreshToken(String refreshToken) {
        String userId = refreshTokenService.validateRefreshToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        Account account = accountRepository.findByUserIdAndDeletedAtIsNull(java.util.UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("Not found user"));

        // Load user principal properly using injected service
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) userDetailsService.loadUserByUsername(account.getUsername());

        // Generate token with permissions
        String token = jwtService.generateTokenForUser(userPrincipal);
        String newRefreshToken = refreshTokenService.issueRefreshToken(userPrincipal.getUserId().toString());

        return new AuthResponse(token, newRefreshToken, new AccountDTO(account));
    }
}
