package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateSchoolClassRequest;
import com.example.highschool_management.dto.request.UpdateSchoolClassRequest;
import com.example.highschool_management.dto.response.PageResponse;
import com.example.highschool_management.dto.response.SchoolClassResponse;
import com.example.highschool_management.model.SchoolClass;

public interface SchoolClassService {
    SchoolClassResponse getSchoolClassById(Long id);
    SchoolClassResponse them(CreateSchoolClassRequest request);
    PageResponse<SchoolClassResponse> timDanhSach(String tuKhoa, int trang, int kichThuoc);
    SchoolClassResponse capNhat(
            Long maLop,
            UpdateSchoolClassRequest request
    );
    void xoaTheoMa(Long maLop);
}
