package com.example.highschool_management.mapper;

import com.example.highschool_management.dto.response.TeachingAssignmentResponse;
import com.example.highschool_management.model.TeachingAssignment;

import org.springframework.stereotype.Component;

@Component
public class TeachingAssignmentMapper {

    public TeachingAssignmentResponse sangResponse(
            TeachingAssignment phanCong
    ) {
        TeachingAssignmentResponse response =
                new TeachingAssignmentResponse();

        response.setMaPhanCong(phanCong.getMaPhanCong());
        response.setMaLopMonHoc(phanCong.getMaLopMonHoc());

        response.setTenLop(phanCong.getTenLop());
        response.setTenMonHoc(phanCong.getTenMonHoc());
        response.setSoHocKy(phanCong.getSoHocKy());
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