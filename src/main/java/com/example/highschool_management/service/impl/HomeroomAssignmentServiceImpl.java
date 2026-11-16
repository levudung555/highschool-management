package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.EnrollmentDao;
import com.example.highschool_management.dao.HomeroomAssignmentDao;
import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dao.TeacherDao;
import com.example.highschool_management.dto.request.CreateHomeroomAssignmentRequest;
import com.example.highschool_management.dto.response.EnrollmentResponse;
import com.example.highschool_management.dto.response.HomeroomAssignmentResponse;
import com.example.highschool_management.dto.response.SchoolYearPeriodResponse;
import com.example.highschool_management.exception.BusinessException;
import com.example.highschool_management.exception.InvalidRequestException;
import com.example.highschool_management.exception.ResourceNotFoundException;
import com.example.highschool_management.mapper.EnrollmentMapper;
import com.example.highschool_management.mapper.HomeroomAssignmentMapper;
import com.example.highschool_management.model.HomeroomAssignment;
import com.example.highschool_management.model.SchoolClass;
import com.example.highschool_management.model.Teacher;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.HomeroomAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class HomeroomAssignmentServiceImpl implements HomeroomAssignmentService {
    private final EnrollmentDao enrollmentDao;
    private final HomeroomAssignmentDao homeroomAssignmentDao;
    private final SchoolClassDao schoolClassDao;
    private final HomeroomAssignmentMapper homeroomAssignmentMapper;
    private final TeacherDao teacherDao;
    private final NguoiDungHienTai nguoiDung;
    private final Clock clock;

    public HomeroomAssignmentServiceImpl(EnrollmentDao enrollmentDao, HomeroomAssignmentDao homeroomAssignmentDao, SchoolClassDao schoolClassDao, HomeroomAssignmentMapper homeroomAssignmentMapper, TeacherDao teacherDao, NguoiDungHienTai nguoiDung, Clock clock) {
        this.enrollmentDao = enrollmentDao;
        this.homeroomAssignmentDao = homeroomAssignmentDao;
        this.schoolClassDao = schoolClassDao;
        this.homeroomAssignmentMapper = homeroomAssignmentMapper;
        this.teacherDao = teacherDao;
        this.nguoiDung = nguoiDung;
        this.clock = clock;
    }

    private void kiemTraLopVaNgay(SchoolClass lop, LocalDate tuNgay){
        if(!"HOAT_DONG".equals(lop.getTrangThai())){
            throw new BusinessException("Lớp học không hoạt động");
        }
        if(tuNgay == null){
            throw new InvalidRequestException("Ngày bắt đầu không được để trống");
        }
        LocalDate now = LocalDate.now(clock);
        if(tuNgay.isBefore(now)){
            throw new InvalidRequestException("Ngày bắt đầu không được ở quá khứ");
        }

        SchoolYearPeriodResponse namHoc = enrollmentDao.namHoc(lop.getMaNamHoc());
        boolean isTruocNamHoc = tuNgay.isBefore(namHoc.getBatDau());
        boolean isSauNamHoc = tuNgay.isAfter(namHoc.getKetThuc());
        if(isTruocNamHoc || isSauNamHoc){
            throw new InvalidRequestException("Ngày bắt đầu không nằm trong năm học");
        }
    }
    private SchoolClass khoaLop(Long maLop){
        Optional<SchoolClass> lop = schoolClassDao.timTheoMaDeCapNhat(maLop);
        if (lop.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy lớp học có mã: " + maLop);
        }
        SchoolClass lopHoc = lop.get();
        return lopHoc;
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HomeroomAssignmentResponse taoPhanCongCN(CreateHomeroomAssignmentRequest request){
        //ktra quyen
        long maTK = nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

        //Lấy dữ liệu từ request
        long maLop = request.getMaLop();
        long maGiaoVien = request.getMaGiaoVien();
        LocalDate tuNgay = request.getTuNgay();

        //ktra giao vien có tồn tại không
        Optional<Teacher> giaoVien = teacherDao.findByMaGV(maGiaoVien);
        if (giaoVien.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy giáo viên có mã: " + maGiaoVien);
        }
        if (giaoVien.get().getTrangThai().equals("NGHI_VIEC")) {
            throw new BusinessException("Giáo viên không hoạt động");
        }

        //Ktra lớp và ngày
        SchoolClass lop = khoaLop(maLop);
        kiemTraLopVaNgay(lop, tuNgay);

        boolean trungPhanCong = homeroomAssignmentDao.coPhanCongTrungThoiGian(maLop, tuNgay, request.getDenNgay());
        if(trungPhanCong) {
            throw new BusinessException("Đã có phân công chủ nhiệm trùng thời gian");
        }

        //chuan bi dữ liệu để tạo phân lớp
        HomeroomAssignment phanCong = new HomeroomAssignment();
        phanCong.setMaLop(maLop);
        phanCong.setMaGiaoVien(maGiaoVien);
        phanCong.setTuNgay(tuNgay);
        phanCong.setDenNgay(request.getDenNgay());
        phanCong.setMaTaiKhoanDuyet(maTK);
        phanCong.setDuyetLuc(LocalDate.now(clock).atStartOfDay());
        phanCong.setTrangThaiDuyet("DA_DUYET");

        //Tạo phân công
        long maPhanCong = homeroomAssignmentDao.tao(phanCong);

        Optional<HomeroomAssignment> phanCongMoi = homeroomAssignmentDao.timTheoMa(maPhanCong);
        if (phanCongMoi.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy phân công chủ nhiệm vừa tạo");
        }

        return homeroomAssignmentMapper.sangResponse(phanCongMoi.get());
    }

    @Override
    @Transactional(readOnly = true)
    public HomeroomAssignmentResponse chiTiet(Long maPhanCong){
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
        Optional<HomeroomAssignment> phanCong = homeroomAssignmentDao.timTheoMa(maPhanCong);
        if (phanCong.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy phân công chủ nhiệm có mã: " + maPhanCong);
        }
        return homeroomAssignmentMapper.sangResponse(phanCong.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomeroomAssignmentResponse> lichSu(Long maLop){
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
        if (maLop == null || maLop <= 0) {
            throw new InvalidRequestException("Mã lớp phải lớn hơn 0");
        }
        // Xem lịch sử chỉ đọc dữ liệu, không dùng SELECT ... FOR UPDATE.
        if (schoolClassDao.findById(maLop).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy lớp học có mã: " + maLop);
        }
        List<HomeroomAssignment> danhSach = homeroomAssignmentDao.lichSuTheoLop(maLop);
        List<HomeroomAssignmentResponse> danhSachResponse = new ArrayList<>();
        for (HomeroomAssignment phanCong : danhSach) {
            HomeroomAssignmentResponse response = homeroomAssignmentMapper.sangResponse(phanCong);
            danhSachResponse.add(response);
        }
        return danhSachResponse;
    }
}
