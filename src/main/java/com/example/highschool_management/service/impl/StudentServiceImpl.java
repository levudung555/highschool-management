package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.ImportErrorResponse;
import com.example.highschool_management.dto.response.ImportStudentResponse;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.mapper.StudentMapper;
import com.example.highschool_management.model.Student;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.StudentService;
import com.example.highschool_management.importer.StudentExcelImporter;
import com.example.highschool_management.importer.StudentExcelImporter.ImportResult;
import com.example.highschool_management.importer.StudentExcelImporter.StudentRow;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentServiceImpl implements StudentService {
  NguoiDungHienTai nguoiDung;
  StudentDao studentDao;
  StudentMapper studentMapper;
  StudentExcelImporter studentExcelImporter;
  Validator validator;

  @Override
  public ImportStudentResponse importStudents(MultipartFile file) throws IOException {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

    // 1. Đọc và phân tích file Excel
    ImportResult importResult = studentExcelImporter.importStudents(file);

    ImportStudentResponse response = new ImportStudentResponse();
    List<ImportErrorResponse> errors = new ArrayList<>(importResult.getErrors());

    int successCount = 0;
    int failureCount = importResult.getErrors().size();

    Set<String> seenPhones = new HashSet<>();
    Set<String> seenCCCDs = new HashSet<>();

    // 2. Xử lý từng học sinh hợp lệ sau khi đọc Excel
    for (StudentRow studentRow : importResult.getStudents()) {

      int rowNumber = studentRow.getRowNumber();
      CreateStudentRequest request = studentRow.getRequest();

      try {
        // 3. Validate dữ liệu theo các annotation trong Request
        var violations = validator.validate(request);

        if (!violations.isEmpty()) {
          for (ConstraintViolation<CreateStudentRequest> violation : violations) {
            addImportError(
                    errors,
                    rowNumber,
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
            );
          }
          failureCount++;
          continue;
        }

        // 4. Kiểm tra số điện thoại trùng
        if (request.getSdt() != null && !request.getSdt().isBlank()) {
          if (seenPhones.contains(request.getSdt()) || studentDao.existsSoDienThoai(request.getSdt())) {
            addImportError(
                    errors,
                    rowNumber,
                    "sdt",
                    "Số điện thoại đã tồn tại"
            );
            failureCount++;
            continue;
          }
        }

        // 5. Kiểm tra CCCD trùng
        if (request.getCmnd() != null && !request.getCmnd().isBlank()) {
          if (seenCCCDs.contains(request.getCmnd()) || studentDao.existsCCCD(request.getCmnd())) {
            addImportError(
                    errors,
                    rowNumber,
                    "cmnd",
                    "CCCD đã tồn tại"
            );
            failureCount++;
            continue;
          }
        }

        // 6. Chuyển Request thành Student
        Student student = studentMapper.toEntity(request);

        // 7. Sinh mã học sinh
        student.setMaSoHS(lastMaSoHS());

        // 8. Thiết lập ngày tiếp nhận
        student.setNgayTiepNhan(Date.valueOf(LocalDate.now()));

        // 9. Lưu vào database
        studentDao.insertStudent(student);

        if (request.getSdt() != null && !request.getSdt().isBlank()) {
          seenPhones.add(request.getSdt());
        }
        if (request.getCmnd() != null && !request.getCmnd().isBlank()) {
          seenCCCDs.add(request.getCmnd());
        }

        successCount++;

      } catch (Exception e) {
        addImportError(
                errors,
                rowNumber,
                "GENERAL",
                e.getMessage() == null
                        ? "Không thể lưu học sinh"
                        : e.getMessage()
        );
        failureCount++;
      }
    }

    // 10. Tổng hợp kết quả
    response.setTotalRows(importResult.getTotalRows());
    response.setSuccessCount(successCount);
    response.setFailureCount(failureCount);
    response.setErrors(errors);

    return response;
  }

  private void addImportError(
      List<ImportErrorResponse> errors, int row, String field, String message) {
    ImportErrorResponse error = new ImportErrorResponse();
    error.setRow(row);
    error.setField(field);
    error.setMessage(message);
    errors.add(error);
  }

  @Override
  public StudentResponse createStudent(CreateStudentRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Student student = studentMapper.toEntity(request);
    student.setMaSoHS(lastMaSoHS());
    if (studentDao.existsSoDienThoai(student.getSdt()))
      throw new com.example.highschool_management.exception.BusinessException(
          "Số điện thoại đã tồn tại");
    student.setNgayTiepNhan(Date.valueOf(LocalDate.now()));
    return studentMapper.toResponse(studentDao.insertStudent(student));
  }

  @Override
  public List<StudentResponse> searchStudents(
      String maSoHS, String hoTen, Long maLop, Long maKhoi, Long maNamHoc) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    List<Student> students = studentDao.searchStudents(maSoHS, hoTen, maLop, maKhoi, maNamHoc);
    List<StudentResponse> responses = new ArrayList<>();
    for (Student s : students) {
      responses.add(studentMapper.toResponse(s));
    }
    return responses;
  }

  @Override
  public List<StudentResponse> getAllStudents() {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    List<Student> students = studentDao.findAll();
    List<StudentResponse> responses = new ArrayList<>();
    for (Student s : students) {
      responses.add(studentMapper.toResponse(s));
    }
    return responses;
  }

  @Override
  public StudentResponse updateStudent(Long maHS, UpdateStudentRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Student student =
        studentDao
            .findByMaHS(maHS)
            .orElseThrow(
                () ->
                    new com.example.highschool_management.exception.BusinessException(
                        "Học sinh không tồn tại"));
    if (request.getSdt() != null
        && !request.getSdt().isBlank()
        && !request.getSdt().equals(student.getSdt())
        && studentDao.existsSoDienThoai(request.getSdt()))
      throw new com.example.highschool_management.exception.BusinessException(
          "Số điện thoại đã tồn tại");
    studentMapper.updateEntity(request, student);
    studentDao.updateStudent(student);

    return studentMapper.toResponse(student);
  }

  @Override
  public boolean deleteStudent(List<Long> maHS) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

    if (maHS == null || maHS.isEmpty()) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Danh sách học sinh không được để trống");
    }

    List<Long> existingIds = studentDao.findExistingIds(maHS);

    if (existingIds.size() != maHS.size()) {
      List<Long> notFoundIds = new ArrayList<>(maHS);
      notFoundIds.removeAll(existingIds);

      throw new com.example.highschool_management.exception.BusinessException(
          "Không tìm thấy học sinh có mã: " + notFoundIds);
    }

    return studentDao.deleteStudents(maHS);
  }

  private String lastMaSoHS() {
    String lastMaSoHS = studentDao.getLastMaSoHS();

    // DAO chỉ trả mã HS + chữ số; mã nhập từ dữ liệu cũ/demo không tham gia sinh mã.
    java.math.BigInteger nextNumber =
        lastMaSoHS == null
            ? java.math.BigInteger.ONE
            : new java.math.BigInteger(lastMaSoHS.substring(2)).add(java.math.BigInteger.ONE);
    if (nextNumber.toString().length() > 18) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Đã vượt giới hạn độ dài mã học sinh");
    }

    return String.format("HS%04d", nextNumber);
  }
}
