package com.example.highschool_management.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ClassCatalogResponse(List<NamHoc> namHoc, List<Khoi> khoi, LocalDate homNay) {
  public record NamHoc(Long maNamHoc, String ten, LocalDate batDau, LocalDate ketThuc) {}

  public record Khoi(Long maKhoi, int soKhoi) {}
}
