package com.example.highschool_management.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TeacherResponse {
    Long maGV;
    String maSoGV;
    String hoTen;
    LocalDate ngaySinh;
    String sdt;
    String email;
    String chuyenMon;
    String trangThai;
}
