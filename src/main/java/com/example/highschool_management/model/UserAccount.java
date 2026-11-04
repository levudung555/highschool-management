package com.example.highschool_management.model;

/** Nội bộ backend; không trả bản ghi có password hash ra API. */
public record UserAccount(
    Long maTaiKhoan,
    String tenDangNhap,
    String matKhauBam,
    String hoTen,
    String trangThai,
    long tokenVersion) {}
