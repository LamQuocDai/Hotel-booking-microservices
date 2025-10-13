package com.hotelbooking.account.seed;

import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.entity.Role;
import com.hotelbooking.account.repository.AccountRepository;
import com.hotelbooking.account.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;

@Component
public class SeedData implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminAccount();
    }

    private void seedRoles() {
        // Tạo role ADMIN
        if (roleRepository.findByNameIgnoreCase("ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role with full access");
            adminRole.setCreatedAt(OffsetDateTime.now());
            roleRepository.save(adminRole);
            System.out.println("Created ADMIN role");
        }

        // Tạo role USER
        if (roleRepository.findByNameIgnoreCase("USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Standard user role with limited access");
            userRole.setCreatedAt(OffsetDateTime.now());
            roleRepository.save(userRole);
            System.out.println("Created USER role");
        }
    }

    private void seedAdminAccount() {
        if (accountRepository.findByEmailAndDeletedAtIsNull("admin@admin.com").isEmpty()) {
            Role adminRole = roleRepository.findByNameIgnoreCase("ADMIN").orElse(null);

            if (adminRole != null) {
                Account admin = new Account();
                admin.setEmail("admin@admin.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setUsername("Admin");
                admin.setPhone("0000000000");
                admin.setCreatedAt(OffsetDateTime.now());
                admin.setRole(adminRole);

                accountRepository.save(admin);
                System.out.println("Created admin account: admin@admin.com / admin123");
            }
        }
    }
}
