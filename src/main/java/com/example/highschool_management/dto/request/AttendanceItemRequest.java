package com.example.highschool_management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceItemRequest {
    @NotNull(message = "Mã xếp lớp không được để trống")
    private Long maXL;

    @NotNull(message = "Trạng thái điểm danh không được để trống")
    private String trangThai; // CO_MAT, VANG_CO_PHEP, VANG_KHONG_PHEP, DI_MUON

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String ghiChu;
}
