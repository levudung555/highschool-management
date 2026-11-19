package com.example.highschool_management.service;

import com.example.highschool_management.dao.EnrollmentDao;
import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.mapper.EnrollmentMapper;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.impl.EnrollmentServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassRosterServiceTest {
    private final EnrollmentDao dao = mock(EnrollmentDao.class);
    private final SchoolClassDao classes = mock(SchoolClassDao.class);
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-11-11T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final EnrollmentServiceImpl service = new EnrollmentServiceImpl(
            dao, classes, new EnrollmentMapper(), mock(NguoiDungHienTai.class), clock);

    @Test
    void existingClassUsesPageSizeAndCalculatedOffset() {
        LocalDate day = LocalDate.of(2026, 11, 15);
        when(classes.findById(2L)).thenReturn(Optional.of(new SchoolClass()));
        when(dao.demDanhSachLop(2L, day)).thenReturn(45L);
        when(dao.danhSachLop(2L, day, 20, 20L)).thenReturn(List.of());

        var result = service.danhSachLop(2L, day, 1, 20);

        assertEquals(45L, result.tongSoPhanTu());
        assertEquals(3L, result.tongSoTrang());
        assertEquals(1, result.trang());
        verify(dao).danhSachLop(2L, day, 20, 20L);
    }

    @Test
    void missingClassIsRejectedBeforeQueryingRoster() {
        when(classes.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.danhSachLop(999L, null, 0, 20));
        verifyNoInteractions(dao);
    }

    @Test
    void omittedDateUsesSchoolClockAndFirstPageStartsAtZero() {
        LocalDate today = LocalDate.of(2026, 11, 11);
        when(classes.findById(2L)).thenReturn(Optional.of(new SchoolClass()));
        when(dao.danhSachLop(2L, today, 10, 0L)).thenReturn(List.of());

        service.danhSachLop(2L, null, 0, 10);

        verify(dao).demDanhSachLop(2L, today);
        verify(dao).danhSachLop(2L, today, 10, 0L);
    }
}
