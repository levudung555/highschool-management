package com.example.highschool_management.mapper;

import com.example.highschool_management.dto.response.HomeroomAssignmentResponse;
import com.example.highschool_management.model.HomeroomAssignment;
import org.springframework.stereotype.Component;

@Component
public class HomeroomAssignmentMapper {

    public HomeroomAssignmentResponse sangResponse(
            HomeroomAssignment phanCong
    ) {
        HomeroomAssignmentResponse response =
                new HomeroomAssignmentResponse();

        response.setMaPhanCong(phanCong.getMaPhanCong());

        response.setMaLop(phanCong.getMaLop());
        response.setTenLop(phanCong.getTenLop());
        response.setNamHoc(phanCong.getNamHoc());

        response.setMaGiaoVien(phanCong.getMaGiaoVien());
        response.setTenGiaoVien(phanCong.getTenGiaoVien());

        response.setTuNgay(phanCong.getTuNgay());
        response.setDenNgay(phanCong.getDenNgay());

        response.setTrangThaiDuyet(
                phanCong.getTrangThaiDuyet()
        );
        response.setMaTaiKhoanDuyet(
                phanCong.getMaTaiKhoanDuyet()
        );
        response.setTenNguoiDuyet(
                phanCong.getTenNguoiDuyet()
        );
        response.setDuyetLuc(phanCong.getDuyetLuc());

        return response;
    }
}