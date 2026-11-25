package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.CreateTeacherRequest;
import com.example.highschool_management.dto.request.UpdateTeacherRequest;
import com.example.highschool_management.dto.ApiResponse;
import com.example.highschool_management.dto.response.ImportTeacherResponse;
import com.example.highschool_management.dto.response.TeacherResponse;
import com.example.highschool_management.service.TeacherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TeacherController {

    TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public TeacherResponse createTeacher(@RequestBody @Valid CreateTeacherRequest request) {
        return teacherService.createTeacher(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public ApiResponse<ImportTeacherResponse> importExcelTeachers(
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        return ApiResponse.<ImportTeacherResponse>builder()
                .result(teacherService.importTeachers(file))
                .build();
    }

    @PutMapping("/{maGV}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public TeacherResponse updateTeacher(@PathVariable Long maGV, @RequestBody @Valid UpdateTeacherRequest request) {
        return teacherService.updateTeacher(maGV, request);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public boolean deleteTeacher(@RequestParam List<Long> maGV) {
        return teacherService.deleteTeacher(maGV);
    }

    @GetMapping("/{maGV}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public TeacherResponse getTeacherById(@PathVariable Long maGV) {
        return teacherService.getTeacherById(maGV);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public List<TeacherResponse> getAllTeachers() {
        return teacherService.getAllTeachers();
    }


}
