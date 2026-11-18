package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateAttendanceSessionRequest;
import com.example.highschool_management.dto.request.UpdateAttendanceSessionRequest;
import com.example.highschool_management.dto.response.AttendancePermissionClassResponse;
import com.example.highschool_management.dto.response.AttendanceSessionResponse;
import com.example.highschool_management.dto.response.AttendanceStudentResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    /**
     * Lấy danh sách lớp người dùng hiện tại được phép điểm danh
     */
    List<AttendancePermissionClassResponse> layDanhSachLopDuocDiemDanh(LocalDate ngay);

    /**
     * Lấy danh sách học sinh theo lớp và ngày (hoặc thông tin phiên đã có nếu đã tạo)
     */
    AttendanceSessionResponse layThongTinDiemDanh(Long maLop, LocalDate ngay, String phamVi, String maKhung, Long maLMH);

    /**
     * Tạo mới phiên điểm danh
     */
    AttendanceSessionResponse taoPhienDiemDanh(CreateAttendanceSessionRequest request);

    /**
     * Cập nhật phiên điểm danh đã có
     */
    AttendanceSessionResponse capNhatPhienDiemDanh(Long maPDD, UpdateAttendanceSessionRequest request);
}
