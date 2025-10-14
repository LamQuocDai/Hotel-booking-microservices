package com.hotelbooking.account.repository;

import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsernameAndDeletedAtIsNull(String username);
    Optional<Account> findByEmailAndDeletedAtIsNull(String email);

    // Find all non-deleted accounts with pagination
    Page<Account> findByDeletedAtIsNull(Pageable pageable);

    // Filter by role
    Page<Account> findByRoleAndDeletedAtIsNull(RoleType role, Pageable pageable);

    Optional<Account> findByUserIdAndDeletedAtIsNull(UUID userId);

    // Search by keyword in username, email, or phone
    @Query("SELECT a FROM Account a WHERE a.deletedAt IS NULL AND " +
           "(LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Account> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Search by keyword and filter by role
    @Query("SELECT a FROM Account a WHERE a.deletedAt IS NULL AND a.role = :role AND " +
           "(LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Account> searchByRoleAndKeyword(@Param("role") RoleType role,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);
}
