package com.example.highschool_management.dao;

import com.example.highschool_management.model.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherDao {
    Teacher insertTeacher(Teacher teacher);
    boolean updateTeacher(Teacher teacher);
    boolean existsSoDienThoai(String soDienThoai);
    boolean existsEmail(String email);
    String getLastMaSoGV();
    Optional<Teacher> findByMaGV(Long maGV);
    boolean deleteTeachers(List<Long> maGV);
    List<Long> findExistingIds(List<Long> maGV);
    List<Teacher> findAll();
}
