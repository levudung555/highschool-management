package com.example.highschool_management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

@lombok.Getter
@lombok.Setter
public class Enrollment {
  private Long maHoSoNhapHoc;

  private Long maXepLop;
  private Long maHocSinh;
  private Long maLop;
  private LocalDate tuNgay;
  private LocalDate denNgay;
  private String lyDoKetThuc;
  private Long maTKNguoiThucHien;
  private LocalDateTime thoiDiemThucHien;
  private Long maXepLopNguon;
  private String lyDoChuyen;

  public Long getMaXepLop() {
    return maXepLop;
  }

  public void setMaXepLop(Long maXepLop) {
    this.maXepLop = maXepLop;
  }

  public Long getMaHocSinh() {
    return maHocSinh;
  }

  public void setMaHocSinh(Long maHocSinh) {
    this.maHocSinh = maHocSinh;
  }

  public Long getMaLop() {
    return maLop;
  }

  public void setMaLop(Long maLop) {
    this.maLop = maLop;
  }

  public LocalDate getTuNgay() {
    return tuNgay;
  }

  public void setTuNgay(LocalDate tuNgay) {
    this.tuNgay = tuNgay;
  }

  public LocalDate getDenNgay() {
    return denNgay;
  }

  public void setDenNgay(LocalDate denNgay) {
    this.denNgay = denNgay;
  }

  public String getLyDoKetThuc() {
    return lyDoKetThuc;
  }

  public void setLyDoKetThuc(String lyDoKetThuc) {
    this.lyDoKetThuc = lyDoKetThuc;
  }

  public Long getMaTKNguoiThucHien() {
    return maTKNguoiThucHien;
  }

  public void setMaTKNguoiThucHien(Long maTKNguoiThucHien) {
    this.maTKNguoiThucHien = maTKNguoiThucHien;
  }

  public LocalDateTime getThoiDiemThucHien() {
    return thoiDiemThucHien;
  }

  public void setThoiDiemThucHien(LocalDateTime thoiDiemThucHien) {
    this.thoiDiemThucHien = thoiDiemThucHien;
  }

  public Long getMaXepLopNguon() {
    return maXepLopNguon;
  }

  public void setMaXepLopNguon(Long maXepLopNguon) {
    this.maXepLopNguon = maXepLopNguon;
  }

  public String getLyDoChuyen() {
    return lyDoChuyen;
  }

  public void setLyDoChuyen(String lyDoChuyen) {
    this.lyDoChuyen = lyDoChuyen;
  }
}
