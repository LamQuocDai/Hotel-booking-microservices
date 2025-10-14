package com.hotelbooking.account.controller;

import com.hotelbooking.account.enums.RoleType;
import com.hotelbooking.account.response.ApiResponse;
import com.hotelbooking.account.security.RequirePermission;
import com.hotelbooking.account.security.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo Authorization", description = "Demo endpoints showing role and permission-based authorization")
@SecurityRequirement(name = "bearerAuth")
public class DemoController {

    @GetMapping("/admin-only")
    @Operation(summary = "Chỉ dành cho ADMIN")
    @RequireRole(RoleType.ADMIN)
    public ResponseEntity<ApiResponse<String>> adminOnly() {
        return ResponseEntity.ok(ApiResponse.success("Admin access granted", "Success"));
    }

    @GetMapping("/staff-and-admin")
    @Operation(summary = "Dành cho STAFF và ADMIN")
    @RequireRole({RoleType.STAFF, RoleType.ADMIN})
    public ResponseEntity<ApiResponse<String>> staffAndAdmin() {
        return ResponseEntity.ok(ApiResponse.success("Staff/Admin access granted", "Success"));
    }

    @GetMapping("/all-authenticated")
    @Operation(summary = "Tất cả user đã đăng nhập")
    @RequireRole({RoleType.USER, RoleType.STAFF, RoleType.ADMIN})
    public ResponseEntity<ApiResponse<String>> allAuthenticated() {
        return ResponseEntity.ok(ApiResponse.success("Authenticated user access granted", "Success"));
    }

    @GetMapping("/manage-accounts")
    @Operation(summary = "Demo permission - Quản lý tài khoản")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_CUSTOMER_ACCOUNTS"})
    public ResponseEntity<ApiResponse<String>> manageAccounts() {
        return ResponseEntity.ok(ApiResponse.success("Account management permission granted", "Success"));
    }

    @GetMapping("/view-own-data")
    @Operation(summary = "Demo permission - Xem dữ liệu của mình")
    @RequirePermission({"VIEW_OWN_ACCOUNT", "VIEW_CUSTOMER_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<ApiResponse<String>> viewOwnData() {
        return ResponseEntity.ok(ApiResponse.success("View data permission granted", "Success"));
    }

    @GetMapping("/admin-users")
    @Operation(summary = "Demo - Admin view users with filter")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<ApiResponse<String>> adminViewUsers() {
        return ResponseEntity.ok(ApiResponse.success("Admin can view and filter users", "Success"));
    }

    @GetMapping("/test-auth")
    @Operation(summary = "Test JWT Authentication - No specific permissions required")
    public ResponseEntity<ApiResponse<String>> testAuth() {
        return ResponseEntity.ok(ApiResponse.success("JWT Authentication is working!", "Success"));
    }
}
