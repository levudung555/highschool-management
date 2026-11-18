package com.example.highschool_management.dao.jdbc;

// import com.example.highschool_management.dto.response.ClassSubjectOptionResponse;

import com.example.highschool_management.dao.ClassSubjectDao;
import com.example.highschool_management.model.ClassSubject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClassSubjectDaoImpl implements ClassSubjectDao {
    private final JdbcTemplate jdbcTemplate;

    public ClassSubjectDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    @Override
//    public List<ClassSubjectOptionResponse> danhSachLuaChon() {
//        String sql = """
//                SELECT lmh.MaLMH, l.MaLop, l.TenLop, n.MaNH, n.MaSoNH,
//                       mh.TenMon, hk.SoHocKy, hk.NgayBatDau, hk.NgayKetThuc, l.TrangThai
//                FROM lop_mon_hoc lmh
//                JOIN lop_hoc l ON l.MaLop = lmh.MaLop
//                JOIN mon_hoc mh ON mh.MaMH = lmh.MaMH
//                JOIN hoc_ky hk ON hk.MaHK = lmh.MaHK AND hk.MaNH = l.MaNH
//                JOIN nam_hoc n ON n.MaNH = l.MaNH
//                ORDER BY n.NgayBatDau DESC, l.TenLop, hk.SoHocKy, mh.TenMon, lmh.MaLMH
//                """;
//        return jdbcTemplate.query(sql, (rs, rowNum) ->
//                new ClassSubjectOptionResponse(
//                        rs.getLong("MaLMH"), rs.getLong("MaLop"), rs.getString("TenLop"),
//                        rs.getLong("MaNH"), rs.getString("MaSoNH"), rs.getString("TenMon"),
//                        rs.getInt("SoHocKy"), rs.getObject("NgayBatDau", java.time.LocalDate.class),
//                        rs.getObject("NgayKetThuc", java.time.LocalDate.class), rs.getString("TrangThai")));
//    }

    private static final String SELECT_LOP_MON_HOC = """
            SELECT
                lmh.MaLMH,
                lmh.MaLop,
                lmh.MaMH,
                lmh.MaHK,
                l.MaNH AS MaNamHocCuaLop,
                l.TrangThai AS TrangThaiLop,
                hk.MaNH AS MaNamHocCuaHocKy,
                hk.NgayBatDau AS NgayBatDauHocKy,
                hk.NgayKetThuc AS NgayKetThucHocKy
            FROM lop_mon_hoc lmh
            JOIN lop_hoc l ON l.MaLop = lmh.MaLop
            JOIN hoc_ky hk ON hk.MaHK = lmh.MaHK
            """;

    private static final RowMapper<ClassSubject> CLASS_SUBJECT_ROW_MAPPER = (rs, rowNum) -> {
        ClassSubject classSubject = new ClassSubject();
        classSubject.setMaLopMonHoc(rs.getLong("MaLMH"));
        classSubject.setMaLop(rs.getLong("MaLop"));
        classSubject.setMaMonHoc(rs.getLong("MaMH"));
        classSubject.setMaHocKy(rs.getLong("MaHK"));
        classSubject.setMaNamHocCuaLop(rs.getLong("MaNamHocCuaLop"));
        classSubject.setTrangThaiLop(rs.getString("TrangThaiLop"));
        classSubject.setMaNamHocCuaHocKy(rs.getLong("MaNamHocCuaHocKy"));
        classSubject.setNgayBatDauHocKy(rs.getDate("NgayBatDauHocKy").toLocalDate());
        classSubject.setNgayKetThucHocKy(rs.getDate("NgayKetThucHocKy").toLocalDate());
        return classSubject;
    };

    @Override
    public Optional<ClassSubject> timTheoMa(Long maLopMonHoc) {
        String sql = SELECT_LOP_MON_HOC + " WHERE lmh.MaLMH = ?";
        List<ClassSubject> results = jdbcTemplate.query(sql, CLASS_SUBJECT_ROW_MAPPER, maLopMonHoc);
        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }
    }

    @Override
    public Optional<ClassSubject> timTheoMaDeCapNhat(Long maLopMonHoc) {
        String sql = SELECT_LOP_MON_HOC + " WHERE lmh.MaLMH = ? FOR UPDATE";
        List<Long> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("MaLMH"), maLopMonHoc);
        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return timTheoMa(maLopMonHoc);
        }
    }
}
