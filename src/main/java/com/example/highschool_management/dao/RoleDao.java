package com.example.highschool_management.dao;

import com.example.highschool_management.model.Role;
import java.util.Optional;

public interface RoleDao {
    Optional<Role> findByName(String name);
    Role save(Role role);
}
