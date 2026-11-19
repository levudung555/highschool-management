-- Chạy một lần trên schema highschool_management hiện có, trước khi dùng API UC09.
-- Không sửa/xóa dữ liệu cũ. Không chạy script này trên database khác.
USE highschool_management;
CREATE TABLE yeu_cau_chuyen_lop (
    MaXLDich BIGINT UNSIGNED NOT NULL,
    MaXLNguon BIGINT UNSIGNED NOT NULL,
    LyDo VARCHAR(255) NOT NULL,
    PRIMARY KEY (MaXLDich),
    CONSTRAINT fk_yccl_dich FOREIGN KEY (MaXLDich) REFERENCES xep_lop(MaXL) ON DELETE RESTRICT,
    CONSTRAINT fk_yccl_nguon FOREIGN KEY (MaXLNguon) REFERENCES xep_lop(MaXL) ON DELETE RESTRICT,
    CONSTRAINT ck_yccl_khac CHECK (MaXLDich <> MaXLNguon),
    CONSTRAINT ck_yccl_lydo CHECK (CHAR_LENGTH(TRIM(LyDo)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
