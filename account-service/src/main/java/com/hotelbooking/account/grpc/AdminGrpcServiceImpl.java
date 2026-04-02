package com.hotelbooking.account.grpc;

import com.hotelbooking.account.dto.AccountDTO;
import com.hotelbooking.account.dto.CreateAccountDTO;
import com.hotelbooking.account.dto.PaginationDTO;
import com.hotelbooking.account.dto.UpdateAccountDTO;
import com.hotelbooking.account.grpc.*;
import com.hotelbooking.account.enums.RoleType;
import com.hotelbooking.account.response.PaginationResponse;
import com.hotelbooking.account.service.AdminUserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class AdminGrpcServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AdminGrpcServiceImpl.class);

    private final AdminUserService adminUserService;

    public AdminGrpcServiceImpl(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        try {
            int pageNumber = request.getPageNumber() > 0 ? request.getPageNumber() : 1;
            int pageSize = request.getPageSize() > 0 ? request.getPageSize() : 10;
            String sortBy = !request.getSortBy().isEmpty() ? request.getSortBy() : "createdAt";
            String sortDirection = !request.getSortDirection().isEmpty() ? request.getSortDirection() : "desc";
            String search = request.getSearch();

            PaginationDTO paginationDTO = new PaginationDTO(pageNumber, pageSize, search, sortBy, sortDirection);
            PaginationResponse<AccountDTO> users = adminUserService.getUsers(paginationDTO);

            ListUsersResponse.Builder builder = ListUsersResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Users retrieved successfully")
                    .setStatusCode(200)
                    .build())
                .setTotalCount(users.getTotalCount())
                .setPageNumber(users.getPageNumber())
                .setPageSize(users.getPageSize())
                .setTotalPages(users.getTotalPages());

            for (AccountDTO account : users.getData()) {
                builder.addUsers(mapAccountDTOToUser(account));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void getUsersByRole(GetUsersByRoleRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        try {
            if (request.getRoleName().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Role name is required")
                    .asException());
                return;
            }

            int pageNumber = request.getPageNumber() > 0 ? request.getPageNumber() : 1;
            int pageSize = request.getPageSize() > 0 ? request.getPageSize() : 10;
            String sortBy = !request.getSortBy().isEmpty() ? request.getSortBy() : "createdAt";
            String sortDirection = !request.getSortDirection().isEmpty() ? request.getSortDirection() : "desc";
            String search = request.getSearch();

            PaginationDTO paginationDTO = new PaginationDTO(pageNumber, pageSize, search, sortBy, sortDirection);
            PaginationResponse<AccountDTO> users = adminUserService.getUsersByRole(paginationDTO, request.getRoleName());

            ListUsersResponse.Builder builder = ListUsersResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Users retrieved successfully")
                    .setStatusCode(200)
                    .build())
                .setTotalCount(users.getTotalCount())
                .setPageNumber(users.getPageNumber())
                .setPageSize(users.getPageSize())
                .setTotalPages(users.getTotalPages());

            for (AccountDTO account : users.getData()) {
                builder.addUsers(mapAccountDTOToUser(account));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error retrieving users by role", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void getAvailableRoles(GetAvailableRolesRequest request, StreamObserver<GetAvailableRolesResponse> responseObserver) {
        try {
            GetAvailableRolesResponse.Builder builder = GetAvailableRolesResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Available roles retrieved successfully")
                    .setStatusCode(200)
                    .build());

            for (RoleType role : RoleType.values()) {
                builder.addRoles(role.name());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error retrieving available roles", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        try {
            if (request.getUsername().isEmpty() || request.getEmail().isEmpty() || 
                request.getPassword().isEmpty() || request.getPhone().isEmpty() || 
                request.getRole().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("All fields are required")
                    .asException());
                return;
            }

            CreateAccountDTO dto = new CreateAccountDTO();
            dto.setUsername(request.getUsername());
            dto.setEmail(request.getEmail());
            dto.setPassword(request.getPassword());
            dto.setPhone(request.getPhone());
            dto.setRole(request.getRole());

            AccountDTO createdUser = adminUserService.createAccount(dto);

            CreateUserResponse response = CreateUserResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User created successfully")
                    .setStatusCode(200)
                    .build())
                .setUser(mapAccountDTOToUser(createdUser))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error creating user", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        try {
            if (request.getUserId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("User ID is required")
                    .asException());
                return;
            }

            UpdateAccountDTO dto = new UpdateAccountDTO();
            dto.setPhone(request.getPhone());
            dto.setImageUrl(request.getImageUrl());

            AccountDTO updatedUser = adminUserService.updateAccount(UUID.fromString(request.getUserId()), dto);

            UpdateUserResponse response = UpdateUserResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User updated successfully")
                    .setStatusCode(200)
                    .build())
                .setUser(mapAccountDTOToUser(updatedUser))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error updating user", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        try {
            if (request.getUserId().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("User ID is required")
                    .asException());
                return;
            }

            adminUserService.deleteAccount(UUID.fromString(request.getUserId()));

            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                .setApiResponse(ApiResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User deleted successfully")
                    .setStatusCode(200)
                    .build())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());
        }
    }

    private User mapAccountDTOToUser(AccountDTO account) {
        return User.newBuilder()
            .setId(account.getId().toString())
            .setUsername(account.getUsername())
            .setEmail(account.getEmail())
            .setPhone(account.getPhone() != null ? account.getPhone() : "")
            .setImageUrl(account.getImageUrl() != null ? account.getImageUrl() : "")
            .setRole(account.getRole() != null ? account.getRole() : "USER")
            .setIsActive(account.isActive())
            .setCreatedAt(account.getCreatedAt() != null ? account.getCreatedAt().toString() : "")
            .setUpdatedAt(account.getUpdatedAt() != null ? account.getUpdatedAt().toString() : "")
            .build();
    }
}
