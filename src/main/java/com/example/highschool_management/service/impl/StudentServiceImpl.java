package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.GuardianDao;
import com.example.highschool_management.dao.StudentDao;
import com.example.highschool_management.dao.StudentGuardianDao;
import com.example.highschool_management.dto.request.CreateStudentRequest;
import com.example.highschool_management.dto.request.StudentGuardianInputRequest;
import com.example.highschool_management.dto.request.UpdateStudentRequest;
import com.example.highschool_management.dto.response.StudentResponse;
import com.example.highschool_management.mapper.StudentMapper;
import com.example.highschool_management.model.Guardian;
import com.example.highschool_management.model.Student;
import com.example.highschool_management.model.StudentGuardian;
import com.example.highschool_management.service.StudentService;
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

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentServiceImpl implements StudentService {
  com.example.highschool_management.security.NguoiDungHienTai nguoiDung;
  StudentDao studentDao;
  StudentMapper studentMapper;
  GuardianDao guardianDao;
  StudentGuardianDao studentGuardianDao;

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
  public StudentResponse createStudent(CreateStudentRequest request) {
    nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
    Student student = studentMapper.toEntity(request);
    student.setMaSoHS(lastMaSoHS());
    if (studentDao.existsEmail(student.getEmail()))
      throw new com.example.highschool_management.exception.BusinessException("Email đã tồn tại");
    else if (studentDao.existsSoDienThoai(student.getSdt()))
      throw new com.example.highschool_management.exception.BusinessException(
          "Số điện thoại đã tồn tại");
    student.setNgayTiepNhan(Date.valueOf(LocalDate.now()));
    Student savedStudent = studentDao.insertStudent(student);

    // Xử lý thông tin người giám hộ (nếu có)
    if (request.getNguoiGiamHo() != null && !request.getNguoiGiamHo().isEmpty()) {
      Set<Long> linkedGuardians = new HashSet<>();
      int countLienHeChinh = 0;

      for (StudentGuardianInputRequest gInput : request.getNguoiGiamHo()) {
        if (Boolean.TRUE.equals(gInput.getLienHeChinh())) {
          countLienHeChinh++;
        }
      }
      if (countLienHeChinh > 1) {
        throw new com.example.highschool_management.exception.BusinessException(
            "Mỗi học sinh chỉ được có tối đa 1 người liên hệ chính");
      }

      for (StudentGuardianInputRequest gInput : request.getNguoiGiamHo()) {
        Long resolvedMaNGH = gInput.getMaNGH();

        if (resolvedMaNGH != null && resolvedMaNGH > 0) {
          final Long idToFind = resolvedMaNGH;
          Guardian existing = guardianDao.findByMaNGH(idToFind).orElseThrow(
              () -> new com.example.highschool_management.exception.BusinessException(
                  "Người giám hộ #" + idToFind + " không tồn tại"));
          resolvedMaNGH = existing.getMaNGH();
        } else {
          // Tạo người giám hộ mới nếu có đủ thông tin họ tên & số điện thoại
          if (gInput.getHoTen() == null || gInput.getHoTen().trim().isEmpty()) {
            throw new com.example.highschool_management.exception.BusinessException(
                "Họ tên người giám hộ không được để trống");
          }
          if (gInput.getSdt() == null || gInput.getSdt().trim().isEmpty()) {
            throw new com.example.highschool_management.exception.BusinessException(
                "Số điện thoại người giám hộ không được để trống");
          }
          if (gInput.getEmail() != null && !gInput.getEmail().isBlank() && guardianDao.existsEmail(gInput.getEmail().trim())) {
            throw new com.example.highschool_management.exception.BusinessException(
                "Email người giám hộ '" + gInput.getEmail() + "' đã tồn tại");
          }
          if (guardianDao.existsSoDienThoai(gInput.getSdt().trim())) {
            throw new com.example.highschool_management.exception.BusinessException(
                "Số điện thoại người giám hộ '" + gInput.getSdt() + "' đã tồn tại");
          }

          Guardian newG = new Guardian();
          newG.setHoTen(gInput.getHoTen().trim());
          newG.setSdt(gInput.getSdt().trim());
          newG.setEmail(gInput.getEmail() != null && !gInput.getEmail().isBlank() ? gInput.getEmail().trim() : null);
          newG.setDiaChi(gInput.getDiaChi() != null && !gInput.getDiaChi().isBlank() ? gInput.getDiaChi().trim() : null);
          Guardian inserted = guardianDao.insertGuardian(newG);
          resolvedMaNGH = inserted.getMaNGH();
        }

        if (linkedGuardians.contains(resolvedMaNGH)) {
          throw new com.example.highschool_management.exception.BusinessException(
              "Không thể tạo liên kết trùng lặp với cùng một người giám hộ (#" + resolvedMaNGH + ")");
        }
        linkedGuardians.add(resolvedMaNGH);

        String mqh = gInput.getMoiQuanHe() != null && !gInput.getMoiQuanHe().trim().isEmpty()
            ? gInput.getMoiQuanHe().trim() : "Người thân";
        StudentGuardian sg = new StudentGuardian(savedStudent.getMaHS(), resolvedMaNGH, mqh, Boolean.TRUE.equals(gInput.getLienHeChinh()));
        studentGuardianDao.link(sg);
      }
    }

    return studentMapper.toResponse(savedStudent);
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
    if (request.getEmail() != null
        && !request.getEmail().isBlank()
        && !request.getEmail().equals(student.getEmail())
        && studentDao.existsEmail(request.getEmail()))
      throw new com.example.highschool_management.exception.BusinessException("Email đã tồn tại");
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

    // Kiểm tra xem có học sinh nào đã được xếp lớp hay chưa
    List<Long> enrolledStudentIds = studentDao.findEnrolledStudentIds(maHS);
    if (!enrolledStudentIds.isEmpty()) {
      throw new com.example.highschool_management.exception.BusinessException(
          "Học sinh có mã " + enrolledStudentIds + " đã được xếp lớp, không thể xóa");
    }

    // Lấy danh sách người giám hộ liên kết với các học sinh sắp bị xóa
    List<Long> guardianIds = studentGuardianDao.findGuardianIdsByMaHSs(maHS);

    // Xóa liên kết trong bảng hs_nguoi_giam_ho
    studentGuardianDao.deleteByMaHSs(maHS);

    // Xóa học sinh
    boolean deleted = studentDao.deleteStudents(maHS);

    // Xóa người giám hộ nếu không còn liên kết với bất kỳ học sinh nào khác
    if (deleted && !guardianIds.isEmpty()) {
      List<Long> guardiansToDelete = new ArrayList<>();
      for (Long gId : guardianIds) {
        if (!studentGuardianDao.existsAnyStudentLinked(gId)) {
          guardiansToDelete.add(gId);
        }
      }
      if (!guardiansToDelete.isEmpty()) {
        guardianDao.deleteGuardians(guardiansToDelete);
      }
    }

    return deleted;
  }
}
