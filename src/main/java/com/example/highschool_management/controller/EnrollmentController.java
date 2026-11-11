package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.*;
import com.example.highschool_management.dto.response.*;
import com.example.highschool_management.service.EnrollmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {
  private final EnrollmentService enrollmentService;

  public EnrollmentController(EnrollmentService enrollmentService) {
    this.enrollmentService = enrollmentService;
  }
  @PostMapping
  public ResponseEntity<EnrollmentResponse> xepLop(@RequestBody EnrollStudentRequest resquest) {
    EnrollmentResponse response = enrollmentService.xepLop(resquest);

    //tao duong dan toi phan lop vua tao
    URI location = URI.create(String.format("/api/v1/enrollments/%d", response.getMaXepLop()));
    //tra http 201 Created va du lieu vua tao
    return ResponseEntity.created(location).body(response);
  }
  @PostMapping("/transfers")
  public ResponseEntity<EnrollmentResponse> chuyenLop(
          @RequestBody TransferStudentRequest request
  ) {
    EnrollmentResponse response =
            enrollmentService.chuyenLop(request);

    URI location = URI.create(
            "/api/v1/enrollments/" + response.getMaXepLop()
    );

    return ResponseEntity
            .created(location)
            .body(response);
  }
  @GetMapping("/{id}")
  public ResponseEntity<EnrollmentResponse> chiTiet(
          @PathVariable("id") Long maXepLop
  ) {
    EnrollmentResponse response =
            enrollmentService.chiTiet(maXepLop);

    return ResponseEntity.ok(response);
  }
}

