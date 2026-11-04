package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.EnrollmentDao;
import com.example.highschool_management.dto.response.ClassStudentResponse;
import com.example.highschool_management.dto.response.EnrollmentRequestResponse;
import com.example.highschool_management.dto.response.EnrollmentStudentResponse;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.model.Enrollment;
import java.sql.Statement;
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

  private static final String SELECT =
      """
      SELECT x.*, y.MaXLNguon, y.LyDo FROM xep_lop x
      LEFT JOIN yeu_cau_chuyen_lop y ON y.MaXLDich = x.MaXL
      """;
  private static final RowMapper<Enrollment> MAPPER =
      (rs, row) -> {
        var e = new Enrollment();
        e.setMaXepLop(rs.getLong("MaXL"));
        e.setMaHocSinh(rs.getLong("MaHS"));
        e.setMaLop(rs.getLong("MaLop"));
        e.setTuNgay(rs.getObject("TuNgay", LocalDate.class));
        e.setDenNgay(rs.getObject("DenNgay", LocalDate.class));
        e.setLyDoKetThuc(rs.getString("LyDoKetThuc"));
        e.setTrangThaiDuyet(rs.getString("TrangThaiDuyet"));
        e.setMaTaiKhoanDuyet(rs.getObject("MaTKDuyet", Long.class));
        e.setDuyetLuc(rs.getObject("DuyetLuc", LocalDateTime.class));
        e.setMaXepLopNguon(rs.getObject("MaXLNguon", Long.class));
        e.setLyDoChuyen(rs.getString("LyDo"));
        return e;
      };

  @Override
  public boolean khoaHocSinh(Long id) {
    return !jdbc.query(
            "SELECT MaHS FROM hoc_sinh WHERE MaHS = ? FOR UPDATE", (r, n) -> r.getLong(1), id)
        .isEmpty();
  }

  @Override
  public NamHoc namHoc(Long id) {
    return jdbc
        .query(
            "SELECT NgayBatDau, NgayKetThuc FROM nam_hoc WHERE MaNH = ?",
            (r, n) -> new NamHoc(r.getObject(1, LocalDate.class), r.getObject(2, LocalDate.class)),
            id)
        .stream()
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học"));
  }

  @Override
  public Optional<Enrollment> timTheoMa(Long id) {
    return jdbc.query(SELECT + " WHERE x.MaXL = ?", MAPPER, id).stream().findFirst();
  }

  @Override
  public List<Enrollment> lichSu(Long id) {
    return jdbc.query(SELECT + " WHERE x.MaHS = ? ORDER BY x.TuNgay DESC, x.MaXL DESC", MAPPER, id);
  }

  @Override
  public boolean trungThoiGian(Long hs, LocalDate start, LocalDate end, Long skip, Long source) {
    var count =
        jdbc.queryForObject(
            """
            SELECT COUNT(*) FROM xep_lop WHERE MaHS = ?
            AND TrangThaiDuyet IN ('CHO_DUYET','DA_DUYET')
            AND (? IS NULL OR MaXL <> ?) AND (? IS NULL OR MaXL <> ?)
            AND (DenNgay IS NULL OR DenNgay > ?)
            AND (? IS NULL OR TuNgay < ?)
            """,
            Long.class,
            hs,
            skip,
            skip,
            source,
            source,
            start,
            end,
            end);
    return Objects.requireNonNull(count) > 0;
  }

  @Override
  public long taoChoDuyet(Enrollment e) {
    var key = new GeneratedKeyHolder();
    jdbc.update(
        c -> {
          var ps =
              c.prepareStatement(
                  """
                  INSERT INTO xep_lop (MaHS, MaLop, TuNgay, DenNgay, TrangThaiDuyet)
                  VALUES (?, ?, ?, ?, 'CHO_DUYET')
                  """,
                  Statement.RETURN_GENERATED_KEYS);
          ps.setLong(1, e.getMaHocSinh());
          ps.setLong(2, e.getMaLop());
          ps.setObject(3, e.getTuNgay());
          ps.setObject(4, e.getDenNgay(), java.sql.Types.DATE);
          return ps;
        },
        key);
    return Objects.requireNonNull(key.getKey(), "Không lấy được mã xếp lớp").longValue();
  }

  @Override
  public void luuYeuCauChuyen(Long target, Long source, String reason) {
    jdbc.update(
        "INSERT INTO yeu_cau_chuyen_lop (MaXLDich, MaXLNguon, LyDo) VALUES (?, ?, ?)",
        target,
        source,
        reason);
  }

  @Override
  public void ketThuc(Long id, LocalDate end, String reason) {
    jdbc.update("UPDATE xep_lop SET DenNgay = ?, LyDoKetThuc = ? WHERE MaXL = ?", end, reason, id);
  }

  @Override
  public void quyetDinh(Long id, String status, long actor, LocalDateTime at) {
    int changed =
        jdbc.update(
            """
            UPDATE xep_lop SET TrangThaiDuyet = ?, MaTKDuyet = ?, DuyetLuc = ?
            WHERE MaXL = ? AND TrangThaiDuyet = 'CHO_DUYET'
            """,
            status,
            actor,
            at,
            id);
    if (changed != 1) throw new IllegalStateException("Trạng thái yêu cầu đã thay đổi");
  }

  @Override
  public long demSiSo(Long id, LocalDate day) {
    return Objects.requireNonNull(
        jdbc.queryForObject(
            """
            SELECT COUNT(DISTINCT MaHS) FROM xep_lop WHERE MaLop = ? AND TrangThaiDuyet = 'DA_DUYET'
            AND TuNgay <= ? AND (DenNgay IS NULL OR DenNgay > ?)
            """,
            Long.class,
            id,
            day,
            day));
  }

  @Override
  public long demDanhSachLop(Long id, LocalDate day) {
    return demSiSo(id, day);
  }

  @Override
  public List<ClassStudentResponse> danhSachLop(Long id, LocalDate day, int size, long offset) {
    return jdbc.query(
        """
        SELECT x.MaXL, h.MaHS, h.MaSoHS, h.HoTen, h.NgaySinh, x.TuNgay, x.DenNgay
        FROM xep_lop x JOIN hoc_sinh h ON h.MaHS = x.MaHS
        WHERE x.MaLop = ? AND x.TrangThaiDuyet = 'DA_DUYET'
        AND x.TuNgay <= ? AND (x.DenNgay IS NULL OR x.DenNgay > ?)
        ORDER BY h.MaHS, x.MaXL LIMIT ? OFFSET ?
        """,
        (r, n) ->
            new ClassStudentResponse(
                r.getLong(1),
                r.getLong(2),
                r.getString(3),
                r.getString(4),
                r.getObject(5, LocalDate.class),
                r.getObject(6, LocalDate.class),
                r.getObject(7, LocalDate.class)),
        id,
        day,
        day,
        size,
        offset);
  }

  private static final String YEAR_LABEL = "CONCAT(YEAR(n.NgayBatDau), '-', YEAR(n.NgayKetThuc))";
  private static final String REQUEST_FROM =
      """
      FROM xep_lop x JOIN hoc_sinh h ON h.MaHS=x.MaHS
      JOIN lop_hoc l ON l.MaLop=x.MaLop JOIN nam_hoc n ON n.MaNH=l.MaNH
      LEFT JOIN yeu_cau_chuyen_lop y ON y.MaXLDich=x.MaXL
      WHERE (?='' OR x.TrangThaiDuyet=?)
      AND (LOCATE(?,h.HoTen)>0 OR LOCATE(?,h.MaSoHS)>0 OR LOCATE(?,l.TenLop)>0)
      """;

  @Override
  public long demHocSinh(String k) {
    return Objects.requireNonNull(
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM hoc_sinh WHERE LOCATE(?,HoTen)>0 OR LOCATE(?,MaSoHS)>0",
            Long.class,
            k,
            k));
  }

  @Override
  public List<EnrollmentStudentResponse> timHocSinh(String k, int size, long offset) {
    return jdbc.query(
        "SELECT MaHS,MaSoHS,HoTen,NgaySinh FROM hoc_sinh WHERE LOCATE(?,HoTen)>0 OR"
            + " LOCATE(?,MaSoHS)>0 ORDER BY MaHS DESC LIMIT ? OFFSET ?",
        (r, i) ->
            new EnrollmentStudentResponse(
                r.getLong(1), r.getString(2), r.getString(3), r.getObject(4, LocalDate.class)),
        k,
        k,
        size,
        offset);
  }

  @Override
  public long demYeuCau(String k, String s) {
    return Objects.requireNonNull(
        jdbc.queryForObject("SELECT COUNT(*) " + REQUEST_FROM, Long.class, s, s, k, k, k));
  }

  @Override
  public List<EnrollmentRequestResponse> timYeuCau(String k, String s, int size, long offset) {
    return jdbc.query(
        "SELECT x.MaXL,x.MaHS,h.MaSoHS,h.HoTen,x.MaLop,l.TenLop,"
            + YEAR_LABEL
            + ",x.TuNgay,x.DenNgay,x.TrangThaiDuyet,y.MaXLNguon,y.LyDo "
            + REQUEST_FROM
            + " ORDER BY x.MaXL DESC LIMIT ? OFFSET ?",
        (r, i) ->
            new EnrollmentRequestResponse(
                r.getLong(1),
                r.getLong(2),
                r.getString(3),
                r.getString(4),
                r.getLong(5),
                r.getString(6),
                r.getString(7),
                r.getObject(8, LocalDate.class),
                r.getObject(9, LocalDate.class),
                r.getString(10),
                r.getObject(11, Long.class),
                r.getString(12)),
        s,
        s,
        k,
        k,
        k,
        size,
        offset);
  }
}
