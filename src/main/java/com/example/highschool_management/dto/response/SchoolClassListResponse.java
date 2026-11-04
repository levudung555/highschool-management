package com.example.highschool_management.dto.response;

/** Dữ liệu chỉ đọc phục vụ nghiệp vụ UC08/UC09. */
public record SchoolClassListResponse(
    Long maLop,
    Long maNamHoc,
    Long maKhoi,
    String tenLop,
    String namHoc,
    int soKhoi,
    String trangThai,
    long siSo) {}
