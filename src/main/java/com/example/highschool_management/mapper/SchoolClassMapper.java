package com.example.highschool_management.mapper;

import com.example.highschool_management.dto.response.SchoolClassResponse;
import com.example.highschool_management.model.SchoolClass;
import org.springframework.stereotype.Component;

@Component
public class SchoolClassMapper {

  public SchoolClassResponse sangResponse(SchoolClass lop) {
    return new SchoolClassResponse(
        lop.getMaLop(), lop.getMaNamHoc(), lop.getMaKhoi(), lop.getTenLop(), lop.getTrangThai());
  }
}
