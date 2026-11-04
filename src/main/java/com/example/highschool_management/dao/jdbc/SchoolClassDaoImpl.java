package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dto.response.ClassCatalogResponse.*;
import com.example.highschool_management.dto.response.SchoolClassListResponse;
import com.example.highschool_management.model.SchoolClass;
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
    String sql = "Insert into lop_hoc (MaNH, MaKhoi, TenLop, TrangThai) values (?, ?, ?, ?)";
    var keyHolder = new GeneratedKeyHolder();

    int soDong =
        jdbcTemplate.update(
            connection -> {
              var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
              ps.setLong(1, schoolClass.getMaNamHoc());
              ps.setLong(2, schoolClass.getMaKhoi());
              ps.setString(3, schoolClass.getTenLop());
              ps.setString(4, schoolClass.getTrangThai());
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
            TrangThai = ?
        WHERE MaLop = ?
        """;

    jdbcTemplate.update(
        sql,
        ps -> {
          ps.setString(1, lop.getTenLop());

          ps.setString(2, lop.getTrangThai());
          ps.setLong(3, lop.getMaLop());
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

  private static final String YEAR_LABEL = "CONCAT(YEAR(n.NgayBatDau), '-', YEAR(n.NgayKetThuc))";
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
                CONCAT(
                    YEAR(namHoc.NgayBatDau),
                    '-',
                    YEAR(namHoc.NgayKetThuc)
                ) AS NamHoc,
                khoi.SoKhoi,
                lop.TrangThai,

                -- Đếm học sinh đã duyệt, còn thuộc lớp tại ngày được chọn
                (
                    SELECT COUNT(DISTINCT xepLop.MaHS)
                    FROM xep_lop xepLop
                    WHERE xepLop.MaLop = lop.MaLop
                      AND xepLop.TrangThaiDuyet = 'DA_DUYET'
                      AND xepLop.TuNgay <= ?
                      AND (
                          xepLop.DenNgay IS NULL
                          OR xepLop.DenNgay > ?
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
}
