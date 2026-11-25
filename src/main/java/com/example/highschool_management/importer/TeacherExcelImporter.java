package com.example.highschool_management.importer;

import com.example.highschool_management.dto.request.CreateTeacherRequest;
import com.example.highschool_management.dto.response.ImportErrorResponse;
import com.example.highschool_management.utils.ExcelHelper;
import java.io.IOException;
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
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class TeacherExcelImporter {

  public static class ImportResult {
    private int totalRows;
    private final List<TeacherRow> teachers = new ArrayList<>();
    private final List<ImportErrorResponse> errors = new ArrayList<>();

    public int getTotalRows() {
      return totalRows;
    }

    public void setTotalRows(int totalRows) {
      this.totalRows = totalRows;
    }

    public List<TeacherRow> getTeachers() {
      return teachers;
    }

    public List<ImportErrorResponse> getErrors() {
      return errors;
    }
  }

  public record TeacherRow(int rowNumber, CreateTeacherRequest request) {}

  public ImportResult importTeachers(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File Excel không được để trống");
    }

    String filename = file.getOriginalFilename();
    if (filename != null
        && !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")
        && !filename.toLowerCase(Locale.ROOT).endsWith(".xls")) {
      throw new IllegalArgumentException("Chỉ chấp nhận file Excel (.xlsx, .xls)");
    }

    ImportResult result = new ImportResult();
    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getSheet("DanhSachGiaoVien");
      if (sheet == null && workbook.getNumberOfSheets() > 0) {
        sheet = workbook.getSheetAt(0);
      }
      if (sheet == null) {
        throw new IllegalArgumentException("Không tìm thấy sheet dữ liệu trong file Excel");
      }

      Columns columns = findColumns(sheet.getRow(0));
      if (columns.hoTen() < 0) {
        throw new IllegalArgumentException("File Excel phải có cột Họ tên giáo viên");
      }

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (isEmptyRow(row)) {
          continue;
        }
        result.setTotalRows(result.getTotalRows() + 1);
        try {
          result.getTeachers().add(parseTeacher(row, columns, i + 1));
        } catch (Exception e) {
          addError(result, i + 1, "GENERAL", e.getMessage());
        }
      }
    }
    return result;
  }

  private TeacherRow parseTeacher(Row row, Columns columns, int rowNumber) {
    String hoTen = value(row, columns.hoTen());
    if (hoTen == null) {
      throw new IllegalArgumentException("Họ tên giáo viên không được để trống");
    }

    CreateTeacherRequest request = new CreateTeacherRequest();
    request.setHoTen(hoTen);

    LocalDate ngaySinh = getLocalDate(cell(row, columns.ngaySinh()));
    if (ngaySinh != null) {
      request.setNgaySinh(Date.valueOf(ngaySinh));
    }

    request.setSdt(value(row, columns.sdt()));
    request.setEmail(value(row, columns.email()));
    request.setChuyenMon(value(row, columns.chuyenMon()));

    String trangThai = value(row, columns.trangThai());
    if (trangThai != null) {
      request.setTrangThai(parseStatus(trangThai));
    }

    return new TeacherRow(rowNumber, request);
  }

  private String parseStatus(String value) {
    String normalized = normalizeHeader(value);
    return switch (normalized) {
      case "DANGCONGTAC" -> "DANG_CONG_TAC";
      case "NGUNGCONGTAC", "NGUNG" -> "NGUNG_CONG_TAC";
      default -> throw new IllegalArgumentException(
          "Tình trạng công tác không hợp lệ: " + value
              + " (chỉ nhận DANG_CONG_TAC hoặc NGUNG_CONG_TAC)");
    };
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
        column(headers, 0, "HOTEN", "HOTENGIAOVIEN", "HOVATEN"),
        column(headers, 1, "NGAYSINH", "NGAYSINHGIAOVIEN"),
        column(headers, 2, "SODIENTHOAI", "SDT", "DIENTHOAI", "SDTGIAOVIEN"),
        column(headers, 3, "EMAIL", "EMAILGIAOVIEN"),
        column(headers, 4, "CHUYENMON", "CHUYENNGANH", "MONCHUYEN"),
        column(headers, 5, "TRANGTHAI", "TINHTRANG", "TINHTRANGCONGTAC"));
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
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replaceAll("[^A-Za-z0-9]", "")
        .toUpperCase(Locale.ROOT);
  }

  private String value(Row row, int index) {
    return index < 0 ? null : ExcelHelper.getCellValue(row, index);
  }

  private Cell cell(Row row, int index) {
    return index < 0 || row == null ? null : row.getCell(index);
  }

  private LocalDate getLocalDate(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }
    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
      return cell.getLocalDateTimeCellValue().toLocalDate();
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
        DateTimeFormatter.ISO_LOCAL_DATE);
    for (DateTimeFormatter format : formats) {
      try {
        return LocalDate.parse(value, format);
      } catch (DateTimeParseException ignored) {
        // Thử định dạng ngày khác.
      }
    }
    throw new IllegalArgumentException("Ngày sinh không đúng định dạng: " + value);
  }

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

  private void addError(ImportResult result, int row, String field, String message) {
    ImportErrorResponse error = new ImportErrorResponse();
    error.setRow(row);
    error.setField(field);
    error.setMessage(message == null ? "Dữ liệu không hợp lệ" : message);
    result.getErrors().add(error);
  }

  private record Columns(
      int hoTen,
      int ngaySinh,
      int sdt,
      int email,
      int chuyenMon,
      int trangThai) {}
}
