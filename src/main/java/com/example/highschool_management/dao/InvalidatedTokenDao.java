package com.example.highschool_management.dao;

import com.example.highschool_management.model.InvalidatedToken;

public interface InvalidatedTokenDao {
    boolean existsById(String id);
    void save(InvalidatedToken invalidatedToken);
}
