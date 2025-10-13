package com.hotelbooking.account.dto;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private AccountDTO user;

    public AuthResponse() {}

    public AuthResponse(String token, String refreshToken, AccountDTO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}
