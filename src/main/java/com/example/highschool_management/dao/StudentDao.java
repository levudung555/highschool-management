package com.example.highschool_management.dao;


import com.example.highschool_management.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentDao {
    Student insertStudent(Student student);
    boolean existsSoDienThoai(String soDienThoai);
    boolean existsEmail(String email);
    String getLastMaSoHS();
    Optional<Student> findByMaHS(Long maHS);
    List<Student> searchStudents(String maSoHS, String hoTen);
    List<Student> findAll();
    boolean deleteStudents(List<Long> maHS);
    List<Long> findExistingIds(List<Long> maHS);

}
