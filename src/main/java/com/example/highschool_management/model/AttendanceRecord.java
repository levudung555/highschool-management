package com.example.highschool_management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {
    Long maDD;
    Long maPDD;
    Long maXL;
    String trangThai;
    Integer soPhutMuon;
    String ghiChu;
    Long maTKCapNhat;
    LocalDateTime capNhatLuc;
}
