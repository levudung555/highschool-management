package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateTeacherRequest;
import com.example.highschool_management.dto.request.UpdateTeacherRequest;
import com.example.highschool_management.dto.response.TeacherResponse;

import java.util.List;

public interface TeacherService {
    List<TeacherResponse> getAllTeachers();
    TeacherResponse getTeacherById(Long maGV);
    TeacherResponse createTeacher(CreateTeacherRequest request);
    TeacherResponse updateTeacher(Long maGV, UpdateTeacherRequest request);
    boolean deleteTeacher(List<Long> maGV);
}
