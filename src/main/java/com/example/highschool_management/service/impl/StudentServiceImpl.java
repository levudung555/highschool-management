package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.dao.AdmissionRecordDao;
import com.example.highschool_management.dao.GuardianDao;
import com.example.highschool_management.dao.StudentGuardianDao;
import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.StudentGuardianInputRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.ImportErrorResponse;
import com.example.highschool_management.dto.response.ImportStudentResponse;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.dto.response.StudentDetailResponse;
import com.example.highschool_management.mapper.StudentMapper;
import com.example.highschool_management.model.Student;
import com.example.highschool_management.model.Guardian;
import com.example.highschool_management.model.StudentGuardian;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.StudentService;
import com.example.highschool_management.importer.StudentExcelImporter;
import com.example.highschool_management.importer.StudentExcelImporter.ImportResult;
import com.example.highschool_management.importer.StudentExcelImporter.StudentRow;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentServiceImpl implements StudentService {
  NguoiDungHienTai nguoiDung;
  StudentDao studentDao;
  AdmissionRecordDao admissionRecordDao;
  GuardianDao guardianDao;
  StudentGuardianDao studentGuardianDao;
  StudentMapper studentMapper;
  StudentExcelImporter studentExcelImporter;
  Validator validator;
  PlatformTransactionManager transactionManager;

  @Override
  public ImportStudentResponse importStudents(MultipartFile file, Long maNamHoc) throws IOException {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    if (maNamHoc == null || maNamHoc <= 0) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Phải chọn năm học hợp lệ");
    }
    List<AdmissionRecordDao.ToHopOption> toHopTheoNam =
        admissionRecordDao.danhSachToHopTheoNamHoc(maNamHoc);
    if (toHopTheoNam.isEmpty()) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Năm học chưa được khai báo tổ hợp môn");
    }
    Map<String, Long> maToHopTheoKyHieu = new HashMap<>();
    for (AdmissionRecordDao.ToHopOption toHop : toHopTheoNam) {
      if (toHop.kyHieu() != null && !toHop.kyHieu().isBlank()) {
        maToHopTheoKyHieu.put(
            toHop.kyHieu().trim().toUpperCase(Locale.ROOT), toHop.maToHop());
      }
    }

    // 1. Đọc và phân tích file Excel
    ImportResult importResult = studentExcelImporter.importStudents(file);

    ImportStudentResponse response = new ImportStudentResponse();
    List<ImportErrorResponse> errors = new ArrayList<>(importResult.getErrors());

    int successCount = 0;
    int failureCount = importResult.getErrors().size();

    Set<String> seenPhones = new HashSet<>();
    Set<String> seenCCCDs = new HashSet<>();

    String lastMaSo = studentDao.getLastMaSoHS();
    java.math.BigInteger nextNumber =
        lastMaSo == null
            ? java.math.BigInteger.ONE
            : new java.math.BigInteger(lastMaSo.substring(2)).add(java.math.BigInteger.ONE);
    TransactionTemplate rowTransaction = new TransactionTemplate(transactionManager);
    rowTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    // 2. Xử lý từng học sinh hợp lệ sau khi đọc Excel
    for (StudentRow studentRow : importResult.getStudents()) {

      int rowNumber = studentRow.getRowNumber();
      CreateStudentRequest request = studentRow.getRequest();
      BigDecimal tongDiemDauVao = studentRow.getTongDiemDauVao();
      Integer thuTuToHop = studentRow.getThuTuToHop();
      String kyHieuToHop = studentRow.getKyHieuToHop();

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

        String loiDiem = validateTongDiemDauVao(tongDiemDauVao);
        if (loiDiem != null) {
          addImportError(errors, rowNumber, "tongDiemDauVao", loiDiem);
          failureCount++;
          continue;
        }
        Long maToHop;
        if (kyHieuToHop != null) {
          maToHop = maToHopTheoKyHieu.get(kyHieuToHop.trim().toUpperCase(Locale.ROOT));
          if (maToHop == null) {
            addImportError(
                errors,
                rowNumber,
                "toHop",
                "Tổ hợp " + kyHieuToHop + " không có trong danh sách của năm học đã chọn");
            failureCount++;
            continue;
          }
        } else {
          if (thuTuToHop == null || thuTuToHop > toHopTheoNam.size()) {
            addImportError(
                errors,
                rowNumber,
                "toHop",
                "Mã tổ hợp "
                    + thuTuToHop
                    + " không hợp lệ; năm học này có "
                    + toHopTheoNam.size()
                    + " tổ hợp");
            failureCount++;
            continue;
          }
          maToHop = toHopTheoNam.get(thuTuToHop - 1).maToHop();
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

        // Mỗi dòng có transaction riêng để học sinh và phụ huynh luôn được lưu cùng nhau.
        String maSoHS = String.format("HS%04d", nextNumber);
        Student student =
            rowTransaction.execute(
                status -> {
                  Student saved = createStudentWithGuardian(request, maSoHS);
                  admissionRecordDao.taoHoSo(
                      saved.getMaHS(), maNamHoc, maToHop, tongDiemDauVao);
                  return saved;
                });
        if (student == null) {
          throw new IllegalStateException("Không thể lưu học sinh");
        }
        nextNumber = nextNumber.add(java.math.BigInteger.ONE);

        if (request.getSdt() != null && !request.getSdt().isBlank()) {
          seenPhones.add(request.getSdt());
        }
        if (request.getCmnd() != null && !request.getCmnd().isBlank()) {
          seenCCCDs.add(request.getCmnd());
        }

        successCount++;

      } catch (Exception e) {
        String msg = e.getMessage();
        if (e.getCause() != null && e.getCause().getMessage() != null) {
          msg = e.getCause().getMessage();
        }
        addImportError(
                errors,
                rowNumber,
                "GENERAL",
                msg == null
                        ? "Không thể lưu học sinh"
                        : msg
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

  private String validateTongDiemDauVao(BigDecimal score) {
    if (score == null) return "Tổng điểm đầu vào không được để trống";
    if (score.signum() < 0 || score.compareTo(new BigDecimal("9999.99")) > 0) {
      return "Tổng điểm đầu vào phải nằm trong khoảng 0 đến 9999.99";
    }
    if (score.scale() > 2) {
      return "Tổng điểm đầu vào chỉ được có tối đa 2 chữ số thập phân";
    }
    return null;
  }

  @Override
  public StudentResponse createStudent(CreateStudentRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Student student = studentMapper.toEntity(request);
    if (studentDao.existsSoDienThoai(student.getSdt()))
      throw new com.example.highschool_management.exception.BusinessException(
          "Số điện thoại đã tồn tại");
    return studentMapper.toResponse(createStudentWithGuardian(request, lastMaSoHS()));
  }

  private Student createStudentWithGuardian(CreateStudentRequest request, String maSoHS) {
    Student student = studentMapper.toEntity(request);
    student.setMaSoHS(maSoHS);
    student.setNgayTiepNhan(Date.valueOf(LocalDate.now()));
    studentDao.insertStudent(student);
    linkGuardian(student, request.getNguoiGiamHo());
    return student;
  }

  private void linkGuardian(Student student, StudentGuardianInputRequest input) {
    if (input == null) {
      return;
    }

    Guardian guardian;
    if (input.getMaNGH() != null) {
      guardian = guardianDao.findByMaNGH(input.getMaNGH())
          .orElseThrow(() -> new com.example.highschool_management.exception.BusinessException(
              "Người giám hộ không tồn tại"));
    } else {
      String hoTen = input.getHoTen() == null ? "" : input.getHoTen().trim();
      if (hoTen.isEmpty()) {
        throw new com.example.highschool_management.exception.BusinessException(
            "Họ tên phụ huynh không được để trống khi nhập thông tin phụ huynh");
      }

      String cmnd = input.getCmnd() == null || input.getCmnd().isBlank()
          ? null
          : input.getCmnd().trim();
      guardian = guardianDao.findByCMND(cmnd).orElse(null);
      if (guardian == null) {
        guardian = new Guardian();
        guardian.setHoTen(hoTen);
        guardian.setCmnd(cmnd);
        guardian.setSdt(blankToNull(input.getSdt()));
        guardian.setDiaChi(blankToNull(input.getDiaChi()));
        guardian = guardianDao.insertGuardian(guardian);
      }
    }

    String relationship = blankToNull(input.getMoiQuanHe());
    if (relationship == null) {
      relationship = "PHU_HUYNH";
    }
    boolean isPrimary = input.getLienHeChinh() == null || input.getLienHeChinh();
    boolean linked = studentGuardianDao.link(new StudentGuardian(
        student.getMaHS(), guardian.getMaNGH(), relationship, isPrimary));
    if (!linked) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Không thể liên kết phụ huynh với học sinh");
    }
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
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
  public StudentDetailResponse getStudentDetail(Long maHS) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Student student =
        studentDao
            .findByMaHS(maHS)
            .orElseThrow(
                () ->
                    new com.example.highschool_management.exception.ResourceNotFoundException(
                        "Học sinh không tồn tại"));
    List<StudentDetailResponse.HoSoNhapHoc> hoSoNhapHoc =
        admissionRecordDao.danhSachHoSoTheoHocSinh(maHS).stream()
            .map(
                hoSo ->
                    new StudentDetailResponse.HoSoNhapHoc(
                        hoSo.maHoSoNhapHoc(),
                        hoSo.maNamHoc(),
                        hoSo.namHoc(),
                        hoSo.maToHop(),
                        hoSo.kyHieuToHop(),
                        hoSo.tenToHop(),
                        hoSo.tongDiemDauVao()))
            .toList();
    return new StudentDetailResponse(
        student.getMaHS(),
        student.getMaSoHS(),
        student.getHoTen(),
        student.getNgaySinh() == null ? null : student.getNgaySinh().toLocalDate(),
        student.getGioiTinh(),
        student.getDiaChi(),
        student.getSdt(),
        student.getCmnd(),
        student.getNgayTiepNhan() == null ? null : student.getNgayTiepNhan().toLocalDate(),
        student.getNgayCapNhat(),
        hoSoNhapHoc);
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
