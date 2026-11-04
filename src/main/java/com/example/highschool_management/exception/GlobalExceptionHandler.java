package com.example.highschool_management.exception;

import com.example.highschool_management.dto.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> xuLyKhongTimThay(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return taoPhanHoi(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> xuLyXungDot(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return taoPhanHoi(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> xuLyValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String thongBao = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(loi -> loi.getField()
                        + ": " + loi.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));

        return taoPhanHoi(
                HttpStatus.BAD_REQUEST,
                thongBao,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> xuLyJsonKhongHopLe(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return taoPhanHoi(
                HttpStatus.BAD_REQUEST,
                "JSON không hợp lệ hoặc sai kiểu dữ liệu",
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> xuLyRangBuocDuLieu(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return taoPhanHoi(
                HttpStatus.CONFLICT,
                "Dữ liệu xung đột với ràng buộc trong cơ sở dữ liệu",
                request
        );
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

    @ExceptionHandler({org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> xuLySaiKieu(Exception exception, HttpServletRequest request) {
        return taoPhanHoi(HttpStatus.BAD_REQUEST, "Tham số sai kiểu hoặc bị thiếu", request);
    }

    private ResponseEntity<ApiError> taoPhanHoi(
            HttpStatus status,
            String thongBao,
            HttpServletRequest request
    ) {
        var loi = new ApiError(
                status.value(),
                thongBao,
                request.getRequestURI()
        );

        return ResponseEntity.status(status)
                .body(loi);
    }
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> xuLyThamSoKhongHopLe(
            InvalidRequestException exception,
            HttpServletRequest request
    ) {
        return taoPhanHoi(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }
}