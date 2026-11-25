package com.example.highschool_management.service;

import com.example.highschool_management.dao.*;
import com.example.highschool_management.dto.request.CreateTeachingAssignmentRequest;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.mapper.TeachingAssignmentMapper;
import com.example.highschool_management.model.*;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.impl.TeachingAssignmentServiceImpl;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeachingAssignmentServiceTest {
    private final TeachingAssignmentDao dao = mock(TeachingAssignmentDao.class);
    private final SchoolClassDao classes = mock(SchoolClassDao.class);
    private final ClassSubjectDao subjects = mock(ClassSubjectDao.class);
    private final TeacherDao teachers = mock(TeacherDao.class);
    private final NguoiDungHienTai actor = mock(NguoiDungHienTai.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-16T03:20:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final TeachingAssignmentServiceImpl service = new TeachingAssignmentServiceImpl(
            dao, classes, actor, new TeachingAssignmentMapper(), teachers, subjects, clock, mock(EnrollmentDao.class));
    private final CreateTeachingAssignmentRequest request = new CreateTeachingAssignmentRequest();
    private final Teacher teacher = new Teacher();

    @BeforeEach
    void setup() {
        request.setMaLopMonHoc(5L);
        request.setMaGiaoVien(1L);
        request.setTuNgay(LocalDate.of(2026, 11, 16));
        ClassSubject subject = new ClassSubject();
        subject.setMaLop(2L);
        subject.setTrangThaiLop("HOAT_DONG");
        subject.setMaNamHocCuaLop(2L);
        subject.setMaNamHocCuaHocKy(2L);
        subject.setNgayBatDauHocKy(LocalDate.of(2026, 9, 1));
        subject.setNgayKetThucHocKy(LocalDate.of(2027, 1, 15));
        when(subjects.timTheoMaDeCapNhat(5L)).thenReturn(Optional.of(subject));
        when(classes.timTheoMaDeCapNhat(2L)).thenReturn(Optional.of(new SchoolClass()));
        teacher.setTrangThai("DANG_CONG_TAC");
        when(teachers.findByMaGV(1L)).thenReturn(Optional.of(teacher));
        when(actor.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU")).thenReturn(7L);
    }

    @Test
    void nullableEndUsesSemesterBoundaryAndAuditsRealTime() {
        when(dao.tao(any())).thenReturn(10L);
        when(dao.timTheoMa(10L)).thenReturn(Optional.of(new TeachingAssignment()));
        service.tao(request);
        verify(dao).coPhanCongTrungThoiGian(5L, request.getTuNgay(), LocalDate.of(2027, 1, 15));
        var captured = ArgumentCaptor.forClass(TeachingAssignment.class);
        verify(dao).tao(captured.capture());
        var saved = captured.getValue();
        assertNull(saved.getDenNgay());
        assertEquals("DA_DUYET", saved.getTrangThaiDuyet());
        assertEquals(10L, saved.getMaPhanCong());
        assertEquals(LocalDateTime.of(2026, 11, 16, 10, 20), saved.getDuyetLuc());
        verify(dao).ghiNhatKyTao(7L, saved, saved.getDuyetLuc());
    }

    @Test
    void inactiveTeacherCannotBeAssigned() {
        teacher.setTrangThai("NGUNG_CONG_TAC");
        assertThrows(BusinessException.class, () -> service.tao(request));
        verify(dao, never()).tao(any());
    }

    @Test
    void overlapReturnsConflictWithoutInserting() {
        when(dao.coPhanCongTrungThoiGian(anyLong(), any(), any())).thenReturn(true);
        var error = assertThrows(ResponseStatusException.class, () -> service.tao(request));
        assertEquals(409, error.getStatusCode().value());
        verify(dao, never()).tao(any());
    }
}
