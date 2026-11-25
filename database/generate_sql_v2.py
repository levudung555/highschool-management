"""Regenerate the reviewed ERD DDL from the original, read-only Workbench export."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent
source = (ROOT / 'exports/highschool_management_full.sql').read_text(encoding='utf-8-sig')
tables = {}
for match in re.finditer(r'CREATE TABLE `([^`]+)` \(.*?\) ENGINE=InnoDB[^;]*;', source, re.S):
    name = match.group(1)
    ddl = re.sub(r' AUTO_INCREMENT=\d+', '', match.group(0))
    # Remove dump-specific character-set introducers; connection uses utf8mb4.
    ddl = ddl.replace('_utf8mb4', '')
    tables[name] = ddl

tables.pop('yeu_cau_chuyen_lop')
tables['xep_lop'] = '''CREATE TABLE `xep_lop` (
  `MaXL` bigint unsigned NOT NULL AUTO_INCREMENT,
  `MaHS` bigint unsigned NOT NULL,
  `MaLop` bigint unsigned NOT NULL,
  `TuNgay` date NOT NULL COMMENT 'Ngay dau tien thuoc lop (bao gom)',
  `DenNgay` date DEFAULT NULL COMMENT 'Ngay cuoi cung thuoc lop (bao gom); NULL = chua ket thuc',
  `LyDoKetThuc` varchar(255) DEFAULT NULL,
  `MaTKNguoiThucHien` bigint unsigned NOT NULL,
  `ThoiDiemThucHien` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`MaXL`),
  KEY `ix_XEP_LOP_MaHS_TuNgay_DenNgay` (`MaHS`,`TuNgay`,`DenNgay`),
  KEY `ix_XEP_LOP_MaLop_TuNgay_DenNgay` (`MaLop`,`TuNgay`,`DenNgay`),
  KEY `fk_XEP_LOP_MaTKNguoiThucHien` (`MaTKNguoiThucHien`),
  CONSTRAINT `fk_XEP_LOP_MaHS` FOREIGN KEY (`MaHS`) REFERENCES `hoc_sinh` (`MaHS`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_XEP_LOP_MaLop` FOREIGN KEY (`MaLop`) REFERENCES `lop_hoc` (`MaLop`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_XEP_LOP_MaTKNguoiThucHien` FOREIGN KEY (`MaTKNguoiThucHien`) REFERENCES `tai_khoan` (`MaTK`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ck_xl_thoigian` CHECK (`DenNgay` IS NULL OR `DenNgay` >= `TuNgay`),
  CONSTRAINT `ck_xl_lydo` CHECK (`LyDoKetThuc` IS NULL OR CHAR_LENGTH(TRIM(`LyDoKetThuc`)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;'''

# Create parents before children, with foreign-key checking left enabled.
ordered = []
pending = dict(tables)
while pending:
    ready = [name for name, ddl in pending.items()
             if set(re.findall(r'REFERENCES `([^`]+)`', ddl)) <= set(ordered)]
    if not ready:
        raise ValueError('Foreign-key dependency cycle or missing table')
    for name in ready:
        ordered.append(name)
        pending.pop(name)
assert len(ordered) == 23

header = '''-- HE THONG QUAN LY HOC SINH THPT - ERD F01-F20
-- MySQL >= 8.0.16; InnoDB; utf8mb4. Generated: 2026-11-08.
-- FRESH INSTALL ONLY. No DROP/TRUNCATE; no existing data is replaced.
-- Default isolated database. Change BOTH database names below if necessary.
-- Stop on the first SQL error; do not import with mysql --force.
-- Re-running against an existing schema intentionally fails on CREATE TABLE.
-- XEP_LOP uses inclusive [TuNgay, DenNgay], direct operations, no approval.
-- Other approval workflows and their original time conventions are preserved.
-- Cross-table rules, authorization, locks and atomic transfers belong to service.
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET SESSION time_zone = '+07:00';
CREATE DATABASE IF NOT EXISTS `highschool_management_v2`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `highschool_management_v2`;

'''
out = header + '\n\n'.join(f'-- {i:02d}. {name.upper()}\n{tables[name]}'
                              for i, name in enumerate(ordered, 1)) + '\n'
(ROOT / 'highschool_management_v2.sql').write_text(out, encoding='utf-8')
print(f'Generated highschool_management_v2.sql: {len(ordered)} tables, dependency-ordered, no DROP.')
