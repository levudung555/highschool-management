package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.mapper.StudentMapper;
import com.example.highschool_management.model.Student;
import com.example.highschool_management.service.StudentService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentServiceImpl implements StudentService {
  com.example.highschool_management.security.NguoiDungHienTai nguoiDung;
  StudentDao studentDao;
  StudentMapper studentMapper;

  @Override
  public StudentResponse createStudent(CreateStudentRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Student student = studentMapper.toEntity(request);
    student.setMaSoHS(lastMaSoHS());
    if (studentDao.existsEmail(student.getEmail()))
      throw new com.example.highschool_management.exception.BusinessException("Email đã tồn tại");
    else if (studentDao.existsSoDienThoai(student.getSdt()))
      throw new com.example.highschool_management.exception.BusinessException(
          "Số điện thoại đã tồn tại");
    student.setNgayTiepNhan(Date.valueOf(LocalDate.now()));
    return studentMapper.toResponse(studentDao.insertStudent(student));
  }

  @Override
  public List<StudentResponse> searchStudents(
      String maSoHS, String hoTen, Long maLop, Long maKhoi, Long maNamHoc) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    List<Student> students = studentDao.searchStudents(maSoHS, hoTen, maLop, maKhoi, maNamHoc);
    List<StudentResponse> responses = new ArrayList<>();
    for (Student s : students) {
      responses.add(studentMapper.toResponse(s));
    }
    return responses;
  }

  @Override
  public List<StudentResponse> getAllStudents() {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    List<Student> students = studentDao.findAll();
    List<StudentResponse> responses = new ArrayList<>();
    for (Student s : students) {
      responses.add(studentMapper.toResponse(s));
    }
    return responses;
  }

  @Override
  public StudentResponse updateStudent(Long maHS, UpdateStudentRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Student student =
        studentDao
            .findByMaHS(maHS)
            .orElseThrow(
                () ->
                    new com.example.highschool_management.exception.BusinessException(
                        "Học sinh không tồn tại"));
    if (request.getEmail() != null
        && !request.getEmail().isBlank()
        && !request.getEmail().equals(student.getEmail())
        && studentDao.existsEmail(request.getEmail()))
      throw new com.example.highschool_management.exception.BusinessException("Email đã tồn tại");
    if (request.getSdt() != null
        && !request.getSdt().isBlank()
        && !request.getSdt().equals(student.getSdt())
        && studentDao.existsSoDienThoai(request.getSdt()))
      throw new com.example.highschool_management.exception.BusinessException(
          "Số điện thoại đã tồn tại");
    studentMapper.updateEntity(request, student);
    studentDao.updateStudent(student);

    return studentMapper.toResponse(student);
  }

  @Override
  public boolean deleteStudent(List<Long> maHS) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

    if (maHS == null || maHS.isEmpty()) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Danh sách học sinh không được để trống");
    }

    List<Long> existingIds = studentDao.findExistingIds(maHS);

    if (existingIds.size() != maHS.size()) {
      List<Long> notFoundIds = new ArrayList<>(maHS);
      notFoundIds.removeAll(existingIds);

      throw new com.example.highschool_management.exception.BusinessException(
          "Không tìm thấy học sinh có mã: " + notFoundIds);
    }

    return studentDao.deleteStudents(maHS);
  }

  private String lastMaSoHS() {
    String lastMaSoHS = studentDao.getLastMaSoHS();

    // DAO chỉ trả mã HS + chữ số; mã nhập từ dữ liệu cũ/demo không tham gia sinh mã.
    java.math.BigInteger nextNumber =
        lastMaSoHS == null
            ? java.math.BigInteger.ONE
            : new java.math.BigInteger(lastMaSoHS.substring(2)).add(java.math.BigInteger.ONE);
    if (nextNumber.toString().length() > 18) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Đã vượt giới hạn độ dài mã học sinh");
    }

    return String.format("HS%04d", nextNumber);
  }
}
