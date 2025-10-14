package com.hotelbooking.account.security;

import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.enums.RolePermissions;
import com.hotelbooking.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new CustomUserPrincipal(account, getAuthorities(account));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Account account) {
        String roleName = account.getRole().getRoleName(); // Get role name from enum

        // Lấy permissions từ enum dựa trên role name
        Set<String> permissions = RolePermissions.getPermissionsByRoleName(roleName);

        // Convert permissions thành GrantedAuthority
        Set<GrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission))
                .collect(Collectors.toSet());

        // Thêm role authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

        return authorities;
    }
}
