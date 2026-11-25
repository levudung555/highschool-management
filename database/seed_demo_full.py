"""Demo fixtures for local highschool_management_v2. Default: validate then rollback.
Run: python -X utf8 database/seed_demo_full.py --apply
Requires pymysql. Never updates/deletes existing rows. One transaction, guarded reruns.
"""
import argparse
from collections import Counter
from datetime import date, datetime, timedelta
import json
import os
from pathlib import Path
import re
import pymysql

ROOT = Path(__file__).resolve().parents[1]
MARKER = 'DEMOFULL26_FULL_V1'
STAMP = '2026-11-19 08:00:00'
HASH = '$2a$10$QCgbrfozJ55A6KZNuDN0w.uFrOArTJgFuMyDsOSzAtfkz.tWqxO3C'

def connect():
    config = (ROOT / 'src/main/resources/application.yaml').read_text(encoding='utf-8')
    def field(name):
        value = re.search(r'^\s*' + name + r':\s*(.*)$', config, re.M)[1].strip().strip('\"\x27')
        return re.sub(r'\$\{([^}:]+)(?::([^}]*))?\}', lambda m: os.environ.get(m[1], m[2] or ''), value)
    url = field('url')
    match = re.fullmatch(r'jdbc:mysql://([^:/]+)(?::(\d+))?/([^?]+)(?:\?.*)?', url)
    if not match or match[1] not in ('localhost', '127.0.0.1') or match[3] != 'highschool_management_v2':
        raise RuntimeError('This fixture script only supports local highschool_management_v2.')
    return pymysql.connect(host=match[1], port=int(match[2] or 3306), database=match[3],
                           user=field('username'), password=field('password'), charset='utf8mb4', autocommit=False)

