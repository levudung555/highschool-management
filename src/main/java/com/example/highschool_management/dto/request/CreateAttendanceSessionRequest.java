package com.example.highschool_management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttendanceSessionRequest {
    @NotNull(message = "Mã lớp không được để trống")
    private Long maLop;

    private Long maHK;

    private Long maLMH;

    @NotNull(message = "Ngày điểm danh không được để trống")
    private LocalDate ngay;

    private String phamVi; // BUOI, TIET, NGAY (mặc định BUOI nếu không truyền)

    @NotNull(message = "Mã khung (buổi/tiết) không được để trống")
    private String maKhung; // SANG, CHIEU, TIET_1...

    @NotEmpty(message = "Danh sách điểm danh không được để trống")
    @Valid
    private List<AttendanceItemRequest> chiTiet;
}
