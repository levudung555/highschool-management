package com.example.highschool_management.dao;

import com.example.highschool_management.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentDao {
  List<Student> findAll();

  List<Student> searchStudents(String maSoHS, String hoTen, Long maLop, Long maKhoi, Long maNamHoc);

  Optional<Student> findByMaHS(Long maHS);

  boolean existsSoDienThoai(String soDienThoai);

  boolean existsEmail(String email);

  String getLastMaSoHS();

  Student insertStudent(Student student);

  void updateStudent(Student student);

  List<Long> findExistingIds(List<Long> maHS);

  List<Long> findEnrolledStudentIds(List<Long> maHS);

  boolean deleteStudents(List<Long> maHS);
}
