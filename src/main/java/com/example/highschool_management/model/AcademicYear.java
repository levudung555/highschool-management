package com.example.highschool_management.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcademicYear {
  private Long maNH;
  private String maSoNH;
  private LocalDate ngayBatDau;
  private LocalDate ngayKetThuc;
}
