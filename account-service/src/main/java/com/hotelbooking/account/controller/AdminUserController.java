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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<ApiResponse<PaginationResponse<AccountDTO>>> getUsers(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
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
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<ApiResponse<PaginationResponse<AccountDTO>>> getUsersByRole(
            @PathVariable String roleName,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PaginationDTO paginationDTO = new PaginationDTO(pageNumber, pageSize, search, sortBy, sortDirection);
        PaginationResponse<AccountDTO> users = adminUserService.getUsersByRole(paginationDTO, roleName);

        return ResponseEntity.ok(ApiResponse.success(users, "Users with role " + roleName + " retrieved successfully"));
    }

    @GetMapping("/roles")
    @RequirePermission({"MANAGE_ACCOUNTS", "VIEW_ALL_ACCOUNTS"})
    public ResponseEntity<ApiResponse<RoleType[]>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success(RoleType.values(), "Roles retrieved successfully"));
    }

    @PostMapping
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
