package com.example.highschool_management.service;

import com.example.highschool_management.dao.GuardianDao;
import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.dao.StudentGuardianDao;
import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.StudentGuardianInputRequest;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.enums.SexEnum;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.mapper.StudentMapper;
import com.example.highschool_management.model.Guardian;
import com.example.highschool_management.model.Student;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentServiceTest {

    @Mock private NguoiDungHienTai nguoiDung;
    @Mock private StudentDao studentDao;
    @Mock private StudentMapper studentMapper;
    @Mock private GuardianDao guardianDao;
    @Mock private StudentGuardianDao studentGuardianDao;

    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl(nguoiDung, studentDao, studentMapper, guardianDao, studentGuardianDao);
        when(nguoiDung.yeuCauVaiTro(any(String[].class))).thenReturn(1L);
    }

    @Test
    @DisplayName("Tạo học sinh mới không kèm người giám hộ")
    void testCreateStudent_KhongGiamHo() {
        CreateStudentRequest req = new CreateStudentRequest();
        req.setHoTen("Nguyen Van A");
        req.setNgaySinh(Date.valueOf("2008-01-01"));
        req.setGioiTinh(SexEnum.NAM);
        req.setSdt("0901234567");
        req.setEmail("an@gmail.com");

        Student st = new Student();
        st.setMaHS(10L);
        st.setHoTen("Nguyen Van A");

        when(studentMapper.toEntity(req)).thenReturn(st);
        when(studentDao.existsEmail("an@gmail.com")).thenReturn(false);
        when(studentDao.existsSoDienThoai("0901234567")).thenReturn(false);
        when(studentDao.insertStudent(any())).thenReturn(st);

        StudentResponse expectedRes = new StudentResponse();
        expectedRes.setMaHS(10L);
        expectedRes.setHoTen("Nguyen Van A");
        when(studentMapper.toResponse(st)).thenReturn(expectedRes);

        StudentResponse res = studentService.createStudent(req);
        assertNotNull(res);
        assertEquals(10L, res.getMaHS());
        verify(studentGuardianDao, never()).link(any());
    }

    @Test
    @DisplayName("Tạo học sinh kèm người giám hộ đã tồn tại")
    void testCreateStudent_KemGiamHoCoSan() {
        CreateStudentRequest req = new CreateStudentRequest();
        req.setHoTen("Nguyen Van B");
        req.setNgaySinh(Date.valueOf("2008-01-01"));
        req.setGioiTinh(SexEnum.NAM);

        StudentGuardianInputRequest gReq = StudentGuardianInputRequest.builder()
                .maNGH(5L)
                .moiQuanHe("Bố")
                .lienHeChinh(true)
                .build();
        req.setNguoiGiamHo(List.of(gReq));

        Student st = new Student();
        st.setMaHS(11L);
        when(studentMapper.toEntity(req)).thenReturn(st);
        when(studentDao.insertStudent(any())).thenReturn(st);

        Guardian g = new Guardian();
        g.setMaNGH(5L);
        when(guardianDao.findByMaNGH(5L)).thenReturn(Optional.of(g));

        studentService.createStudent(req);

        verify(guardianDao, never()).insertGuardian(any());
        verify(studentGuardianDao, times(1)).link(argThat(sg ->
                sg.getMaHS().equals(11L) && sg.getMaNGH().equals(5L) && sg.getMoiQuanHe().equals("Bố") && Boolean.TRUE.equals(sg.getLienHeChinh())
        ));
    }

    @Test
    @DisplayName("Tạo học sinh kèm người giám hộ mới")
    void testCreateStudent_KemGiamHoMoi() {
        CreateStudentRequest req = new CreateStudentRequest();
        req.setHoTen("Nguyen Van C");
        req.setNgaySinh(Date.valueOf("2008-01-01"));
        req.setGioiTinh(SexEnum.NAM);

        StudentGuardianInputRequest gReq = StudentGuardianInputRequest.builder()
                .hoTen("Nguyen Van Bo")
                .sdt("0987654321")
                .email("bo@gmail.com")
                .moiQuanHe("Bố")
                .lienHeChinh(true)
                .build();
        req.setNguoiGiamHo(List.of(gReq));

        Student st = new Student();
        st.setMaHS(12L);
        when(studentMapper.toEntity(req)).thenReturn(st);
        when(studentDao.insertStudent(any())).thenReturn(st);

        when(guardianDao.existsSoDienThoai("0987654321")).thenReturn(false);
        when(guardianDao.existsEmail("bo@gmail.com")).thenReturn(false);

        Guardian insertedG = new Guardian();
        insertedG.setMaNGH(100L);
        insertedG.setHoTen("Nguyen Van Bo");
        when(guardianDao.insertGuardian(any())).thenReturn(insertedG);

        studentService.createStudent(req);

        verify(guardianDao, times(1)).insertGuardian(any());
        verify(studentGuardianDao, times(1)).link(argThat(sg ->
                sg.getMaHS().equals(12L) && sg.getMaNGH().equals(100L)
        ));
    }

    @Test
    @DisplayName("Báo lỗi nếu chọn nhiều hơn 1 người liên hệ chính khi tạo học sinh")
    void testCreateStudent_NhieuHon1LienHeChinh_BaoLoi() {
        CreateStudentRequest req = new CreateStudentRequest();
        req.setHoTen("Nguyen Van D");
        req.setNgaySinh(Date.valueOf("2008-01-01"));
        req.setGioiTinh(SexEnum.NAM);

        StudentGuardianInputRequest g1 = StudentGuardianInputRequest.builder().maNGH(1L).lienHeChinh(true).build();
        StudentGuardianInputRequest g2 = StudentGuardianInputRequest.builder().maNGH(2L).lienHeChinh(true).build();
        req.setNguoiGiamHo(List.of(g1, g2));

        Student st = new Student();
        when(studentMapper.toEntity(req)).thenReturn(st);
        when(studentDao.insertStudent(any())).thenReturn(st);

        assertThrows(BusinessException.class, () -> studentService.createStudent(req));
    }
}
