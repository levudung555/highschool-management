package com.example.highschool_management.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
public class Teacher {
    Long maGV;
    String maSoGV;
    String hoTen;
    Date ngaySinh;
    String sdt;
    String email;
    String chuyenMon;
    String trangThai;
}
