package com.example.highschool_management.dao;

import java.math.BigDecimal;
import java.util.List;

public interface AdmissionRecordDao {
  List<ToHopOption> danhSachToHopTheoNamHoc(Long maNamHoc);

  List<HoSoNhapHocDetail> danhSachHoSoTheoHocSinh(Long maHS);

  void taoHoSo(Long maHS, Long maNamHoc, Long maToHop, BigDecimal tongDiemDauVao);

  record ToHopOption(Long maToHop, String kyHieu) {}

  record HoSoNhapHocDetail(
      Long maHoSoNhapHoc,
      Long maNamHoc,
      String namHoc,
      Long maToHop,
      String kyHieuToHop,
      String tenToHop,
      BigDecimal tongDiemDauVao) {}
}
