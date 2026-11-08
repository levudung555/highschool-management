package com.example.highschool_management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Một khoảng học sinh thuộc lớp. Khi chuyển lớp phải tạo bản ghi mới để giữ lịch sử. */
public class Enrollment {
  private Long maXepLop;
  private Long maHocSinh;
  private Long maLop;
  private LocalDate tuNgay;
  // Ngày cuối còn thuộc lớp (bao gồm ngày này); null là chưa kết thúc.
  private LocalDate denNgay;
  private String lyDoKetThuc;
  private Long maTaiKhoanThucHien;
  private LocalDateTime thoiDiemThucHien;

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

  public Long getMaTaiKhoanThucHien() {
    return maTaiKhoanThucHien;
  }

  public void setMaTaiKhoanThucHien(Long maTaiKhoanThucHien) {
    this.maTaiKhoanThucHien = maTaiKhoanThucHien;
  }

  public LocalDateTime getThoiDiemThucHien() {
    return thoiDiemThucHien;
  }

  public void setThoiDiemThucHien(LocalDateTime thoiDiemThucHien) {
    this.thoiDiemThucHien = thoiDiemThucHien;
  }

  private String hoTen;
  public String getHoTen() { return hoTen; }
  public void setHoTen(String hoTen) { this.hoTen = hoTen; }

  private String maSoHocSinh;
  public String getMaSoHocSinh() { return maSoHocSinh; }
  public void setMaSoHocSinh(String maSoHocSinh) { this.maSoHocSinh = maSoHocSinh; }

  private String tenLop;
  public String getTenLop() { return tenLop; }
  public void setTenLop(String tenLop) { this.tenLop = tenLop; }

  private String namHoc;
  public String getNamHoc() { return namHoc; }
  public void setNamHoc(String namHoc) { this.namHoc = namHoc; }

  private String tenNguoiThucHien;
  public String getTenNguoiThucHien() { return tenNguoiThucHien; }
  public void setTenNguoiThucHien(String tenNguoiThucHien) { this.tenNguoiThucHien = tenNguoiThucHien; }
}
