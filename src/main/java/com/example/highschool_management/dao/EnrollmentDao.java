package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.ClassStudentResponse;
import com.example.highschool_management.dto.response.EnrollmentRequestResponse;
import com.example.highschool_management.dto.response.EnrollmentStudentResponse;
import com.example.highschool_management.model.Enrollment;
import java.time.*;
import java.util.*;

public interface EnrollmentDao {
  record NamHoc(LocalDate batDau, LocalDate ketThuc) {}

  boolean khoaHocSinh(Long maHocSinh);

  NamHoc namHoc(Long maNamHoc);

  Optional<Enrollment> timTheoMa(Long maXepLop);

  List<Enrollment> lichSu(Long maHocSinh);

  boolean trungThoiGian(
      Long maHocSinh, LocalDate tuNgay, LocalDate denNgay, Long boQuaMaXepLop, Long boQuaMaNguon);

  long taoChoDuyet(Enrollment e);

  void luuYeuCauChuyen(Long maXepLopDich, Long maXepLopNguon, String lyDo);

  void ketThuc(Long maXepLop, LocalDate denNgay, String lyDo);

  void quyetDinh(Long maXepLop, String trangThai, long nguoiDuyet, LocalDateTime lucDuyet);

  long demSiSo(Long maLop, LocalDate ngay);

  long demDanhSachLop(Long maLop, LocalDate ngay);

  List<ClassStudentResponse> danhSachLop(Long maLop, LocalDate ngay, int size, long offset);

  long demHocSinh(String tuKhoa);

  List<EnrollmentStudentResponse> timHocSinh(String tuKhoa, int kichThuoc, long viTriBatDau);

  long demYeuCau(String tuKhoa, String trangThai);

  List<EnrollmentRequestResponse> timYeuCau(
      String tuKhoa, String trangThai, int kichThuoc, long viTriBatDau);
}
