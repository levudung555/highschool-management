package com.example.highschool_management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendanceSessionRequest {
    @NotEmpty(message = "Danh sách chi tiết điểm danh không được để trống")
    @Valid
    private List<AttendanceItemRequest> chiTiet;
}
