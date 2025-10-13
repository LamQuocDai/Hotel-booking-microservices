package com.hotelbooking.account.controller;

import com.hotelbooking.account.dto.RoleDTO;
import com.hotelbooking.account.response.ApiResponse;
import com.hotelbooking.account.service.RoleService;
import com.hotelbooking.account.validation.Validation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/roles")
@CrossOrigin("*")
public class RoleController {

    private final RoleService roleService;
    private final Logger logger = LoggerFactory.getLogger(RoleController.class);

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleDTO roleDTO, BindingResult bindingResult) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                logger.info(bindingResult.toString());
                return Validation.validateBody(bindingResult);
            }

            RoleDTO createdRole = roleService.createRole(roleDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Role created successfully", createdRole, 201));
        } catch (Exception e) {
            logger.error("Error occurred while creating role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatusCode.valueOf(500))
                    .body(new ApiResponse<>(false, "An error occurred while creating the role: " + e.getMessage(), null, 500));
        }
    }
}
