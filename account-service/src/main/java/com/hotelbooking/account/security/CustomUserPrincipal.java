package com.hotelbooking.account.security;

import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.enums.RolePermissions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class CustomUserPrincipal implements UserDetails {

    private final Account account;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return account.getDeletedAt() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.getDeletedAt() == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return account.getDeletedAt() == null;
    }

    // Microservice-friendly utility methods
    public UUID getUserId() {
        return account.getId();
    }

    public String getEmail() {
        return account.getEmail();
    }

    public String getRoleName() {
        return account.getRole().getRoleName(); // Get role name from enum
    }

    public String getPhone() {
        return account.getPhone();
    }

    // Permission checking methods for microservice interactions
    public boolean hasPermission(String permission) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("PERMISSION_" + permission));
    }

    public boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getAllPermissions() {
        return RolePermissions.getPermissionsByRoleName(getRoleName());
    }

    // Microservice authorization methods
    public boolean canAccessBookingService() {
        return hasAnyPermission("ADMIN_BOOKING_SERVICE", "STAFF_BOOKING_SERVICE", "USER_BOOKING_SERVICE");
    }

    public boolean canAccessPaymentService() {
        return hasAnyPermission("ADMIN_PAYMENT_SERVICE", "USER_PAYMENT_SERVICE");
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRoleName());
    }

    public boolean isStaff() {
        return "STAFF".equals(getRoleName());
    }

    public boolean isUser() {
        return "USER".equals(getRoleName());
    }

    // Method to create JWT claims for microservice communication
    public java.util.Map<String, Object> getJwtClaims() {
        return java.util.Map.of(
            "userId", getUserId().toString(), // Convert UUID to String for JWT serialization
            "role", getRoleName(),
            "permissions", getAllPermissions()
        );
    }
}
