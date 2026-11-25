package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.TeacherDao;
import com.example.highschool_management.dto.request.CreateTeacherRequest;
import com.example.highschool_management.dto.request.UpdateTeacherRequest;
import com.example.highschool_management.dto.response.ImportErrorResponse;
import com.example.highschool_management.dto.response.ImportTeacherResponse;
import com.example.highschool_management.dto.response.TeacherResponse;
import com.example.highschool_management.importer.TeacherExcelImporter;
import com.example.highschool_management.importer.TeacherExcelImporter.ImportResult;
import com.example.highschool_management.importer.TeacherExcelImporter.TeacherRow;
import com.example.highschool_management.mapper.TeacherMapper;
import com.example.highschool_management.model.Teacher;
import com.example.highschool_management.service.TeacherService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherServiceImpl implements TeacherService {
  com.example.highschool_management.security.NguoiDungHienTai nguoiDung;

  TeacherDao teacherDao;
  TeacherMapper teacherMapper;
  TeacherExcelImporter teacherExcelImporter;
  Validator validator;
  PlatformTransactionManager transactionManager;

  @Override
  public List<TeacherResponse> getAllTeachers() {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    return teacherDao.findAll().stream().map(teacherMapper::toResponse).toList();
  }

  @Override
  public TeacherResponse getTeacherById(Long maGV) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Teacher teacher =
        teacherDao
            .findByMaGV(maGV)
            .orElseThrow(
                () ->
                    new com.example.highschool_management.exception.BusinessException(
                        "Giáo viên không tồn tại"));
    return teacherMapper.toResponse(teacher);
  }

  @Override
  public TeacherResponse createTeacher(CreateTeacherRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    if (teacherDao.existsSoDienThoai(request.getSdt())) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Số điện thoại đã tồn tại");
    }
    if (request.getEmail() != null
        && !request.getEmail().isBlank()
        && teacherDao.existsEmail(request.getEmail())) {
      throw new com.example.highschool_management.exception.BusinessException("Email đã tồn tại");
    }

    Teacher teacher = teacherMapper.toEntity(request);
    teacher.setMaSoGV(generateMaSoGV());
    return teacherMapper.toResponse(teacherDao.insertTeacher(teacher));
  }

  @Override
  public ImportTeacherResponse importTeachers(MultipartFile file) throws IOException {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    ImportResult importResult = teacherExcelImporter.importTeachers(file);

    ImportTeacherResponse response = new ImportTeacherResponse();
    List<ImportErrorResponse> errors = new ArrayList<>(importResult.getErrors());
    int successCount = 0;
    int failureCount = errors.size();

    TransactionTemplate rowTransaction = new TransactionTemplate(transactionManager);
    rowTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    for (TeacherRow teacherRow : importResult.getTeachers()) {
      int rowNumber = teacherRow.rowNumber();
      CreateTeacherRequest request = teacherRow.request();
      try {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
          for (ConstraintViolation<CreateTeacherRequest> violation : violations) {
            addImportError(
                errors,
                rowNumber,
                violation.getPropertyPath().toString(),
                violation.getMessage());
          }
          failureCount++;
          continue;
        }

        TeacherResponse saved = rowTransaction.execute(status -> createTeacher(request));
        if (saved == null) {
          throw new IllegalStateException("Không thể lưu giáo viên");
        }
        successCount++;
      } catch (Exception e) {
        String message = e.getMessage();
        if (e.getCause() != null && e.getCause().getMessage() != null) {
          message = e.getCause().getMessage();
        }
        addImportError(
            errors,
            rowNumber,
            "GENERAL",
            message == null ? "Không thể lưu giáo viên" : message);
        failureCount++;
      }
    }

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
    error.setMessage(message == null ? "Dữ liệu không hợp lệ" : message);
    errors.add(error);
  }

  private String generateMaSoGV() {
    String lastMaSoGV = teacherDao.getLastMaSoGV();
    java.math.BigInteger nextNumber =
        lastMaSoGV == null
            ? java.math.BigInteger.ONE
            : new java.math.BigInteger(lastMaSoGV.substring(2)).add(java.math.BigInteger.ONE);
    if (nextNumber.toString().length() > 28) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Đã vượt giới hạn độ dài mã giáo viên");
    }
    return String.format("GV%04d", nextNumber);
  }

  @Override
  public TeacherResponse updateTeacher(Long maGV, UpdateTeacherRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Teacher teacher =
        teacherDao
            .findByMaGV(maGV)
            .orElseThrow(
                () ->
                    new com.example.highschool_management.exception.BusinessException(
                        "Giáo viên không tồn tại"));

    if (request.getSdt() != null && !request.getSdt().equals(teacher.getSdt())
        && teacherDao.existsSoDienThoai(request.getSdt())) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Số điện thoại đã tồn tại");
    }
    if (request.getEmail() != null
        && !request.getEmail().isBlank()
        && !request.getEmail().equals(teacher.getEmail())
        && teacherDao.existsEmail(request.getEmail())) {
      throw new com.example.highschool_management.exception.BusinessException("Email đã tồn tại");
    }

    teacherMapper.updateEntity(request, teacher);
    teacherDao.updateTeacher(teacher);
    return teacherMapper.toResponse(teacher);
  }

  @Override
  public boolean deleteTeacher(List<Long> maGV) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    if (maGV == null || maGV.isEmpty()) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Danh sách giáo viên không được để trống");
    }

    List<Long> existingIds = teacherDao.findExistingIds(maGV);
    if (existingIds.size() != maGV.size()) {
      List<Long> notFoundIds = new ArrayList<>(maGV);
      notFoundIds.removeAll(existingIds);
      throw new com.example.highschool_management.exception.BusinessException(
          "Không tìm thấy giáo viên có mã: " + notFoundIds);
    }

    return teacherDao.deleteTeachers(maGV);
  }
}
