package com.example.highschool_management.config;

import com.example.highschool_management.dao.RoleDao;
import com.example.highschool_management.dao.UserDao;
import com.example.highschool_management.enums.RoleType;
import com.example.highschool_management.model.Role;
import com.example.highschool_management.model.User;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;
import javax.sql.DataSource;
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
    CommandLineRunner initData(DataSource dataSource) {
        return args -> {
            initSchema(dataSource);

            createRoleIfNotExists(RoleType.ADMIN.name(), "Quản trị viên");
            createRoleIfNotExists(RoleType.TEACHER.name(), "Giáo viên");
            createRoleIfNotExists(RoleType.STUDENT.name(), "Học sinh");

            createAdminIfNotExists();
        };
    }

    private void initSchema(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS vai_tro ("
                    + "MaVT BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, "
                    + "MaSoVaiTro VARCHAR(40) NOT NULL UNIQUE, "
                    + "TenVaiTro VARCHAR(100) NOT NULL"
                    + ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS tai_khoan ("
                    + "MaTK BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, "
                    + "MaGV BIGINT UNSIGNED, "
                    + "TenDangNhap VARCHAR(100) NOT NULL UNIQUE, "
                    + "MatKhauBam VARCHAR(255) NOT NULL, "
                    + "HoTen VARCHAR(150), "
                    + "Email VARCHAR(254) UNIQUE, "
                    + "TrangThai VARCHAR(20) DEFAULT 'HOAT_DONG', "
                    + "TokenVersion INT UNSIGNED DEFAULT 0"
                    + ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS tai_khoan_vai_tro ("
                    + "MaTK BIGINT UNSIGNED NOT NULL, "
                    + "MaVT BIGINT UNSIGNED NOT NULL, "
                    + "PRIMARY KEY (MaTK, MaVT)"
                    + ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS invalidated_tokens ("
                    + "id VARCHAR(64) PRIMARY KEY, "
                    + "expire_time TIMESTAMP NOT NULL"
                    + ")");
        } catch (Exception e) {
            log.warn("Could not auto-create auth tables: {}", e.getMessage());
        }
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
            Role adminRole = roleDao.findByName(RoleType.ADMIN.name())
                    .orElseThrow(() -> new RuntimeException("Role not found"));

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
