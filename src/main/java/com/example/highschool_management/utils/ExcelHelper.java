package com.example.highschool_management.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

public class ExcelHelper {

    private static final DataFormatter FORMATTER = new DataFormatter();

    public static String getCellValue(Row row, int columnIndex) {
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(
                columnIndex,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        if (cell == null) {
            return null;
        }

        String cellValue = FORMATTER.formatCellValue(cell).trim();

        return cellValue.isEmpty() ? null : cellValue;
    }
}