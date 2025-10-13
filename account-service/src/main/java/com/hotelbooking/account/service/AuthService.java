package com.hotelbooking.account.service;

import com.hotelbooking.account.dto.AccountDTO;
import com.hotelbooking.account.dto.AuthResponse;
import com.hotelbooking.account.dto.CreateAccountDTO;
import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.repository.AccountRepository;
import com.hotelbooking.account.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse login(String email, String password) {
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        AccountDTO accountDTO = new AccountDTO(account);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", accountDTO.getId());
        claims.put("role", accountDTO.getRole());

        String token = jwtService.generateToken(String.valueOf(accountDTO.getId()), claims);
        String refreshToken = refreshTokenService.issueRefreshToken(String.valueOf(accountDTO.getId()));

        return new AuthResponse(token, refreshToken, accountDTO);
    }

    public AccountDTO registerAccount(CreateAccountDTO accountDTO) throws Exception {
        if (accountRepository.findByEmailAndDeletedAtIsNull(accountDTO.getEmail()).isPresent()) {
            throw new Exception("Email already in use");
        }

        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());
        account.setPhone(accountDTO.getPhone());
        account.setImageUrl(accountDTO.getImageUrl());
        account.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        account.setRole(roleRepository.findByNameIgnoreCase(accountDTO.getRole()).orElseThrow(() -> new Exception("Role not found")));

        return new AccountDTO(accountRepository.save(account));
    }
}
