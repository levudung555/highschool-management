package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.ClassCatalogResponse.*;
import com.example.highschool_management.dto.response.Grade10ClassProposalResponse;
import com.example.highschool_management.dto.response.SchoolClassListResponse;
import com.example.highschool_management.dto.response.SubjectCombinationPreferenceSummaryResponse;
import com.example.highschool_management.model.SchoolClass;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SchoolClassDao {
  Optional<SchoolClass> findById(Long id);

  Optional<SchoolClass> timTheoMaDeCapNhat(Long maLop);

  boolean tonTaiNamHoc(Long id);

  boolean tonTaiKhoi(Long id);

  boolean tonTaiLop(String tenLop, Long maNamHoc);

  Long themLop(SchoolClass schoolClass);

  boolean tonTaiLopKhac(Long maLop, String tenLop, Long maNamHoc);

  void capNhat(SchoolClass lop);

  boolean tonTaiDuLieuLienQuan(Long maLop);

  int xoaTheoMa(Long maLop);

  List<NamHoc> danhSachNamHoc();

  List<Khoi> danhSachKhoi();

  long demDanhSach(String tuKhoa, Long maNamHoc, Long maKhoi, String trangThai);

  List<SchoolClassListResponse> timDanhSach(String tuKhoa, Long maNamHoc, Long maKhoi, String trangThai, LocalDate ngay, int kichThuoc, long viTriBatDau);

  List<SubjectCombinationPreferenceSummaryResponse> thongKeNguyenVongTheoNam(Long maNamHoc);

  List<Grade10ClassProposalResponse> deXuatMoLop10(Long maNamHoc, LocalDate homNay, int chiTieuLopMoi);

  Optional<LocalDate> khoaNamHocDeMoLop(Long maNamHoc);

  Optional<SchoolClass> timLopTheoNamVaTen(Long maNamHoc, String tenLop);

  List<SchoolClass> danhSachLop10DeXepDauNam(Long maNamHoc);
}
