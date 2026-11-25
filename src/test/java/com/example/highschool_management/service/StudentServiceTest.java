package com.example.highschool_management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.enums.SexEnum;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.mapper.StudentMapper;
import com.example.highschool_management.model.Student;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.impl.StudentServiceImpl;
import com.example.highschool_management.importer.StudentExcelImporter;
import com.example.highschool_management.dto.response.ImportStudentResponse;
import jakarta.validation.Validator;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  @Mock private NguoiDungHienTai nguoiDung;
  @Mock private StudentDao studentDao;
  @Mock private StudentExcelImporter studentExcelImporter;
  @Mock private Validator validator;
  @Spy private StudentMapper studentMapper = new StudentMapper();

  @InjectMocks private StudentServiceImpl studentService;

  private CreateStudentRequest request;

  @BeforeEach
  void setUp() {
    request = new CreateStudentRequest();
    request.setHoTen("Nguyen Van A");
    request.setNgaySinh(Date.valueOf("2008-01-01"));
    request.setGioiTinh(SexEnum.NAM);
    request.setDiaChi("Ha Noi");
    request.setSdt("0912345678");
  }

  @Test
  @DisplayName("createStudent: Tạo học sinh thành công khi thông tin hợp lệ")
  void createStudent_Success() {
    // Given
    when(studentDao.getLastMaSoHS()).thenReturn("HS0005");
    when(studentDao.existsSoDienThoai("0912345678")).thenReturn(false);
    when(studentDao.insertStudent(any(Student.class)))
        .thenAnswer(
            invocation -> {
              Student s = invocation.getArgument(0);
              s.setMaHS(1L);
              return s;
            });

    // When
    StudentResponse response = studentService.createStudent(request);

    // Then
    assertNotNull(response);
    assertEquals(1L, response.getMaHS());
    assertEquals("HS0006", response.getMaSoHS());
    assertEquals("Nguyen Van A", response.getHoTen());
    assertEquals(SexEnum.NAM, response.getGioiTinh());

    verify(nguoiDung).yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    verify(studentDao).insertStudent(any(Student.class));
  }

  @Test
  @DisplayName("createStudent: Ném BusinessException khi số điện thoại đã tồn tại")
  void createStudent_ThrowsException_WhenPhoneExists() {
    // Given
    when(studentDao.getLastMaSoHS()).thenReturn("HS0001");
    when(studentDao.existsSoDienThoai("0912345678")).thenReturn(true);

    // When & Then
    BusinessException exception =
        assertThrows(BusinessException.class, () -> studentService.createStudent(request));

    assertEquals("Số điện thoại đã tồn tại", exception.getMessage());
    verify(studentDao, never()).insertStudent(any());
  }

  @Test
  @DisplayName("importStudents: Import học sinh từ Excel thành công")
  void importStudents_Success() throws IOException {
    MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1, 2, 3});
    StudentExcelImporter.ImportResult importResult = new StudentExcelImporter.ImportResult();
    importResult.setTotalRows(1);
    importResult.getStudents().add(new StudentExcelImporter.StudentRow(2, request));

    when(studentExcelImporter.importStudents(file)).thenReturn(importResult);
    when(validator.validate(request)).thenReturn(Collections.emptySet());
    when(studentDao.existsSoDienThoai("0912345678")).thenReturn(false);
    when(studentDao.getLastMaSoHS()).thenReturn("HS0010");

    ImportStudentResponse response = studentService.importStudents(file);

    assertNotNull(response);
    assertEquals(1, response.getTotalRows());
    assertEquals(1, response.getSuccessCount());
    assertEquals(0, response.getFailureCount());
    assertTrue(response.getErrors().isEmpty());

    verify(nguoiDung).yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    verify(studentDao).insertStudent(any(Student.class));
  }
}
