package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.RoleDao;
import com.example.highschool_management.model.Role;
import com.example.highschool_management.utils.JdbcConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDaoImpl implements RoleDao {

    @Override
    public Optional<Role> findByName(String name) {
        String sql = "SELECT MaVT, MaSoVaiTro, TenVaiTro FROM vai_tro WHERE MaSoVaiTro = ?";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Role role = Role.builder()
                            .id(rs.getLong("MaVT"))
                            .name(rs.getString("MaSoVaiTro"))
                            .description(rs.getString("TenVaiTro"))
                            .build();
                    return Optional.of(role);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm vai trò theo mã", e);
        }
        return Optional.empty();
    }

    @Override
    public Role save(Role role) {
        String sql = "INSERT INTO vai_tro (MaSoVaiTro, TenVaiTro) VALUES (?, ?)";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, role.getName());
            ps.setString(2, role.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    role.setId(rs.getLong(1));
                }
            }
            return role;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu vai trò", e);
        }
    }
}
