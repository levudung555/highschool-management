package com.example.highschool_management.controller;

import com.example.highschool_management.dto.response.ClassSubjectOptionResponse;

import com.example.highschool_management.dto.request.CreateTeachingAssignmentRequest;
import com.example.highschool_management.dto.response.TeachingAssignmentResponse;
import com.example.highschool_management.service.TeachingAssignmentService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teaching-assignments")
public class TeachingAssignmentController {
    private final TeachingAssignmentService service;

    public TeachingAssignmentController(TeachingAssignmentService service) {
        this.service = service;
    }

    @GetMapping("/options")
    public List<ClassSubjectOptionResponse> luaChon() {
        return service.danhSachLuaChon();
    }

    @PostMapping
    public ResponseEntity<TeachingAssignmentResponse> taoPhanCong(@Valid @RequestBody CreateTeachingAssignmentRequest request) {
        TeachingAssignmentResponse response = service.tao(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TeachingAssignmentResponse>> lichSu(
            @RequestParam("maLopMonHoc") Long maLopMonHoc
    ) {
        List<TeachingAssignmentResponse> response =
                service.lichSuTheoLopMonHoc(maLopMonHoc);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<TeachingAssignmentResponse> chiTiet(@PathVariable("id") Long id) {
        TeachingAssignmentResponse response = service.chiTiet(id);
        return ResponseEntity.ok(response);
    }
}
