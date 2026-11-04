package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.*;
import com.example.highschool_management.dto.response.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public interface EnrollmentService {
  EnrollmentResponse xepLop(@Valid @NotNull EnrollStudentRequest request);

  EnrollmentResponse chuyenLop(@Valid @NotNull TransferStudentRequest request);

  EnrollmentResponse duyet(Long maXepLop);

  EnrollmentResponse tuChoi(Long maXepLop);

  EnrollmentResponse chiTiet(Long maXepLop);

  List<EnrollmentResponse> lichSu(Long maHocSinh);

  PageResponse<ClassStudentResponse> danhSachLop(
      Long maLop, LocalDate ngay, int trang, int kichThuoc);

  PageResponse<EnrollmentStudentResponse> timHocSinh(String tuKhoa, int trang, int kichThuoc);

  PageResponse<EnrollmentRequestResponse> timYeuCau(
      String tuKhoa, String trangThai, int trang, int kichThuoc);
}
