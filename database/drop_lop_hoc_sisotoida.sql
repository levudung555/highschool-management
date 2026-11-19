-- Áp dụng một lần cho schema cũ còn cột SiSoToiDa. Dữ liệu lớp/xếp lớp được giữ nguyên.
USE highschool_management;
ALTER TABLE lop_hoc
    DROP CHECK ck_lop_siso,
    DROP COLUMN SiSoToiDa;
