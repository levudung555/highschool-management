package com.example.highschool_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePermissionClassResponse {
    private Long maLop;
    private String tenLop;
    private Long maNH;
    private String namHoc;
    private Long maKhoi;
    private Integer soKhoi;
    private String vaiTroPhuTrach; // "CHU_NHIEM", "BO_MON", "QUAN_TRI"
    private Long maLMH;
    private String tenMonHoc;
}
