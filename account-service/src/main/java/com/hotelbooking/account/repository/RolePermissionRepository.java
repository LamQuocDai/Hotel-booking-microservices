package com.hotelbooking.account.repository;

import com.hotelbooking.account.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

}
