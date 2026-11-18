package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.ClassSubjectOptionResponse;

import com.example.highschool_management.model.ClassSubject;

import java.util.Optional;
import java.util.List;

public interface ClassSubjectDao {

    List<ClassSubjectOptionResponse> danhSachLuaChon();

    Optional<ClassSubject> timTheoMa(Long maLopMonHoc);

    Optional<ClassSubject> timTheoMaDeCapNhat(Long maLopMonHoc);
}
