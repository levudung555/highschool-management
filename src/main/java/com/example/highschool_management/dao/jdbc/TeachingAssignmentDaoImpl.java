package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.TeachingAssignmentDao;
import com.example.highschool_management.model.TeachingAssignment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TeachingAssignmentDaoImpl implements TeachingAssignmentDao {
    private final JdbcTemplate jdbcTemplate;

    public TeachingAssignmentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_PHAN_CONG = """
        SELECT
            p.MaPCGD,
            p.MaLMH,
            p.MaGV,
            p.TuNgay,
            p.DenNgay,
            p.TrangThaiDuyet,
            p.MaTKDuyet,
            p.DuyetLuc,
            l.TenLop,
            mh.TenMon AS TenMonHoc,
            hk.SoHocKy,
            n.MaSoNH AS NamHoc,
            gv.HoTen AS TenGiaoVien,
            tk.HoTen AS TenNguoiDuyet
        FROM phan_cong_giang_day p
        JOIN lop_mon_hoc lmh ON lmh.MaLMH = p.MaLMH
        JOIN lop_hoc l ON l.MaLop = lmh.MaLop
        JOIN mon_hoc mh ON mh.MaMH = lmh.MaMH
        JOIN hoc_ky hk ON hk.MaHK = lmh.MaHK
        JOIN nam_hoc n ON n.MaNH = hk.MaNH
        JOIN giao_vien gv ON gv.MaGV = p.MaGV
        LEFT JOIN tai_khoan tk ON tk.MaTK = p.MaTKDuyet
        """;
    private static final RowMapper<TeachingAssignment> PHAN_CONG_MAPPER =
            (rs, rowNum) -> {
                TeachingAssignment phanCong = new TeachingAssignment();

                phanCong.setMaPhanCong(rs.getLong("MaPCGD"));
                phanCong.setMaLopMonHoc(rs.getLong("MaLMH"));
                phanCong.setMaGiaoVien(rs.getLong("MaGV"));

                phanCong.setTuNgay(
                        rs.getObject("TuNgay", LocalDate.class)
                );

                phanCong.setDenNgay(
                        rs.getObject("DenNgay", LocalDate.class)
                );

                phanCong.setTrangThaiDuyet(
                        rs.getString("TrangThaiDuyet")
                );

                phanCong.setMaTaiKhoanDuyet(
                        rs.getObject("MaTKDuyet", Long.class)
                );

                phanCong.setDuyetLuc(
                        rs.getObject("DuyetLuc", LocalDateTime.class)
                );

                phanCong.setTenLop(rs.getString("TenLop"));
                phanCong.setTenMonHoc(rs.getString("TenMonHoc"));
                phanCong.setSoHocKy(rs.getInt("SoHocKy"));
                phanCong.setNamHoc(rs.getString("NamHoc"));

                phanCong.setTenGiaoVien(
                        rs.getString("TenGiaoVien")
                );

                phanCong.setTenNguoiDuyet(
                        rs.getString("TenNguoiDuyet")
                );

                return phanCong;
            };

    @Override
    public long tao(TeachingAssignment phanCong){
        String sql = """
                INSERT INTO phan_cong_giang_day (MaLMH, MaGV, TuNgay, DenNgay, TrangThaiDuyet, MaTKDuyet, DuyetLuc)
                VALUES (?, ?, ?, ?, 'DA_DUYET', ?, ?)
                """;
        var keyHolder = new GeneratedKeyHolder();
        int soDong = jdbcTemplate.update(connection ->{
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, phanCong.getMaLopMonHoc());
            ps.setLong(2, phanCong.getMaGiaoVien());
            ps.setDate(3, java.sql.Date.valueOf(phanCong.getTuNgay()));
            ps.setObject(4, phanCong.getDenNgay(), java.sql.Types.DATE);
            ps.setLong(5, phanCong.getMaTaiKhoanDuyet());
            ps.setObject(6, phanCong.getDuyetLuc(), java.sql.Types.TIMESTAMP);
            return ps;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        if (soDong != 1 || generatedId == null) {
            throw new IllegalStateException("Không tạo được phân công giảng dạy");
        }
        return generatedId.longValue();
    }

    @Override
    public Optional<TeachingAssignment> timTheoMa(Long maPhanCong){
        String sql = SELECT_PHAN_CONG + " WHERE MaPCGD = ?";
        var ketQua = jdbcTemplate.query(sql, PHAN_CONG_MAPPER, maPhanCong);
        if (ketQua.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ketQua.get(0));
    }

    @Override
    public List<TeachingAssignment> lichSuTheoLopMonHoc(Long maLopMonHoc) {
        String sql = SELECT_PHAN_CONG + " WHERE p.MaLMH = ? ORDER BY p.TuNgay DESC";
        return jdbcTemplate.query(sql, PHAN_CONG_MAPPER, maLopMonHoc);
    }

    @Override
    public void ghiNhatKyTao(
            Long maTaiKhoan,
            TeachingAssignment phanCong,
            LocalDateTime thoiDiem
    ) {
        String sql = """
            INSERT INTO nhat_ky_he_thong (
                MaTK,
                HanhDong,
                LoaiDoiTuong,
                MaDoiTuong,
                DuLieuTruoc,
                DuLieuSau,
                ThoiDiem
            )
            VALUES (
                ?,
                'TAO_PHAN_CONG_GIANG_DAY',
                'PHAN_CONG_GIANG_DAY',
                ?,
                NULL,
                JSON_OBJECT(
                    'MaPCGD', ?,
                    'MaLMH', ?,
                    'MaGV', ?,
                    'TuNgay', ?,
                    'DenNgay', ?,
                    'TrangThaiDuyet', 'DA_DUYET',
                    'MaTKDuyet', ?
                ),
                ?
            )
            """;

        String denNgay = null;

        if (phanCong.getDenNgay() != null) {
            denNgay = phanCong.getDenNgay().toString();
        }

        int soDongThem = jdbcTemplate.update(
                sql,
                maTaiKhoan,
                phanCong.getMaPhanCong().toString(),
                phanCong.getMaPhanCong(),
                phanCong.getMaLopMonHoc(),
                phanCong.getMaGiaoVien(),
                phanCong.getTuNgay().toString(),
                denNgay,
                phanCong.getMaTaiKhoanDuyet(),
                thoiDiem
        );

        if (soDongThem != 1) {
            throw new IllegalStateException(
                    "Không ghi được nhật ký phân công giảng dạy"
            );
        }
    }

    @Override
    public boolean coPhanCongTrungThoiGian(Long maLopMonHoc, LocalDate tuNgay, LocalDate denNgayHieuLuc){
        String sql = """
            SELECT COUNT(*)
            FROM phan_cong_giang_day p
            JOIN lop_mon_hoc lmh ON lmh.MaLMH = p.MaLMH
            JOIN hoc_ky hk ON hk.MaHK = lmh.MaHK
            WHERE p.MaLMH = ?
              AND p.TrangThaiDuyet = 'DA_DUYET'
              AND p.TuNgay <= ?
              AND COALESCE(p.DenNgay, hk.NgayKetThuc) >= ?
            """;

        Long soPhanCongTrung = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                maLopMonHoc,
                denNgayHieuLuc,
                tuNgay
        );

        return soPhanCongTrung != null && soPhanCongTrung > 0;
    }


}
