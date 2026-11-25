package com.example.highschool_management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSession {
    Long maPDD;
    Long maLop;
    Long maHK;
    Long maLMH;
    LocalDate ngay;
    String phamVi;
    String maKhung;
    String trangThai;
    Long maTKTao;
}
