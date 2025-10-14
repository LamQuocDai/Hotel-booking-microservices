package com.hotelbooking.account.seed;

import com.hotelbooking.account.entity.Account;
import com.hotelbooking.account.enums.RoleType;
import com.hotelbooking.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;

@Component
public class SeedData implements CommandLineRunner {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdminAccount();
    }

    private void seedAdminAccount() {
        if (accountRepository.findByEmailAndDeletedAtIsNull("admin@admin.com").isEmpty()) {
            Account admin = new Account();
            admin.setEmail("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setUsername("Admin");
            admin.setPhone("0000000000");
            admin.setCreatedAt(OffsetDateTime.now());
            admin.setRole(RoleType.ADMIN); // Sử dụng trực tiếp enum

            accountRepository.save(admin);
            System.out.println("Created admin account: admin@admin.com / admin123");
        }
    }
}
