package com.hotelbooking.account.repository;

import com.hotelbooking.account.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByPermission(String permission);
    List<Permission> findAllByDeletedAtIsNull();
}


