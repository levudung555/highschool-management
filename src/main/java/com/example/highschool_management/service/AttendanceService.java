package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateAttendanceSessionRequest;
import com.example.highschool_management.dto.request.UpdateAttendanceSessionRequest;
import com.example.highschool_management.dto.response.AttendancePermissionClassResponse;
import com.example.highschool_management.dto.response.AttendanceSessionResponse;
import com.example.highschool_management.dto.response.AttendanceStudentResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    List<AttendancePermissionClassResponse> layDanhSachLopDuocDiemDanh(LocalDate ngay);


    AttendanceSessionResponse layThongTinDiemDanh(Long maLop, LocalDate ngay, String phamVi, String maKhung, Long maLMH);


    AttendanceSessionResponse taoPhienDiemDanh(CreateAttendanceSessionRequest request);


    AttendanceSessionResponse capNhatPhienDiemDanh(Long maPDD, UpdateAttendanceSessionRequest request);
}
