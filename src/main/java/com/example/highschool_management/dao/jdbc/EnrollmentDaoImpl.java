package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.EnrollmentDao;
import com.example.highschool_management.dto.response.ClassStudentResponse;

import com.example.highschool_management.dto.response.EnrollmentStudentResponse;
import com.example.highschool_management.dto.response.SchoolYearPeriodResponse;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.model.Enrollment;

import java.awt.image.Kernel;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class EnrollmentDaoImpl implements EnrollmentDao {
    private final JdbcTemplate jdbc;

    public EnrollmentDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT = """
            select x.MaXL, x.MaHS, x.MaLop, x.TuNgay, x.DenNgay, x.LyDoKetThuc, x.MaTKNguoiThucHien, x.ThoiDiemThucHien,
                   h.HoTen, h.MaSoHS, l.TenLop, n.MaSoNH, t.HoTen AS TenNguoiThucHien
            from xep_lop x
            join hoc_sinh h on h.MaHS=x.MaHS
            join lop_hoc l on l.MaLop=x.MaLop
            join nam_hoc n on n.MaNH=l.MaNH
            join tai_khoan t on t.MaTK=x.MaTKNguoiThucHien""";

    private static final String DANH_SACH_LOP_FROM = """
        FROM xep_lop x
        JOIN hoc_sinh h ON h.MaHS = x.MaHS
        JOIN lop_hoc l ON l.MaLop = x.MaLop
        JOIN nam_hoc n ON n.MaNH = l.MaNH
        WHERE x.MaLop = ?
          AND ? BETWEEN n.NgayBatDau AND n.NgayKetThuc
          AND x.TuNgay <= ?
          AND (x.DenNgay IS NULL OR x.DenNgay >= ?)
        """;

    private static final RowMapper<Enrollment> MAPPER = (rs, rowNum) -> {
        Enrollment enrollment = new Enrollment();
        enrollment.setMaXepLop(rs.getLong("MaXL"));
        enrollment.setMaHocSinh(rs.getLong("MaHS"));
        enrollment.setMaLop(rs.getLong("MaLop"));
        enrollment.setTuNgay(
                rs.getObject("TuNgay", LocalDate.class)
        );
        enrollment.setDenNgay(
                rs.getObject("DenNgay", LocalDate.class)
        );
        enrollment.setLyDoKetThuc(
                rs.getString("LyDoKetThuc")
        );
        enrollment.setMaTaiKhoanThucHien(
                rs.getLong("MaTKNguoiThucHien")
        );
        enrollment.setThoiDiemThucHien(
                rs.getObject("ThoiDiemThucHien", LocalDateTime.class)
        );

        enrollment.setHoTen(rs.getString("HoTen"));
        enrollment.setMaSoHocSinh(rs.getString("MaSoHS"));
        enrollment.setTenLop(rs.getString("TenLop"));
        enrollment.setNamHoc(rs.getString("MaSoNH"));
        enrollment.setTenNguoiThucHien(rs.getString("TenNguoiThucHien"));
        return enrollment;
    };

    private static final RowMapper<EnrollmentStudentResponse>
        HOC_SINH_MAPPER = (rs, rowNum) ->
        new EnrollmentStudentResponse(
                rs.getLong("MaHS"),
                rs.getString("MaSoHS"),
                rs.getString("HoTen"),
                rs.getObject("NgaySinh", LocalDate.class)
        );

    private static final String TIM_HOC_SINH_FROM = """
        FROM hoc_sinh h
        WHERE (
            LOCATE(?, h.HoTen) > 0
            OR LOCATE(?, h.MaSoHS) > 0
        )
        AND (
            ? IS NULL
            OR NOT EXISTS (
                SELECT 1
                FROM xep_lop x
                JOIN lop_hoc l ON l.MaLop = x.MaLop
                WHERE x.MaHS = h.MaHS
                  AND l.MaNH = ?
            )
        )
        """;

    @Override
    public boolean khoaHocSinh(Long maHocSinh){
        String sql = """
                select MaHs
                From hoc_sinh
                where MaHS = ?
                for update
                """;
        List<Long> result = jdbc.query(sql, (rs, rowNum) -> rs.getLong("MaHS"), maHocSinh);
        if (result.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean coXepLopTrongNamHoc(Long maHocSinh, Long maNamHoc){
        String sql = """
                select count(*)
                from xep_lop x
                join lop_hoc l on l.MaLop = x.MaLop
                where x.MaHS = ?
                and l.MaNH = ?""";
        Long count = jdbc.queryForObject(sql, Long.class, maNamHoc, maNamHoc);
        return count != null && count > 0;
    }

    @Override
    public long tao(Enrollment en){
        String sql = """
                insert into xep_lop(MaHS, MaLop, TuNgay, DenNgay, LyDoKetThuc, MaTKNguoiThucHien, ThoiDiemThucHien)
                values(?, ?, ?, ?, ?, ?, ?)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
         jdbc.update(connection ->{
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, en.getMaHocSinh());
            ps.setLong(2, en.getMaLop());
            ps.setObject(3, en.getTuNgay(), Types.DATE);
            ps.setObject(4, en.getDenNgay(), Types.DATE);
            ps.setString(5, en.getLyDoKetThuc());
            ps.setLong(6, en.getMaTaiKhoanThucHien());
            ps.setObject(7, en.getThoiDiemThucHien(), Types.TIMESTAMP);
            return ps;
        }, keyHolder);
        Number maDuocTao = keyHolder.getKey();

        if (maDuocTao == null) {
            throw new IllegalStateException(
                    "Không lấy được mã phân lớp vừa tạo"
            );
        }
        return maDuocTao.longValue();
    }
    @Override
    public SchoolYearPeriodResponse namHoc(Long maNamHoc){
        String sql = """
                select NgayBatDau, NgayKetThuc
                from nam_hoc
                where MaNH = ?""";

        List<SchoolYearPeriodResponse> danhSach = jdbc.query(sql,
                (rs, rowNum) -> new SchoolYearPeriodResponse(
                    rs.getObject("NgayBatDau", LocalDate.class),
                    rs.getObject("NgayKetThuc", LocalDate.class)
                ), maNamHoc);
        if (danhSach.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy năm học có mã: " + maNamHoc);
        }
        return danhSach.get(0);
    }

    @Override
    public Optional<Enrollment> timTheoMa(Long maXepLop) {
        String sql = """
                select x.MaXL, x.MaHS, x.MaLop, x.TuNgay, x.DenNgay, x.LyDoKetThuc, x.MaTKNguoiThucHien, x.ThoiDiemThucHien,
                       h.HoTen, h.MaSoHS, l.TenLop, n.MaSoNH, t.HoTen AS TenNguoiThucHien
                from xep_lop x
                join hoc_sinh h on h.MaHS=x.MaHS
                join lop_hoc l on l.MaLop=x.MaLop
                join nam_hoc n on n.MaNH=l.MaNH
                join tai_khoan t on t.MaTK = x.MaTKNguoiThucHien
                where x.MaXL = ?
                """;
        List<Enrollment> danhSach = jdbc.query(sql, MAPPER, maXepLop);
        if (danhSach.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(danhSach.get(0));
    }


    @Override
    public Optional<Enrollment> timTheoMaDeCapNhat(Long maXepLop){
        String sql = """
                select MaXL
                from xep_lop
                where MaXL = ?
                for update
                """;
        List<Long> danhSach = jdbc.query(sql, (rs, rowNum) -> rs.getLong("MaXL"), maXepLop);
        if (danhSach.isEmpty()) {
            return Optional.empty();
        }
        return timTheoMa(maXepLop); //doc day du thong tin xeplop
    }

    @Override
    public void ketThuc(Long maXepLop, LocalDate denNgay, String lyDo){
        String sql = """
                update xep_lop
                set DenNgay = ?,
                LyDoKetThuc = ?
                where MaXL = ?
                And DenNgay IS NULL
                And TuNgay <= ?
                """;
        int soDongBiCapNhat = jdbc.update(sql, denNgay, lyDo, maXepLop, denNgay);
        if (soDongBiCapNhat != 1) {
            throw new BusinessException( "Không thể kết thúc phân lớp: "
                    + "nguồn không tồn tại, đã kết thúc "
                    + "hoặc ngày kết thúc không hợp lệ");
        }
    }
    @Override
    public boolean trungThoiGian(Long maHocSinh, Long maNH, LocalDate tuNgay, LocalDate denNgay, Long boQuaMaXepLop){
        String sql = """
        SELECT COUNT(*)
        FROM xep_lop
        JOIN lop_hoc ON lop_hoc.MaLop = xep_lop.MaLop
        JOIN nam_hoc ON nam_hoc.MaNH = lop_hoc.MaNH
        WHERE xep_lop.MaHS = ?
          AND lop_hoc.MaNH = ?
          AND (? IS NULL OR xep_lop.MaXL <> ?)
          AND xep_lop.TuNgay <= COALESCE(?, nam_hoc.NgayKetThuc)
          AND COALESCE(xep_lop.DenNgay, nam_hoc.NgayKetThuc) >= ?
        """;
        Long soPhanLopTrung = jdbc.queryForObject(
                sql,
                Long.class,
                maHocSinh,
                maNH,
                boQuaMaXepLop,
                boQuaMaXepLop,
                denNgay,
                tuNgay
        );

        return Objects.requireNonNull(soPhanLopTrung) > 0;
    }
    @Override
    public void ghiNhatKyChuyenLop(Long maTaiKhoan, Long maXepLopNguon, Long maXepLopDich, LocalDate ngayChuyen, String lyDo, LocalDateTime thoiDiem
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
                'CHUYEN_LOP',
                'XEP_LOP',
                ?,
                JSON_OBJECT(
                    'MaXL', ?,
                    'DenNgay', NULL
                ),
                JSON_OBJECT(
                    'MaXLNguon', ?,
                    'MaXLDich', ?,
                    'DenNgayNguon', ?,
                    'NgayChuyen', ?,
                    'LyDo', ?
                ),
                ?
            )
            """;

        LocalDate ngayKetThucNguon = ngayChuyen.minusDays(1);

        jdbc.update(sql, maTaiKhoan, maXepLopNguon.toString(), maXepLopNguon, maXepLopNguon, maXepLopDich, ngayKetThucNguon.toString(), ngayChuyen.toString(), lyDo, thoiDiem);
    }

    @Override
    public List<Enrollment> lichSu(Long maHocSinh) {
        String sql = """
            SELECT
                x.MaXL,
                x.MaHS,
                x.MaLop,
                x.TuNgay,
                x.DenNgay,
                x.LyDoKetThuc,
                x.MaTKNguoiThucHien,
                x.ThoiDiemThucHien,
                h.HoTen,
                h.MaSoHS,
                l.TenLop,
                n.MaSoNH,
                t.HoTen AS TenNguoiThucHien
            FROM xep_lop x
            JOIN hoc_sinh h ON h.MaHS = x.MaHS
            JOIN lop_hoc l ON l.MaLop = x.MaLop
            JOIN nam_hoc n ON n.MaNH = l.MaNH
            JOIN tai_khoan t ON t.MaTK = x.MaTKNguoiThucHien
            WHERE x.MaHS = ?
            ORDER BY x.TuNgay DESC, x.MaXL DESC
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> {
                    Enrollment enrollment = new Enrollment();

                    enrollment.setMaXepLop(rs.getLong("MaXL"));
                    enrollment.setMaHocSinh(rs.getLong("MaHS"));
                    enrollment.setMaLop(rs.getLong("MaLop"));

                    enrollment.setTuNgay(
                            rs.getObject("TuNgay", LocalDate.class)
                    );

                    enrollment.setDenNgay(
                            rs.getObject("DenNgay", LocalDate.class)
                    );

                    enrollment.setLyDoKetThuc(
                            rs.getString("LyDoKetThuc")
                    );

                    enrollment.setMaTaiKhoanThucHien(
                            rs.getLong("MaTKNguoiThucHien")
                    );

                    enrollment.setThoiDiemThucHien(
                            rs.getObject(
                                    "ThoiDiemThucHien",
                                    LocalDateTime.class
                            )
                    );

                    enrollment.setHoTen(rs.getString("HoTen"));
                    enrollment.setMaSoHocSinh(rs.getString("MaSoHS"));
                    enrollment.setTenLop(rs.getString("TenLop"));
                    enrollment.setNamHoc(rs.getString("MaSoNH"));

                    enrollment.setTenNguoiThucHien(
                            rs.getString("TenNguoiThucHien")
                    );

                    return enrollment;
                },
                maHocSinh
        );


    }

    @Override
    public long demDanhSachLop(Long maLop, LocalDate ngayHienTai) {
        String sql = "SELECT COUNT(*) " + DANH_SACH_LOP_FROM;

        Long count = jdbc.queryForObject(
                sql,
                Long.class,
                maLop,
                ngayHienTai,
                ngayHienTai,
                ngayHienTai
        );

        return count != null ? count : 0;
    }

    @Override
    public List<ClassStudentResponse> danhSachLop(
            Long maLop,
            LocalDate ngay,
            int kichThuoc,
            long viTriBatDau
    ) {
        String sql = """
            SELECT
                x.MaXL,
                h.MaHS,
                h.MaSoHS,
                h.HoTen,
                h.NgaySinh,
                x.TuNgay,
                x.DenNgay
                
            """
                + DANH_SACH_LOP_FROM
                + """
            
            ORDER BY h.MaHS, x.MaXL
            LIMIT ? OFFSET ?
            """;
        return jdbc.query(
                sql,
                (rs, rowNum) -> new ClassStudentResponse(
                        rs.getLong("MaXL"),
                        rs.getLong("MaHS"),
                        rs.getString("MaSoHS"),
                        rs.getString("HoTen"),
                        rs.getObject("NgaySinh", LocalDate.class),
                        rs.getObject("TuNgay", LocalDate.class),
                        rs.getObject("DenNgay", LocalDate.class)
                ),
                maLop,
                ngay,
                ngay,
                ngay,
                kichThuoc,
                viTriBatDau
        );
    }
}
