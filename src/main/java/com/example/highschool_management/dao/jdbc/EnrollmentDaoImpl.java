package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.EnrollmentDao;
import com.example.highschool_management.dto.response.ClassStudentResponse;

import com.example.highschool_management.dto.response.EnrollmentStudentResponse;
import com.example.highschool_management.dto.response.SchoolYearPeriodResponse;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.model.Enrollment;
import java.sql.Statement;
import java.sql.Types;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
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
    public boolean khoaHocSinh(Long maHS){
        String sql = """
                Select MaHS from hoc_sinh where MaHS = ? for update""";
        List<Long> danhSachHocSinh = jdbc.query(sql, (rs, rowNum) -> rs.getLong("MaHS"), maHS);
        return !danhSachHocSinh.isEmpty();
    }

    @Override
    public SchoolYearPeriodResponse namHoc(Long maNamHoc){
        String sql = """
                select NgayBatDau, NgayKetThuc from nam_hoc where MaNH = ?""";
        return jdbc.query(sql, (rs, rowNum) -> new SchoolYearPeriodResponse(
                rs.getObject("NgayBatDau", LocalDate.class),
                rs.getObject("NgayKetThuc", LocalDate.class)
                )
        , maNamHoc).stream().findFirst().orElseThrow(()-> new ResourceNotFoundException("Năm học không tồn tại")) ;
    }
    @Override
    public Optional<Enrollment> timTheoMa(Long maXepLop) {
        return jdbc.query(
                SELECT + " WHERE x.MaXL = ?",
                MAPPER,
                maXepLop
        ).stream().findFirst();
    }
    @Override
    public Optional<Enrollment> timTheoMaDeCapNhat(Long maXepLop) {
        List<Long> danhSachMa = jdbc.query(
            "SELECT MaXL FROM xep_lop WHERE MaXL = ? FOR UPDATE",
            (rs, rowNum) -> rs.getLong("MaXL"), maXepLop);
        if (danhSachMa.isEmpty()) return Optional.empty();
        return timTheoMa(maXepLop);
    }
    @Override
    public List<Enrollment> lichSu(Long maHocSinh) {
        return jdbc.query(
                SELECT + """
                 WHERE x.MaHS = ?
                 ORDER BY x.TuNgay DESC, x.MaXL DESC
                """,
                MAPPER,
                maHocSinh
        );
    }
    @Override
    public boolean coXepLopTrongNamHoc(Long maHocSinh, Long maNamHoc) {

        Long count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM xep_lop x
                JOIN lop_hoc l ON l.MaLop = x.MaLop
                WHERE x.MaHS = ?
                  AND l.MaNH = ?
                """,
                Long.class,
                maHocSinh,
                maNamHoc
        );

        return Objects.requireNonNull(count) > 0;
    }
    @Override
    public boolean trungThoiGian(
            Long maHocSinh,
            Long maNamHoc,
            LocalDate tuNgay,
            LocalDate denNgay,
            Long boQuaMaXepLop
    ) {
        /*
         * Hai khoảng ngày đóng [a,b] và [c,d] trùng khi:
         * a <= d và c <= b.
         *
         * Giới hạn theo cùng năm học.
         * NULL được xem là kéo dài đến cuối năm học.
         * Service kiểm tra trước các ngày thuộc năm học.
         */
        Long count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM xep_lop x
                JOIN lop_hoc l ON l.MaLop = x.MaLop
                JOIN nam_hoc n ON n.MaNH = l.MaNH
                WHERE x.MaHS = ?
                  AND l.MaNH = ?
                  AND (? IS NULL OR x.MaXL <> ?)
                  AND x.TuNgay <= COALESCE(?, n.NgayKetThuc)
                  AND COALESCE(x.DenNgay, n.NgayKetThuc) >= ?
                """,
                Long.class,
                maHocSinh,
                maNamHoc,
                boQuaMaXepLop,
                boQuaMaXepLop,
                denNgay,
                tuNgay
        );

        return Objects.requireNonNull(count) > 0;
    }

    @Override
    public long tao(Enrollment enrollment) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    """
                    INSERT INTO xep_lop (
                        MaHS,
                        MaLop,
                        TuNgay,
                        DenNgay,
                        LyDoKetThuc,
                        MaTKNguoiThucHien,
                        ThoiDiemThucHien
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setLong(1, enrollment.getMaHocSinh());
            statement.setLong(2, enrollment.getMaLop());
            statement.setObject(
                    3, enrollment.getTuNgay(), Types.DATE
            );
            statement.setObject(
                    4, enrollment.getDenNgay(), Types.DATE
            );
            statement.setString(
                    5, enrollment.getLyDoKetThuc()
            );

            // Các giá trị này đã được service lấy từ hệ thống.
            statement.setLong(
                    6, enrollment.getMaTaiKhoanThucHien()
            );
            statement.setObject(
                    7,
                    enrollment.getThoiDiemThucHien(),
                    Types.TIMESTAMP
            );

            return statement;
        }, keyHolder);

        return Objects.requireNonNull(
                keyHolder.getKey(),
                "Không lấy được mã xếp lớp vừa tạo"
        ).longValue();
    }

    @Override
    public void ketThuc(
            Long maXepLop,
            LocalDate denNgay,
            String lyDo
    ) {

        int changed = jdbc.update(
                """
                UPDATE xep_lop
                SET DenNgay = ?,
                    LyDoKetThuc = ?
                WHERE MaXL = ?
                  AND DenNgay IS NULL
                  AND TuNgay <= ?
                """,
                denNgay,
                lyDo,
                maXepLop,
                denNgay
        );

        if (changed != 1) {
            throw new BusinessException(
                    "Phân lớp đã kết thúc, đã có lịch chuyển hoặc ngày kết thúc không hợp lệ"
            );
        }
    }

    @Override
    public long demSiSo(Long maLop, LocalDate ngay) {
        return Objects.requireNonNull(
                jdbc.queryForObject(
                        "SELECT COUNT(DISTINCT x.MaHS) "
                                + DANH_SACH_LOP_FROM,
                        Long.class,
                        maLop,
                        ngay,
                        ngay,
                        ngay
                )
        );
    }

    @Override
    public long demDanhSachLop(Long maLop, LocalDate ngay) {
        return demSiSo(maLop, ngay);
    }

    @Override
    public List<ClassStudentResponse> danhSachLop(
            Long maLop,
            LocalDate ngay,
            int kichThuoc,
            long viTriBatDau
    ) {

        return jdbc.query(
                """
                SELECT x.MaXL,
                       h.MaHS,
                       h.MaSoHS,
                       h.HoTen,
                       h.NgaySinh,
                       x.TuNgay,
                       x.DenNgay
                """ + DANH_SACH_LOP_FROM + """
            ORDER BY h.MaHS, x.MaXL
            LIMIT ? OFFSET ?
            """,
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

    @Override
    public long demHocSinh(
            String tuKhoa,
            Long maNamHocChuaXep
    ) {
        // Service chuẩn hóa từ khóa null thành chuỗi rỗng.
        return Objects.requireNonNull(
                jdbc.queryForObject(
                        "SELECT COUNT(*) " + TIM_HOC_SINH_FROM,
                        Long.class,
                        tuKhoa,
                        tuKhoa,
                        maNamHocChuaXep,
                        maNamHocChuaXep
                )
        );
    }

    @Override
    public List<EnrollmentStudentResponse> timHocSinh(
            String tuKhoa,
            Long maNamHocChuaXep,
            int kichThuoc,
            long viTriBatDau
    ) {
        return jdbc.query(
                """
                SELECT h.MaHS, h.MaSoHS, h.HoTen, h.NgaySinh
                """ + TIM_HOC_SINH_FROM + """
            ORDER BY h.MaHS DESC
            LIMIT ? OFFSET ?
            """,
                HOC_SINH_MAPPER,
                tuKhoa,
                tuKhoa,
                maNamHocChuaXep,
                maNamHocChuaXep,
                kichThuoc,
                viTriBatDau
        );
    }

    @Override
    public void ghiNhatKyChuyenLop(
            Long maTaiKhoan,
            Long maXepLopNguon,
            Long maXepLopDich,
            LocalDate ngayChuyen,
            String lyDo,
            LocalDateTime thoiDiem
    ) {

        jdbc.update(
                """
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
                    JSON_OBJECT('MaXL', ?, 'DenNgay', NULL),
                    JSON_OBJECT(
                        'MaXLNguon', ?,
                        'MaXLDich', ?,
                        'DenNgayNguon', ?,
                        'NgayChuyen', ?,
                        'LyDo', ?
                    ),
                    ?
                )
                """,
                maTaiKhoan,
                maXepLopNguon.toString(),
                maXepLopNguon,
                maXepLopNguon,
                maXepLopDich,
                ngayChuyen.minusDays(1).toString(),
                ngayChuyen.toString(),
                lyDo,
                thoiDiem
        );
    }


    private static final String LOC_PHAN_LOP = """
        WHERE (? IS NULL OR l.MaNH = ?)
        AND (LOCATE(?, h.HoTen)>0 OR LOCATE(?, h.MaSoHS)>0 OR LOCATE(?, l.TenLop)>0)
        """;

    @Override
    public long demPhanLop(String tuKhoa, Long maNamHoc) {
        String sql = "SELECT COUNT(*) FROM xep_lop x JOIN hoc_sinh h ON h.MaHS=x.MaHS "
            + "JOIN lop_hoc l ON l.MaLop=x.MaLop " + LOC_PHAN_LOP;
        return Objects.requireNonNull(jdbc.queryForObject(sql, Long.class,
            maNamHoc, maNamHoc, tuKhoa, tuKhoa, tuKhoa));
    }

    @Override
    public List<Enrollment> timPhanLop(String tuKhoa, Long maNamHoc, int kichThuoc, long viTriBatDau) {
        return jdbc.query(SELECT + " " + LOC_PHAN_LOP + " ORDER BY x.MaXL DESC LIMIT ? OFFSET ?",
            MAPPER, maNamHoc, maNamHoc, tuKhoa, tuKhoa, tuKhoa, kichThuoc, viTriBatDau);
    }
}
