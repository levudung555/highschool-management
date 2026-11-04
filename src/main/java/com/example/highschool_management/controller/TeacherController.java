package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.CreateTeacherRequest;
import com.example.highschool_management.dto.request.UpdateTeacherRequest;
import com.example.highschool_management.dto.response.TeacherResponse;
import com.example.highschool_management.service.TeacherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TeacherController {

    TeacherService teacherService;

    @PostMapping
    public TeacherResponse createTeacher(@RequestBody @Valid CreateTeacherRequest request) {
        return teacherService.createTeacher(request);
    }

    @PutMapping("/{maGV}")
    public TeacherResponse updateTeacher(@PathVariable Long maGV, @RequestBody @Valid UpdateTeacherRequest request) {
        return teacherService.updateTeacher(maGV, request);
    }

    @DeleteMapping
    public boolean deleteTeacher(@RequestParam List<Long> maGV) {
        return teacherService.deleteTeacher(maGV);
    }

    @GetMapping("/{maGV}")
    public TeacherResponse getTeacherById(@PathVariable Long maGV) {
        return teacherService.getTeacherById(maGV);
    }

    @GetMapping
    public List<TeacherResponse> getAllTeachers() {
        return teacherService.getAllTeachers();
    }
}
