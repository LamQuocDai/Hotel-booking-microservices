package com.hotelbooking.account.service;

import com.hotelbooking.account.dto.AccountDTO;
import com.hotelbooking.account.dto.CreateAccountDTO;
import com.hotelbooking.account.dto.PaginationDTO;
import com.hotelbooking.account.dto.UpdateAccountDTO;
import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.enums.RoleType;
import com.hotelbooking.account.repository.AccountRepository;
import com.hotelbooking.account.response.PaginationResponse;
import com.hotelbooking.account.validation.PhoneValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AdminUserService {

    private final AccountRepository accountRepository;

    public AdminUserService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public PaginationResponse<AccountDTO> getUsers(PaginationDTO paginationDTO) {
        return getUsers(paginationDTO, null); // Get all users without role filter
    }

    public PaginationResponse<AccountDTO> getUsers(PaginationDTO paginationDTO, RoleType roleFilter) {
        Sort sort = Sort.by(Sort.Direction.fromString(paginationDTO.sortDirection), paginationDTO.sortBy);
        Pageable pageable = PageRequest.of(paginationDTO.pageNumber - 1, paginationDTO.pageSize, sort);

        Page<Account> accounts;

        // Determine which query method to use based on search and role filter
        if (roleFilter != null && (paginationDTO.search != null && !paginationDTO.search.trim().isEmpty())) {
            // Both role filter and search
            accounts = accountRepository.searchByRoleAndKeyword(roleFilter, paginationDTO.search.trim(), pageable);
        } else if (roleFilter != null) {
            // Only role filter
            accounts = accountRepository.findByRoleAndDeletedAtIsNull(roleFilter, pageable);
        } else if (paginationDTO.search != null && !paginationDTO.search.trim().isEmpty()) {
            // Only search
            accounts = accountRepository.searchByKeyword(paginationDTO.search.trim(), pageable);
        } else {
            // No filter, get all
            accounts = accountRepository.findByDeletedAtIsNull(pageable);
        }

        // Convert to DTOs
        var accountDTOs = accounts.stream().map(AccountDTO::new).toList();

        // Create pagination response
        PaginationResponse<AccountDTO> response = new PaginationResponse<>(null, null);
        var paging = response.new Paging(
                (int) accounts.getTotalElements(),
                paginationDTO.pageNumber,
                paginationDTO.pageSize,
                accounts.getTotalPages(),
                accounts.hasNext(),
                accounts.hasPrevious()
        );

        return new PaginationResponse<>(accountDTOs, paging);
    }

    public PaginationResponse<AccountDTO> getUsersByRole(PaginationDTO paginationDTO, String roleName) {
        try {
            RoleType roleType = RoleType.valueOf(roleName.toUpperCase());
            return getUsers(paginationDTO, roleType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
    }

    public AccountDTO getAccount(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        return new AccountDTO(account);
    }

    public AccountDTO createAccount(CreateAccountDTO accountDTO) {
        if (accountRepository.findByEmailAndDeletedAtIsNull(accountDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (PhoneValidator.isValidVietnamesePhone(accountDTO.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number");
        }


        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());
        account.setPhone(accountDTO.getPhone());
        account.setImageUrl(accountDTO.getImageUrl());
        account.setPassword(accountDTO.getPassword());
        account.setRole(RoleType.valueOf(accountDTO.getRole().toUpperCase())); // Set enum directly

        Account savedAccount = accountRepository.save(account);
        return new AccountDTO(savedAccount);
    }

    public AccountDTO updateAccount(UUID id, UpdateAccountDTO accountDTO) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

        if (PhoneValidator.isValidVietnamesePhone(accountDTO.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number");
        }

        account.setUsername(accountDTO.getUsername());
        account.setPhone(accountDTO.getPhone());
        account.setImageUrl(accountDTO.getImageUrl());

        Account savedAccount = accountRepository.save(account);
        return new AccountDTO(savedAccount);
    }

    public AccountDTO deleteAccount(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        account.setDeletedAt(OffsetDateTime.now());
        Account savedAccount = accountRepository.save(account);
        return new AccountDTO(savedAccount);
    }
}
