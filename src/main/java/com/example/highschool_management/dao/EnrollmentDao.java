package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.ClassStudentResponse;

import com.example.highschool_management.dto.response.EnrollmentStudentResponse;
import com.example.highschool_management.dto.response.SchoolYearPeriodResponse;
import com.example.highschool_management.model.Enrollment;
import com.example.highschool_management.model.Grade10PlacementCandidate;
import com.example.highschool_management.model.SchoolClass;

import com.example.highschool_management.dto.response.EnrollmentRequestResponse;
import java.time.*;
import java.util.*;

public interface EnrollmentDao {
  SchoolYearPeriodResponse namHoc(Long maNamHoc);
  List<Grade10PlacementCandidate> danhSachXepLop10DauNam(Long maNamHoc);
  boolean daChotXepLop10(Long maNamHoc);
  Optional<LocalDate> ngayVaoLopDaChot(Long maNamHoc);
  void luuDotChotXepLop10(Long maNamHoc, long maTaiKhoanChot, LocalDate ngayVaoLop, LocalDateTime thoiDiemChot, String maKiemTraPhuongAn);


  boolean khoaHocSinh(Long maHocSinh);
  boolean coXepLopTrongNamHoc(Long maHocSinh, Long maNamHoc);
  Optional<LocalDate> timNgayLopHetCho(Long maLop, LocalDate ngayChuyen, LocalDate ngayKetThucNamHoc, int siSoToiDa);
  long tao(Enrollment enrollment);

  Optional<Enrollment> timTheoMaDeCapNhat(Long maXepLop);

  boolean trungThoiGian(Long maHocSinh, Long maNH, LocalDate tuNgay, LocalDate denNgay, Long boQuaMaXepLop);
  void ketThuc(Long maXepLop, LocalDate denNgay, String lyDo);

  void ghiNhatKyChuyenLop(Long maTaiKhoan, Long maXepLopNguon, Long maXepLopDich, LocalDate ngayChuyen, String lyDo, LocalDateTime thoiDiem);

  Optional<Enrollment> timTheoMa(Long maXepLop);
  long demDanhSachLop(Long maLop, LocalDate ngay);

  List<ClassStudentResponse> danhSachLop(Long maLop, LocalDate ngay, int kichThuoc, long viTriBatDau);

  List<Enrollment> lichSu(Long maHocSinh);

long demHocSinh(String tuKhoa, Long year);

  List<EnrollmentStudentResponse> timHocSinh(String tuKhoa, Long year, int kichThuoc, long viTriBatDau);

  long demYeuCau(String tuKhoa, Long year);

  List<EnrollmentRequestResponse> timYeuCau(
      String tuKhoa, Long year, int kichThuoc, long viTriBatDau);
}
