package com.example.highschool_management.dao;

import com.example.highschool_management.model.SchoolClass;
import java.util.List;
import java.util.Optional;

public interface SchoolClassDao {
  Optional<SchoolClass> findById(Long id);

  boolean tonTaiNamHoc(Long id);

  boolean tonTaiKhoi(Long id);

  boolean tonTaiLop(String tenLop, Long maNamHoc);

  Long themLop(SchoolClass schoolClass);

  List<SchoolClass> timDanhSach(String tuKhoa, int kichThuoc, long viTriBatDau);

  long demDanhSach(String tuKhoa);

  Optional<SchoolClass> timTheoMaDeCapNhat(Long maLop);

  boolean tonTaiLopKhac(Long maLop, String tenLop, Long maNamHoc);

  void capNhat(SchoolClass lop);

  boolean tonTaiDuLieuLienQuan(Long maLop);

  int xoaTheoMa(Long maLop);
}
