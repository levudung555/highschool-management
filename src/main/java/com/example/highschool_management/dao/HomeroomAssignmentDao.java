package com.example.highschool_management.dao;

import com.example.highschool_management.model.HomeroomAssignment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HomeroomAssignmentDao {
    boolean coPhanCongTrungThoiGian(Long maLop, LocalDate ngayBatDau, LocalDate ngayKetThuc);
    long tao(HomeroomAssignment phanCong);
    Optional<HomeroomAssignment> timTheoMa(Long maPhanCong);
    List<HomeroomAssignment> lichSuTheoLop(Long maLop);
    void ghiNhatKyTao(Long maTaiKhoan, HomeroomAssignment phanCong, LocalDateTime thoiDiem);
}
