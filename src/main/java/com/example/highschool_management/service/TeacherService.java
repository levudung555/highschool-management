package com.example.highschool_management.service;

import com.example.highschool_management.dto.request.CreateTeacherRequest;
import com.example.highschool_management.dto.request.UpdateTeacherRequest;
import com.example.highschool_management.dto.response.ImportTeacherResponse;
import com.example.highschool_management.dto.response.TeacherResponse;

import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TeacherService {
    List<TeacherResponse> getAllTeachers();
    TeacherResponse getTeacherById(Long maGV);
    TeacherResponse createTeacher(CreateTeacherRequest request);
    ImportTeacherResponse importTeachers(MultipartFile file) throws IOException;
    TeacherResponse updateTeacher(Long maGV, UpdateTeacherRequest request);
    boolean deleteTeacher(List<Long> maGV);
}
