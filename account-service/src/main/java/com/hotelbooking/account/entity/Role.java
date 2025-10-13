package com.hotelbooking.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String description;

    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    private List<Account> accounts;

    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    private List<RolePermission> rolePermissions;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime deletedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }


}

