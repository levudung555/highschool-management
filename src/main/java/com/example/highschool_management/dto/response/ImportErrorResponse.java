package com.example.highschool_management.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportErrorResponse {
    private int row;
    private String field;
    private String message;
}