package com.example.highschool_management.config;

import com.example.highschool_management.dao.RoleDao;
import com.example.highschool_management.dao.UserDao;
import com.example.highschool_management.model.Role;
import com.example.highschool_management.model.User;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer {

    private final RoleDao roleDao;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            try {
                createRoleIfNotExists("ADMIN", "Quản trị viên");
                createRoleIfNotExists("BGH", "Ban Giám hiệu");
                createRoleIfNotExists("GIAO_VU", "Giáo vụ");
                createRoleIfNotExists("GIAO_VIEN", "Giáo viên");
                createRoleIfNotExists("TEACHER", "Giáo viên");
                createRoleIfNotExists("STUDENT", "Học sinh");

                createAdminIfNotExists();
            } catch (Exception e) {
                log.error("Lỗi khi khởi tạo dữ liệu: ", e);
            }
        };
    }

    private void createRoleIfNotExists(String name, String description) {
        if (roleDao.findByName(name).isEmpty()) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            roleDao.save(role);
        }
    }

    private void createAdminIfNotExists() {
        if (!userDao.existsByUsername(adminUsername)) {
            Role adminRole = roleDao.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            User user = User.builder()
                    .username(adminUsername)
                    .fullName("Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .enabled(true)
                    .trangThai("HOAT_DONG")
                    .tokenVersion(0)
                    .roles(Set.of(adminRole))
                    .build();
            userDao.save(user);
        }
    }
}
