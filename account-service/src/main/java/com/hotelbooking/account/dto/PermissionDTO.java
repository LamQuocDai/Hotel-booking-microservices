package com.hotelbooking.account.dto;

import com.hotelbooking.account.entity.Permission;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PermissionDTO {
    private UUID id;
    private String name;
    private String permission;
    private OffsetDateTime createdAt;

    public PermissionDTO() {
    }

    public PermissionDTO(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
        this.permission = permission.getPermission();
        this.createdAt = permission.getCreatedAt();
    }
}
