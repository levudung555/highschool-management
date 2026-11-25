package com.example.highschool_management.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Semester {
  private Long maHK;
  private Long maNH;
  private Integer soHocKy;
  private LocalDate ngayBatDau;
  private LocalDate ngayKetThuc;
  private String trangThai;
}
