-- Chạy một lần trên database đã tồn tại trước khi bật import phụ huynh.
ALTER TABLE nguoi_giam_ho
  ADD COLUMN CMND varchar(20) DEFAULT NULL AFTER HoTen,
  ADD UNIQUE KEY uk_NGUOI_GIAM_HO_CMND (CMND);
