package com.example.highschool_management.service.impl;

import com.example.highschool_management.dao.EnrollmentDao;
import com.example.highschool_management.dao.SchoolClassDao;
import com.example.highschool_management.dto.request.*;
import com.example.highschool_management.dto.response.*;
import com.example.highschool_management.exception.*;
import com.example.highschool_management.mapper.EnrollmentMapper;
import com.example.highschool_management.model.*;
import com.example.highschool_management.security.NguoiDungHienTai;
import com.example.highschool_management.service.EnrollmentService;
import java.time.*;
import java.util.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentDao enrollmentDao;
    private final SchoolClassDao schoolClassDao;
    private final EnrollmentMapper enrollmentMapper;
    private final NguoiDungHienTai nguoiDung;
    private final Clock clock;

    public EnrollmentServiceImpl(EnrollmentDao enrollmentDao, SchoolClassDao schoolClassDao, EnrollmentMapper enrollmentMapper, NguoiDungHienTai nguoiDung, Clock clock) {
        this.enrollmentDao = enrollmentDao;
        this.schoolClassDao = schoolClassDao;
        this.enrollmentMapper = enrollmentMapper;
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
    public EnrollmentResponse xepLop(@NotNull @Valid EnrollStudentRequest request){
        //kiemtraquyen
        long maTaiKhoan = nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

        //lấy dữ liệu từ request
        Long maHocSinh = request.getMaHocSinh();
        Long maLop = request.getMaLop();
        LocalDate tuNgay = request.getTuNgay();

        //ktra mahocsinh tồn tại khoogn và khóa lớp
        boolean tonTaiHocSinh = enrollmentDao.khoaHocSinh(maHocSinh);
        if(!tonTaiHocSinh){
            throw new ResourceNotFoundException("Không tìm thấy học sinh có mã: " + maHocSinh);
        }

        //Đọc và khóa lớp cần xếp
        SchoolClass lopHoc = khoaLop(maLop);

        //ktra lop dang hoạt động và ngày bắt đầu có hợp lệ không
        kiemTraLopVaNgay(lopHoc, tuNgay);

        //ktra học sinh đã xếp lớp trong năm học chưa
        boolean daXepLop = enrollmentDao.coXepLopTrongNamHoc(maHocSinh, lopHoc.getMaNamHoc());
        if(daXepLop) {
            throw new BusinessException("Học sinh đã xếp lớp trong năm học này");
        }

        //chuan bi dữ liệu để tạo phân lớp
        Enrollment en = new Enrollment();
        en.setMaHocSinh(maHocSinh);
        en.setMaLop(maLop);
        en.setTuNgay(tuNgay);
        en.setDenNgay(null);
        en.setLyDoKetThuc(null);
        en.setMaTaiKhoanThucHien(maTaiKhoan);
        en.setThoiDiemThucHien(LocalDateTime.now(clock));

        //luu xuong database
        long maXepLop = enrollmentDao.tao(en);

        //Đọc lại để lấy cả tên học sinh, lớp, năm học, người thực hiện.
        Optional<Enrollment> ketQuaPhanLop = enrollmentDao.timTheoMa(maXepLop);
        if (ketQuaPhanLop.isEmpty()) {
            throw new IllegalStateException(
                    "Không đọc được phân lớp vừa tạo"
            );
        }
        Enrollment phanLopDaLuu = ketQuaPhanLop.get();

        //chuyen du lieu sang response
        EnrollmentResponse response = enrollmentMapper.sangResponse(phanLopDaLuu);
        return response;
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentResponse chuyenLop(TransferStudentRequest request){
        //kiem tra quyen
        long maTaiKhoan = nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
        Long maXepLopNguon = request.getMaXepLopHienTai();
        Long maLopDich = request.getMaLopDich();
        LocalDate ngayChuyen = request.getNgayChuyen();

        //Doc so bo de biet hoc sinh nao can xoa
        Optional<Enrollment> ketQuaBanDau = enrollmentDao.timTheoMaDeCapNhat(maXepLopNguon);
        if(ketQuaBanDau.isEmpty()){
            throw new ResourceNotFoundException("Không tìm thấy phân lớp có mã: " + maXepLopNguon);
        }
        Long maHocSinh = ketQuaBanDau.get().getMaHocSinh();

        //Khoc hoc sinh can chuyen
        boolean hocSinhTonTai = enrollmentDao.khoaHocSinh(maHocSinh);
        if(!hocSinhTonTai){
            throw new ResourceNotFoundException("Không tìm thấy học sinh có mã: " + maHocSinh);
        }

        //Khoa va doc lai nguon sau khi khoa hoc sinh
        Optional<Enrollment> ketQuaNguon = enrollmentDao.timTheoMaDeCapNhat(maXepLopNguon);
        if(ketQuaNguon.isEmpty()){
            throw new ResourceNotFoundException("Không tìm thấy phân lớp có mã: " + maXepLopNguon);
        }
        Enrollment xepLopNguon = ketQuaNguon.get();
        if(!maHocSinh.equals(xepLopNguon.getMaHocSinh())){
            throw new BusinessException("Mã học sinh không khớp với phân lớp hiện tại");
        }

        //nguon phai dang mo va dich phai khac nguon
        if(xepLopNguon.getDenNgay() != null){
            throw new BusinessException("Phân lớp hiện tại đã kết thúc, không thể chuyển lớp");
        }

        Long maLopNguon = xepLopNguon.getMaLop();

        if(maLopNguon.equals(maLopDich)){
            throw new BusinessException("Lớp nguồn và lớp đích không được trùng nhau");
        }

        //khoa 2 lop nguon va dich
        SchoolClass lopNguon;
        SchoolClass lopDich;
        if(maLopNguon < maLopDich){
            lopNguon = khoaLop(maLopNguon);
            lopDich = khoaLop(maLopDich);
        }else {
            lopDich = khoaLop(maLopDich);
            lopNguon = khoaLop(maLopNguon);
        }

        //hai lop phai cung nam hoc cung khoi
        if(!lopNguon.getMaNamHoc().equals(lopDich.getMaNamHoc())){
            throw new BusinessException("Hai lớp không cùng năm học");
        }
        if(!lopNguon.getMaKhoi().equals(lopDich.getMaKhoi())) {
            throw new BusinessException("Hai lớp không cùng khối");
        }


         //kiem tra lop dich co hoat dong vaf ngay chuyen co hop le khong
        kiemTraLopVaNgay(lopDich, ngayChuyen);
        if(!ngayChuyen.isAfter(xepLopNguon.getTuNgay())){
            throw new InvalidRequestException("Ngày chuyển lớp phải sau ngày bắt đầu của phân lớp hiện tại");
        }

        //ktra khoang moi co bi trung thoi gian voi cac phan lop khac cua hoc sinh khong
        boolean trungThoiGian = enrollmentDao.trungThoiGian(maHocSinh, lopDich.getMaNamHoc(), ngayChuyen, null, maXepLopNguon);
        if(trungThoiGian){
            throw new BusinessException("Khoảng thời gian của phân lớp mới bị trùng với phân lớp khác của học sinh");
        }

        //Chuẩn bị thông tin dùng chung cho các thao tác ghi
        String lyDo = request.getLyDo();
        LocalDate ngayKetThucNguon = ngayChuyen.minusDays(1);
        LocalDateTime thoiDiem = LocalDateTime.now(clock);

        //Ket thuc lop nguon
        enrollmentDao.ketThuc(maXepLopNguon, ngayKetThucNguon, lyDo);

        //tao phan lop dich
        Enrollment enDich = new Enrollment();
        enDich.setMaHocSinh(maHocSinh);
        enDich.setMaLop(maLopDich);
        enDich.setTuNgay(ngayChuyen);
        enDich.setDenNgay(null);
        enDich.setLyDoKetThuc(null);
        enDich.setMaTaiKhoanThucHien(maTaiKhoan);
        enDich.setThoiDiemThucHien(thoiDiem);
        long maXepLopDich = enrollmentDao.tao(enDich);

        // Ghi nhật ký chuyển lớp.
        enrollmentDao.ghiNhatKyChuyenLop(
                maTaiKhoan,
                maXepLopNguon,
                maXepLopDich,
                ngayChuyen,
                lyDo,
                thoiDiem
        );

        //Đọc lại để lấy cả tên học sinh, lớp, năm học, người thực hiện.
        Optional<Enrollment> ketQuaPhanLopDich = enrollmentDao.timTheoMa(maXepLopDich);
        if (ketQuaPhanLopDich.isEmpty()) {
            throw new IllegalStateException(
                    "Không đọc được phân lớp vừa tạo"
            );
        }

        return enrollmentMapper.sangResponse(ketQuaPhanLopDich.get());
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse chiTiet(Long maXepLop){
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

        if (maXepLop == null || maXepLop <= 0) {
            throw new InvalidRequestException("Mã xếp lớp không được để trống");
        }

        Optional<Enrollment> ketQua = enrollmentDao.timTheoMa(maXepLop);
        if (ketQua.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy phân lớp có mã: " + maXepLop);
        }

        return enrollmentMapper.sangResponse(ketQua.get());
    }
    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> lichSu(Long maHocSinh) {

        // 1. Kiểm tra quyền.
        nguoiDung.yeuCauVaiTro(
                "ADMIN",
                "BGH",
                "GIAO_VU"
        );

        // 2. Kiểm tra mã đầu vào.
        if (maHocSinh == null || maHocSinh <= 0) {
            throw new InvalidRequestException(
                    "Mã học sinh phải lớn hơn 0"
            );
        }

        // 3. Đọc danh sách phân lớp.
        List<Enrollment> danhSach =
                enrollmentDao.lichSu(maHocSinh);

        // 4. Chuyển từng Model thành Response.
        List<EnrollmentResponse> ketQua = new ArrayList<>();

        for (Enrollment enrollment : danhSach) {
            EnrollmentResponse response =
                    enrollmentMapper.sangResponse(enrollment);

            ketQua.add(response);
        }

        return ketQua;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassStudentResponse> danhSachLop(Long maLop, LocalDate ngay, int trang, int kichThuoc) {
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

        if(maLop == null || maLop <= 0) {
        throw new InvalidRequestException("Mã lớp không được để trống");}

        if(trang <0){
            throw new InvalidRequestException("Số trang phải lớn hơn hoặc bằng 0");
        }
        if(kichThuoc<1 || kichThuoc>100){
            throw new InvalidRequestException("Kích thước trang phải từ 1 đến 100");
        }

        //ktra lop có tồn tại không
        if(schoolClassDao.findById(maLop).isEmpty()){
            throw new ResourceNotFoundException("Không tìm thấy lớp học có mã: " + maLop);
        }

        //neu khong truyen ngay thi lay ngay hien tai
        LocalDate ngayTraCuu = ngay;
        if(ngayTraCuu == null){
            ngayTraCuu = LocalDate.now(clock);
        }

        //dem tong so hoc sinh trong lop
        long tongSoHocSinh = enrollmentDao.demDanhSachLop(maLop, ngayTraCuu);

        //tinh vi tri bat dau
        long viTriBatDau = (long) trang * kichThuoc;

        //lay danh sach cua trang can xem'
        List<ClassStudentResponse> danhSach = enrollmentDao.danhSachLop(maLop, ngayTraCuu, kichThuoc, viTriBatDau);

        //tong so trang
        long tongSoTrang = (tongSoHocSinh + kichThuoc - 1) / kichThuoc;

        return new PageResponse<>(danhSach, trang, kichThuoc, tongSoHocSinh, tongSoTrang);
    }

}
