package com.example.highschool_management.model;

import com.example.highschool_management.enums.SexEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.sql.Date;
import java.time.LocalDate;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
public class Student {
    Long maHS;
    String maSoHS;
    String hoTen;
    Date ngaySinh;
    SexEnum gioiTinh;
    String diaChi;
    String sdt;
    String email;
    Date ngayTiepNhan;
}
