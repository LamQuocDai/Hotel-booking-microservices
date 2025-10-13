package com.hotelbooking.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String permission;

    @OneToMany(mappedBy = "permission")
    @ToString.Exclude
    private List<RolePermission> rolePermissions;

    @Column(nullable = false, name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @PrePersist
    public void onCreate()
    {
        this.createdAt = OffsetDateTime.now();
    }
}

