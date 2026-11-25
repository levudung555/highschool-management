package com.example.highschool_management.dao;

import com.example.highschool_management.model.Guardian;

import java.util.List;
import java.util.Optional;

public interface GuardianDao {
    List<Guardian> findAll();
    Optional<Guardian> findByMaNGH(Long maNGH);
    boolean existsSoDienThoai(String soDienThoai);
    boolean existsEmail(String email);
    Guardian insertGuardian(Guardian guardian);
    boolean updateGuardian(Guardian guardian);
    List<Long> findExistingIds(List<Long> maNGH);
    boolean deleteGuardians(List<Long> maNGH);
}
