package com.example.highschool_management.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> danhSach,
        int trang,
        int kichThuoc,
        long tongSoPhanTu,
        long tongSoTrang
){
}
