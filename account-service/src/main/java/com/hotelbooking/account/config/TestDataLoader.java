package com.hotelbooking.account.config;

import com.hotelbooking.account.entity.Permission;
import com.hotelbooking.account.entity.Role;
import com.hotelbooking.account.entity.RolePermission;
import com.hotelbooking.account.repository.PermissionRepository;
import com.hotelbooking.account.repository.RoleRepository;
import com.hotelbooking.account.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("test") // Chỉ chạy khi profile test
public class TestDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading test data...");

        // Kiểm tra xem đã có dữ liệu chưa
        if (roleRepository.count() > 0) {
            log.info("Test data already exists, skipping...");
            return;
        }

        // Tạo Roles
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setCreatedAt(OffsetDateTime.now());
        adminRole = roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setCreatedAt(OffsetDateTime.now());
        userRole = roleRepository.save(userRole);

        Role staffRole = new Role();
        staffRole.setName("STAFF");
        staffRole.setCreatedAt(OffsetDateTime.now());
        staffRole = roleRepository.save(staffRole);

        // Tạo Permissions
        Permission viewUsers = createPermission("View Users", "VIEW_USERS");
        Permission createUsers = createPermission("Create Users", "CREATE_USERS");
        Permission editUsers = createPermission("Edit Users", "EDIT_USERS");
        Permission deleteUsers = createPermission("Delete Users", "DELETE_USERS");
        Permission viewBookings = createPermission("View Bookings", "VIEW_BOOKINGS");
        Permission manageBookings = createPermission("Manage Bookings", "MANAGE_BOOKINGS");

        // Gán quyền cho ADMIN (tất cả quyền)
        createRolePermission(adminRole, viewUsers);
        createRolePermission(adminRole, createUsers);
        createRolePermission(adminRole, editUsers);
        createRolePermission(adminRole, deleteUsers);
        createRolePermission(adminRole, viewBookings);
        createRolePermission(adminRole, manageBookings);

        // Gán quyền cho USER (chỉ xem bookings)
        createRolePermission(userRole, viewBookings);

        // Gán quyền cho STAFF (xem users và quản lý bookings)
        createRolePermission(staffRole, viewUsers);
        createRolePermission(staffRole, viewBookings);
        createRolePermission(staffRole, manageBookings);

        log.info("Test data loaded successfully!");
    }

    private Permission createPermission(String name, String permission) {
        Permission perm = new Permission();
        perm.setName(name);
        perm.setPermission(permission);
        perm.setCreatedAt(OffsetDateTime.now());
        return permissionRepository.save(perm);
    }

    private void createRolePermission(Role role, Permission permission) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermissionRepository.save(rolePermission);
    }
}
