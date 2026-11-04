package com.example.highschool_management.dto.response;

import com.example.highschool_management.enums.SexEnum;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentResponse {
  Long maHS;
  String maSoHS;
  String hoTen;
  LocalDate ngaySinh;
  SexEnum gioiTinh;
  String diaChi;
  String sdt;
  String email;
  LocalDate ngayTiepNhan;
}
