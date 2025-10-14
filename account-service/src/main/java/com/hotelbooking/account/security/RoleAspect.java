package com.hotelbooking.account.security;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        // Lấy role hiện tại của user
        String currentRole = authentication.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(authority -> authority.getAuthority().substring(5)) // Bỏ prefix "ROLE_"
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("User has no role"));

        // Kiểm tra xem role hiện tại có trong danh sách allowed roles không
        boolean hasRequiredRole = Arrays.stream(requireRole.value())
                .anyMatch(roleType -> roleType.getRoleName().equals(currentRole));

        if (!hasRequiredRole) {
            throw new AccessDeniedException("User does not have required role. Required: " +
                    Arrays.toString(requireRole.value()) + ", Current: " + currentRole);
        }
    }
}
