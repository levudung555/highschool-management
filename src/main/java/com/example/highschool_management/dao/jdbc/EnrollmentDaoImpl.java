package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.EnrollmentDao;
import com.example.highschool_management.dto.response.*;

import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.model.Enrollment;

import java.awt.image.Kernel;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.*;
import java.util.*;

import com.example.highschool_management.model.Grade10PlacementCandidate;
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

    private static final String TRANSFER_JOIN = """
        LEFT JOIN nhat_ky_he_thong y ON y.LoaiDoiTuong = 'XEP_LOP'
          AND y.HanhDong = 'CHUYEN_LOP' AND y.MaDoiTuong = CAST(x.MaXL AS CHAR(30))
        """;
    private static final tools.jackson.databind.ObjectMapper JSON = new tools.jackson.databind.ObjectMapper();

    private static final String SELECT = """
            select y.DuLieuSau, x.MaHoSoNhapHoc, x.MaXL, x.MaHS, x.MaLop, x.TuNgay, x.DenNgay, x.LyDoKetThuc, x.MaTKNguoiThucHien, x.ThoiDiemThucHien,
                   h.HoTen, h.MaSoHS, l.TenLop, n.MaSoNH, t.HoTen AS TenNguoiThucHien
            from xep_lop x
            join hoc_sinh h on h.MaHS=x.MaHS
            join lop_hoc l on l.MaLop=x.MaLop
            join nam_hoc n on n.MaNH=l.MaNH
            join tai_khoan t on t.MaTK=x.MaTKNguoiThucHien
            """ + TRANSFER_JOIN;

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
        enrollment.setMaHoSoNhapHoc(rs.getObject("MaHoSoNhapHoc", Long.class));
        var transfer = rs.getString("DuLieuSau");
        if (transfer != null) {
            var data = JSON.readTree(transfer);
            enrollment.setMaXepLopNguon(data.path("MaXLNguon").longValue());
            enrollment.setLyDoChuyen(data.path("LyDo").asString());
        }
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

    // Lấy hồ sơ của năm học theo tổ hợp và điểm giảm dần. Khi bằng điểm, mã học sinh quyết định thứ tự.
    @Override
    public List<Grade10PlacementCandidate> danhSachXepLop10DauNam(Long maNamHoc){
        String sql = """
                SELECT hsnh.MaHoSoNhapHoc,
                hs.MaHS,
                hs.MaSoHS,
                hs.HoTen,
                th.MaToHop,
                th.KyHieu,
                hsnh.TongDiemDauVao
                FROM ho_so_nhap_hoc hsnh
                JOIN hoc_sinh hs ON hs.MaHS = hsnh.MaHS
                JOIN to_hop_mon th ON th.MaToHop = hsnh.MaToHop
                WHERE hsnh.MaNH = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM xep_lop xl
                      JOIN lop_hoc lop ON lop.MaLop = xl.MaLop
                      WHERE xl.MaHS = hs.MaHS
                        AND lop.MaNH = hsnh.MaNH
                  )
                ORDER BY th.KyHieu, hsnh.TongDiemDauVao DESC, hs.MaHS
                """;
        return jdbc.query(sql, (rs, rowNum) -> new Grade10PlacementCandidate(
                rs.getLong("MaHoSoNhapHoc"),
                rs.getLong("MaHS"),
                rs.getString("MaSoHS"),
                rs.getString("HoTen"),
                rs.getLong("MaToHop"),
                rs.getString("KyHieu"),
                rs.getBigDecimal("TongDiemDauVao")
        ), maNamHoc);
    }

    // Kiểm tra năm học đã có đợt chốt để ngăn ghi kết quả lần thứ hai.
    @Override
    public boolean daChotXepLop10(Long maNamHoc) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM dot_xep_lop_10
                WHERE MaNH = ?
            )
            """;

        return Boolean.TRUE.equals(
                jdbc.queryForObject(sql, Boolean.class, maNamHoc));
    }
    // Đọc ngày vào lớp chung mà giáo vụ đã chọn khi chốt đợt.
    @Override
    public Optional<LocalDate> ngayVaoLopDaChot(Long maNamHoc) {
        return jdbc.query("SELECT NgayVaoLop FROM dot_xep_lop_10 WHERE MaNH = ?",
                (rs, rowNum) -> rs.getObject("NgayVaoLop", LocalDate.class), maNamHoc)
                .stream().findFirst();
    }

    // Lưu người chốt, thời điểm chốt, ngày vào lớp chung và mã phương án của đợt.
    @Override
    public void luuDotChotXepLop10(
            Long maNamHoc,
            long maTaiKhoanChot,
            LocalDate ngayVaoLop,
            LocalDateTime thoiDiemChot,
            String maKiemTraPhuongAn) {

        String sql = """
            INSERT INTO dot_xep_lop_10 (
                MaNH, MaTKChot, NgayVaoLop,
                ThoiDiemChot, MaKiemTraPhuongAn
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        jdbc.update(sql, maNamHoc, maTaiKhoanChot, ngayVaoLop,
                thoiDiemChot, maKiemTraPhuongAn);
    }


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
        Long count = jdbc.queryForObject(sql, Long.class, maHocSinh, maNamHoc);
        return count != null && count > 0;
    }
    // Tìm ngày đầu tiên lớp đã đủ sĩ số, xét cả ngày nhận học sinh và các ngày có học sinh vào lớp sau đó.
    @Override
    public Optional<LocalDate> timNgayLopHetCho(
            Long maLop,
            LocalDate ngayChuyen,
            LocalDate ngayKetThucNamHoc,
            int siSoToiDa) {

        String sql = """
            WITH moc_kiem_tra AS (
                SELECT CAST(? AS DATE) AS Ngay

                UNION

                SELECT TuNgay
                FROM xep_lop
                WHERE MaLop = ?
                  AND TuNgay BETWEEN ? AND ?
            )
            SELECT moc.Ngay
            FROM moc_kiem_tra moc
            JOIN xep_lop xl
              ON xl.MaLop = ?
             AND xl.TuNgay <= moc.Ngay
             AND (xl.DenNgay IS NULL OR xl.DenNgay >= moc.Ngay)
            GROUP BY moc.Ngay
            HAVING COUNT(DISTINCT xl.MaHS) >= ?
            ORDER BY moc.Ngay
            LIMIT 1
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> rs.getObject("Ngay", LocalDate.class),
                ngayChuyen,
                maLop,
                ngayChuyen,
                ngayKetThucNamHoc,
                maLop,
                siSoToiDa
        ).stream().findFirst();
    }
    @Override
    public long tao(Enrollment en){
        String sql = """
        INSERT INTO xep_lop (
            MaHS, MaLop, TuNgay, DenNgay, LyDoKetThuc,
            MaTKNguoiThucHien, ThoiDiemThucHien, MaHoSoNhapHoc
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
         jdbc.update(connection ->{
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"MaXL"});
            ps.setLong(1, en.getMaHocSinh());
            ps.setLong(2, en.getMaLop());
            ps.setObject(3, en.getTuNgay(), Types.DATE);
            ps.setObject(4, en.getDenNgay(), Types.DATE);
            ps.setString(5, en.getLyDoKetThuc());
            ps.setLong(6, en.getMaTaiKhoanThucHien());
            ps.setObject(7, en.getThoiDiemThucHien(), Types.TIMESTAMP);
            ps.setObject(8, en.getMaHoSoNhapHoc(), Types.BIGINT);
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

        jdbc.update(sql, maTaiKhoan, maXepLopDich.toString(), maXepLopNguon, maXepLopNguon, maXepLopDich, ngayKetThucNguon.toString(), ngayChuyen.toString(), lyDo, thoiDiem);
    }

    @Override
    public Optional<Enrollment> timTheoMa(Long maXepLop) {
        return jdbc.query(SELECT + " WHERE x.MaXL = ?", MAPPER, maXepLop).stream().findFirst();
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

    @Override
    public List<Enrollment> lichSu(Long maHocSinh) {
        return jdbc.query(SELECT + " WHERE x.MaHS = ? ORDER BY x.TuNgay DESC, x.MaXL DESC", MAPPER, maHocSinh);
    }

  private static final String YEAR_LABEL = "n.MaSoNH";
  private static final String REQUEST_FROM =
      """
      FROM xep_lop x JOIN hoc_sinh h ON h.MaHS=x.MaHS
      JOIN lop_hoc l ON l.MaLop=x.MaLop JOIN nam_hoc n ON n.MaNH=l.MaNH
      """ + TRANSFER_JOIN + """
      WHERE (LOCATE(?,h.HoTen)>0 OR LOCATE(?,h.MaSoHS)>0 OR LOCATE(?,l.TenLop)>0) AND (? IS NULL OR l.MaNH=?)
      """;


  @Override
  public long demHocSinh(String k, Long year) {
    return jdbc.queryForObject("SELECT COUNT(*) " + TIM_HOC_SINH_FROM, Long.class, k, k, year, year);
  }

  @Override
  public List<EnrollmentStudentResponse> timHocSinh(String k, Long year, int size, long offset) {
    return jdbc.query(
        "SELECT MaHS,MaSoHS,HoTen,NgaySinh " + TIM_HOC_SINH_FROM + " ORDER BY MaHS DESC LIMIT ? OFFSET ?",
        HOC_SINH_MAPPER,
        k,
        k,
        year, year,
        size,
        offset);
  }

  @Override
  public long demYeuCau(String k, Long year) {
    return Objects.requireNonNull(
        jdbc.queryForObject("SELECT COUNT(*) " + REQUEST_FROM, Long.class, k, k, k, year, year));
  }

  @Override
  public List<EnrollmentRequestResponse> timYeuCau(String k, Long year, int size, long offset) {
    return jdbc.query(
        "SELECT x.MaXL,x.MaHS,h.MaSoHS,h.HoTen,x.MaLop,l.TenLop,"
            + YEAR_LABEL
            + ",x.TuNgay,x.DenNgay,y.DuLieuSau "
            + REQUEST_FROM
            + " ORDER BY x.MaXL DESC LIMIT ? OFFSET ?",
        (r, i) -> {
          var payload = r.getString(10);
          var transfer = payload == null ? null : JSON.readTree(payload);
          return new EnrollmentRequestResponse(
                r.getLong(1),
                r.getLong(2),
                r.getString(3),
                r.getString(4),
                r.getLong(5),
                r.getString(6),
                r.getString(7),
                r.getObject(8, LocalDate.class),
                r.getObject(9, LocalDate.class),
                transfer == null ? null : transfer.path("MaXLNguon").longValue(),
                transfer == null ? null : transfer.path("LyDo").asString());
        },
        k, k, k, year, year,
        size,
        offset);
  }
}
