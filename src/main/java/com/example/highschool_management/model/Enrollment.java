package com.example.highschool_management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Enrollment {
  private Long maXepLop;
  private Long maHocSinh;
  private Long maLop;
  private LocalDate tuNgay;
  private LocalDate denNgay;
  private String lyDoKetThuc;
  private String trangThaiDuyet;
  private Long maTaiKhoanDuyet;
  private LocalDateTime duyetLuc;
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

  public String getTrangThaiDuyet() {
    return trangThaiDuyet;
  }

  public void setTrangThaiDuyet(String trangThaiDuyet) {
    this.trangThaiDuyet = trangThaiDuyet;
  }

  public Long getMaTaiKhoanDuyet() {
    return maTaiKhoanDuyet;
  }

  public void setMaTaiKhoanDuyet(Long maTaiKhoanDuyet) {
    this.maTaiKhoanDuyet = maTaiKhoanDuyet;
  }

  public LocalDateTime getDuyetLuc() {
    return duyetLuc;
  }

  public void setDuyetLuc(LocalDateTime duyetLuc) {
    this.duyetLuc = duyetLuc;
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
