package com.hotelbooking.account.dto;

import com.hotelbooking.account.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class RoleDTO {
    private UUID id;

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    private OffsetDateTime createdAt;

    public RoleDTO() {}

    public RoleDTO(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.createdAt = role.getCreatedAt();
    }
}
