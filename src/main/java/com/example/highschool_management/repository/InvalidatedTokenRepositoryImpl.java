package com.example.highschool_management.repository;

import com.example.highschool_management.entity.InvalidatedToken;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InvalidatedTokenRepositoryImpl implements InvalidatedTokenRepository {
  private final JdbcTemplate jdbc;

  public InvalidatedTokenRepositoryImpl(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @PostConstruct
  public void init() {
    jdbc.execute(
        """
        CREATE TABLE IF NOT EXISTS invalidated_token (
            id VARCHAR(255) PRIMARY KEY,
            expire_time TIMESTAMP NOT NULL
        )
        """);
  }

  @Override
  public void save(InvalidatedToken token) {
    jdbc.update(
        "INSERT INTO invalidated_token (id, expire_time) VALUES (?, ?)",
        token.getId(),
        new Timestamp(token.getExpireTime().getTime()));
  }

  @Override
  public boolean existsById(String id) {
    Integer count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM invalidated_token WHERE id = ?", Integer.class, id);
    return count != null && count > 0;
  }
}
