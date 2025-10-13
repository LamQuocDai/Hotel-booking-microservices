package com.hotelbooking.account.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {
    // In-memory storage cho refresh tokens - thay thế Redis hoàn toàn
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    public String issueRefreshToken(String userId) {
        String token = UUID.randomUUID().toString();
        String key = refreshKey(token);

        // Lưu vào memory thay vì Redis
        tokenStore.put(key, userId);
        return token;
    }

    public String validateRefreshToken(String token) {
        String key = refreshKey(token);
        return tokenStore.get(key);
    }

    public void revoke(String token) {
        String key = refreshKey(token);
        tokenStore.remove(key);
    }

    private String refreshKey(String token) {
        return "refresh:" + token;
    }
}
