package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.GuardianResponse;
import com.example.highschool_management.model.StudentGuardian;

import java.util.List;

public interface StudentGuardianDao {
    boolean link(StudentGuardian studentGuardian);
    boolean unlink(Long maHS, Long maNGH);
    boolean existsByMaHSAndMaNGH(Long maHS, Long maNGH);
    List<GuardianResponse> findGuardiansByMaHS(Long maHS);
    boolean deleteByMaNGHs(List<Long> maNGHs);
    boolean deleteByMaHSs(List<Long> maHSs);
}
