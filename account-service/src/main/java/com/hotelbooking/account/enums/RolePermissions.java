package com.hotelbooking.account.enums;

import java.util.Set;

public enum RolePermissions {
    ADMIN(Set.of(
        // Account Management
        "MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS", "CREATE_ACCOUNT", "UPDATE_ACCOUNT", "DELETE_ACCOUNT",

        // Role Management
        "MANAGE_ROLES", "VIEW_ROLES", "CREATE_ROLE", "UPDATE_ROLE", "DELETE_ROLE",

        // System Administration
        "SYSTEM_CONFIG", "VIEW_LOGS", "MANAGE_SYSTEM",

        // Full access to all booking operations
        "VIEW_ALL_BOOKINGS", "MANAGE_BOOKINGS", "CANCEL_ANY_BOOKING",

        // Full access to payment operations
        "VIEW_ALL_PAYMENTS", "MANAGE_PAYMENTS", "PROCESS_REFUNDS",

        // Cross-service admin permissions
        "ADMIN_BOOKING_SERVICE", "ADMIN_PAYMENT_SERVICE"
    )),

    STAFF(Set.of(
        // Limited account management
        "VIEW_CUSTOMER_ACCOUNTS", "UPDATE_CUSTOMER_ACCOUNT",

        // Booking management for customers
        "VIEW_BOOKINGS", "CREATE_BOOKING", "UPDATE_BOOKING", "CANCEL_BOOKING",

        // Basic payment operations
        "VIEW_PAYMENTS", "PROCESS_PAYMENT",

        // Staff-level booking service access
        "STAFF_BOOKING_SERVICE"
    )),

    USER(Set.of(
        // Self account management
        "VIEW_OWN_ACCOUNT", "UPDATE_OWN_ACCOUNT",

        // Own booking management
        "VIEW_OWN_BOOKINGS", "CREATE_OWN_BOOKING", "UPDATE_OWN_BOOKING", "CANCEL_OWN_BOOKING",

        // Own payment operations
        "VIEW_OWN_PAYMENTS", "MAKE_PAYMENT",

        // User-level service access
        "USER_BOOKING_SERVICE", "USER_PAYMENT_SERVICE"
    ));

    private final Set<String> permissions;

    RolePermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    // Utility method để lấy permissions theo role name
    public static Set<String> getPermissionsByRoleName(String roleName) {
        try {
            return RolePermissions.valueOf(roleName.toUpperCase()).getPermissions();
        } catch (IllegalArgumentException e) {
            return Set.of(); // Return empty set nếu role không tồn tại
        }
    }
}
