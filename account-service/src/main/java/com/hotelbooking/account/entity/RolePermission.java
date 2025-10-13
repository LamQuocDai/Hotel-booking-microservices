package com.hotelbooking.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "role_permissions")
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "rote_id", referencedColumnName = "id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "permission_id", referencedColumnName = "id")
    private Permission permission;
}
