package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.CreateHomeroomAssignmentRequest;
import com.example.highschool_management.dto.response.HomeroomAssignmentResponse;
import com.example.highschool_management.service.HomeroomAssignmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/homeroom-assignments")
@PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
public class HomeroomAssignmentController {

    private final HomeroomAssignmentService service;

    public HomeroomAssignmentController(
            HomeroomAssignmentService service
    ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<HomeroomAssignmentResponse> taoPhanCongCN(@Valid @RequestBody CreateHomeroomAssignmentRequest request) {
        HomeroomAssignmentResponse response = service.taoPhanCongCN(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HomeroomAssignmentResponse>> lichSu(@RequestParam("maLop") Long maLop) {
        List<HomeroomAssignmentResponse> response = service.lichSu(maLop);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<HomeroomAssignmentResponse> chiTiet(@PathVariable("id") Long maPhanCong) {
        HomeroomAssignmentResponse response = service.chiTiet(maPhanCong);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
