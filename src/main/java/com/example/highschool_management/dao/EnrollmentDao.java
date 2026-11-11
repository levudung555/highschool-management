package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.ClassStudentResponse;

import com.example.highschool_management.dto.response.EnrollmentStudentResponse;
import com.example.highschool_management.dto.response.SchoolYearPeriodResponse;
import com.example.highschool_management.model.Enrollment;
import com.example.highschool_management.model.SchoolClass;

import java.time.*;
import java.util.*;

public interface EnrollmentDao {
  boolean khoaHocSinh(Long maHocSinh);
  boolean coXepLopTrongNamHoc(Long maHocSinh, Long maNamHoc);
  long tao(Enrollment enrollment);
  SchoolYearPeriodResponse namHoc(Long maNamHoc);
  Optional<Enrollment> timTheoMa(Long maXepLop);


  Optional<Enrollment> timTheoMaDeCapNhat(Long maXepLop);
  void ketThuc(Long maXepLop, LocalDate denNgay, String lyDo);
  boolean trungThoiGian(Long maHocSinh, Long maNH, LocalDate tuNgay, LocalDate denNgay, Long boQuaMaXepLop);
  void ghiNhatKyChuyenLop(Long maTaiKhoan, Long maXepLopNguon, Long maXepLopDich, LocalDate ngayChuyen, String lyDo, LocalDateTime thoiDiem);

  List<Enrollment> lichSu(Long maHocSinh);

  long demDanhSachLop(Long maLop, LocalDate ngay);
  List<ClassStudentResponse> danhSachLop(Long maLop, LocalDate ngay, int kichThuoc, long viTriBatDau);
}
