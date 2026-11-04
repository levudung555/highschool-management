package com.example.highschool_management.utils;

import java.sql.*;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Component;

/** Các DAO JDBC dùng chung DataSource và transaction Spring. */
@Component
public class JdbcConnect {
  private static DataSource source;

  public JdbcConnect(DataSource dataSource) {
    source = new TransactionAwareDataSourceProxy(dataSource);
  }

  public static Connection getConnection() throws SQLException {
    return source.getConnection();
  }
}
