package com.example.highschool_management.controller;

import com.example.highschool_management.dto.request.CreateGuardianRequest;
import com.example.highschool_management.dto.request.LinkGuardianRequest;
import com.example.highschool_management.dto.request.UpdateGuardianRequest;
import com.example.highschool_management.dto.response.GuardianResponse;
import com.example.highschool_management.service.GuardianService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guardian")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GuardianController {

    GuardianService guardianService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public GuardianResponse createGuardian(@RequestBody @Valid CreateGuardianRequest request) {
        return guardianService.createGuardian(request);
    }

    @PutMapping("/{maNGH}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public GuardianResponse updateGuardian(@PathVariable Long maNGH, @RequestBody @Valid UpdateGuardianRequest request) {
        return guardianService.updateGuardian(maNGH, request);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public boolean deleteGuardian(@RequestParam List<Long> maNGH) {
        return guardianService.deleteGuardian(maNGH);
    }

    @GetMapping("/{maNGH}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public GuardianResponse getGuardianById(@PathVariable Long maNGH) {
        return guardianService.getGuardianById(maNGH);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public List<GuardianResponse> getAllGuardians() {
        return guardianService.getAllGuardians();
    }

    @PostMapping("/link")
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public boolean linkGuardian(@RequestBody @Valid LinkGuardianRequest request) {
        return guardianService.linkGuardian(request);
    }

    @DeleteMapping("/unlink")
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public boolean unlinkGuardian(@RequestParam Long maHS, @RequestParam Long maNGH) {
        return guardianService.unlinkGuardian(maHS, maNGH);
    }

    @GetMapping("/student/{maHS}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public List<GuardianResponse> getGuardiansByStudent(@PathVariable Long maHS) {
        return guardianService.getGuardiansByStudent(maHS);
    }

    @GetMapping("/{maNGH}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'BGH', 'GIAO_VU')")
    public List<com.example.highschool_management.dto.response.StudentOfGuardianResponse> getStudentsByGuardian(@PathVariable Long maNGH) {
        return guardianService.getStudentsByGuardian(maNGH);
    }
}
