package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.InvalidatedTokenDao;
import com.example.highschool_management.model.InvalidatedToken;
import com.example.highschool_management.utils.JdbcConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.springframework.stereotype.Repository;

@Repository
public class InvalidatedTokenDaoImpl implements InvalidatedTokenDao {

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM invalidated_tokens WHERE id = ?)";
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi kiểm tra token vô hiệu", e);
        }
        return false;
    }

    @Override
    public void save(InvalidatedToken invalidatedToken) {
        String sql = """
            INSERT INTO invalidated_tokens (id, expire_time)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE expire_time = VALUES(expire_time)
            """;
        try (Connection conn = JdbcConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invalidatedToken.getId());
            ps.setTimestamp(2, new Timestamp(invalidatedToken.getExpireTime().getTime()));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu token vô hiệu", e);
        }
    }
}
