package com.example.highschool_management.dao;

import com.example.highschool_management.model.TeachingAssignment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeachingAssignmentDao {
    long tao(TeachingAssignment phanCong);
    Optional<TeachingAssignment> timTheoMa(Long maPhanCong);
    List<TeachingAssignment> lichSuTheoLopMonHoc(Long maLopMonHoc);
    void ghiNhatKyTao(Long maTaiKhoan, TeachingAssignment phanCong, LocalDateTime thoiDiem);
    boolean coPhanCongTrungThoiGian(Long maLopMonHoc, LocalDate tuNgay, LocalDate denNgayHieuLuc);
}