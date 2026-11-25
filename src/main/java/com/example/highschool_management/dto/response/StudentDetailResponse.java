package com.example.highschool_management.dto.response;

import com.example.highschool_management.enums.SexEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StudentDetailResponse(
    Long maHS,
    String maSoHS,
    String hoTen,
    LocalDate ngaySinh,
    SexEnum gioiTinh,
    String diaChi,
    String sdt,
    String cmnd,
    LocalDate ngayTiepNhan,
    LocalDateTime ngayCapNhat,
    List<HoSoNhapHoc> hoSoNhapHoc) {

  public record HoSoNhapHoc(
      Long maHoSoNhapHoc,
      Long maNamHoc,
      String namHoc,
      Long maToHop,
      String kyHieuToHop,
      String tenToHop,
      BigDecimal tongDiemDauVao) {}
}
