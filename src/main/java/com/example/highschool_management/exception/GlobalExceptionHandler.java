package com.example.highschool_management.exception;

import com.example.highschool_management.dto.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Các DAO JDBC của nhánh hồ sơ bọc SQLException trong RuntimeException.
  // Spring vẫn tìm handler theo nguyên nhân gốc để trả lỗi dễ hiểu cho form.
  @ExceptionHandler(java.sql.SQLIntegrityConstraintViolationException.class)
  public ResponseEntity<ApiError> xuLyRangBuocJdbc(
      java.sql.SQLIntegrityConstraintViolationException exception, HttpServletRequest request) {
    String msg = exception.getMessage() != null ? exception.getMessage() : "";
    int errorCode = exception.getErrorCode();

    String thongBao;
    if (errorCode == 1062 || msg.contains("Duplicate entry")) {
      thongBao = "Dữ liệu bị trùng lặp (đã tồn tại trong hệ thống)";
    } else if (errorCode == 1451 || msg.contains("a foreign key constraint fails")) {
      if (msg.contains("fk_XEP_LOP_MaHS") || msg.contains("xep_lop")) {
        thongBao = "Không thể xóa học sinh vì đã có dữ liệu xếp lớp";
      } else if (msg.contains("fk_KET_QUA_MON_MaHS") || msg.contains("ket_qua_mon")) {
        thongBao = "Không thể xóa học sinh vì đã có điểm/kết quả môn học";
      } else if (msg.contains("fk_TONG_KET_HOC_KY_MaHS") || msg.contains("tong_ket_hoc_ky")) {
        thongBao = "Không thể xóa học sinh vì đã có kết quả tổng kết học kỳ";
      } else if (msg.contains("fk_TONG_KET_NAM_MaHS") || msg.contains("tong_ket_nam")) {
        thongBao = "Không thể xóa học sinh vì đã có kết quả tổng kết năm học";
      } else if (msg.contains("fk_HS_NGUOI_GIAM_HO_MaHS")) {
        thongBao = "Không thể xóa học sinh vì đang có người giám hộ liên kết";
      } else if (msg.contains("fk_HS_NGUOI_GIAM_HO_MaNGH")) {
        thongBao = "Không thể xóa người giám hộ vì đang liên kết với học sinh";
      } else if (msg.contains("fk_PHAN_CONG_CHU_NHIEM_MaGV")) {
        thongBao = "Không thể xóa giáo viên vì đang có phân công chủ nhiệm";
      } else if (msg.contains("fk_PHAN_CONG_GIANG_DAY_MaGV")) {
        thongBao = "Không thể xóa giáo viên vì đang có phân công giảng dạy";
      } else if (msg.contains("fk_TAI_KHOAN_MaGV")) {
        thongBao = "Không thể xóa giáo viên vì đã có tài khoản người dùng liên kết";
      } else {
        thongBao = "Không thể xóa vì hồ sơ đang có dữ liệu liên quan khác trong hệ thống";
      }
    } else {
      thongBao = "Lỗi ràng buộc dữ liệu: " + exception.getMessage();
    }

    return taoPhanHoi(HttpStatus.CONFLICT, thongBao, request);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> xuLyKhongTimThay(
      ResourceNotFoundException exception, HttpServletRequest request) {
    return taoPhanHoi(HttpStatus.NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiError> xuLyXungDot(
      BusinessException exception, HttpServletRequest request) {
    return taoPhanHoi(HttpStatus.CONFLICT, exception.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> xuLyValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    String thongBao =
        exception.getBindingResult().getFieldErrors().stream()
            .map(loi -> loi.getField() + ": " + loi.getDefaultMessage())
            .distinct()
            .sorted()
            .collect(Collectors.joining("; "));

    return taoPhanHoi(HttpStatus.BAD_REQUEST, thongBao, request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> xuLyJsonKhongHopLe(
      HttpMessageNotReadableException exception, HttpServletRequest request) {
    return taoPhanHoi(HttpStatus.BAD_REQUEST, "JSON không hợp lệ hoặc sai kiểu dữ liệu", request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> xuLyRangBuocDuLieu(
      DataIntegrityViolationException exception, HttpServletRequest request) {
    String msg = exception.getMessage() != null ? exception.getMessage() : "";
    Throwable rootCause = exception.getRootCause();
    if (rootCause != null && rootCause.getMessage() != null) {
      msg += " " + rootCause.getMessage();
    }

    String thongBao;
    if (msg.contains("Duplicate entry")) {
      thongBao = "Dữ liệu bị trùng lặp (đã tồn tại trong hệ thống)";
    } else if (msg.contains("a foreign key constraint fails") || msg.contains("foreign key")) {
      if (msg.contains("fk_XEP_LOP_MaHS") || msg.contains("xep_lop")) {
        thongBao = "Không thể xóa học sinh vì đã có dữ liệu xếp lớp";
      } else if (msg.contains("fk_KET_QUA_MON_MaHS") || msg.contains("ket_qua_mon")) {
        thongBao = "Không thể xóa học sinh vì đã có điểm/kết quả môn học";
      } else if (msg.contains("fk_TONG_KET_HOC_KY_MaHS") || msg.contains("tong_ket_hoc_ky")) {
        thongBao = "Không thể xóa học sinh vì đã có kết quả tổng kết học kỳ";
      } else if (msg.contains("fk_TONG_KET_NAM_MaHS") || msg.contains("tong_ket_nam")) {
        thongBao = "Không thể xóa học sinh vì đã có kết quả tổng kết năm học";
      } else if (msg.contains("fk_HS_NGUOI_GIAM_HO_MaHS")) {
        thongBao = "Không thể xóa học sinh vì đang có người giám hộ liên kết";
      } else if (msg.contains("fk_HS_NGUOI_GIAM_HO_MaNGH")) {
        thongBao = "Không thể xóa người giám hộ vì đang liên kết với học sinh";
      } else if (msg.contains("fk_PHAN_CONG_CHU_NHIEM_MaGV")) {
        thongBao = "Không thể xóa giáo viên vì đang có phân công chủ nhiệm";
      } else if (msg.contains("fk_PHAN_CONG_GIANG_DAY_MaGV")) {
        thongBao = "Không thể xóa giáo viên vì đang có phân công giảng dạy";
      } else if (msg.contains("fk_TAI_KHOAN_MaGV")) {
        thongBao = "Không thể xóa giáo viên vì đã có tài khoản người dùng liên kết";
      } else {
        thongBao = "Không thể xóa vì hồ sơ đang có dữ liệu liên quan khác trong hệ thống";
      }
    } else {
      thongBao = "Dữ liệu xung đột với ràng buộc trong cơ sở dữ liệu";
    }

    return taoPhanHoi(HttpStatus.CONFLICT, thongBao, request);
  }

  @ExceptionHandler(AccessException.class)
  public ResponseEntity<ApiError> xuLyQuyen(AccessException exception, HttpServletRequest request) {
    return taoPhanHoi(HttpStatus.valueOf(exception.getStatus()), exception.getMessage(), request);
  }

  @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
  public ResponseEntity<ApiError> xuLyRangBuocDauVao(
      jakarta.validation.ConstraintViolationException exception, HttpServletRequest request) {
    return taoPhanHoi(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ", request);
  }

  @ExceptionHandler({
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class
  })
  public ResponseEntity<ApiError> xuLySaiKieu(Exception exception, HttpServletRequest request) {
    return taoPhanHoi(HttpStatus.BAD_REQUEST, "Tham số sai kiểu hoặc bị thiếu", request);
  }

  private ResponseEntity<ApiError> taoPhanHoi(
      HttpStatus status, String thongBao, HttpServletRequest request) {
    var loi = new ApiError(status.value(), thongBao, request.getRequestURI());

    return ResponseEntity.status(status).body(loi);
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<ApiError> xuLyThamSoKhongHopLe(
      InvalidRequestException exception, HttpServletRequest request) {
    return taoPhanHoi(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
  }
}
