package com.hotelbooking.account.controller;

import com.hotelbooking.account.dto.AccountDTO;
import com.hotelbooking.account.dto.CreateAccountDTO;
import com.hotelbooking.account.dto.PaginationDTO;
import com.hotelbooking.account.dto.UpdateAccountDTO;
import com.hotelbooking.account.enums.RoleType;
import com.hotelbooking.account.response.ApiResponse;
import com.hotelbooking.account.response.PaginationResponse;
import com.hotelbooking.account.security.RequirePermission;
import com.hotelbooking.account.service.AdminUserService;
import com.hotelbooking.account.validation.Validation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.tags.Param;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "APIs for admin to manage users")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Get all users with pagination, search and role filter")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<ApiResponse<PaginationResponse<AccountDTO>>> getUsers(
            @Parameter(description = "Page number (starts from 1)")
            @RequestParam(defaultValue = "1") int pageNumber,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Search keyword (username, email, or phone)")
            @RequestParam(defaultValue = "") String search,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String sortDirection,

            @Parameter(description = "Filter by role (ADMIN, STAFF, USER)")
            @RequestParam(required = false) String role
    ) {
        PaginationDTO paginationDTO = new PaginationDTO(pageNumber, pageSize, search, sortBy, sortDirection);

        PaginationResponse<AccountDTO> users;
        if (role != null && !role.trim().isEmpty()) {
            users = adminUserService.getUsersByRole(paginationDTO, role);
        } else {
            users = adminUserService.getUsers(paginationDTO);
        }

        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/by-role/{roleName}")
    @Operation(summary = "Get users by specific role")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<ApiResponse<PaginationResponse<AccountDTO>>> getUsersByRole(
            @Parameter(description = "Role name (ADMIN, STAFF, USER)")
            @PathVariable String roleName,

            @Parameter(description = "Page number (starts from 1)")
            @RequestParam(defaultValue = "1") int pageNumber,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Search keyword (username, email, or phone)")
            @RequestParam(defaultValue = "") String search,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PaginationDTO paginationDTO = new PaginationDTO(pageNumber, pageSize, search, sortBy, sortDirection);
        PaginationResponse<AccountDTO> users = adminUserService.getUsersByRole(paginationDTO, roleName);

        return ResponseEntity.ok(ApiResponse.success(users, "Users with role " + roleName + " retrieved successfully"));
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all available roles")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<ApiResponse<RoleType[]>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success(RoleType.values(), "Roles retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateAccountDTO accountDTO, BindingResult bindingResult) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }
            AccountDTO user = adminUserService.createAccount(accountDTO);
            return ResponseEntity.ok(ApiResponse.success(user, "User created successfully"));
        }
        catch(Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an existing user")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<?> updateUser(@PathVariable UUID id,@Valid @RequestBody UpdateAccountDTO accountDTO, BindingResult bindingResult) {
        try {
            if(Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }
            AccountDTO user = adminUserService.updateAccount(id, accountDTO);
            return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an existing user")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        try {
            AccountDTO user = adminUserService.deleteAccount(id);
            return ResponseEntity.ok(ApiResponse.success(user, "User deleted successfully"));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }
}