def seed(apply=False):
    conn = connect()
    counts = Counter()
    try:
        with conn.cursor() as q:
            q.execute("SELECT GET_LOCK('DEMOFULL26_FULL_V1', 5)")
            if q.fetchone()[0] != 1:
                raise RuntimeError('Another demo seed is running.')
            q.execute("SET SESSION time_zone = '+07:00'")
            q.execute('SELECT MaNK FROM nhat_ky_he_thong WHERE HanhDong=%s', (MARKER,))
            if q.fetchone():
                print('Demo already exists; no changes made.')
                return
            def find(table, pk, **where):
                q.execute(f'SELECT `{pk}` FROM `{table}` WHERE ' + ' AND '.join(f'`{k}`=%s' for k in where), tuple(where.values()))
                row = q.fetchone()
                return row[0] if row else None
            def add(table, **data):
                q.execute(f'INSERT INTO `{table}` (' + ','.join(f'`{k}`' for k in data) + ') VALUES (' + ','.join(['%s']*len(data)) + ')', tuple(data.values()))
                counts[table] += 1
                return q.lastrowid
            # Refuse a partially populated namespace instead of changing user/demo records.
            for table, col in [('hoc_sinh','MaSoHS'),('giao_vien','MaSoGV'),('lop_hoc','TenLop'),('mon_hoc','MaSoMon')]:
                q.execute(f"SELECT COUNT(*) FROM `{table}` WHERE `{col}` LIKE 'DEMOFULL26-%'")
                if q.fetchone()[0]:
                    raise RuntimeError('DEMOFULL26 namespace already exists without completion marker: ' + table)
            roles = {}
            for code, label in [('ADMIN','Quản trị'),('BGH','Ban giám hiệu'),('GIAO_VU','Giáo vụ'),('GIAO_VIEN','Giáo viên')]:
                roles[code] = find('vai_tro','MaVT',MaSoVaiTro=code) or add('vai_tro',MaSoVaiTro=code,TenVaiTro=label)
            def account(login, name, role, teacher=None):
                if find('tai_khoan','MaTK',TenDangNhap=login):
                    raise RuntimeError('Demo account already exists: ' + login)
                key=add('tai_khoan',TenDangNhap=login,HoTen=name,MatKhauBam=HASH,MaGV=teacher,TrangThai='HOAT_DONG')
                add('tai_khoan_vai_tro',MaTK=key,MaVT=roles[role])
                return key
            admin=account('demofull26_admin','Quản trị demo 2026','ADMIN')
            actor=account('demofull26_giaovu','Giáo vụ demo 2026','GIAO_VU')
            bgh=account('demofull26_bgh','Ban giám hiệu demo 2026','BGH')
            years={}; semesters={}; periods={}
            for y in [2025,2026,2027]:
                years[y]=find('nam_hoc','MaNH',MaSoNH=f'{y}-{y+1}') or add('nam_hoc',MaSoNH=f'{y}-{y+1}',NgayBatDau=f'{y}-09-01',NgayKetThuc=f'{y+1}-05-31')
                for term,start,end in [(1,f'{y}-09-01',f'{y+1}-01-15'),(2,f'{y+1}-01-18',f'{y+1}-05-31')]:
                    status='DA_KET_THUC' if y==2025 else ('DANG_DIEN_RA' if y==2026 and term==1 else 'CHUA_BAT_DAU')
                    hk=find('hoc_ky','MaHK',MaNH=years[y],SoHocKy=term) or add('hoc_ky',MaNH=years[y],SoHocKy=term,NgayBatDau=start,NgayKetThuc=end,TrangThai=status)
                    semesters[y,term]=hk
                    q.execute('SELECT NgayBatDau,NgayKetThuc FROM hoc_ky WHERE MaHK=%s',(hk,))
                    periods[y,term]=q.fetchone()
            grades={g:find('khoi_lop','MaKhoi',SoKhoi=g) or add('khoi_lop',SoKhoi=g) for g in [10,11,12]}
            subjects=[]
            for code,name in [('TOAN','Toán'),('VAN','Ngữ văn'),('ANH','Tiếng Anh'),('LY','Vật lý'),('HOA','Hóa học'),('SINH','Sinh học'),('SU','Lịch sử'),('GDTC','Giáo dục thể chất')]:
                subjects.append((add('mon_hoc',MaSoMon='DEMOFULL26-'+code,TenMon=name+' (DEMOFULL26)'),name,'NHAN_XET' if code=='GDTC' else 'DIEM_SO_KEM_NHAN_XET'))
            surnames=['Nguyễn','Trần','Lê','Phạm','Hoàng','Vũ','Đặng','Bùi','Đỗ','Hồ']
            names=['Minh An','Thu Hà','Hoàng Nam','Ngọc Linh','Quang Huy','Mai Anh','Đức Long','Thanh Tâm','Gia Bảo','Khánh Vy','Tuấn Kiệt','Phương Thảo','Hải Đăng','Bảo Ngọc','Minh Khang']
            teachers=[]
            for i in range(30):
                teachers.append(add('giao_vien',MaSoGV=f'DEMOFULL26-GV{i+1:03}',HoTen=f'{surnames[i%10]} {names[i%15]} (DEMOFULL26)',NgaySinh=f'{1980+i%15}-03-15',Email=f'gv{i+1}@demofull26.example',ChuyenMon=subjects[i%8][1],TrangThai='NGUNG_CONG_TAC' if i>=28 else 'DANG_CONG_TAC'))
            teacher_users={}
            for i in range(12):
                teacher_users[i]=account(f'demofull26_gv{i+1:02}',f'Giáo viên demo {i+1:02}','GIAO_VIEN',teachers[i])
            classes={}; class_subjects={}; assignments={}
            approved=dict(TrangThaiDuyet='DA_DUYET',MaTKDuyet=actor,DuyetLuc=STAMP)
            layouts={2025:[(g,a) for g in [10,11] for a in [1,2]],2026:[(g,a) for g in [10,11,12] for a in range(1,5)],2027:[(g,1) for g in [10,11,12]]}
            for y,layout in layouts.items():
                for ci,(g,a) in enumerate(layout):
                    cls=add('lop_hoc',MaNH=years[y],MaKhoi=grades[g],TenLop=f'DEMOFULL26-{g}A{a}',TrangThai='HOAT_DONG')
                    classes[y,g,a]=cls
                    add('phan_cong_chu_nhiem',MaLop=cls,MaGV=teachers[ci],TuNgay=f'{y}-09-01',DenNgay=f'{y+1}-05-31',**approved)
                    for term in [1,2]:
                        start,end=periods[y,term]
                        for si,(mh,_,form) in enumerate(subjects):
                            lmh=add('lop_mon_hoc',MaLop=cls,MaMH=mh,MaHK=semesters[y,term],HinhThucDanhGia=form,SoTietNam=105 if si<3 else 70)
                            class_subjects[cls,term,si]=lmh
                            # Keep semester 2 of next year free for manual assignment tests.
                            if y==2027 and term==2: continue
                            teacher=teachers[si]
                            if y==2026 and ci==0 and term==1 and si==0:
                                add('phan_cong_giang_day',MaLMH=lmh,MaGV=teachers[16],TuNgay=start,DenNgay='2026-10-31',**approved)
                                start=date(2026,11,1)
                            assignments[cls,term,si]=add('phan_cong_giang_day',MaLMH=lmh,MaGV=teacher,TuNgay=start,DenNgay=end,**approved)
            empty=add('lop_hoc',MaNH=years[2026],MaKhoi=grades[10],TenLop='DEMOFULL26-10TRONG',TrangThai='HOAT_DONG')
            closed=add('lop_hoc',MaNH=years[2026],MaKhoi=grades[10],TenLop='DEMOFULL26-10DONG',TrangThai='NGUNG_HOAT_DONG')
            for cls in [empty,closed]:
                for si,(mh,_,form) in enumerate(subjects):
                    add('lop_mon_hoc',MaLop=cls,MaMH=mh,MaHK=semesters[2026,1],HinhThucDanhGia=form,SoTietNam=70)
            current=[]; historical=[]; students=[]
            for i in range(330):
                g=10+min(i//100,2)
                hs=add('hoc_sinh',MaSoHS=f'DEMOFULL26-HS{i+1:04}',HoTen=f'{surnames[i%10]} {names[(i//10)%15]} {i+1:03}',NgaySinh=f'{2026-g-5}-{i%12+1:02}-{i%27+1:02}',GioiTinh='NAM' if i%2==0 else 'NU',DiaChi=f'{i+1} đường Mẫu, phường Demo',Email=f'hs{i+1}@demofull26.example',NgayTiepNhan='2025-08-20' if g>10 and i<300 else '2026-08-20')
                students.append(hs)
                for parent in range(2 if i%5==0 else 1):
                    guardian=add('nguoi_giam_ho',HoTen=f'{surnames[i%10]} '+('Văn Thành' if parent==0 else 'Thị Hương')+f' (DEMOFULL26-{i+1:04})',Email=f'ph{i+1}-{parent}@demofull26.example',DiaChi=f'{i+1} đường Mẫu, phường Demo')
                    add('hs_nguoi_giam_ho',MaHS=hs,MaNGH=guardian,MoiQuanHe='CHA' if parent==0 else 'ME',LienHeChinh=1 if parent==0 else 0)
                if i>=300: continue
                a=(i%100)//25+1; cls=classes[2026,g,a]
                if g>10:
                    old=classes[2025,g-1,1 if a<=2 else 2]
                    add('xep_lop',MaHS=hs,MaLop=old,TuNgay='2025-09-01',DenNgay='2026-05-31',LyDoKetThuc='Kết thúc năm học (DEMOFULL26)',MaTKNguoiThucHien=actor,ThoiDiemThucHien=STAMP)
                    historical.append((hs,old,i))
                if i%25==0:
                    add('xep_lop',MaHS=hs,MaLop=cls,TuNgay='2026-09-01',DenNgay='2026-10-31',LyDoKetThuc='Chuyển lớp theo nguyện vọng (DEMOFULL26)',MaTKNguoiThucHien=actor,ThoiDiemThucHien=STAMP)
                    cls=classes[2026,g,a%4+1]; start='2026-11-01'
                else: start='2026-09-01'
                xl=add('xep_lop',MaHS=hs,MaLop=cls,TuNgay=start,DenNgay=None,MaTKNguoiThucHien=actor,ThoiDiemThucHien=STAMP)
                current.append((hs,cls,xl,i))
            for ci,(g,a) in enumerate(layouts[2026]):
                cls=classes[2026,g,a]; roster=[r for r in current if r[1]==cls]
                days=[date(2026,11,2)+timedelta(days=d) for d in range(18) if (date(2026,11,2)+timedelta(days=d)).weekday()<5]
                for di,day in enumerate(days):
                    for scope,frame,subject in [('BUOI','SANG',None),('BUOI','CHIEU',None)]+([('TIET','TIET_1',class_subjects[cls,1,0])] if day==date(2026,11,19) else []):
                        pdd=add('phien_diem_danh',MaLop=cls,MaHK=semesters[2026,1],MaLMH=subject,Ngay=day,PhamVi=scope,MaKhung=frame,TrangThai='DANG_NHAP' if day==date(2026,11,19) else 'DA_CHOT',MaTKTao=actor)
                        for hs,_,xl,i in roster:
                            pick=(i+di)%20
                            status='VANG_CO_PHEP' if pick==0 else 'VANG_KHONG_PHEP' if pick==1 else 'CO_MAT'
                            if day==date(2026,11,19) and pick==2: status='CHUA_GHI_NHAN'
                            add('diem_danh',MaPDD=pdd,MaXL=xl,TrangThai=status,SoPhutMuon=10 if status=='CO_MAT' and pick==3 else 0,GhiChu='Dữ liệu giả lập DEMOFULL26',MaTKCapNhat=actor,CapNhatLuc=f'{day} 16:00:00')
            # Current assessments have no final exam yet; completed old year includes summaries.
            for y,rows in [(2026,[(hs,cls,i) for hs,cls,xl,i in current]),(2025,historical)]:
                terms=[1] if y==2026 else [1,2]
                for hs,cls,i in rows:
                    for term in terms:
                        for si in [0,1,7]:
                            mh,_,form=subjects[si]
                            kqm=add('ket_qua_mon',MaHS=hs,MaMH=mh,MaHK=semesters[y,term],HinhThucDanhGia=form,TrangThai='DANG_HOC' if y==2026 else 'DA_CHOT')
                            evals=[('THUONG_XUYEN',1),('THUONG_XUYEN',2),('GIUA_KY',1)] + ([('CUOI_KY',1)] if y==2025 else [])
                            for ei,(kind,num) in enumerate(evals):
                                day=date(2026,11,3+ei*5) if y==2026 else periods[y,term][1]-timedelta(days=18-ei*5)
                                add('lan_danh_gia',MaKQM=kqm,MaPCGD=assignments[cls,term,si],LoaiDanhGia=kind,SoThuTu=num,NgayDanhGia=day,DiemSo=round(5+(i+ei)%51/10,1) if form!='NHAN_XET' else None,MucDanhGia='DAT' if form=='NHAN_XET' else None,NhanXet='Kết quả giả lập DEMOFULL26',TrangThai='DA_DANH_GIA',MaTKCapNhat=actor,CapNhatLuc=STAMP)
                        if y==2025:
                            add('tong_ket_hoc_ky',MaHS=hs,MaHK=semesters[y,term],KetQuaHocTap='KHA',KetQuaRenLuyen='TOT',NhanXet='Tổng kết minh họa DEMOFULL26, chưa tính bằng service',**approved)
                    if y==2025:
                        add('tong_ket_nam',MaHS=hs,MaNH=years[y],KetQuaHocTap='KHA',KetQuaRenLuyen='TOT',KetQuaLenLop='LEN_LOP',**approved)
            # Validate relationships beyond the foreign keys before committing.
            checks={
                'enrollment overlaps': "SELECT COUNT(*) FROM xep_lop a JOIN xep_lop b ON a.MaHS=b.MaHS AND a.MaXL<b.MaXL JOIN hoc_sinh h ON h.MaHS=a.MaHS JOIN lop_hoc la ON la.MaLop=a.MaLop JOIN lop_hoc lb ON lb.MaLop=b.MaLop WHERE h.MaSoHS LIKE 'DEMOFULL26-%' AND a.TuNgay<=COALESCE(b.DenNgay,(SELECT NgayKetThuc FROM nam_hoc WHERE MaNH=lb.MaNH)) AND b.TuNgay<=COALESCE(a.DenNgay,(SELECT NgayKetThuc FROM nam_hoc WHERE MaNH=la.MaNH))",
                'attendance membership': "SELECT COUNT(*) FROM diem_danh d JOIN phien_diem_danh p ON p.MaPDD=d.MaPDD JOIN xep_lop x ON x.MaXL=d.MaXL JOIN lop_hoc l ON l.MaLop=p.MaLop WHERE l.TenLop LIKE 'DEMOFULL26-%' AND (x.MaLop<>p.MaLop OR p.Ngay<x.TuNgay OR p.Ngay>COALESCE(x.DenNgay,'9999-12-31'))",
                'assessment assignment': "SELECT COUNT(*) FROM lan_danh_gia d JOIN ket_qua_mon k ON k.MaKQM=d.MaKQM JOIN hoc_sinh h ON h.MaHS=k.MaHS JOIN phan_cong_giang_day p ON p.MaPCGD=d.MaPCGD JOIN lop_mon_hoc x ON x.MaLMH=p.MaLMH WHERE h.MaSoHS LIKE 'DEMOFULL26-%' AND (k.MaMH<>x.MaMH OR k.MaHK<>x.MaHK OR d.NgayDanhGia<p.TuNgay OR d.NgayDanhGia>COALESCE(p.DenNgay,'9999-12-31'))"
            }
            for label,sql in checks.items():
                q.execute(sql)
                if q.fetchone()[0]: raise RuntimeError('Validation failed: '+label)
            for table,pk,group,end_join,end_col in [
                ('phan_cong_chu_nhiem','MaPCCN','MaLop','JOIN lop_hoc l ON l.MaLop=a.MaLop JOIN nam_hoc n ON n.MaNH=l.MaNH','n.NgayKetThuc'),
                ('phan_cong_giang_day','MaPCGD','MaLMH','JOIN lop_mon_hoc x ON x.MaLMH=a.MaLMH JOIN lop_hoc l ON l.MaLop=x.MaLop JOIN hoc_ky n ON n.MaHK=x.MaHK','n.NgayKetThuc')
            ]:
                q.execute(f"SELECT COUNT(*) FROM {table} a JOIN {table} b ON a.{group}=b.{group} AND a.{pk}<b.{pk} {end_join} WHERE l.TenLop LIKE 'DEMOFULL26-%' AND a.TrangThaiDuyet='DA_DUYET' AND b.TrangThaiDuyet='DA_DUYET' AND a.TuNgay<=COALESCE(b.DenNgay,{end_col}) AND b.TuNgay<=COALESCE(a.DenNgay,{end_col})")
                if q.fetchone()[0]: raise RuntimeError('Overlapping demo assignments: '+table)
                checks[table+' overlaps']='checked'
            add('nhat_ky_he_thong',MaTK=admin,HanhDong=MARKER,LoaiDoiTuong='DEMO_DATASET',MaDoiTuong=MARKER,DuLieuSau=json.dumps(dict(counts)),ThoiDiem=STAMP)
            report={'applied':apply,'rows_added':dict(counts),'total_rows':sum(counts.values()),'accounts':['demofull26_admin','demofull26_giaovu','demofull26_bgh']+[f'demofull26_gv{i:02}' for i in range(1,13)],'validation':list(checks)}
            if apply:
                conn.commit()
                (ROOT/'database/demo_full_report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
            else: conn.rollback()
            print(json.dumps(report,ensure_ascii=False,indent=2))
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apply',action='store_true')
    seed(parser.parse_args().apply)
