package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.mapper.StudentMapper;
import com.example.highschool_management.model.Student;
import com.example.highschool_management.service.StudentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentServiceImpl implements StudentService {
    StudentDao studentDao;
    StudentMapper studentMapper;

    @Override
    public StudentResponse createStudent(CreateStudentRequest request) {
        Student student =  studentMapper.toEntity(request);
        student.setMaSoHS(lastMaSoHS());
        if(studentDao.existsEmail(student.getEmail())) throw new RuntimeException("Email đã tồn tại");
        else if(studentDao.existsSoDienThoai(student.getSdt())) throw new RuntimeException("Số điện thoại đã tồn tại");
        student.setNgayTiepNhan(Date.valueOf(LocalDate.now()));
        return studentMapper.toResponse(studentDao.insertStudent(student));
    }

    @Override
    public List<StudentResponse> searchStudents(String maSoHS, String hoTen) {
        List<Student> students = studentDao.searchStudents(maSoHS, hoTen);
        List<StudentResponse> responses = new ArrayList<>();
        for (Student s : students) {
            responses.add(studentMapper.toResponse(s));
        }
        return responses;
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        List<Student> students = studentDao.findAll();
        List<StudentResponse> responses = new ArrayList<>();
        for (Student s : students) {
            responses.add(studentMapper.toResponse(s));
        }
        return responses;
    }

    @Override
    public StudentResponse updateStudent(Long maHS, UpdateStudentRequest request) {
        Student student = studentDao.findByMaHS(maHS).orElseThrow(() -> new RuntimeException("Học sinh không tồn tại"));
        if(studentDao.existsEmail(student.getEmail())) throw new RuntimeException("Email đã tồn tại");
        else if(studentDao.existsSoDienThoai(student.getSdt())) throw new RuntimeException("Số điện thoại đã tồn tại");
        studentMapper.updateEntity(request, student);

        return studentMapper.toResponse(student);
    }
    @Override
    public boolean deleteStudent(List<Long> maHS) {

        if (maHS == null || maHS.isEmpty()) {
            throw new RuntimeException("Danh sách học sinh không được để trống");
        }

        List<Long> existingIds = studentDao.findExistingIds(maHS);

        if (existingIds.size() != maHS.size()) {
            List<Long> notFoundIds = new ArrayList<>(maHS);
            notFoundIds.removeAll(existingIds);

            throw new RuntimeException(
                    "Không tìm thấy học sinh có mã: " + notFoundIds
            );
        }

        return studentDao.deleteStudents(maHS);
    }

    private String lastMaSoHS() {
        String lastMaSoHS = studentDao.getLastMaSoHS();

        int nextNumber = (lastMaSoHS == null)
                ? 1
                : Integer.parseInt(lastMaSoHS.substring(2)) + 1;

        return String.format("HS%04d", nextNumber);

    }
}
