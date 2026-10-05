package com.example.library.config;

import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * بيحل مشكلة "أول أدمن" (Chicken & Egg): وقت ما التطبيق يشتغل أول مرة،
 * لو مفيش أي مستخدم بدور ADMIN في قاعدة البيانات، بينشئ واحد ببيانات ثابتة.
 * بعد كده أي أدمن جديد لازم يتعمل عن طريق POST /api/auth/admin/register
 * (اللي محمي ومحتاج توكن أدمن موجود بالفعل).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_EMAIL = "admin@library.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@12345";

    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setEmail(DEFAULT_ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            log.warn("=================================================================");
            log.warn("No admin found — a default admin account was created automatically:");
            log.warn("  Email:    {}", DEFAULT_ADMIN_EMAIL);
            log.warn("  Password: {}", DEFAULT_ADMIN_PASSWORD);
            log.warn("Please log in and create your own admin, then consider disabling this seeder.");
            log.warn("=================================================================");
        }
    }
}
