package com.hotelbooking.account.entity;

import com.hotelbooking.account.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(length = 200)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role; // Sử dụng trực tiếp enum thay vì entity Role

    @Column(nullable = false)
    private Boolean isActive = false; // Email verification status

    @Column(length = 255)
    private String verificationToken; // Token for email verification

    @Column
    private OffsetDateTime verificationTokenExpiresAt; // Token expiration

    @Column(length = 255)
    private String passwordResetToken; // Token for password reset

    @Column
    private OffsetDateTime passwordResetTokenExpiresAt; // Password reset token expiration

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime deletedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
