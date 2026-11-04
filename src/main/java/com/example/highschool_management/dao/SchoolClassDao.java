package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.ClassCatalogResponse.*;
import com.example.highschool_management.dto.response.SchoolClassListResponse;
import com.example.highschool_management.model.SchoolClass;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SchoolClassDao {
  Optional<SchoolClass> findById(Long id);

  boolean tonTaiNamHoc(Long id);

  boolean tonTaiKhoi(Long id);

  boolean tonTaiLop(String tenLop, Long maNamHoc);

  Long themLop(SchoolClass schoolClass);

  Optional<SchoolClass> timTheoMaDeCapNhat(Long maLop);

  boolean tonTaiLopKhac(Long maLop, String tenLop, Long maNamHoc);

  void capNhat(SchoolClass lop);

  boolean tonTaiDuLieuLienQuan(Long maLop);

  int xoaTheoMa(Long maLop);

  List<NamHoc> danhSachNamHoc();

  List<Khoi> danhSachKhoi();

  long demDanhSach(String tuKhoa, Long maNamHoc, Long maKhoi, String trangThai);

  List<SchoolClassListResponse> timDanhSach(
      String tuKhoa,
      Long maNamHoc,
      Long maKhoi,
      String trangThai,
      LocalDate ngay,
      int kichThuoc,
      long viTriBatDau);
}
