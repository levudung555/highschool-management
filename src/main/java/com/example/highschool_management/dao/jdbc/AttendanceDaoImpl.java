package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.AttendanceDao;
import com.example.highschool_management.dto.response.AttendancePermissionClassResponse;
import com.example.highschool_management.dto.response.AttendanceStudentResponse;
import com.example.highschool_management.model.AttendanceRecord;
import com.example.highschool_management.model.AttendanceSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AttendanceDaoImpl implements AttendanceDao {
    private final JdbcTemplate jdbc;

    public AttendanceDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<AttendanceSession> SESSION_MAPPER = (rs, rowNum) -> {
        AttendanceSession session = new AttendanceSession();
        session.setMaPDD(rs.getLong("MaPDD"));
        session.setMaLop(rs.getLong("MaLop"));
        session.setMaHK(rs.getLong("MaHK"));
        long maLMH = rs.getLong("MaLMH");
        session.setMaLMH(rs.wasNull() ? null : maLMH);
        session.setNgay(rs.getObject("Ngay", LocalDate.class));
        session.setPhamVi(rs.getString("PhamVi"));
        session.setMaKhung(rs.getString("MaKhung"));
        session.setTrangThai(rs.getString("TrangThai"));
        session.setMaTKTao(rs.getLong("MaTKTao"));
        return session;
    };

    private static final RowMapper<AttendanceRecord> RECORD_MAPPER = (rs, rowNum) -> {
        AttendanceRecord record = new AttendanceRecord();
        record.setMaDD(rs.getLong("MaDD"));
        record.setMaPDD(rs.getLong("MaPDD"));
        record.setMaXL(rs.getLong("MaXL"));
        record.setTrangThai(rs.getString("TrangThai"));
        record.setSoPhutMuon(rs.getInt("SoPhutMuon"));
        record.setGhiChu(rs.getString("GhiChu"));
        record.setMaTKCapNhat(rs.getLong("MaTKCapNhat"));
        record.setCapNhatLuc(rs.getObject("CapNhatLuc", LocalDateTime.class));
        return record;
    };

    @Override
    public Optional<Long> timMaGiaoVienTheoMaTK(Long maTK) {
        String sql = "SELECT MaGV FROM tai_khoan WHERE MaTK = ?";
        try {
            Long maGV = jdbc.queryForObject(sql, Long.class, maTK);
            return Optional.ofNullable(maGV);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean laChuNhiemLop(Long maGV, Long maLop, LocalDate ngay) {
        String sql = """
            SELECT COUNT(*) FROM phan_cong_chu_nhiem
            WHERE MaGV = ? AND MaLop = ?
              AND TrangThaiDuyet = 'DA_DUYET'
              AND TuNgay <= ?
              AND (DenNgay IS NULL OR DenNgay >= ?)
            """;
        Integer count = jdbc.queryForObject(sql, Integer.class, maGV, maLop, ngay, ngay);
        return count != null && count > 0;
    }

    @Override
    public boolean laGiaoVienBoMon(Long maGV, Long maLop, Long maLMH, LocalDate ngay) {
        if (maLMH != null) {
            String sql = """
                SELECT COUNT(*) FROM phan_cong_giang_day pc
                JOIN lop_mon_hoc lmh ON lmh.MaLMH = pc.MaLMH
                WHERE pc.MaGV = ? AND lmh.MaLop = ? AND pc.MaLMH = ?
                  AND pc.TrangThaiDuyet = 'DA_DUYET'
                  AND pc.TuNgay <= ?
                  AND (pc.DenNgay IS NULL OR pc.DenNgay >= ?)
                """;
            Integer count = jdbc.queryForObject(sql, Integer.class, maGV, maLop, maLMH, ngay, ngay);
            return count != null && count > 0;
        } else {
            String sql = """
                SELECT COUNT(*) FROM phan_cong_giang_day pc
                JOIN lop_mon_hoc lmh ON lmh.MaLMH = pc.MaLMH
                WHERE pc.MaGV = ? AND lmh.MaLop = ?
                  AND pc.TrangThaiDuyet = 'DA_DUYET'
                  AND pc.TuNgay <= ?
                  AND (pc.DenNgay IS NULL OR pc.DenNgay >= ?)
                """;
            Integer count = jdbc.queryForObject(sql, Integer.class, maGV, maLop, ngay, ngay);
            return count != null && count > 0;
        }
    }

    @Override
    public List<AttendancePermissionClassResponse> layDanhSachLopQuanTri() {
        String sql = """
            SELECT l.MaLop, l.TenLop, l.MaNH,
                   CONCAT(YEAR(n.NgayBatDau), '-', YEAR(n.NgayKetThuc)) AS NamHoc,
                   l.MaKhoi, k.SoKhoi
            FROM lop_hoc l
            JOIN nam_hoc n ON n.MaNH = l.MaNH
            JOIN khoi_lop k ON k.MaKhoi = l.MaKhoi
            WHERE l.TrangThai = 'HOAT_DONG'
            ORDER BY n.NgayBatDau DESC, k.SoKhoi, l.TenLop
            """;
        return jdbc.query(sql, (rs, rowNum) -> AttendancePermissionClassResponse.builder()
                .maLop(rs.getLong("MaLop"))
                .tenLop(rs.getString("TenLop"))
                .maNH(rs.getLong("MaNH"))
                .namHoc(rs.getString("NamHoc"))
                .maKhoi(rs.getLong("MaKhoi"))
                .soKhoi(rs.getInt("SoKhoi"))
                .vaiTroPhuTrach("QUAN_TRI")
                .build());
    }

    @Override
    public List<AttendancePermissionClassResponse> layDanhSachLopGiaoVien(Long maGV, LocalDate ngay) {
        List<AttendancePermissionClassResponse> result = new ArrayList<>();

        // 1. Lớp chủ nhiệm
        String sqlCN = """
            SELECT DISTINCT l.MaLop, l.TenLop, l.MaNH,
                   CONCAT(YEAR(n.NgayBatDau), '-', YEAR(n.NgayKetThuc)) AS NamHoc,
                   l.MaKhoi, k.SoKhoi
            FROM phan_cong_chu_nhiem pccn
            JOIN lop_hoc l ON l.MaLop = pccn.MaLop
            JOIN nam_hoc n ON n.MaNH = l.MaNH
            JOIN khoi_lop k ON k.MaKhoi = l.MaKhoi
            WHERE pccn.MaGV = ?
              AND pccn.TrangThaiDuyet = 'DA_DUYET'
              AND pccn.TuNgay <= ?
              AND (pccn.DenNgay IS NULL OR pccn.DenNgay >= ?)
              AND l.TrangThai = 'HOAT_DONG'
            ORDER BY n.NgayBatDau DESC, k.SoKhoi, l.TenLop
            """;
        List<AttendancePermissionClassResponse> cnList = jdbc.query(sqlCN, (rs, rowNum) ->
                AttendancePermissionClassResponse.builder()
                        .maLop(rs.getLong("MaLop"))
                        .tenLop(rs.getString("TenLop"))
                        .maNH(rs.getLong("MaNH"))
                        .namHoc(rs.getString("NamHoc"))
                        .maKhoi(rs.getLong("MaKhoi"))
                        .soKhoi(rs.getInt("SoKhoi"))
                        .vaiTroPhuTrach("CHU_NHIEM")
                        .build(),
                maGV, ngay, ngay);
        result.addAll(cnList);

        // 2. Lớp bộ môn
        String sqlBM = """
            SELECT DISTINCT l.MaLop, l.TenLop, l.MaNH,
                   CONCAT(YEAR(n.NgayBatDau), '-', YEAR(n.NgayKetThuc)) AS NamHoc,
                   l.MaKhoi, k.SoKhoi,
                   lmh.MaLMH, mh.TenMon
            FROM phan_cong_giang_day pcgd
            JOIN lop_mon_hoc lmh ON lmh.MaLMH = pcgd.MaLMH
            JOIN mon_hoc mh ON mh.MaMH = lmh.MaMH
            JOIN lop_hoc l ON l.MaLop = lmh.MaLop
            JOIN nam_hoc n ON n.MaNH = l.MaNH
            JOIN khoi_lop k ON k.MaKhoi = l.MaKhoi
            WHERE pcgd.MaGV = ?
              AND pcgd.TrangThaiDuyet = 'DA_DUYET'
              AND pcgd.TuNgay <= ?
              AND (pcgd.DenNgay IS NULL OR pcgd.DenNgay >= ?)
              AND l.TrangThai = 'HOAT_DONG'
            ORDER BY n.NgayBatDau DESC, k.SoKhoi, l.TenLop
            """;
        List<AttendancePermissionClassResponse> bmList = jdbc.query(sqlBM, (rs, rowNum) ->
                AttendancePermissionClassResponse.builder()
                        .maLop(rs.getLong("MaLop"))
                        .tenLop(rs.getString("TenLop"))
                        .maNH(rs.getLong("MaNH"))
                        .namHoc(rs.getString("NamHoc"))
                        .maKhoi(rs.getLong("MaKhoi"))
                        .soKhoi(rs.getInt("SoKhoi"))
                        .vaiTroPhuTrach("BO_MON")
                        .maLMH(rs.getLong("MaLMH"))
                        .tenMonHoc(rs.getString("TenMon"))
                        .build(),
                maGV, ngay, ngay);
        result.addAll(bmList);

        return result;
    }

    @Override
    public Optional<Long> timHocKyTheoNgayVaLop(Long maLop, LocalDate ngay) {
        String sql = """
            SELECT hk.MaHK
            FROM lop_hoc l
            JOIN hoc_ky hk ON hk.MaNH = l.MaNH
            WHERE l.MaLop = ?
              AND ? BETWEEN hk.NgayBatDau AND hk.NgayKetThuc
            LIMIT 1
            """;
        try {
            Long maHK = jdbc.queryForObject(sql, Long.class, maLop, ngay);
            return Optional.ofNullable(maHK);
        } catch (EmptyResultDataAccessException e) {
            // Trường hợp học kỳ không khớp chính xác, thử tìm học kỳ đang diễn ra hoặc gần nhất của năm học lớp
            String sqlFallback = """
                SELECT hk.MaHK
                FROM lop_hoc l
                JOIN hoc_ky hk ON hk.MaNH = l.MaNH
                WHERE l.MaLop = ?
                ORDER BY hk.SoHocKy ASC
                LIMIT 1
                """;
            try {
                Long maHK = jdbc.queryForObject(sqlFallback, Long.class, maLop);
                return Optional.ofNullable(maHK);
            } catch (EmptyResultDataAccessException ex) {
                return Optional.empty();
            }
        }
    }

    @Override
    public List<AttendanceStudentResponse> layDanhSachHocSinhTheoLopVaNgay(Long maLop, LocalDate ngay) {
        String sql = """
            SELECT x.MaXL, h.MaHS, h.MaSoHS, h.HoTen, h.NgaySinh
            FROM xep_lop x
            JOIN hoc_sinh h ON h.MaHS = x.MaHS
            JOIN lop_hoc l ON l.MaLop = x.MaLop
            JOIN nam_hoc n ON n.MaNH = l.MaNH
            WHERE x.MaLop = ?
              AND ? BETWEEN n.NgayBatDau AND n.NgayKetThuc
              AND x.TuNgay <= ?
              AND (x.DenNgay IS NULL OR x.DenNgay >= ?)
            ORDER BY h.HoTen ASC, h.MaSoHS ASC
            """;
        return jdbc.query(sql, (rs, rowNum) -> AttendanceStudentResponse.builder()
                .maXL(rs.getLong("MaXL"))
                .maHS(rs.getLong("MaHS"))
                .maSoHS(rs.getString("MaSoHS"))
                .hoTen(rs.getString("HoTen"))
                .ngaySinh(rs.getObject("NgaySinh", LocalDate.class))
                .trangThai("CO_MAT")
                .ghiChu("")
                .build(),
                maLop, ngay, ngay, ngay);
    }

    @Override
    public Optional<AttendanceSession> timPhienDiemDanh(Long maLop, LocalDate ngay, String phamVi, String maKhung) {
        String sql = """
            SELECT MaPDD, MaLop, MaHK, MaLMH, Ngay, PhamVi, MaKhung, TrangThai, MaTKTao
            FROM phien_diem_danh
            WHERE MaLop = ? AND Ngay = ? AND PhamVi = ? AND MaKhung = ?
            """;
        try {
            AttendanceSession session = jdbc.queryForObject(sql, SESSION_MAPPER, maLop, ngay, phamVi, maKhung);
            return Optional.ofNullable(session);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AttendanceSession> timPhienDiemDanhTheoId(Long maPDD) {
        String sql = """
            SELECT MaPDD, MaLop, MaHK, MaLMH, Ngay, PhamVi, MaKhung, TrangThai, MaTKTao
            FROM phien_diem_danh
            WHERE MaPDD = ?
            """;
        try {
            AttendanceSession session = jdbc.queryForObject(sql, SESSION_MAPPER, maPDD);
            return Optional.ofNullable(session);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AttendanceStudentResponse> layChiTietDiemDanhTheoPhien(Long maPDD) {
        String sql = """
            SELECT dd.MaXL, h.MaHS, h.MaSoHS, h.HoTen, h.NgaySinh,
                   dd.TrangThai, dd.SoPhutMuon, dd.GhiChu
            FROM diem_danh dd
            JOIN xep_lop x ON x.MaXL = dd.MaXL
            JOIN hoc_sinh h ON h.MaHS = x.MaHS
            WHERE dd.MaPDD = ?
            ORDER BY h.HoTen ASC, h.MaSoHS ASC
            """;
        return jdbc.query(sql, (rs, rowNum) -> {
            String tt = rs.getString("TrangThai");
            int phutMuon = rs.getInt("SoPhutMuon");
            String displayTrangThai = ("CO_MAT".equals(tt) && phutMuon > 0) ? "DI_MUON" : tt;
            return AttendanceStudentResponse.builder()
                    .maXL(rs.getLong("MaXL"))
                    .maHS(rs.getLong("MaHS"))
                    .maSoHS(rs.getString("MaSoHS"))
                    .hoTen(rs.getString("HoTen"))
                    .ngaySinh(rs.getObject("NgaySinh", LocalDate.class))
                    .trangThai(displayTrangThai)
                    .ghiChu(rs.getString("GhiChu") != null ? rs.getString("GhiChu") : "")
                    .build();
        }, maPDD);
    }

    @Override
    public Long taoPhienDiemDanh(AttendanceSession phien) {
        String sql = """
            INSERT INTO phien_diem_danh (MaLop, MaHK, MaLMH, Ngay, PhamVi, MaKhung, TrangThai, MaTKTao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, phien.getMaLop());
            ps.setLong(2, phien.getMaHK());
            if (phien.getMaLMH() != null) {
                ps.setLong(3, phien.getMaLMH());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setDate(4, Date.valueOf(phien.getNgay()));
            ps.setString(5, phien.getPhamVi());
            ps.setString(6, phien.getMaKhung());
            ps.setString(7, phien.getTrangThai() != null ? phien.getTrangThai() : "DANG_NHAP");
            ps.setLong(8, phien.getMaTKTao());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    @Override
    public void luuChiTietDiemDanh(List<AttendanceRecord> chiTietList) {
        String sql = """
            INSERT INTO diem_danh (MaPDD, MaXL, TrangThai, SoPhutMuon, GhiChu, MaTKCapNhat, CapNhatLuc)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AttendanceRecord r = chiTietList.get(i);
                ps.setLong(1, r.getMaPDD());
                ps.setLong(2, r.getMaXL());
                ps.setString(3, r.getTrangThai());
                ps.setInt(4, r.getSoPhutMuon() != null ? r.getSoPhutMuon() : 0);
                ps.setString(5, r.getGhiChu());
                ps.setLong(6, r.getMaTKCapNhat());
                ps.setTimestamp(7, Timestamp.valueOf(r.getCapNhatLuc() != null ? r.getCapNhatLuc() : LocalDateTime.now()));
            }

            @Override
            public int getBatchSize() {
                return chiTietList.size();
            }
        });
    }

    @Override
    public void capNhatChiTietDiemDanh(Long maPDD, List<AttendanceRecord> chiTietList) {
        String sql = """
            UPDATE diem_danh
            SET TrangThai = ?, SoPhutMuon = ?, GhiChu = ?, MaTKCapNhat = ?, CapNhatLuc = ?
            WHERE MaPDD = ? AND MaXL = ?
            """;
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AttendanceRecord r = chiTietList.get(i);
                ps.setString(1, r.getTrangThai());
                ps.setInt(2, r.getSoPhutMuon() != null ? r.getSoPhutMuon() : 0);
                ps.setString(3, r.getGhiChu());
                ps.setLong(4, r.getMaTKCapNhat());
                ps.setTimestamp(5, Timestamp.valueOf(r.getCapNhatLuc() != null ? r.getCapNhatLuc() : LocalDateTime.now()));
                ps.setLong(6, maPDD);
                ps.setLong(7, r.getMaXL());
            }

            @Override
            public int getBatchSize() {
                return chiTietList.size();
            }
        });
    }

    @Override
    public List<AttendanceRecord> layDiemDanhHienTaiTheoPhien(Long maPDD) {
        String sql = "SELECT MaDD, MaPDD, MaXL, TrangThai, SoPhutMuon, GhiChu, MaTKCapNhat, CapNhatLuc FROM diem_danh WHERE MaPDD = ?";
        return jdbc.query(sql, RECORD_MAPPER, maPDD);
    }

    @Override
    public void ghiNhatKy(Long maTK, String hanhDong, String loaiDoiTuong, String maDoiTuong, String duLieuTruocJson, String duLieuSauJson, LocalDateTime thoiDiem) {
        String sql = """
            INSERT INTO nhat_ky_he_thong (MaTK, HanhDong, LoaiDoiTuong, MaDoiTuong, DuLieuTruoc, DuLieuSau, ThoiDiem)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        jdbc.update(sql, maTK, hanhDong, loaiDoiTuong, maDoiTuong, duLieuTruocJson, duLieuSauJson, thoiDiem);
    }
}
