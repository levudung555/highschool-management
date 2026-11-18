package com.example.highschool_management.service.impl;

import com.example.highschool_management.dto.response.ClassSubjectOptionResponse;

import com.example.highschool_management.dao.*;
import com.example.highschool_management.dto.request.CreateTeachingAssignmentRequest;
import com.example.highschool_management.dto.response.TeachingAssignmentResponse;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.InvalidRequestException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.mapper.TeachingAssignmentMapper;
import com.example.highschool_management.model.ClassSubject;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.model.Teacher;
import com.example.highschool_management.model.TeachingAssignment;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.TeachingAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Validated
public class TeachingAssignmentServiceImpl implements TeachingAssignmentService {
    private final TeachingAssignmentDao teachingAssignmentDao;
    private final SchoolClassDao schoolClassDao;
    private final NguoiDungHienTai nguoiDung;
    private final TeachingAssignmentMapper teachingAssignmentMapper;
    private final TeacherDao teacherDao;
    private final ClassSubjectDao classSubjectDao;
    private final Clock clock;
    private final EnrollmentDao enrollmentDao;

    public TeachingAssignmentServiceImpl(TeachingAssignmentDao teachingAssignmentDao, SchoolClassDao schoolClassDao, NguoiDungHienTai nguoiDung, TeachingAssignmentMapper teachingAssignmentMapper, TeacherDao teacherDao, ClassSubjectDao classSubjectDao, Clock clock, EnrollmentDao enrollmentDao) {
        this.teachingAssignmentDao = teachingAssignmentDao;
        this.schoolClassDao = schoolClassDao;
        this.nguoiDung = nguoiDung;
        this.teachingAssignmentMapper = teachingAssignmentMapper;
        this.classSubjectDao = classSubjectDao;
        this.teacherDao = teacherDao;
        this.enrollmentDao = enrollmentDao;
        this.clock = clock;
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TeachingAssignmentResponse tao(CreateTeachingAssignmentRequest phanCong){
        long maTaiKhoan = nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

        Long maLopMonHoc = phanCong.getMaLopMonHoc();

        Long maGiaoVien = phanCong.getMaGiaoVien();
        LocalDate tuNgay = phanCong.getTuNgay();
        LocalDate denNgay = phanCong.getDenNgay();

        if(maLopMonHoc == null || maLopMonHoc <= 0 || maGiaoVien == null || maGiaoVien <= 0 || tuNgay == null){
            throw new InvalidRequestException("Thông tin phân công không hợp lệ");
        }

        Optional<ClassSubject> lopMonHoc = classSubjectDao.timTheoMaDeCapNhat(maLopMonHoc);
        if(lopMonHoc.isEmpty()){
            throw new InvalidRequestException("Lớp môn học không tồn tại");
        }

        SchoolClass lop = schoolClassDao.timTheoMaDeCapNhat(lopMonHoc.get().getMaLop()).orElseThrow(() -> new InvalidRequestException("Lớp học không tồn tại"));
        if (!"HOAT_DONG".equals(lopMonHoc.get().getTrangThaiLop())) {
            throw new InvalidRequestException("Lớp môn học không hoạt động");
        }
        if (!Objects.equals(
                lopMonHoc.get().getMaNamHocCuaLop(),
                lopMonHoc.get().getMaNamHocCuaHocKy())) {
            throw new InvalidRequestException("Lớp môn học không thuộc cùng năm học với học kỳ");
        }

        Optional<Teacher> giaoVien = teacherDao.findByMaGV(maGiaoVien);
        if (giaoVien.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy giáo viên có mã: " + maGiaoVien);
        }
        if (!"DANG_CONG_TAC".equals(giaoVien.get().getTrangThai())) {
            throw new BusinessException("Giáo viên không hoạt động");
        }

        LocalDate batDauHocKy = lopMonHoc.get().getNgayBatDauHocKy();
        LocalDate ketThucHocKy = lopMonHoc.get().getNgayKetThucHocKy();
        // 1. Bắt buộc có ngày bắt đầu
        if (tuNgay == null) {
            throw new InvalidRequestException("Ngày bắt đầu không được để trống");
        }

        // 2. Ngày bắt đầu phải nằm trong học kỳ
        if (tuNgay.isBefore(batDauHocKy)
                || tuNgay.isAfter(ketThucHocKy)) {
            throw new InvalidRequestException("Ngày bắt đầu phải nằm trong học kỳ");
        }

        // 3. Chỉ kiểm tra ngày kết thúc khi người dùng có nhập
        if (denNgay != null) {
            if (denNgay.isBefore(batDauHocKy)
                    || denNgay.isAfter(ketThucHocKy)) {
                throw new InvalidRequestException("Ngày kết thúc phải nằm trong học kỳ");
            }
            // isAfter đảm bảo ngày kết thúc lớn hơn ngày bắt đầu,
            // không cho phép hai ngày bằng nhau.
            if (!denNgay.isAfter(tuNgay)) {
                throw new InvalidRequestException("Ngày kết thúc phải lớn hơn ngày bắt đầu");
            }
        }
        LocalDate denNgayHieuLuc = denNgay != null ? denNgay : ketThucHocKy;

        boolean coPhanCongTrung = teachingAssignmentDao.coPhanCongTrungThoiGian(maLopMonHoc, tuNgay, denNgayHieuLuc);
        if (coPhanCongTrung) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phân công giảng dạy trùng thời gian với phân công khác");
        }

        TeachingAssignment phanCongGiangDay = new TeachingAssignment();
        phanCongGiangDay.setMaLopMonHoc(maLopMonHoc);
        phanCongGiangDay.setMaGiaoVien(maGiaoVien);
        phanCongGiangDay.setTuNgay(tuNgay);
        phanCongGiangDay.setDenNgay(denNgay);
        phanCongGiangDay.setMaTaiKhoanDuyet(maTaiKhoan);
        phanCongGiangDay.setTrangThaiDuyet("DA_DUYET");
        phanCongGiangDay.setDuyetLuc(LocalDateTime.now(clock));

        long maPhanCong = teachingAssignmentDao.tao(phanCongGiangDay);
        phanCongGiangDay.setMaPhanCong(maPhanCong);
        teachingAssignmentDao.ghiNhatKyTao(maTaiKhoan, phanCongGiangDay, phanCongGiangDay.getDuyetLuc());

        Optional<TeachingAssignment> phanCongMoi = teachingAssignmentDao.timTheoMa(maPhanCong);
        if (phanCongMoi.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không tìm thấy phân công giảng dạy vừa tạo");
        }
        return teachingAssignmentMapper.sangResponse(phanCongMoi.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSubjectOptionResponse> danhSachLuaChon() {
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
        return classSubjectDao.danhSachLuaChon();
    }

    @Override
    @Transactional(readOnly = true)
    public TeachingAssignmentResponse chiTiet(Long maPhanCong){
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
        Optional<TeachingAssignment> phanCong = teachingAssignmentDao.timTheoMa(maPhanCong);
        if(phanCong.isEmpty()){
            throw new ResourceNotFoundException("Không tìm thấy phân công giảng dạy có mã: " + maPhanCong);
        }
        return teachingAssignmentMapper.sangResponse(phanCong.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> lichSuTheoLopMonHoc(Long maLopMonHoc){
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
        if(maLopMonHoc == null || maLopMonHoc <= 0){
            throw new InvalidRequestException("Mã lớp môn học không hợp lệ");
        }

        if(classSubjectDao.timTheoMa(maLopMonHoc).isEmpty()){
            throw new ResourceNotFoundException("Không tìm thấy lớp môn học có mã: " + maLopMonHoc);
        }
        return teachingAssignmentDao.lichSuTheoLopMonHoc(maLopMonHoc).stream()
                .map(teachingAssignmentMapper::sangResponse)
                .toList();
    }

}
