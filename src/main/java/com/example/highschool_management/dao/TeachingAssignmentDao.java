package com.example.highschool_management.dao;

import com.example.highschool_management.model.TeachingAssignment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeachingAssignmentDao {
    boolean coPhanCongTrungThoiGian(Long maLopMonHoc, LocalDate tuNgay, LocalDate denNgayHieuLuc);
    long tao(TeachingAssignment phanCong);
    void ghiNhatKyTao(Long maTaiKhoan, TeachingAssignment phanCong, LocalDateTime thoiDiem);
    List<TeachingAssignment> lichSuTheoLopMonHoc(Long maLopMonHoc);
    Optional<TeachingAssignment> timTheoMa(Long maPhanCong);
}