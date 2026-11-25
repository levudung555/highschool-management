package com.example.highschool_management.dao;

import com.example.highschool_management.model.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherDao {
    List<Teacher> findAll();
    Optional<Teacher> findByMaGV(Long maGV);
    boolean existsSoDienThoai(String soDienThoai);
    boolean existsEmail(String email);
    String getLastMaSoGV();
    Teacher insertTeacher(Teacher teacher);
    boolean updateTeacher(Teacher teacher);
    List<Long> findExistingIds(List<Long> maGV);
    boolean deleteTeachers(List<Long> maGV);
}
