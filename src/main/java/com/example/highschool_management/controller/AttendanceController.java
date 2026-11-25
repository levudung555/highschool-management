package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.CreateAttendanceSessionRequest;
import com.example.highschool_management.dto.request.UpdateAttendanceSessionRequest;
import com.example.highschool_management.dto.response.AttendancePermissionClassResponse;
import com.example.highschool_management.dto.response.AttendanceSessionResponse;
import com.example.highschool_management.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Lấy danh sách lớp người dùng hiện tại được phép điểm danh (giáo viên hoặc quản trị)
     */
    @GetMapping("/classes")
    public List<AttendancePermissionClassResponse> layDanhSachLop(
            @RequestParam(name = "ngay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {
        return attendanceService.layDanhSachLopDuocDiemDanh(ngay);
    }

    /**
     * Lấy thông tin điểm danh của lớp theo ngày, buổi/tiết.
     * Nếu đã tạo phiên -> trả về dữ liệu đã lưu.
     * Nếu chưa tạo -> trả về danh sách học sinh xếp lớp có hiệu lực tại ngày đó, trạng thái mặc định CO_MAT.
     */
    @GetMapping("/session")
    public AttendanceSessionResponse layThongTinDiemDanh(
            @RequestParam("maLop") Long maLop,
            @RequestParam(name = "ngay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(name = "phamVi", defaultValue = "BUOI") String phamVi,
            @RequestParam(name = "maKhung", defaultValue = "SANG") String maKhung,
            @RequestParam(name = "maLMH", required = false) Long maLMH) {
        return attendanceService.layThongTinDiemDanh(maLop, ngay, phamVi, maKhung, maLMH);
    }

    /**
     * Tạo mới phiên điểm danh
     */
    @PostMapping("/session")
    public ResponseEntity<AttendanceSessionResponse> taoPhienDiemDanh(
            @Valid @RequestBody CreateAttendanceSessionRequest request) {
        AttendanceSessionResponse response = attendanceService.taoPhienDiemDanh(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật phiên điểm danh đã có
     */
    @PutMapping("/session/{maPDD}")
    public AttendanceSessionResponse capNhatPhienDiemDanh(
            @PathVariable("maPDD") Long maPDD,
            @Valid @RequestBody UpdateAttendanceSessionRequest request) {
        return attendanceService.capNhatPhienDiemDanh(maPDD, request);
    }
}
