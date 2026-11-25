package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.ConfirmGrade10ClassesRequest;
import com.example.highschool_management.dto.request.CreateSchoolClassRequest;
import com.example.highschool_management.dto.request.UpdateSchoolClassRequest;
import com.example.highschool_management.dto.response.*;

import java.util.List;

public interface SchoolClassService {
  ClassCatalogResponse danhMuc();

  PageResponse<SchoolClassListResponse> timDanhSach(
      String tuKhoa, Long maNamHoc, Long maKhoi, String trangThai, int trang, int kichThuoc);

  SchoolClassResponse getSchoolClassById(Long id);

  SchoolClassResponse them(CreateSchoolClassRequest request);

  SchoolClassResponse capNhat(Long maLop, UpdateSchoolClassRequest request);

  void xoaTheoMa(Long maLop);

  List<Grade10ClassProposalResponse> deXuatMoLop10(Long maNamHoc, int chiTieuLopMoi);

  List<Long> xacNhanMoLop10(ConfirmGrade10ClassesRequest request);

}
