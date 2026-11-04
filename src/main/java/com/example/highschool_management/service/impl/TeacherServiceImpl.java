package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.TeacherDao;
import com.example.highschool_management.dto.request.CreateTeacherRequest;
import com.example.highschool_management.dto.request.UpdateTeacherRequest;
import com.example.highschool_management.dto.response.TeacherResponse;
import com.example.highschool_management.mapper.TeacherMapper;
import com.example.highschool_management.model.Teacher;
import com.example.highschool_management.service.TeacherService;
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
public class TeacherServiceImpl implements TeacherService {

    TeacherDao teacherDao;
    TeacherMapper teacherMapper;

    @Override
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        if (teacherDao.existsSoDienThoai(request.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && teacherDao.existsEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Teacher teacher = teacherMapper.toEntity(request);
        teacher.setMaSoGV(generateMaSoGV());
        return teacherMapper.toResponse(teacherDao.insertTeacher(teacher));
    }

    @Override
    public TeacherResponse updateTeacher(Long maGV, UpdateTeacherRequest request) {
        Teacher teacher = teacherDao.findByMaGV(maGV)
                .orElseThrow(() -> new RuntimeException("Giáo viên không tồn tại"));

        if (!request.getSdt().equals(teacher.getSdt()) && teacherDao.existsSoDienThoai(request.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(teacher.getEmail())
                && teacherDao.existsEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        teacherMapper.updateEntity(request, teacher);
        teacherDao.updateTeacher(teacher);
        return teacherMapper.toResponse(teacher);
    }

    @Override
    public boolean deleteTeacher(List<Long> maGV) {
        if (maGV == null || maGV.isEmpty()) {
            throw new RuntimeException("Danh sách giáo viên không được để trống");
        }

        List<Long> existingIds = teacherDao.findExistingIds(maGV);
        if (existingIds.size() != maGV.size()) {
            List<Long> notFoundIds = new ArrayList<>(maGV);
            notFoundIds.removeAll(existingIds);
            throw new RuntimeException("Không tìm thấy giáo viên có mã: " + notFoundIds);
        }

        return teacherDao.deleteTeachers(maGV);
    }

    @Override
    public TeacherResponse getTeacherById(Long maGV) {
        Teacher teacher = teacherDao.findByMaGV(maGV)
                .orElseThrow(() -> new RuntimeException("Giáo viên không tồn tại"));
        return teacherMapper.toResponse(teacher);
    }

    @Override
    public List<TeacherResponse> getAllTeachers() {
        return teacherDao.findAll().stream()
                .map(teacherMapper::toResponse)
                .toList();
    }

    private String generateMaSoGV() {
        String lastMaSoGV = teacherDao.getLastMaSoGV();
        int nextNumber = (lastMaSoGV == null || !lastMaSoGV.startsWith("GV"))
                ? 1
                : Integer.parseInt(lastMaSoGV.substring(2)) + 1;
        return String.format("GV%04d", nextNumber);
    }
}
