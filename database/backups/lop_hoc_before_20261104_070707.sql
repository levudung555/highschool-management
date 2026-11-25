CREATE TABLE `lop_hoc` (
  `MaLop` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaNH` bigint unsigned NOT NULL,
  `MaKhoi` bigint unsigned NOT NULL,
  `TenLop` varchar(30) NOT NULL,
  `SiSoToiDa` smallint unsigned DEFAULT NULL,
  `TrangThai` varchar(20) NOT NULL DEFAULT 'HOAT_DONG',
  PRIMARY KEY (`MaLop`),
  UNIQUE KEY `uk_lop_nam_ten` (`MaNH`,`TenLop`),
  KEY `fk_LOP_HOC_MaKhoi` (`MaKhoi`),
  CONSTRAINT `fk_LOP_HOC_MaKhoi` FOREIGN KEY (`MaKhoi`) REFERENCES `khoi_lop` (`MaKhoi`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_LOP_HOC_MaNH` FOREIGN KEY (`MaNH`) REFERENCES `nam_hoc` (`MaNH`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_lop_siso` CHECK (((`SiSoToiDa` is null) or (`SiSoToiDa` > 0))),
  CONSTRAINT `ck_lop_ten` CHECK ((char_length(trim(`TenLop`)) > 0)),
  CONSTRAINT `ck_lop_trangthai` CHECK ((`TrangThai` in (_utf8mb4'HOAT_DONG',_utf8mb4'NGUNG_HOAT_DONG')))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
