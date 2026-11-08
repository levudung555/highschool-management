package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.ClassStudentResponse;

import com.example.highschool_management.dto.response.EnrollmentStudentResponse;
import com.example.highschool_management.dto.response.SchoolYearPeriodResponse;
import com.example.highschool_management.model.Enrollment;
import java.time.*;
import java.util.*;

public interface EnrollmentDao {
  SchoolYearPeriodResponse namHoc(Long maNamHoc);

  boolean khoaHocSinh(Long maHocSinh);

  // Đọc thông thường, không khóa.
  Optional<Enrollment> timTheoMa(Long maXepLop);

  // Đọc và khóa bản ghi nguồn trước khi chuyển lớp.
  Optional<Enrollment> timTheoMaDeCapNhat(Long maXepLop);

  List<Enrollment> lichSu(Long maHocSinh);

  // Kiểm tra học sinh từng được xếp lớp trong năm học hay chưa.
  //bao gồm bản ghi đã kết thúc, đang có hiệu lực và bắt đầu trong tương lai.
  boolean coXepLopTrongNamHoc(Long maHocSinh, Long maNamHoc);


  boolean trungThoiGian(
          Long maHocSinh,
          Long maNamHoc,
          LocalDate tuNgay,
          LocalDate denNgay,
          Long boQuaMaXepLop
  );
  // Tạo phân lớp trực tiếp, trả về MaXL được sinh.
  long tao(Enrollment enrollment);


  void ketThuc(Long maXepLop, LocalDate denNgay, String lyDo);

  long demSiSo(Long maLop, LocalDate ngay);

  long demDanhSachLop(Long maLop, LocalDate ngay);

  List<ClassStudentResponse> danhSachLop(
          Long maLop,
          LocalDate ngay,
          int kichThuoc,
          long viTriBatDau
  );


  long demHocSinh(String tuKhoa, Long maNamHocChuaXep);

  List<EnrollmentStudentResponse> timHocSinh(
          String tuKhoa,
          Long maNamHocChuaXep,
          int kichThuoc,
          long viTriBatDau
  );


  void ghiNhatKyChuyenLop(
          Long maTaiKhoan,
          Long maXepLopNguon,
          Long maXepLopDich,
          LocalDate ngayChuyen,
          String lyDo,
          LocalDateTime thoiDiem
  );
  long demPhanLop(String tuKhoa, Long maNamHoc);
  List<Enrollment> timPhanLop(String tuKhoa, Long maNamHoc, int kichThuoc, long viTriBatDau);
}
