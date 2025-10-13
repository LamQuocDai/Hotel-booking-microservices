package com.hotelbooking.account.repository;

import com.hotelbooking.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsername(String username);
    Optional<Account> findByEmailAndDeletedAtIsNull(String email);
}


