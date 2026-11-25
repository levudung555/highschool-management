package com.example.highschool_management.dao;

import com.example.highschool_management.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    List<String> findRoleNames(long id);
    Optional<String> findFullName(long id);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
}
