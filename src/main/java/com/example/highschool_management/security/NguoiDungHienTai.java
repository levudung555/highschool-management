package com.example.highschool_management.security;

/**
 * Adapter identity: implementation must use authenticated server context, never request JSON/header
 * IDs.
 */
public interface NguoiDungHienTai {
  long yeuCauVaiTro(String... vaiTro);
}
