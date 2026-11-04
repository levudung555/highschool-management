package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateSchoolClassRequest;
import com.example.highschool_management.dto.request.UpdateSchoolClassRequest;
import com.example.highschool_management.dto.response.ClassCatalogResponse;
import com.example.highschool_management.dto.response.PageResponse;
import com.example.highschool_management.dto.response.SchoolClassListResponse;
import com.example.highschool_management.dto.response.SchoolClassResponse;

public interface SchoolClassService {
  SchoolClassResponse getSchoolClassById(Long id);

  SchoolClassResponse them(CreateSchoolClassRequest request);

  PageResponse<SchoolClassListResponse> timDanhSach(
      String tuKhoa, Long maNamHoc, Long maKhoi, String trangThai, int trang, int kichThuoc);

  ClassCatalogResponse danhMuc();

  SchoolClassResponse capNhat(Long maLop, UpdateSchoolClassRequest request);

  void xoaTheoMa(Long maLop);
}
