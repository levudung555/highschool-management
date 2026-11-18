package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.AttendancePermissionClassResponse;
import com.example.highschool_management.dto.response.AttendanceStudentResponse;
import com.example.highschool_management.model.AttendanceRecord;
import com.example.highschool_management.model.AttendanceSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceDao {
    /**
     * Lấy mã giáo viên gắn với tài khoản (nếu là tài khoản giáo viên)
     */
    Optional<Long> timMaGiaoVienTheoMaTK(Long maTK);

    /**
     * Kiểm tra giáo viên có phải chủ nhiệm lớp tại ngày chỉ định hay không (TrangThaiDuyet = 'DA_DUYET')
     */
    boolean laChuNhiemLop(Long maGV, Long maLop, LocalDate ngay);

    /**
     * Kiểm tra giáo viên có được phân công giảng dạy môn học / lớp tại ngày chỉ định (TrangThaiDuyet = 'DA_DUYET')
     */
    boolean laGiaoVienBoMon(Long maGV, Long maLop, Long maLMH, LocalDate ngay);

    /**
     * Lấy danh sách lớp được phép điểm danh theo quyền quản trị (ADMIN, BGH, GIAO_VU)
     */
    List<AttendancePermissionClassResponse> layDanhSachLopQuanTri();

    /**
     * Lấy danh sách lớp được phép điểm danh cho giáo viên (gồm lớp chủ nhiệm và lớp bộ môn)
     */
    List<AttendancePermissionClassResponse> layDanhSachLopGiaoVien(Long maGV, LocalDate ngay);

    /**
     * Tìm học kỳ chứa ngày chỉ định (theo năm học của lớp hoặc bao quát)
     */
    Optional<Long> timHocKyTheoNgayVaLop(Long maLop, LocalDate ngay);

    /**
     * Lấy danh sách học sinh có hiệu lực xếp lớp tại ngày chỉ định
     */
    List<AttendanceStudentResponse> layDanhSachHocSinhTheoLopVaNgay(Long maLop, LocalDate ngay);

    /**
     * Tìm phiên điểm danh theo phạm vi (lớp, ngày, phạm vi, mã khung)
     */
    Optional<AttendanceSession> timPhienDiemDanh(Long maLop, LocalDate ngay, String phamVi, String maKhung);

    /**
     * Tìm phiên điểm danh theo ID
     */
    Optional<AttendanceSession> timPhienDiemDanhTheoId(Long maPDD);

    /**
     * Lấy chi tiết điểm danh của một phiên
     */
    List<AttendanceStudentResponse> layChiTietDiemDanhTheoPhien(Long maPDD);

    /**
     * Tạo mới phiên điểm danh trong phien_diem_danh
     */
    Long taoPhienDiemDanh(AttendanceSession phien);

    /**
     * Lưu chi tiết điểm danh học sinh vào diem_danh
     */
    void luuChiTietDiemDanh(List<AttendanceRecord> chiTietList);

    /**
     * Cập nhật chi tiết điểm danh từng học sinh
     */
    void capNhatChiTietDiemDanh(Long maPDD, List<AttendanceRecord> chiTietList);

    /**
     * Lấy các bản ghi điểm danh hiện tại theo maPDD (để lưu log DuLieuTruoc)
     */
    List<AttendanceRecord> layDiemDanhHienTaiTheoPhien(Long maPDD);

    /**
     * Ghi nhật ký hệ thống
     */
    void ghiNhatKy(Long maTK, String hanhDong, String loaiDoiTuong, String maDoiTuong, String duLieuTruocJson, String duLieuSauJson, LocalDateTime thoiDiem);
}
