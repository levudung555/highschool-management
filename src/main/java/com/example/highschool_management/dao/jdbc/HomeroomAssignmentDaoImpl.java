package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.HomeroomAssignmentDao;
import com.example.highschool_management.model.HomeroomAssignment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class HomeroomAssignmentDaoImpl implements HomeroomAssignmentDao {
    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<HomeroomAssignment> PHAN_CONG_MAPPER =
            (rs, rowNum) -> {
                HomeroomAssignment phanCong = new HomeroomAssignment();

                phanCong.setMaPhanCong(rs.getLong("MaPCCN"));
                phanCong.setMaLop(rs.getLong("MaLop"));
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
                phanCong.setTenGiaoVien(rs.getString("TenGiaoVien"));
                phanCong.setNamHoc(rs.getString("NamHoc"));
                phanCong.setTenNguoiDuyet(rs.getString("TenNguoiDuyet"));

                return phanCong;
            };
    private static final String SELECT_PHAN_CONG = """
        SELECT
            p.MaPCCN,
            p.MaLop,
            p.MaGV,
            p.TuNgay,
            p.DenNgay,
            p.TrangThaiDuyet,
            p.MaTKDuyet,
            p.DuyetLuc,
            l.TenLop,
            g.HoTen AS TenGiaoVien,
            n.MaSoNH AS NamHoc,
            t.HoTen AS TenNguoiDuyet
        FROM phan_cong_chu_nhiem p
        JOIN lop_hoc l ON l.MaLop = p.MaLop
        JOIN giao_vien g ON g.MaGV = p.MaGV
        JOIN nam_hoc n ON n.MaNH = l.MaNH
        LEFT JOIN tai_khoan t ON t.MaTK = p.MaTKDuyet
        """;

    public HomeroomAssignmentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Hai phân công đã giao nhau nếu có chung ít nhất một ngày. Ngày kết thúc để trống được tính đến cuối năm học.
    @Override
    public boolean coPhanCongTrungThoiGian(Long maLop, LocalDate ngayBatDau, LocalDate ngayKetThuc){
        String sql = """
                SELECT COUNT(*)
                FROM phan_cong_chu_nhiem p
                JOIN lop_hoc l ON l.MaLop = p.MaLop
                JOIN nam_hoc n ON n.MaNH = l.MaNH
                WHERE p.MaLop = ?
                AND p.TrangThaiDuyet = 'DA_DUYET'
                AND p.TuNgay <= COALESCE(?, n.NgayKetThuc)
                AND COALESCE(p.DenNgay, n.NgayKetThuc) >= ?
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, maLop, ngayKetThuc, ngayBatDau);
        return count != null && count > 0;
    }
    // Lưu phân công chủ nhiệm và trả về mã vừa tạo để đọc lại bản ghi.
    @Override
    public long tao(HomeroomAssignment phanCong){
        String sql = """
            INSERT INTO phan_cong_chu_nhiem (
                MaLop,
                MaGV,
                TuNgay,
                DenNgay,
                TrangThaiDuyet,
                MaTKDuyet,
                DuyetLuc
            )
            VALUES (?, ?, ?, ?, 'DA_DUYET', ?, ?)
            """;
        var keyHolder = new GeneratedKeyHolder();
        int soDongThem = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, phanCong.getMaLop());
            ps.setLong(2, phanCong.getMaGiaoVien());
            ps.setObject(3, phanCong.getTuNgay(), java.sql.Types.DATE);
            ps.setObject(4, phanCong.getDenNgay(), java.sql.Types.DATE);
            ps.setLong(5, phanCong.getMaTaiKhoanDuyet());
            ps.setObject(6, phanCong.getDuyetLuc(), java.sql.Types.TIMESTAMP);
            return ps;
        }, keyHolder);

        Number maDuocTao = keyHolder.getKey();

        if (soDongThem != 1 || maDuocTao == null) {
            throw new IllegalStateException(
                    "Không tạo được phân công chủ nhiệm"
            );
        }

        return maDuocTao.longValue();
    }
    // Lưu thông tin của lần tạo phân công chủ nhiệm vào nhật ký hệ thống.
    @Override
    public void ghiNhatKyTao(
            Long maTaiKhoan,
            HomeroomAssignment phanCong,
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
                'TAO_PHAN_CONG_CHU_NHIEM',
                'PHAN_CONG_CHU_NHIEM',
                ?,
                NULL,
                JSON_OBJECT(
                    'MaPCCN', ?,
                    'MaLop', ?,
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
                phanCong.getMaLop(),
                phanCong.getMaGiaoVien(),
                phanCong.getTuNgay().toString(),
                denNgay,
                phanCong.getMaTaiKhoanDuyet(),
                thoiDiem
        );

        if (soDongThem != 1) {
            throw new IllegalStateException(
                    "Không ghi được nhật ký phân công chủ nhiệm"
            );
        }
    }

    // Sắp lịch sử chủ nhiệm của lớp theo ngày bắt đầu, mới nhất trước.
    @Override
    public List<HomeroomAssignment> lichSuTheoLop(Long maLop){
        String sql = SELECT_PHAN_CONG + " WHERE p.MaLop = ? ORDER BY p.TuNgay DESC";
        return jdbcTemplate.query(sql, PHAN_CONG_MAPPER, maLop);
    }

    // Tìm đúng lần phân công theo mã chủ nhiệm, kể cả khi lớp đã đổi giáo viên.
    @Override
    public Optional<HomeroomAssignment> timTheoMa(Long maPhanCong){
        String sql = SELECT_PHAN_CONG + " WHERE p.MaPCCN = ?";
        List<HomeroomAssignment> ketQua = jdbcTemplate.query(sql, PHAN_CONG_MAPPER, maPhanCong);
        if (ketQua.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ketQua.get(0));
    }

}
