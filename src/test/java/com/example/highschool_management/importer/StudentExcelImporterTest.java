package com.example.highschool_management.importer;

import static org.junit.jupiter.api.Assertions.*;

import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.enums.SexEnum;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class StudentExcelImporterTest {

  private StudentExcelImporter importer;

  @BeforeEach
  void setUp() {
    importer = new StudentExcelImporter();
  }

  @Test
  @DisplayName("importStudents: Đọc đúng file Excel với đầy đủ các cột")
  void importStudents_ValidExcel() throws IOException {
    byte[] excelBytes;
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("DanhSachThiVao10");

      // Row 0: Header
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Họ và tên");
      header.createCell(1).setCellValue("Ngày sinh");
      header.createCell(2).setCellValue("Giới tính");
      header.createCell(3).setCellValue("Địa chỉ");
      header.createCell(4).setCellValue("Số điện thoại");
      header.createCell(5).setCellValue("CCCD");
      header.createCell(6).setCellValue("Điểm đầu vào");

      // Row 1: Student 1
      Row row1 = sheet.createRow(1);
      row1.createCell(0).setCellValue("Nguyễn Văn An");
      row1.createCell(1).setCellValue("20/05/2008");
      row1.createCell(2).setCellValue("Nam");
      row1.createCell(3).setCellValue("Hà Nội");
      row1.createCell(4).setCellValue("0912345678");
      row1.createCell(5).setCellValue("001208001234");
      row1.createCell(6).setCellValue(8.5);

      // Row 2: Student 2
      Row row2 = sheet.createRow(2);
      row2.createCell(0).setCellValue("Trần Thị Bình");
      row2.createCell(1).setCellValue("2008-08-15");
      row2.createCell(2).setCellValue("Nữ");
      row2.createCell(3).setCellValue("Đà Nẵng");
      row2.createCell(4).setCellValue("0987654321");
      row2.createCell(5).setCellValue("001208005678");
      row2.createCell(6).setCellValue("9,25");

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      workbook.write(out);
      excelBytes = out.toByteArray();
    }

    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "danh_sach.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelBytes);

    StudentExcelImporter.ImportResult result = importer.importStudents(file);

    assertNotNull(result);
    assertEquals(2, result.getTotalRows());
    assertEquals(2, result.getStudents().size());
    assertTrue(result.getErrors().isEmpty());

    CreateStudentRequest req1 = result.getStudents().get(0).getRequest();
    assertEquals("Nguyễn Văn An", req1.getHoTen());
    assertEquals("2008-05-20", req1.getNgaySinh().toString());
    assertEquals(SexEnum.NAM, req1.getGioiTinh());
    assertEquals("Hà Nội", req1.getDiaChi());
    assertEquals("0912345678", req1.getSdt());
    assertEquals("001208001234", req1.getCmnd());
    assertEquals(new BigDecimal("8.5"), req1.getDiemDauVao());

    CreateStudentRequest req2 = result.getStudents().get(1).getRequest();
    assertEquals("Trần Thị Bình", req2.getHoTen());
    assertEquals("2008-08-15", req2.getNgaySinh().toString());
    assertEquals(SexEnum.NU, req2.getGioiTinh());
    assertEquals("Đà Nẵng", req2.getDiaChi());
    assertEquals("0987654321", req2.getSdt());
    assertEquals("001208005678", req2.getCmnd());
    assertEquals(new BigDecimal("9.25"), req2.getDiemDauVao());
  }

  @Test
  @DisplayName("importStudents: Báo lỗi khi file rỗng")
  void importStudents_EmptyFile() {
    MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.xlsx", "text/plain", new byte[0]);
    assertThrows(IllegalArgumentException.class, () -> importer.importStudents(emptyFile));
  }
}
