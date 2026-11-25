package com.example.highschool_management.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportTeacherResponse {
  private int totalRows;
  private int successCount;
  private int failureCount;
  private List<ImportErrorResponse> errors;
}
