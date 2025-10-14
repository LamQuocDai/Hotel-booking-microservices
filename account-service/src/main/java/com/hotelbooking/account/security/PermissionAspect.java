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
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String[] requiredPermissions = requirePermission.value();

        // Kiểm tra xem user có ít nhất một trong các permission yêu cầu không
        boolean hasRequiredPermission = Arrays.stream(requiredPermissions)
                .anyMatch(permission -> authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("PERMISSION_" + permission)));

        if (!hasRequiredPermission) {
            throw new AccessDeniedException("User does not have required permission(s): " +
                    Arrays.toString(requiredPermissions));
        }
    }
}
