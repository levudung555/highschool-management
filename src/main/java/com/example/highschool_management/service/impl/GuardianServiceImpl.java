package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.GuardianDao;
import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.dao.StudentGuardianDao;
import com.example.highschool_management.dto.request.CreateGuardianRequest;
import com.example.highschool_management.dto.request.LinkGuardianRequest;
import com.example.highschool_management.dto.request.UpdateGuardianRequest;
import com.example.highschool_management.dto.response.GuardianResponse;
import com.example.highschool_management.mapper.GuardianMapper;
import com.example.highschool_management.model.Guardian;
import com.example.highschool_management.model.StudentGuardian;
import com.example.highschool_management.service.GuardianService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GuardianServiceImpl implements GuardianService {

    GuardianDao guardianDao;
    StudentGuardianDao studentGuardianDao;
    StudentDao studentDao;
    GuardianMapper guardianMapper;

    @Override
    public GuardianResponse createGuardian(CreateGuardianRequest request) {
        if (guardianDao.existsSoDienThoai(request.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && guardianDao.existsEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Guardian guardian = guardianMapper.toEntity(request);
        return guardianMapper.toResponse(guardianDao.insertGuardian(guardian));
    }

    @Override
    public GuardianResponse updateGuardian(Long maNGH, UpdateGuardianRequest request) {
        Guardian guardian = guardianDao.findByMaNGH(maNGH)
                .orElseThrow(() -> new RuntimeException("Người giám hộ không tồn tại"));

        if (!request.getSdt().equals(guardian.getSdt()) && guardianDao.existsSoDienThoai(request.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(guardian.getEmail())
                && guardianDao.existsEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        guardianMapper.updateEntity(request, guardian);
        guardianDao.updateGuardian(guardian);
        return guardianMapper.toResponse(guardian);
    }

    @Override
    public boolean deleteGuardian(List<Long> maNGH) {
        if (maNGH == null || maNGH.isEmpty()) {
            throw new RuntimeException("Danh sách người giám hộ không được để trống");
        }

        List<Long> existingIds = guardianDao.findExistingIds(maNGH);
        if (existingIds.size() != maNGH.size()) {
            List<Long> notFoundIds = new ArrayList<>(maNGH);
            notFoundIds.removeAll(existingIds);
            throw new RuntimeException("Không tìm thấy người giám hộ có mã: " + notFoundIds);
        }

        studentGuardianDao.deleteByMaNGHs(maNGH);
        return guardianDao.deleteGuardians(maNGH);
    }

    @Override
    public GuardianResponse getGuardianById(Long maNGH) {
        Guardian guardian = guardianDao.findByMaNGH(maNGH)
                .orElseThrow(() -> new RuntimeException("Người giám hộ không tồn tại"));
        return guardianMapper.toResponse(guardian);
    }

    @Override
    public List<GuardianResponse> getAllGuardians() {
        return guardianDao.findAll().stream()
                .map(guardianMapper::toResponse)
                .toList();
    }

    @Override
    public boolean linkGuardian(LinkGuardianRequest request) {
        studentDao.findByMaHS(request.getMaHS())
                .orElseThrow(() -> new RuntimeException("Học sinh không tồn tại"));
        guardianDao.findByMaNGH(request.getMaNGH())
                .orElseThrow(() -> new RuntimeException("Người giám hộ không tồn tại"));

        StudentGuardian studentGuardian = new StudentGuardian(
                request.getMaHS(),
                request.getMaNGH(),
                request.getMoiQuanHe(),
                request.getLienHeChinh()
        );
        return studentGuardianDao.link(studentGuardian);
    }

    @Override
    public boolean unlinkGuardian(Long maHS, Long maNGH) {
        return studentGuardianDao.unlink(maHS, maNGH);
    }

    @Override
    public List<GuardianResponse> getGuardiansByStudent(Long maHS) {
        studentDao.findByMaHS(maHS)
                .orElseThrow(() -> new RuntimeException("Học sinh không tồn tại"));
        return studentGuardianDao.findGuardiansByMaHS(maHS);
    }
}
