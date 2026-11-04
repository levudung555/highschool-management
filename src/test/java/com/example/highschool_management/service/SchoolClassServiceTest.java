package com.example.highschool_management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dto.request.CreateSchoolClassRequest;
import com.example.highschool_management.dto.response.SchoolClassResponse;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.mapper.SchoolClassMapper;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.impl.SchoolClassServiceImpl;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {

  @Mock private SchoolClassDao schoolClassDao;
  @Spy private SchoolClassMapper schoolClassMapper = new SchoolClassMapper();
  @Mock private NguoiDungHienTai nguoiDung;
  @Mock private Clock clock;

  @InjectMocks private SchoolClassServiceImpl schoolClassService;

  private CreateSchoolClassRequest request;

  @BeforeEach
  void setUp() {
    request = new CreateSchoolClassRequest(1L, 10L, "10A1");
  }

  @Test
  @DisplayName("them: Thêm lớp học thành công khi dữ liệu hợp lệ")
  void them_Success() {
    // Given
    when(schoolClassDao.tonTaiNamHoc(1L)).thenReturn(true);
    when(schoolClassDao.tonTaiKhoi(10L)).thenReturn(true);
    when(schoolClassDao.tonTaiLop("10A1", 1L)).thenReturn(false);
    when(schoolClassDao.themLop(any(SchoolClass.class))).thenReturn(100L);

    // When
    SchoolClassResponse response = schoolClassService.them(request);

    // Then
    assertNotNull(response);
    assertEquals(100L, response.maLop());
    assertEquals(1L, response.maNamHoc());
    assertEquals(10L, response.maKhoi());
    assertEquals("10A1", response.tenLop());
    assertEquals("HOAT_DONG", response.trangThai());

    verify(nguoiDung).yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    verify(schoolClassDao).themLop(any(SchoolClass.class));
  }

  @Test
  @DisplayName("them: Ném BusinessException khi tên lớp đã tồn tại trong năm học")
  void them_ThrowsException_WhenTenLopDaTonTai() {
    // Given
    when(schoolClassDao.tonTaiNamHoc(1L)).thenReturn(true);
    when(schoolClassDao.tonTaiKhoi(10L)).thenReturn(true);
    when(schoolClassDao.tonTaiLop("10A1", 1L)).thenReturn(true);

    // When & Then
    BusinessException exception =
        assertThrows(BusinessException.class, () -> schoolClassService.them(request));

    assertTrue(exception.getMessage().contains("Lớp học đã tồn tại trong năm học"));
    verify(schoolClassDao, never()).themLop(any());
  }
}
