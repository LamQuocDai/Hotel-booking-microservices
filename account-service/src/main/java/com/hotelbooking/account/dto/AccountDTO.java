package com.hotelbooking.account.dto;

import com.hotelbooking.account.entity.Account;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AccountDTO {
    private UUID id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String imageUrl;
    private OffsetDateTime createdAt;

    public AccountDTO() {
    }

    public AccountDTO(Account account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.email = account.getEmail();
        this.phone = account.getPhone();
        this.role = account.getRole().getRoleName(); // Fix: use getRoleName() from enum
        this.imageUrl = account.getImageUrl();
        this.createdAt = account.getCreatedAt();
    }
}
