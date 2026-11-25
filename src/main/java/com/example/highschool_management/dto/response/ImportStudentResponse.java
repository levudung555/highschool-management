package com.example.highschool_management.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImportStudentResponse {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<ImportErrorResponse> errors;
}