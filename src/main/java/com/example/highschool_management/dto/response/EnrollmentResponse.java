package com.example.highschool_management.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class EnrollmentResponse {
    private Long maXepLop;
    private Long maHocSinh;
    private Long maLop;
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private String lyDoKetThuc;
    private Long maTaiKhoanThucHien;
    private LocalDateTime thoiDiemThucHien;
    private String hoTen;
    private String maSoHocSinh;
    private String tenLop;
    private String namHoc;
    private String tenNguoiThucHien;

    public EnrollmentResponse() {}

    public EnrollmentResponse(Long maXepLop, Long maHocSinh, Long maLop, LocalDate tuNgay, LocalDate denNgay, String lyDoKetThuc, Long maTaiKhoanThucHien, LocalDateTime thoiDiemThucHien, String hoTen, String maSoHocSinh, String tenLop, String namHoc, String tenNguoiThucHien) {
        this.maXepLop = maXepLop;
        this.maHocSinh = maHocSinh;
        this.maLop = maLop;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.lyDoKetThuc = lyDoKetThuc;
        this.maTaiKhoanThucHien = maTaiKhoanThucHien;
        this.thoiDiemThucHien = thoiDiemThucHien;
        this.hoTen = hoTen;
        this.maSoHocSinh = maSoHocSinh;
        this.tenLop = tenLop;
        this.namHoc = namHoc;
        this.tenNguoiThucHien = tenNguoiThucHien;
    }

    public Long getMaXepLop() { return maXepLop; }
    public void setMaXepLop(Long maXepLop) { this.maXepLop = maXepLop; }

    public Long getMaHocSinh() { return maHocSinh; }
    public void setMaHocSinh(Long maHocSinh) { this.maHocSinh = maHocSinh; }

    public Long getMaLop() { return maLop; }
    public void setMaLop(Long maLop) { this.maLop = maLop; }

    public LocalDate getTuNgay() { return tuNgay; }
    public void setTuNgay(LocalDate tuNgay) { this.tuNgay = tuNgay; }

    public LocalDate getDenNgay() { return denNgay; }
    public void setDenNgay(LocalDate denNgay) { this.denNgay = denNgay; }

    public String getLyDoKetThuc() { return lyDoKetThuc; }
    public void setLyDoKetThuc(String lyDoKetThuc) { this.lyDoKetThuc = lyDoKetThuc; }

    public Long getMaTaiKhoanThucHien() { return maTaiKhoanThucHien; }
    public void setMaTaiKhoanThucHien(Long maTaiKhoanThucHien) { this.maTaiKhoanThucHien = maTaiKhoanThucHien; }

    public LocalDateTime getThoiDiemThucHien() { return thoiDiemThucHien; }
    public void setThoiDiemThucHien(LocalDateTime thoiDiemThucHien) { this.thoiDiemThucHien = thoiDiemThucHien; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getMaSoHocSinh() { return maSoHocSinh; }
    public void setMaSoHocSinh(String maSoHocSinh) { this.maSoHocSinh = maSoHocSinh; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public String getNamHoc() { return namHoc; }
    public void setNamHoc(String namHoc) { this.namHoc = namHoc; }

    public String getTenNguoiThucHien() { return tenNguoiThucHien; }
    public void setTenNguoiThucHien(String tenNguoiThucHien) { this.tenNguoiThucHien = tenNguoiThucHien; }
}
