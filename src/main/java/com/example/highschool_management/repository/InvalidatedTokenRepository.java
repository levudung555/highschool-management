package com.example.highschool_management.repository;

import com.example.highschool_management.entity.InvalidatedToken;

public interface InvalidatedTokenRepository {
  boolean existsById(String id);

  void save(InvalidatedToken token);
}
