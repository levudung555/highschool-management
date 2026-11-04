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
import java.sql.Date;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  @Mock private NguoiDungHienTai nguoiDung;
  @Mock private StudentDao studentDao;
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
    request.setEmail("vana@example.com");
  }

  @Test
  @DisplayName("createStudent: Tạo học sinh thành công khi thông tin hợp lệ")
  void createStudent_Success() {
    // Given
    when(studentDao.getLastMaSoHS()).thenReturn("HS0005");
    when(studentDao.existsEmail("vana@example.com")).thenReturn(false);
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
    assertEquals("vana@example.com", response.getEmail());

    verify(nguoiDung).yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    verify(studentDao).insertStudent(any(Student.class));
  }

  @Test
  @DisplayName("createStudent: Ném BusinessException khi email đã tồn tại")
  void createStudent_ThrowsException_WhenEmailExists() {
    // Given
    when(studentDao.getLastMaSoHS()).thenReturn("HS0001");
    when(studentDao.existsEmail("vana@example.com")).thenReturn(true);

    // When & Then
    BusinessException exception =
        assertThrows(BusinessException.class, () -> studentService.createStudent(request));

    assertEquals("Email đã tồn tại", exception.getMessage());
    verify(studentDao, never()).insertStudent(any());
  }
}
