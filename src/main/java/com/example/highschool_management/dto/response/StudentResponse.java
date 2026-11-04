package com.example.highschool_management.dto.response;

import com.example.highschool_management.enums.SexEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class StudentResponse {
    Long maHS;
    String MaSoHS;
    String HoTen;
    LocalDate ngaySinh;
    SexEnum gioiTinh;
    String diaChi;
    String sdt;
    String Email;
    LocalDate ngayTiepNhan;




}
