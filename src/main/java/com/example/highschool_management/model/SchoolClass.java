package com.example.highschool_management.model;

public class SchoolClass {
  private Long maLop;
  private Long maNamHoc;
  private Long maKhoi;
  private String tenLop;
  private String trangThai;

  public SchoolClass() {}

  public Long getMaLop() {
    return maLop;
  }

  public void setMaLop(Long maLop) {
    this.maLop = maLop;
  }

  public Long getMaNamHoc() {
    return maNamHoc;
  }

  public void setMaNamHoc(Long maNamHoc) {
    this.maNamHoc = maNamHoc;
  }

  public Long getMaKhoi() {
    return maKhoi;
  }

  public void setMaKhoi(Long maKhoi) {
    this.maKhoi = maKhoi;
  }

  public String getTenLop() {
    return tenLop;
  }

  public void setTenLop(String tenLop) {
    this.tenLop = tenLop;
  }

  public String getTrangThai() {
    return trangThai;
  }

  public void setTrangThai(String trangThai) {
    this.trangThai = trangThai;
  }
}
