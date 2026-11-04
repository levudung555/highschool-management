package com.example.highschool_management.dao;

import com.example.highschool_management.model.Guardian;

import java.util.List;
import java.util.Optional;

public interface GuardianDao {
    Guardian insertGuardian(Guardian guardian);
    boolean updateGuardian(Guardian guardian);
    boolean existsSoDienThoai(String soDienThoai);
    boolean existsEmail(String email);
    Optional<Guardian> findByMaNGH(Long maNGH);
    boolean deleteGuardians(List<Long> maNGH);
    List<Long> findExistingIds(List<Long> maNGH);
    List<Guardian> findAll();
}
