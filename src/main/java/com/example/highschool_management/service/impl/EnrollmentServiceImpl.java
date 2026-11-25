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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    // Đợt đã chốt trả về ngày vào lớp chung. Đợt chưa chốt trả về các lớp sẵn sàng nhận học sinh.
    @Override
    @Transactional(readOnly = true)
    public Grade10PlacementStatusResponse trangThaiXepLop10(Long maNamHoc) {
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
        if (!schoolClassDao.tonTaiNamHoc(maNamHoc)) {
            throw new ResourceNotFoundException("Không tìm thấy năm học: " + maNamHoc);
        }
        var ngayDaChot = enrollmentDao.ngayVaoLopDaChot(maNamHoc);
        if (ngayDaChot.isPresent()) {
            return new Grade10PlacementStatusResponse(ngayDaChot.get(), List.of());
        }
        return new Grade10PlacementStatusResponse(null,
                schoolClassDao.danhSachLop10DeXepDauNam(maNamHoc));
    }

    // Lập danh sách dự kiến theo từng tổ hợp để giáo vụ xem trước khi ghi kết quả xếp lớp.
    @Override
    @Transactional(readOnly = true)
    public Grade10PlacementPreviewResponse xemTruocXepLop10(Long maNamHoc) {
        nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");

        if (maNamHoc == null || maNamHoc <= 0) {
            throw new InvalidRequestException("Mã năm học phải lớn hơn 0");
        }
        if (!schoolClassDao.tonTaiNamHoc(maNamHoc)) {
            throw new ResourceNotFoundException("Không tìm thấy năm học: " + maNamHoc);
        }

        List<Grade10PlacementCandidate> danhSachHocSinh =
                enrollmentDao.danhSachXepLop10DauNam(maNamHoc);
        if (danhSachHocSinh.isEmpty()) {
            throw new BusinessException("Năm học này chưa có hồ sơ nhập học");
        }

        List<SchoolClass> danhSachLop =
                schoolClassDao.danhSachLop10DeXepDauNam(maNamHoc);

        // ---- Bước 1: gom học sinh theo tổ hợp (giữ thứ tự điểm DAO đã sắp) ----
        Map<Long, List<Grade10PlacementCandidate>> hocSinhTheoToHop =
                new LinkedHashMap<Long, List<Grade10PlacementCandidate>>();

        for (Grade10PlacementCandidate hocSinh : danhSachHocSinh) {
            Long maToHop = hocSinh.maToHop();

            List<Grade10PlacementCandidate> dsHocSinhCuaToHop =
                    hocSinhTheoToHop.get(maToHop);
            if (dsHocSinhCuaToHop == null) {
                dsHocSinhCuaToHop = new ArrayList<Grade10PlacementCandidate>();
                hocSinhTheoToHop.put(maToHop, dsHocSinhCuaToHop);
            }
            dsHocSinhCuaToHop.add(hocSinh);
        }

        // ---- Bước 2: gom lớp theo tổ hợp (giữ thứ tự ThuTuXep DAO đã sắp) ----
        Map<Long, List<SchoolClass>> lopTheoToHop =
                new HashMap<Long, List<SchoolClass>>();

        for (SchoolClass lop : danhSachLop) {
            Long maToHop = lop.getMaToHop();

            List<SchoolClass> dsLopCuaToHop = lopTheoToHop.get(maToHop);
            if (dsLopCuaToHop == null) {
                dsLopCuaToHop = new ArrayList<SchoolClass>();
                lopTheoToHop.put(maToHop, dsLopCuaToHop);
            }
            dsLopCuaToHop.add(lop);
        }

        // ---- Bước 3: xếp thử từng tổ hợp ----
        List<Grade10CombinationPreviewResponse> ketQua =
                new ArrayList<Grade10CombinationPreviewResponse>();

        for (Map.Entry<Long, List<Grade10PlacementCandidate>> nhom
                : hocSinhTheoToHop.entrySet()) {

            Long maToHop = nhom.getKey();
            List<Grade10PlacementCandidate> hocSinhCuaToHop = nhom.getValue();
            String kyHieu = hocSinhCuaToHop.get(0).kyHieu();

            List<SchoolClass> lopCuaToHop = lopTheoToHop.get(maToHop);
            if (lopCuaToHop == null) {
                lopCuaToHop = List.of();   // tổ hợp chưa có lớp nào
            }

            var cacLopXepThu = xepThuMotToHop(kyHieu, hocSinhCuaToHop, lopCuaToHop);

            ketQua.add(new Grade10CombinationPreviewResponse(
                    maToHop, kyHieu, cacLopXepThu
            ));
        }

        String maKiemTraPhuongAn = tinhMaKiemTraPhuongAn(maNamHoc, ketQua);
        return new Grade10PlacementPreviewResponse(maNamHoc, maKiemTraPhuongAn, ketQua);
    }

    // Lấy học sinh theo thứ tự điểm đã sắp, lần lượt điền đủ số suất của từng lớp trong tổ hợp.
    private List<Grade10ClassPreviewResponse> xepThuMotToHop(String kyHieuToHop, List<Grade10PlacementCandidate> danhSachHocSinh, List<SchoolClass> danhSachLop){
        int[] soSuat = tinhSoSuatMoiLop(kyHieuToHop, danhSachHocSinh.size(), danhSachLop);
        List<Grade10ClassPreviewResponse> ketQua = new ArrayList<>();
        int viTriHocSinh = 0;
        for(int i = 0; i < danhSachLop.size(); i++){
            SchoolClass lop = danhSachLop.get(i);
            List<Grade10PlacementCandidate> danhSachHocSinhCuaLop = new ArrayList<>();
            for (int j = 0; j < soSuat[i]; j++){
                danhSachHocSinhCuaLop.add(danhSachHocSinh.get(viTriHocSinh));
                viTriHocSinh++;
            }
            ketQua.add(new Grade10ClassPreviewResponse(lop.getMaLop(), lop.getTenLop(), danhSachHocSinhCuaLop));
        }
        return ketQua;
    }


    // Phần tử thứ i là số học sinh lớp thứ i sẽ nhận. Mỗi lượt cấp một suất cho lớp còn chỗ có ít suất nhất.
    private int[] tinhSoSuatMoiLop(String kyHieuTH, int soHocSinh, List<SchoolClass> danhSachLop){
        int soLop = danhSachLop.size();
        int[] soSuatMoiLop = new int[soLop];

        long tongSucChua = 0;
        for(SchoolClass schoolClass : danhSachLop){
            tongSucChua += schoolClass.getSiSoToiDa();
        }
        if (tongSucChua < soHocSinh) {
            long soChoThieu = soHocSinh - tongSucChua;
            throw new BusinessException(
                    "Tổ hợp " + kyHieuTH + " còn thiếu " + soChoThieu + " chỗ");
        }

        for(int luot = 0; luot < soHocSinh; luot++){
            int lopDuocChon = -1;
            for (int i = 0; i < soLop; i++) {
                int chiTieu = danhSachLop.get(i).getSiSoToiDa();
                if (soSuatMoiLop[i] >= chiTieu) {
                    continue;
                }
                if (lopDuocChon == -1 || soSuatMoiLop[i] < soSuatMoiLop[lopDuocChon]) {
                    lopDuocChon = i;
                }
            }
            soSuatMoiLop[lopDuocChon]++;

        }
        return soSuatMoiLop;

    }

    // Tạo mã từ năm học, tổ hợp, lớp, hồ sơ và điểm để đối chiếu phương án lúc xem trước với lúc chốt.
    private String tinhMaKiemTraPhuongAn(
            Long maNamHoc,
            List<Grade10CombinationPreviewResponse> danhSachToHop) {

        StringBuilder noiDung = new StringBuilder();
        noiDung.append("NAM|").append(maNamHoc).append('\n');

        for (var toHop : danhSachToHop) {
            noiDung.append("TO_HOP|").append(toHop.maToHop()).append('\n');

            for (var lop : toHop.danhSachLop()) {
                noiDung.append("LOP|").append(lop.maLop()).append('\n');

                for (var hocSinh : lop.danhSachHocSinh()) {
                    String diem = hocSinh.tongDiemDauVao()
                            .stripTrailingZeros()
                            .toPlainString();

                    noiDung.append("HOC_SINH|")
                            .append(hocSinh.maHoSoNhapHoc()).append('|')
                            .append(hocSinh.maHocSinh()).append('|')
                            .append(diem).append('\n');
                }
            }
        }

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] duLieu = noiDung.toString().getBytes(StandardCharsets.UTF_8);
            byte[] maBam = sha256.digest(duLieu);
            return HexFormat.of().formatHex(maBam);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Không thể tính mã kiểm tra phương án", e);
        }
    }
    // Ghi đợt chốt và kết quả của tất cả học sinh trong cùng giao dịch, dùng chung ngày vào lớp giáo vụ chọn.
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int chotXepLop10(ConfirmGrade10PlacementRequest yeuCau) {
        long maTaiKhoan = nguoiDung.yeuCauVaiTro("ADMIN", "BGH", "GIAO_VU");
        Long maNamHoc = yeuCau.maNamHoc();

        schoolClassDao.khoaNamHocDeMoLop(maNamHoc).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học có mã: " + maNamHoc));
        if(enrollmentDao.daChotXepLop10(maNamHoc)){
            throw new BusinessException("Năm học này đã chốt xếp lớp 10");
        }
        LocalDate thoiDiemChot = LocalDate.now(clock);
        LocalDate ngayVaoLop = yeuCau.ngayVaoLop();
        if (ngayVaoLop.isBefore(thoiDiemChot.atStartOfDay().toLocalDate())) {
            throw new BusinessException("Ngày vào lớp phải từ hôm nay trở đi");
        }
        SchoolYearPeriodResponse namHoc = enrollmentDao.namHoc(maNamHoc);
        if (ngayVaoLop.isBefore(namHoc.getBatDau())
                || ngayVaoLop.isAfter(namHoc.getKetThuc())) {
            throw new BusinessException("Ngày vào lớp phải nằm trong năm học đã chọn");
        }

        //lặp lại phương án và so với mã người dùng gửi
        Grade10PlacementPreviewResponse phuongAn = kiemTraPhuongAnTruocKhiChot(yeuCau);

        //lưu đợt chốt xếp lớp 10
        enrollmentDao.luuDotChotXepLop10(maNamHoc, maTaiKhoan, ngayVaoLop, thoiDiemChot.atStartOfDay(), phuongAn.maKiemTraPhuongAn());

        //ghi tung hoc sinh theo dung phuong an da kiem tra
        int soHocSinhDaXep = 0;
        for(var toHop : phuongAn.danhSachToHop()){
            for(var lop : toHop.danhSachLop()){
                for (var hocSinh : lop.danhSachHocSinh()){
                    if (enrollmentDao.coXepLopTrongNamHoc(
                            hocSinh.maHocSinh(), maNamHoc)) {
                        throw new BusinessException(
                                "Học sinh " + hocSinh.maSoHocSinh()
                                        + " đã có lớp trong năm học này");
                    }
                    Enrollment en = new Enrollment();
                    en.setMaHocSinh(hocSinh.maHocSinh());
                    en.setMaLop(lop.maLop());
                    en.setTuNgay(ngayVaoLop);
                    en.setDenNgay(null);
                    en.setLyDoKetThuc(null);
                    en.setMaTaiKhoanThucHien(maTaiKhoan);
                    en.setThoiDiemThucHien(LocalDateTime.now(clock));
                    en.setMaHoSoNhapHoc(hocSinh.maHoSoNhapHoc());
                    enrollmentDao.tao(en);
                    soHocSinhDaXep++;
                }
            }
        }
        return soHocSinhDaXep;
    }

    // Tính lại phương án từ dữ liệu hiện tại rồi đối chiếu với mã của bản giáo vụ đã xem.
    private Grade10PlacementPreviewResponse kiemTraPhuongAnTruocKhiChot(
            ConfirmGrade10PlacementRequest yeuCau) {

        // Mã của bản xem trước mà giáo vụ đã duyệt.
        String maDaDuyet = yeuCau.maKiemTraPhuongAn();

        // Lập lại phương án từ dữ liệu hiện tại của đúng năm học đó.
        Grade10PlacementPreviewResponse phuongAnHienTai =
                xemTruocXepLop10(yeuCau.maNamHoc());

        String maHienTai = phuongAnHienTai.maKiemTraPhuongAn();

        if (!maHienTai.equals(maDaDuyet)) {
            throw new BusinessException(
                    "Phương án xếp lớp đã thay đổi. Vui lòng xem trước và duyệt lại.");
        }

        return phuongAnHienTai;
    }


    // Xếp riêng một học sinh chưa có lớp trong năm học, tính từ ngày vào lớp được chọn.
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

        // Kiểm tra lớp còn chỗ trước khi thêm học sinh.
        Integer siSoToiDa = lopHoc.getSiSoToiDa();

        if (siSoToiDa != null) {
            LocalDate ngayKetThucNamHoc =
                    enrollmentDao.namHoc(lopHoc.getMaNamHoc()).getKetThuc();

            Optional<LocalDate> ngayHetCho = enrollmentDao.timNgayLopHetCho(
                    maLop,
                    tuNgay,
                    ngayKetThucNamHoc,
                    siSoToiDa
            );

            if (ngayHetCho.isPresent()) {
                throw new BusinessException(
                        "Không thể xếp học sinh vào lớp " + lopHoc.getTenLop()
                                + ". Lớp đã đủ " + siSoToiDa
                                + " học sinh vào ngày " + ngayHetCho.get()
                );
            }
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

    // Kết thúc phân lớp nguồn vào ngày trước ngày chuyển và ghi phân lớp đích từ ngày chuyển.
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

        Integer siSoToiDa = lopDich.getSiSoToiDa();

        if (siSoToiDa != null) {
            LocalDate ngayKetThucNamHoc =
                    enrollmentDao.namHoc(lopDich.getMaNamHoc()).getKetThuc();

            Optional<LocalDate> ngayHetCho = enrollmentDao.timNgayLopHetCho(
                    lopDich.getMaLop(),
                    ngayChuyen,
                    ngayKetThucNamHoc,
                    siSoToiDa
            );

            if (ngayHetCho.isPresent()) {
                throw new BusinessException(
                        "Không thể chuyển vào lớp " + lopDich.getTenLop()
                                + " vì lớp hết chỗ vào ngày " + ngayHetCho.get()
                                + " (sĩ số tối đa " + siSoToiDa + ")");
            }
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

    private SchoolClass khoaLop(Long maLop){
        Optional<SchoolClass> lop = schoolClassDao.timTheoMaDeCapNhat(maLop);
        if (lop.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy lớp học có mã: " + maLop);
        }
        SchoolClass lopHoc = lop.get();
        return lopHoc;
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
}
