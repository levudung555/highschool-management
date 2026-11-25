package com.example.highschool_management.dao.jdbc;

import com.example.highschool_management.dao.AdmissionRecordDao;
import com.example.highschool_management.dao.AdmissionRecordDao.ToHopOption;
import com.example.highschool_management.dao.AdmissionRecordDao.HoSoNhapHocDetail;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdmissionRecordDaoImpl implements AdmissionRecordDao {
  private final JdbcTemplate jdbcTemplate;

  public AdmissionRecordDaoImpl(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<ToHopOption> danhSachToHopTheoNamHoc(Long maNamHoc) {
    return jdbcTemplate.query(
        "SELECT MaToHop, KyHieu FROM to_hop_mon WHERE MaNH = ? ORDER BY KyHieu",
        (rs, rowNum) ->
            new ToHopOption(rs.getLong("MaToHop"), rs.getString("KyHieu")),
        maNamHoc);
  }

  @Override
  public List<HoSoNhapHocDetail> danhSachHoSoTheoHocSinh(Long maHS) {
    return jdbcTemplate.query(
        """
        SELECT
          hsnh.MaHoSoNhapHoc,
          hsnh.MaNH,
          nh.MaSoNH AS NamHoc,
          hsnh.MaToHop,
          th.KyHieu AS KyHieuToHop,
          th.TenToHop,
          hsnh.TongDiemDauVao
        FROM ho_so_nhap_hoc hsnh
        JOIN nam_hoc nh ON nh.MaNH = hsnh.MaNH
        JOIN to_hop_mon th ON th.MaNH = hsnh.MaNH AND th.MaToHop = hsnh.MaToHop
        WHERE hsnh.MaHS = ?
        ORDER BY nh.NgayBatDau DESC, hsnh.MaHoSoNhapHoc DESC
        """,
        (rs, rowNum) ->
            new HoSoNhapHocDetail(
                rs.getLong("MaHoSoNhapHoc"),
                rs.getLong("MaNH"),
                rs.getString("NamHoc"),
                rs.getLong("MaToHop"),
                rs.getString("KyHieuToHop"),
                rs.getString("TenToHop"),
                rs.getBigDecimal("TongDiemDauVao")),
        maHS);
  }

  @Override
  public void taoHoSo(Long maHS, Long maNamHoc, Long maToHop, BigDecimal tongDiemDauVao) {
    jdbcTemplate.update(
        "INSERT INTO ho_so_nhap_hoc (MaHS, MaNH, MaToHop, TongDiemDauVao) VALUES (?, ?, ?, ?)",
        maHS,
        maNamHoc,
        maToHop,
        tongDiemDauVao);
  }
}
