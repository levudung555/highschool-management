package com.example.highschool_management.dao;

import com.example.highschool_management.dto.response.GuardianResponse;
import com.example.highschool_management.dto.response.StudentOfGuardianResponse;
import com.example.highschool_management.model.StudentGuardian;

import java.util.List;

public interface StudentGuardianDao {
    List<GuardianResponse> findGuardiansByMaHS(Long maHS);
    List<StudentOfGuardianResponse> findStudentsByMaNGH(Long maNGH);
    boolean existsByMaHSAndMaNGH(Long maHS, Long maNGH);
    boolean existsAnyStudentLinked(Long maNGH);
    void resetLienHeChinh(Long maHS);
    boolean link(StudentGuardian studentGuardian);
    boolean unlink(Long maHS, Long maNGH);
    boolean deleteByMaNGHs(List<Long> maNGHs);
    boolean deleteByMaHSs(List<Long> maHSs);
}
