
package com.example.highschool_management.importer;

import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.response.ImportErrorResponse;
import com.example.highschool_management.enums.SexEnum;
import com.example.highschool_management.utils.ExcelHelper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.Normalizer;

@Component
public class StudentExcelImporter {

    /*
     * Kết quả sau khi đọc và xử lý file Excel.
     */
    public static class ImportResult {
        private int totalRows;
        private final List<StudentRow> students = new ArrayList<>();
        private final List<ImportErrorResponse> errors = new ArrayList<>();

        public int getTotalRows() {
            return totalRows;
        }

        public void setTotalRows(int totalRows) {
            this.totalRows = totalRows;
        }

        public List<StudentRow> getStudents() {
            return students;
        }

        public List<ImportErrorResponse> getErrors() {
            return errors;
        }
    }

    /*
     * Lưu số dòng Excel và dữ liệu học sinh tương ứng.
     */
    public static class StudentRow {
        private final int rowNumber;
        private final CreateStudentRequest request;

        public StudentRow(int rowNumber, CreateStudentRequest request) {
            this.rowNumber = rowNumber;
            this.request = request;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public CreateStudentRequest getRequest() {
            return request;
        }
    }

    /*
     * Đọc file Excel và chuyển dữ liệu thành CreateStudentRequest.
     */
    public ImportResult importStudents(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File Excel không được để trống");
        }

        String filename = file.getOriginalFilename();
        if (filename != null && !filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file Excel (.xlsx, .xls)");
        }

        ImportResult result = new ImportResult();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheet("DanhSachThiVao10");
            if (sheet == null && workbook.getNumberOfSheets() > 0) {
                sheet = workbook.getSheetAt(0);
            }

            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy sheet dữ liệu trong file Excel"
                );
            }

            // Bỏ qua dòng tiêu đề, bắt đầu từ dòng số 2 trong Excel.
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                // Bỏ qua dòng trống.
                if (isEmptyRow(row)) {
                    continue;
                }

                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    CreateStudentRequest request = parseStudent(row);

                    result.getStudents().add(
                            new StudentRow(i + 1, request)
                    );

                } catch (Exception e) {
                    addError(
                            result,
                            i + 1,
                            "GENERAL",
                            e.getMessage()
                    );
                }
            }
        }

        return result;
    }

    /*
     * Đọc dữ liệu của một dòng Excel.
     */
    private CreateStudentRequest parseStudent(Row row) {

        CreateStudentRequest request = new CreateStudentRequest();

        // A - Họ tên
        String hoTen = ExcelHelper.getCellValue(row, 0);

        if (hoTen == null) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }

        request.setHoTen(hoTen);

        // B - Ngày sinh
        LocalDate ngaySinh = getLocalDate(row.getCell(1));

        if (ngaySinh == null) {
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        }

        request.setNgaySinh(Date.valueOf(ngaySinh));

        // C - Giới tính
        String gioiTinh = ExcelHelper.getCellValue(row, 2);

        if (gioiTinh == null) {
            throw new IllegalArgumentException("Giới tính không được để trống");
        }

        request.setGioiTinh(parseGender(gioiTinh));

        // D - Địa chỉ
        request.setDiaChi(ExcelHelper.getCellValue(row, 3));

        // E - Số điện thoại
        request.setSdt(ExcelHelper.getCellValue(row, 4));

        // F - CCCD
        request.setCmnd(ExcelHelper.getCellValue(row, 5));

        // G - Điểm đầu vào
        request.setDiemDauVao(getBigDecimal(row.getCell(6)));

        return request;
    }

    /*
     * Chuyển dữ liệu giới tính trong Excel thành SexEnum.
     */
    private SexEnum parseGender(String value) {

        String normalized = removeAccent(value)
                .trim()
                .toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "nam" -> SexEnum.NAM;
            case "nu" -> SexEnum.NU;
            case "khac" -> SexEnum.KHAC;
            default -> throw new IllegalArgumentException(
                    "Giới tính không hợp lệ: " + value
            );
        };
    }

    /*
     * Chuyển ô Excel thành LocalDate.
     * Hỗ trợ ô ngày tháng của Excel và ngày tháng dạng chuỗi.
     */
    private LocalDate getLocalDate(Cell cell) {

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (Exception ignored) {
            // Bỏ qua lỗi POI và thử đọc dạng chuỗi
        }

        String value = new DataFormatter().formatCellValue(cell).trim();

        if (value.isEmpty()) {
            return null;
        }

        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ISO_LOCAL_DATE
        );

        for (DateTimeFormatter format : formats) {
            try {
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException ignored) {
                // Thử định dạng tiếp theo.
            }
        }

        throw new IllegalArgumentException(
                "Ngày sinh không đúng định dạng: " + value
        );
    }

    /*
     * Chuyển ô Excel thành BigDecimal.
     */
    private BigDecimal getBigDecimal(Cell cell) {

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        String value = new DataFormatter().formatCellValue(cell).trim();

        if (value.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(value.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Điểm đầu vào không hợp lệ: " + value
            );
        }
    }

    /*
     * Kiểm tra một dòng có hoàn toàn trống hay không.
     */
    private boolean isEmptyRow(Row row) {

        if (row == null) {
            return true;
        }

        for (int i = 0; i < 8; i++) {
            if (ExcelHelper.getCellValue(row, i) != null) {
                return false;
            }
        }

        return true;
    }

    /*
     * Loại bỏ dấu tiếng Việt để so sánh giới tính.
     */
    private String removeAccent(String value) {

        String normalized = Normalizer.normalize(
                value,
                Normalizer.Form.NFD
        );

        return normalized.replaceAll("\\p{M}", "");
    }

    /*
     * Ghi nhận lỗi của từng dòng Excel.
     */
    private void addError(
            ImportResult result,
            int row,
            String field,
            String message
    ) {
        ImportErrorResponse error = new ImportErrorResponse();

        error.setRow(row);
        error.setField(field);
        error.setMessage(message);

        result.getErrors().add(error);
    }
}
