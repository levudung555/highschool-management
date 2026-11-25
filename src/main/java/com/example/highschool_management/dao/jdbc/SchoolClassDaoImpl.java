package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dto.response.ClassCatalogResponse.*;
import com.example.highschool_management.dto.response.SchoolClassListResponse;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.dto.response.Grade10ClassProposalResponse;
import com.example.highschool_management.dto.response.SubjectCombinationPreferenceSummaryResponse;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class SchoolClassDaoImpl implements SchoolClassDao {
  private final JdbcTemplate jdbcTemplate;

  private static final RowMapper<SchoolClass> LOP_ROW_MAPPER =
      (rs, rowNum) -> {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setMaLop(rs.getLong("MaLop"));
        schoolClass.setMaNamHoc(rs.getLong("MaNH"));
        schoolClass.setMaKhoi(rs.getLong("MaKhoi"));
        schoolClass.setTenLop(rs.getString("TenLop"));
        schoolClass.setTrangThai(rs.getString("TrangThai"));
        schoolClass.setMaToHop(rs.getObject("MaToHop", Long.class));
        schoolClass.setSiSoToiDa(rs.getObject("SiSoToiDa", Integer.class));
        schoolClass.setThuTuXep(rs.getObject("ThuTuXep", Integer.class));
        return schoolClass;
      };

  public SchoolClassDaoImpl(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Optional<SchoolClass> findById(Long id) {
    String sql = "SELECT * FROM lop_hoc WHERE MaLop = ?";
    return jdbcTemplate.query(sql, LOP_ROW_MAPPER, id).stream().findFirst();
  }

  @Override
  public boolean tonTaiNamHoc(Long maNamHoc) {
    String sql = "Select exists (select 1 from nam_hoc where MaNH = ?)";
    boolean check = jdbcTemplate.queryForObject(sql, Boolean.class, maNamHoc);
    return check;
  }

  @Override
  public boolean tonTaiKhoi(Long maKhoi) {
    String sql = "Select exists (select 1 from khoi_lop where MaKhoi = ?)";
    boolean check = jdbcTemplate.queryForObject(sql, Boolean.class, maKhoi);
    return check;
  }

  @Override
  public boolean tonTaiLop(String tenLop, Long maNamHoc) {
    String sql = "Select exists (select 1 from lop_hoc where TenLop = ? and MaNH = ?)";
    boolean check = jdbcTemplate.queryForObject(sql, Boolean.class, tenLop, maNamHoc);
    return check;
  }

  @Override
  public Long themLop(SchoolClass schoolClass) {
    String sql = "Insert into lop_hoc (MaNH, MaKhoi, TenLop, TrangThai, MaToHop, SiSoToiDa, ThuTuXep) values (?, ?, ?, ?, ?, ?, ?)";
    var keyHolder = new GeneratedKeyHolder();

    int soDong =
        jdbcTemplate.update(
            connection -> {
              var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
              ps.setLong(1, schoolClass.getMaNamHoc());
              ps.setLong(2, schoolClass.getMaKhoi());
              ps.setString(3, schoolClass.getTenLop());
              ps.setString(4, schoolClass.getTrangThai());
              ps.setObject(5, schoolClass.getMaToHop());
              ps.setObject(6, schoolClass.getSiSoToiDa());
              ps.setObject(7, schoolClass.getThuTuXep());
              return ps;
            },
            keyHolder);
    Number malop = keyHolder.getKey();
    if (soDong != 1 || malop == null) {
      throw new IllegalStateException("Không thể thêm lớp học"); // check
    }
    return malop.longValue();
  }

  @Override
  public Optional<SchoolClass> timTheoMaDeCapNhat(Long maLop) {
    String sql = "SELECT * FROM lop_hoc WHERE MaLop = ? FOR UPDATE"; // check
    return jdbcTemplate.query(sql, LOP_ROW_MAPPER, maLop).stream().findFirst();
  }

  @Override
  public boolean tonTaiLopKhac(Long maLop, String tenLop, Long maNamHoc) {
    String sql =
        "Select exists (select 1 from lop_hoc where MaLop <> ? and TenLop = ? and MaNH = ?)";
    boolean check = jdbcTemplate.queryForObject(sql, Boolean.class, maLop, tenLop, maNamHoc);
    return check;
  }

  @Override
  public void capNhat(SchoolClass lop) {
    String sql =
        """
        UPDATE lop_hoc
        SET TenLop = ?,
            TrangThai = ?, MaToHop = ?, SiSoToiDa = ?, ThuTuXep = ?
        WHERE MaLop = ?
        """;

    jdbcTemplate.update(
        sql,
        ps -> {
          ps.setString(1, lop.getTenLop());

          ps.setString(2, lop.getTrangThai());
          ps.setObject(3, lop.getMaToHop());
          ps.setObject(4, lop.getSiSoToiDa());
          ps.setObject(5, lop.getThuTuXep());
          ps.setLong(6, lop.getMaLop());
        });
  }

  @Override
  public boolean tonTaiDuLieuLienQuan(Long maLop) {
    String sql =
        """
        SELECT EXISTS (
            SELECT 1
            FROM xep_lop
            WHERE MaLop = ?
        )
        OR EXISTS (
            SELECT 1
            FROM phan_cong_chu_nhiem
            WHERE MaLop = ?
        )
        OR EXISTS (
            SELECT 1
            FROM lop_mon_hoc
            WHERE MaLop = ?
        )
        OR EXISTS (
            SELECT 1
            FROM phien_diem_danh
            WHERE MaLop = ?
        )

        """;
    boolean check = jdbcTemplate.queryForObject(sql, Boolean.class, maLop, maLop, maLop, maLop);
    return check;
  }

  @Override
  public int xoaTheoMa(Long maLop) {
    String sql = "DELETE FROM lop_hoc WHERE MaLop = ?";
    return jdbcTemplate.update(sql, maLop);
  }

  private static final String YEAR_LABEL = "n.MaSoNH";
  private static final String CLASS_FILTER =
      """
      WHERE LOCATE(?,l.TenLop)>0 AND (? IS NULL OR l.MaNH=?)
      AND (? IS NULL OR l.MaKhoi=?) AND (?='' OR l.TrangThai=?)
      """;

  @Override
  public List<NamHoc> danhSachNamHoc() {
    return jdbcTemplate.query(
        "SELECT MaNH,"
            + YEAR_LABEL
            + ",NgayBatDau,NgayKetThuc FROM nam_hoc n ORDER BY NgayBatDau DESC",
        (r, i) ->
            new NamHoc(
                r.getLong(1),
                r.getString(2),
                r.getObject(3, LocalDate.class),
                r.getObject(4, LocalDate.class)));
  }

  @Override
  public List<Khoi> danhSachKhoi() {
    return jdbcTemplate.query(
        "SELECT MaKhoi,SoKhoi FROM khoi_lop ORDER BY SoKhoi",
        (r, i) -> new Khoi(r.getLong(1), r.getInt(2)));
  }

  @Override
  public long demDanhSach(String k, Long y, Long g, String s) {
    return Objects.requireNonNull(
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lop_hoc l " + CLASS_FILTER, Long.class, k, y, y, g, g, s, s));
  }

  @Override
  public List<SchoolClassListResponse> timDanhSach(
          String tuKhoa,
          Long maNamHoc,
          Long maKhoi,
          String trangThai,
          LocalDate ngayTinhSiSo,
          int kichThuocTrang,
          long viTriBatDau
  ) {
    String sql = """
            SELECT
                lop.MaLop,
                lop.MaNH,
                lop.MaKhoi,
                lop.TenLop,
                namHoc.MaSoNH AS NamHoc,
                khoi.SoKhoi,
                lop.TrangThai,

                -- Đếm học sinh còn thuộc lớp tại ngày được chọn
                (
                    SELECT COUNT(DISTINCT xepLop.MaHS)
                    FROM xep_lop xepLop
                    WHERE xepLop.MaLop = lop.MaLop
                      AND xepLop.TuNgay <= ?
                      AND (
                          xepLop.DenNgay IS NULL
                          OR xepLop.DenNgay >= ?
                      )
                ) AS SiSo

            FROM lop_hoc lop
            JOIN nam_hoc namHoc ON namHoc.MaNH = lop.MaNH
            JOIN khoi_lop khoi ON khoi.MaKhoi = lop.MaKhoi

            WHERE LOCATE(?, lop.TenLop) > 0
              AND (? IS NULL OR lop.MaNH = ?)
              AND (? IS NULL OR lop.MaKhoi = ?)
              AND (? = '' OR lop.TrangThai = ?)

            ORDER BY lop.MaLop DESC
            LIMIT ? OFFSET ?
            """;

    return jdbcTemplate.query(
            sql,
            (resultSet, rowNumber) -> new SchoolClassListResponse(
                    resultSet.getLong("MaLop"),
                    resultSet.getLong("MaNH"),
                    resultSet.getLong("MaKhoi"),
                    resultSet.getString("TenLop"),
                    resultSet.getString("NamHoc"),
                    resultSet.getInt("SoKhoi"),
                    resultSet.getString("TrangThai"),
                    resultSet.getLong("SiSo")
            ),
            ngayTinhSiSo,
            ngayTinhSiSo,
            tuKhoa,
            maNamHoc,
            maNamHoc,
            maKhoi,
            maKhoi,
            trangThai,
            trangThai,
            kichThuocTrang,
            viTriBatDau
    );
  }

@Override
  public List<SubjectCombinationPreferenceSummaryResponse> thongKeNguyenVongTheoNam(Long maNamHoc){
     String sql = """
             SELECT
                    nh.MaSoNH AS NamHoc,
                    th.MaToHop AS MaToHop,
                    th.KyHieu AS KyHieu,
                    th.TenToHop AS TenToHop,
                    COUNT(hs.MaHoSoNhapHoc) AS SoHocSinhDangKy
             FROM nam_hoc nh 
             JOIN to_hop_mon th ON th.MaNH = nh.MaNH
             LEFT JOIN ho_so_nhap_hoc hs ON hs.MaNH = nh.MaNH AND hs.MaToHop = th.MaToHop
             WHERE nh.MaNH = ?
             GROUP BY nh.MaSoNH, th.MaToHop, th.KyHieu, th.TenToHop
             ORDER BY th.KyHieu
             """;
     return jdbcTemplate.query(
             sql,
             (resultSet, rowNumber) -> new SubjectCombinationPreferenceSummaryResponse(
                     resultSet.getLong("MaToHop"),
                     resultSet.getString("KyHieu"),
                     resultSet.getString("TenToHop"),
                     resultSet.getLong("SoHocSinhDangKy")
             ),
             maNamHoc
     );
  }

    // Trừ sức chứa của các lớp 10 đã chuẩn bị khỏi số hồ sơ từng tổ hợp, rồi quy đổi số chỗ thiếu thành số lớp mới.
    @Override
    public List<Grade10ClassProposalResponse> deXuatMoLop10(Long maNamHoc, LocalDate homNay, int chiTieuLopMoi) {
        String sql = """
        WITH nam_dang_xet AS (
            SELECT MaNH, MaSoNH
            FROM nam_hoc
            WHERE MaNH = ?
              AND NgayKetThuc >= ?
        ),
        nhu_cau AS (
            SELECT
                nam.MaSoNH,
                th.MaToHop,
                th.KyHieu,
                COUNT(hs.MaHoSoNhapHoc) AS TongNguyenVong
            FROM nam_dang_xet nam
            JOIN to_hop_mon th ON th.MaNH = nam.MaNH
            LEFT JOIN ho_so_nhap_hoc hs
                ON hs.MaNH = th.MaNH
               AND hs.MaToHop = th.MaToHop
            GROUP BY nam.MaSoNH, th.MaToHop, th.KyHieu
        ),
        lop_da_chuan_bi AS (
            SELECT
                lop.MaToHop,
                COUNT(*) AS SoLopDaChuanBi,
                SUM(lop.SiSoToiDa) AS TongSucChua
            FROM nam_dang_xet nam
            JOIN lop_hoc lop ON lop.MaNH = nam.MaNH
            JOIN khoi_lop khoi ON khoi.MaKhoi = lop.MaKhoi
            WHERE khoi.SoKhoi = 10
              AND lop.TrangThai = 'HOAT_DONG'
              AND lop.MaToHop IS NOT NULL
              AND lop.SiSoToiDa > 0
              AND lop.ThuTuXep > 0
              AND NOT EXISTS (
                  SELECT 1
                  FROM xep_lop xl
                  WHERE xl.MaLop = lop.MaLop
              )
            GROUP BY lop.MaToHop
        ),
        de_xuat AS (
            SELECT
                nc.MaSoNH,
                nc.MaToHop,
                nc.KyHieu,
                nc.TongNguyenVong,
                COALESCE(lop.SoLopDaChuanBi, 0) AS SoLopDaChuanBi,
                COALESCE(lop.TongSucChua, 0) AS TongSucChua,
                GREATEST(
                    0,
                    CAST(nc.TongNguyenVong AS SIGNED)
                        - COALESCE(lop.TongSucChua, 0)
                ) AS SoChoConThieu
            FROM nhu_cau nc
            LEFT JOIN lop_da_chuan_bi lop ON lop.MaToHop = nc.MaToHop
        )
        SELECT
            MaSoNH AS NamHoc,
            MaToHop,
            KyHieu,
            TongNguyenVong,
            SoLopDaChuanBi,
            TongSucChua,
            SoChoConThieu,
            (SoChoConThieu + ? - 1) DIV ? AS SoLopCanMoThem
        FROM de_xuat
        ORDER BY KyHieu
        """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Grade10ClassProposalResponse(
                        rs.getLong("MaToHop"),
                        rs.getString("KyHieu"),
                        rs.getLong("TongNguyenVong"),
                        rs.getLong("SoLopDaChuanBi"),
                        rs.getLong("TongSucChua"),
                        rs.getLong("SoChoConThieu"),
                        rs.getLong("SoLopCanMoThem")
                ),
                maNamHoc,
                homNay,
                chiTieuLopMoi,
                chiTieuLopMoi
        );
    }

    // Khóa dòng năm học để các lần xác nhận mở lớp hoặc chốt xếp lớp cùng năm được xử lý lần lượt.
    @Override
    public Optional<LocalDate> khoaNamHocDeMoLop(Long maNamHoc) {
        String sql = """
            SELECT NgayKetThuc
            FROM nam_hoc
            WHERE MaNH = ?
            FOR UPDATE
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getObject("NgayKetThuc", LocalDate.class),
                maNamHoc
        ).stream().findFirst();
    }

    // Tìm lớp cùng tên trong đúng năm học để kiểm tra khả năng dùng lại khi xác nhận mở lớp.
    @Override
    public Optional<SchoolClass> timLopTheoNamVaTen(Long maNamHoc, String tenLop) {
        String sql = """
            SELECT MaLop, MaNH, MaKhoi, TenLop, TrangThai,
                   MaToHop, SiSoToiDa, ThuTuXep
            FROM lop_hoc
            WHERE MaNH = ?
              AND TenLop = ?
            FOR UPDATE
            """;

        return jdbcTemplate.query(
                sql,
                LOP_ROW_MAPPER,
                maNamHoc,
                tenLop
        ).stream().findFirst();
    }

    // Chọn các lớp 10 đang hoạt động, có cấu hình hợp lệ và chưa có bản ghi xếp lớp, theo thứ tự xếp trong tổ hợp.
    @Override
    public List<SchoolClass> danhSachLop10DeXepDauNam(Long maNamHoc){
      String sql = """
              SELECT lop.MaLop,
              lop.MaNH,
              lop.MaKhoi,
              lop.TenLop,
              lop.TrangThai,
              lop.MaToHop,
              lop.SiSoToiDa,
              lop.ThuTuXep
              FROM lop_hoc lop
              JOIN khoi_lop khoi ON khoi.MaKhoi = lop.MaKhoi
              JOIN to_hop_mon th ON th.MaNH = lop.MaNH AND th.MaToHop = lop.MaToHop
              WHERE lop.MaNH = ?
              AND khoi.SoKhoi = 10
                AND lop.TrangThai = 'HOAT_DONG'
                AND lop.SiSoToiDa > 0
                AND lop.ThuTuXep > 0
                AND NOT EXISTS (
                    SELECT 1
                    FROM xep_lop xl
                    WHERE xl.MaLop = lop.MaLop
                )
              ORDER BY th.KyHieu, lop.ThuTuXep
              """;
        return jdbcTemplate.query(
                sql,
                LOP_ROW_MAPPER,
                maNamHoc
        );
    }
}
