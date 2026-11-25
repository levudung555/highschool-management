
package com.example.highschool_management.importer;

import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.StudentGuardianInputRequest;
import com.example.highschool_management.dto.response.ImportErrorResponse;
import com.example.highschool_management.enums.SexEnum;
import com.example.highschool_management.exception.InvalidRequestException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        private final BigDecimal tongDiemDauVao;
        private final Integer thuTuToHop;
        private final String kyHieuToHop;

        public StudentRow(
                int rowNumber,
                CreateStudentRequest request,
                BigDecimal tongDiemDauVao,
                Integer thuTuToHop) {
            this(rowNumber, request, tongDiemDauVao, thuTuToHop, null);
        }

        public StudentRow(
                int rowNumber,
                CreateStudentRequest request,
                BigDecimal tongDiemDauVao,
                Integer thuTuToHop,
                String kyHieuToHop) {
            this.rowNumber = rowNumber;
            this.request = request;
            this.tongDiemDauVao = tongDiemDauVao;
            this.thuTuToHop = thuTuToHop;
            this.kyHieuToHop = kyHieuToHop;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public CreateStudentRequest getRequest() {
            return request;
        }

        public BigDecimal getTongDiemDauVao() {
            return tongDiemDauVao;
        }

        public Integer getThuTuToHop() {
            return thuTuToHop;
        }

        public String getKyHieuToHop() {
            return kyHieuToHop;
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

            Columns columns = findColumns(sheet.getRow(0));
            if (columns.kyHieuToHop() < 0 && columns.thuTuToHop() < 0) {
                throw new InvalidRequestException(
                        "File Excel phải có cột ToHopLuaChon hoặc cột mã/thứ tự tổ hợp");
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
                    StudentRow student = parseStudent(row, columns, i + 1);

                    result.getStudents().add(student);

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
    private StudentRow parseStudent(Row row, Columns columns, int rowNumber) {

        CreateStudentRequest request = new CreateStudentRequest();

        // A - Họ tên
        String hoTen = value(row, columns.hoTen());

        if (hoTen == null) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }

        request.setHoTen(hoTen);

        // B - Ngày sinh
        LocalDate ngaySinh = getLocalDate(cell(row, columns.ngaySinh()));

        if (ngaySinh == null) {
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        }

        request.setNgaySinh(Date.valueOf(ngaySinh));

        // C - Giới tính
        String gioiTinh = value(row, columns.gioiTinh());

        if (gioiTinh == null) {
            throw new IllegalArgumentException("Giới tính không được để trống");
        }

        request.setGioiTinh(parseGender(gioiTinh));

        // D - Địa chỉ
        request.setDiaChi(value(row, columns.diaChi()));

        // E - Số điện thoại
        request.setSdt(value(row, columns.sdt()));

        // F - CCCD
        request.setCmnd(value(row, columns.cmnd()));

        // G - Điểm đầu vào
        BigDecimal tongDiemDauVao = getBigDecimal(cell(row, columns.diemDauVao()));
        String giaTriToHop = value(row, columns.kyHieuToHop());
        Integer thuTuToHop = null;
        String kyHieuToHop = null;
        if (isNumeric(giaTriToHop)) {
            thuTuToHop = getPositiveInteger(cell(row, columns.kyHieuToHop()));
        } else {
            kyHieuToHop = normalizeCombinationCode(giaTriToHop);
        }

        // Các file cũ có thể chỉ có cột tổ hợp dạng thứ tự hoặc mã tổ hợp.
        if (thuTuToHop == null && kyHieuToHop == null) {
            String giaTriMaToHop = value(row, columns.thuTuToHop());
            if (isNumeric(giaTriMaToHop)) {
                thuTuToHop = getPositiveInteger(cell(row, columns.thuTuToHop()));
            } else if (giaTriMaToHop != null && !giaTriMaToHop.startsWith("=")) {
                kyHieuToHop = normalizeCombinationCode(giaTriMaToHop);
            }
        }
        if (thuTuToHop == null && kyHieuToHop == null) {
            throw new IllegalArgumentException(
                    "Mã tổ hợp không được để trống; nhập mã như A00 ở cột ToHopLuaChon "
                            + "hoặc mã số ở cột MaToHop");
        }

        String tenPhuHuynh = value(row, columns.tenPhuHuynh());
        String cmndPhuHuynh = value(row, columns.cmndPhuHuynh());
        String sdtPhuHuynh = value(row, columns.sdtPhuHuynh());
        if (tenPhuHuynh != null || cmndPhuHuynh != null || sdtPhuHuynh != null) {
            if (tenPhuHuynh == null) {
                throw new IllegalArgumentException(
                        "Họ tên phụ huynh không được để trống khi có thông tin phụ huynh");
            }
            StudentGuardianInputRequest guardian = new StudentGuardianInputRequest();
            guardian.setHoTen(tenPhuHuynh);
            guardian.setCmnd(cmndPhuHuynh);
            guardian.setSdt(sdtPhuHuynh);
            guardian.setMoiQuanHe("PHU_HUYNH");
            guardian.setLienHeChinh(true);
            request.setNguoiGiamHo(guardian);
        }

        return new StudentRow(rowNumber, request, tongDiemDauVao, thuTuToHop, kyHieuToHop);
    }

    private Columns findColumns(Row header) {
        Map<String, Integer> headers = new HashMap<>();
        if (header != null) {
            for (Cell cell : header) {
                String label = new DataFormatter().formatCellValue(cell).trim();
                if (!label.isEmpty()) {
                    headers.put(normalizeHeader(label), cell.getColumnIndex());
                }
            }
        }

        return new Columns(
                column(headers, 0, "HOTEN", "HOTENHOCSINH", "TENHOCSINH", "HOVATEN"),
                column(headers, 1, "NGAYSINH"),
                column(headers, 2, "GIOITINH", "PHAI"),
                column(headers, 3, "DIACHI"),
                column(headers, 4, "SODIENTHOAI", "SDT", "DIENTHOAI"),
                column(headers, 5, "CCCD", "CMND", "CMMD", "SOCCCD"),
                column(headers, 6, "DIEMDAUVAO", "DIEMTHI"),
                column(headers, -1, "TOHOPLUACHON", "KYHIEUTOHOP", "MATOHOPKYHIEU", "TOHOPCODE"),
                column(headers, -1, "TOHOP", "TOHOPMON", "TOHOPTHI", "MATOHOP", "MASO", "NHOMTOHOP", "TOHOPTUYENSINH"),
                column(headers, -1, "TENPHUHUYNH", "HOTENPHUHUYNH", "TENNGUOIGIAMHO"),
                column(headers, -1, "CMNDPHUHUYNH", "CCCDPHUHUYNH", "CMNDNGUOIGIAMHO", "CCCDNGUOIGIAMHO"),
                column(headers, -1, "SDTPHUHUYNH", "SODIENTHOAIPHUHUYNH", "DIENTHOAIPHUHUYNH", "SDTNGUOIGIAMHO", "SODIENTHOAINGUOIGIAMHO")
        );
    }

    private int column(Map<String, Integer> headers, int fallback, String... aliases) {
        for (String alias : aliases) {
            Integer index = headers.get(alias);
            if (index != null) {
                return index;
            }
        }
        return fallback;
    }

    private String normalizeHeader(String value) {
        return removeAccent(value).replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeCombinationCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            new BigDecimal(value.trim().replace(",", "."));
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String value(Row row, int index) {
        return index < 0 ? null : ExcelHelper.getCellValue(row, index);
    }

    private Cell cell(Row row, int index) {
        return index < 0 || row == null ? null : row.getCell(index);
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

    private Integer getPositiveInteger(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        String value = new DataFormatter().formatCellValue(cell).trim().replace(",", ".");
        if (value.isEmpty()) {
            return null;
        }

        try {
            int code = new BigDecimal(value).intValueExact();
            if (code <= 0) {
                throw new IllegalArgumentException("Mã tổ hợp phải là số nguyên dương");
            }
            return code;
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Mã tổ hợp phải là số nguyên dương (1, 2, 3...): " + value);
        }
    }

    /*
     * Kiểm tra một dòng có hoàn toàn trống hay không.
     */
    private boolean isEmptyRow(Row row) {

        if (row == null) {
            return true;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            if (ExcelHelper.getCellValue(row, i) != null) {
                return false;
            }
        }

        return true;
    }

    private record Columns(
            int hoTen,
            int ngaySinh,
            int gioiTinh,
            int diaChi,
            int sdt,
            int cmnd,
            int diemDauVao,
            int kyHieuToHop,
            int thuTuToHop,
            int tenPhuHuynh,
            int cmndPhuHuynh,
            int sdtPhuHuynh
    ) {}

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
