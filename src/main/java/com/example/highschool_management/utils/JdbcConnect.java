package com.example.highschool_management.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class JdbcConnect {

    private static String url;
    private static String username;
    private static String password;

    @Value("${spring.datasource.url}")
    public void setUrl(String url) {
        JdbcConnect.url = url;
    }

    @Value("${spring.datasource.username}")
    public void setUsername(String username) {
        JdbcConnect.username = username;
    }

    @Value("${spring.datasource.password}")
    public void setPassword(String password) {
        JdbcConnect.password = password;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
