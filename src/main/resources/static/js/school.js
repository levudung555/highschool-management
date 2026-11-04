import {authorizedFetch,currentUser,logout} from './auth.js';
// Giao diện Bootstrap; API cùng origin với Spring Boot, không cần CORS hoặc frontend server.
const $ = (id) => document.getElementById(id);
const paths = {
 logout:'<path d="M9 3H4v18h5M13 8l5 4-5 4m-5-4h10"/>',
 school:'<path d="m3 10 9-7 9 7v10H3z"/><path d="M9 20v-7h6v7M8 9h.01M16 9h.01"/>',
 building:'<path d="M4 21V7h6V3h10v18M2 21h20M7 10v2m0 3v2m7-11h3m-3 4h3m-3 4h3m-3 4h3"/>',
 grid:'<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
 users:'<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6m3 10v-3a6 6 0 0 0-2-4"/>',
 inbox:'<path d="m3 13 3-9h12l3 9v7H3zM3 13h5l2 3h4l2-3h5"/>',
 history:'<path d="M3 11a9 9 0 1 1 3 8M3 4v7h7M12 7v5l3 2"/>',
 shield:'<path d="m12 3 8 3v6c0 5-8 9-8 9s-8-4-8-9V6zM8 12l3 3 5-6"/>',
 refresh:'<path d="M20 8a8 8 0 0 0-14-3L3 8m0-5v5h5M4 16a8 8 0 0 0 14 3l3-3m0 5v-5h-5"/>',
 'check-circle':'<circle cx="12" cy="12" r="9"/><path d="m8 12 3 3 5-6"/>',
 clock:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
 plus:'<path d="M12 5v14M5 12h14"/>', search:'<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>',
 'filter-x':'<path d="M3 5h18M6 10h12M9 15h3m4 1 5 5m0-5-5 5"/>',
 edit:'<path d="m14 5 5 5M4 20l5-1L21 7l-5-5L4 14z"/>',
 trash:'<path d="M3 6h18M9 6V3h6v3M5 6l1 15h12l1-15M10 10v7m4-7v7"/>',
 info:'<circle cx="12" cy="12" r="9"/><path d="M12 11v6m0-10h.01"/>',
 transfer:'<path d="M3 7h17m-4-4 4 4-4 4M21 17H4m4-4-4 4 4 4"/>',
 eye:'<path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12"/><circle cx="12" cy="12" r="3"/>',
 menu:'<path d="M4 6h16M4 12h16M4 18h16"/>',
 check:'<path d="m5 12 4 4L19 6"/>', x:'<path d="m6 6 12 12M6 18 18 6"/>'
};
function icon(name) { return `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">${paths[name] || paths.info}</svg>`; }
function icons() { document.querySelectorAll('[data-icon]').forEach(el => { el.innerHTML = icon(el.dataset.icon); el.removeAttribute('data-icon'); }); }
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const fmt = value => new Intl.NumberFormat('vi-VN').format(value || 0);
const date = value => value ? new Intl.DateTimeFormat('vi-VN').format(new Date(value.slice(0,10)+'T12:00:00')) : 'Chưa xác định';
const addDay = (day,n=1) => { const d=new Date(day+'T12:00:00Z'); d.setUTCDate(d.getUTCDate()+n); return d.toISOString().slice(0,10); };
const query = values => new URLSearchParams(Object.entries(values).filter(([,v])=>v!=='' && v!==null && v!==undefined)).toString();
const state={view:'classes',catalog:null,classes:[],classPage:0,requestPage:0,rosterPage:0,requestStatus:'CHO_DUYET',classRows:[],requestRows:[],rosterRows:[],students:new Map(),seq:{}};
const modal = name => bootstrap.Modal.getOrCreateInstance($(name));
function setError(container,message) { const el=container.querySelector('.form-error'); if(el){el.textContent=message;el.classList.remove('d-none');} }
function clearErrors(container=document) { container.querySelectorAll('.form-error').forEach(el=>{el.textContent='';el.classList.add('d-none');}); }
function toast(message) { $('toastMessage').textContent=message; bootstrap.Toast.getOrCreateInstance($('successToast'),{delay:4500}).show(); }
function globalError(error) { $('globalError').textContent=error.message || 'Có lỗi xảy ra. Vui lòng thử lại.'; $('globalError').classList.remove('d-none'); }
async function api(path,options={}) {
 let response;
 try { response=await authorizedFetch('/api/v1'+path,{...options,headers:{...(options.body?{'Content-Type':'application/json'}:{}),...options.headers}}); }
 catch { throw new Error('Không kết nối được hệ thống. Kiểm tra Spring Boot đã chạy và thử lại.'); }
 if(response.status===204)return null;
 const body=await response.json().catch(()=>({}));
 if(!response.ok) {
  if(response.status===401)throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
  throw new Error(body.message || (response.status===500?'Hệ thống chưa xử lý được yêu cầu. Kiểm tra dữ liệu và thử lại.':`Yêu cầu không thành công (${response.status}).`));
 }
 return body;
}
async function busy(button,task) {
 if(button.disabled)return;
 const original=button.innerHTML;button.disabled=true;button.innerHTML='<span class="spinner-border" aria-hidden="true"></span> Đang xử lý';
 try {return await task();} finally {button.disabled=false;button.innerHTML=original;}
}
const status = value => {
 const map={HOAT_DONG:['Đang hoạt động',''],NGUNG_HOAT_DONG:['Ngừng hoạt động','stopped'],CHO_DUYET:['Chờ duyệt','pending'],DA_DUYET:['Đã duyệt',''],TU_CHOI:['Đã từ chối','rejected']};
 const [label,css]=map[value] || [value,'stopped'];return `<span class="status-badge ${css}">${escapeHtml(label)}</span>`;
};
function empty(tbody,cols,title,message) { $(tbody).innerHTML=`<tr><td colspan="${cols}" class="empty-state"><div class="empty-symbol">${icon('inbox')}</div><strong>${escapeHtml(title)}</strong><p>${escapeHtml(message)}</p></td></tr>`; }
function loading(tbody,cols) { $(tbody).innerHTML=`<tr><td class="table-loading" colspan="${cols}"><span class="spinner-border me-2" aria-hidden="true"></span> Đang tải dữ liệu…</td></tr>`; }
function tableError(tbody,cols,error) { empty(tbody,cols,'Chưa tải được dữ liệu',error.message); }
function pager(id,data,onPage) {
 const {trang:p,kichThuoc:s,tongSoPhanTu:total,tongSoTrang:pages}=data;
 const start=total&&data.danhSach.length?p*s+1:0,end=total?Math.min((p+1)*s,total):0;
 const selected=new Set([0,p-1,p,p+1,pages-1]);const nums=[...selected].filter(i=>i>=0&&i<pages).sort((a,b)=>a-b);let last=-1;
 let buttons=`<button class="page-button" data-page="${p-1}" ${p<=0?'disabled':''} aria-label="Trang trước">‹</button>`;
 for(const i of nums){if(last>=0&&i-last>1)buttons+='<span class="px-1">…</span>';buttons+=`<button class="page-button ${i===p?'current':''}" data-page="${i}" ${i===p?'aria-current="page"':''}>${i+1}</button>`;last=i;}
 buttons+=`<button class="page-button" data-page="${p+1}" ${p+1>=pages?'disabled':''} aria-label="Trang tiếp">›</button>`;
 $(id).innerHTML=`<span>Hiển thị <strong>${fmt(start)}–${fmt(end)}</strong> trong ${fmt(total)} ${id==='classPager'?'lớp học':id==='rosterPager'?'học sinh':'yêu cầu'}</span><div class="pager-buttons">${buttons}</div>`;
 $(id).querySelectorAll('[data-page]').forEach(button=>button.addEventListener('click',()=>onPage(Number(button.dataset.page))));
}
function selectOptions(id,items,label,value,placeholder) {
 const el=$(id),old=el.value;el.innerHTML=`<option value="">${escapeHtml(placeholder)}</option>`+items.map(item=>`<option value="${Number(value(item))}">${escapeHtml(label(item))}</option>`).join('');
 if([...el.options].some(o=>o.value===old))el.value=old;
}
function mayApprove(){return Boolean(state.user?.vaiTro.some(r=>r==='ADMIN'||r==='BGH'));}
function className(id) { const c=state.classes.find(c=>c.maLop===Number(id));return c?`${c.tenLop} · ${c.namHoc}`:`Lớp #${id}`; }
function classOptions(id,activeOnly=false,placeholder='Chọn lớp học') { selectOptions(id,state.classes.filter(c=>!activeOnly||c.trangThai==='HOAT_DONG'),c=>`${c.tenLop} · ${c.namHoc}`,c=>c.maLop,placeholder); }
async function loadAllClasses() {
 const data=[];let page=0;
 while(true){const result=await api('/classes?'+query({trang:page,kichThuoc:100}));data.push(...result.danhSach);if(++page>=result.tongSoTrang)break;}
 state.classes=data;classOptions('rosterClass',false,'Chọn lớp để xem danh sách');classOptions('enrollmentClassInput',true);
}

async function loadClasses(page=state.classPage) {
 state.classPage=page;const seq=state.seq.classes=(state.seq.classes||0)+1;loading('classRows',6);
 try {
  const result=await api('/classes?'+query({tuKhoa:$('classKeyword').value,maNamHoc:$('classYear').value,maKhoi:$('classGrade').value,trangThai:$('classStatus').value,trang:page,kichThuoc:8}));
  if(seq!==state.seq.classes)return;
  if(result.tongSoPhanTu&&page>=result.tongSoTrang)return loadClasses(Math.max(0,result.tongSoTrang-1));
  state.classRows=result.danhSach;$('classCount').textContent=fmt(result.tongSoPhanTu);
  if(!result.danhSach.length)empty('classRows',6,'Chưa có lớp học phù hợp','Thử thay đổi bộ lọc hoặc thêm lớp học đầu tiên.');
  else $('classRows').innerHTML=result.danhSach.map((c,i)=>`<tr><td><div class="class-identity"><span class="class-tile tone-${i%3}">${escapeHtml(c.tenLop.slice(0,2))}</span><div><strong>${escapeHtml(c.tenLop)}</strong><small>Mã lớp #${c.maLop}</small></div></div></td><td><span class="grade-chip">Khối ${c.soKhoi}</span></td><td>${escapeHtml(c.namHoc)}</td><td><span class="d-inline-flex align-items-center gap-2">${icon('users')}<strong>${fmt(c.siSo)}</strong><span class="subtle-label">học sinh</span></span></td><td>${status(c.trangThai)}</td><td><div class="row-actions"><button class="btn btn-outline-primary" data-action="roster" data-id="${c.maLop}">Danh sách</button><button class="btn icon-button" data-action="edit-class" data-id="${c.maLop}" title="Sửa lớp ${escapeHtml(c.tenLop)}" aria-label="Sửa lớp ${escapeHtml(c.tenLop)}">${icon('edit')}</button><button class="btn icon-button danger" data-action="delete-class" data-id="${c.maLop}" title="Xóa lớp ${escapeHtml(c.tenLop)}" aria-label="Xóa lớp ${escapeHtml(c.tenLop)}">${icon('trash')}</button></div></td></tr>`).join('');
  pager('classPager',result,p=>loadClasses(p));
 }catch(error){if(seq===state.seq.classes){tableError('classRows',6,error);$('classPager').textContent='';}}
}
async function loadRoster(page=state.rosterPage) {
 state.rosterPage=page;const seq=state.seq.roster=(state.seq.roster||0)+1;const id=$('rosterClass').value,day=$('rosterDate').value;
 if(!id||!day){empty('rosterRows',5,'Chọn lớp học để bắt đầu','Danh sách sẽ hiển thị học sinh đã được duyệt và còn thuộc lớp tại ngày chọn.');$('rosterPager').textContent='';return;}
 loading('rosterRows',5);
 try {
  const result=await api(`/classes/${id}/students?`+query({ngay:day,trang:page,kichThuoc:10}));if(seq!==state.seq.roster)return;
  state.rosterRows=result.danhSach;$('rosterBanner').textContent=`${className(id)} · ${fmt(result.tongSoPhanTu)} học sinh · Ngày ${date(day)}`;
  if(!result.danhSach.length)empty('rosterRows',5,'Lớp chưa có học sinh tại ngày này','Tạo yêu cầu xếp lớp và phê duyệt để học sinh xuất hiện trong danh sách.');
  else $('rosterRows').innerHTML=result.danhSach.map(s=>`<tr><td><div class="student-identity"><span class="student-avatar">${escapeHtml(s.hoTen.split(' ').filter(Boolean).slice(-2).map(x=>x[0]).join(''))}</span><div><strong>${escapeHtml(s.hoTen)}</strong><small>Hồ sơ #${s.maHocSinh}</small></div></div></td><td>${escapeHtml(s.maSoHocSinh)}</td><td>${date(s.ngaySinh)}</td><td>${date(s.tuNgay)} → ${date(s.denNgay)}</td><td><div class="row-actions"><button class="btn btn-outline-primary" data-action="student-history" data-id="${s.maHocSinh}">Lịch sử</button><button class="btn icon-button" data-action="student-transfer" data-id="${s.maHocSinh}" title="Chuyển lớp" aria-label="Chuyển lớp cho ${escapeHtml(s.hoTen)}">${icon('transfer')}</button></div></td></tr>`).join('');
  pager('rosterPager',result,p=>loadRoster(p));
 }catch(error){if(seq===state.seq.roster){tableError('rosterRows',5,error);$('rosterPager').textContent='';}}
}
async function loadRequests(page=state.requestPage) {
 state.requestPage=page;const seq=state.seq.requests=(state.seq.requests||0)+1;loading('requestRows',6);
 document.querySelectorAll('[data-request-status]').forEach(el=>el.classList.toggle('active',el.dataset.requestStatus===state.requestStatus));
 try {
  const result=await api('/enrollments?'+query({tuKhoa:$('requestKeyword').value,trangThai:state.requestStatus,trang:page,kichThuoc:8}));if(seq!==state.seq.requests)return;
  if(result.tongSoPhanTu&&page>=result.tongSoTrang)return loadRequests(Math.max(0,result.tongSoTrang-1));
  state.requestRows=result.danhSach;
  if(!result.danhSach.length)empty('requestRows',6,'Không có yêu cầu phù hợp',state.requestStatus==='CHO_DUYET'?'Mọi việc đã được xử lý. Bạn có thể tạo yêu cầu xếp hoặc chuyển lớp mới.':'Thử thay đổi trạng thái hoặc từ khóa tìm kiếm.');
  else $('requestRows').innerHTML=result.danhSach.map(r=>`<tr><td><div class="student-identity"><span class="student-avatar">${escapeHtml(r.hoTen.split(' ').filter(Boolean).slice(-2).map(x=>x[0]).join(''))}</span><div><strong>${escapeHtml(r.hoTen)}</strong><small>${escapeHtml(r.maSoHocSinh)}</small></div></div></td><td><span class="type-label">${icon(r.maXepLopNguon?'transfer':'plus')}${r.maXepLopNguon?'Chuyển lớp':'Xếp lớp'}</span><small class="subtle-label">Yêu cầu #${r.maXepLop}</small></td><td><strong>${escapeHtml(r.tenLop)}</strong><small class="d-block subtle-label mt-1">${escapeHtml(r.namHoc)}</small></td><td>${date(r.tuNgay)}</td><td>${status(r.trangThaiDuyet)}</td><td><div class="row-actions"><button class="btn icon-button" data-action="detail" data-id="${r.maXepLop}" aria-label="Chi tiết yêu cầu ${r.maXepLop}" title="Chi tiết">${icon('eye')}</button>${r.trangThaiDuyet==='CHO_DUYET'&&mayApprove()?`<button class="btn btn-outline-primary" data-action="approve" data-id="${r.maXepLop}">${icon('check')} Duyệt</button><button class="btn icon-button danger" data-action="reject" data-id="${r.maXepLop}" aria-label="Từ chối yêu cầu ${r.maXepLop}" title="Từ chối">${icon('x')}</button>`:''}</div></td></tr>`).join('');
  pager('requestPager',result,p=>loadRequests(p));
 }catch(error){if(seq===state.seq.requests){tableError('requestRows',6,error);$('requestPager').textContent='';}}
}
async function loadStudents(target,keyword='',selected=null) {
 const seq=state.seq[target]=(state.seq[target]||0)+1;
 try {
  const result=await api('/enrollments/students?'+query({tuKhoa:keyword,kichThuoc:30}));if(seq!==state.seq[target])return;
  const items=result.danhSach;
  if(selected&&!items.some(s=>s.maHocSinh===selected.maHocSinh))items.unshift(selected);
  items.forEach(s=>state.students.set(s.maHocSinh,s));
  selectOptions(target,items,s=>`${s.hoTen} · ${s.maSoHocSinh}`,s=>s.maHocSinh,items.length?'Chọn học sinh':'Chưa có học sinh phù hợp');
  if(selected)$(target).value=selected.maHocSinh;
 }catch(error){if(seq===state.seq[target])throw error;}
}
async function loadHistory() {
 const id=$('historyStudent').value;if(!id){$('historyTimeline').innerHTML=`<div class="empty-state"><div class="empty-symbol">${icon('history')}</div><strong>Chưa chọn học sinh</strong><p>Chọn hồ sơ bên trái để xem quá trình xếp lớp.</p></div>`;return;}
 const seq=state.seq.history=(state.seq.history||0)+1;$('historyTimeline').innerHTML='<div class="empty-state">Đang tải lịch sử…</div>';
 try {
  const data=await api(`/students/${id}/enrollments`);if(seq!==state.seq.history)return;
  const student=state.students.get(Number(id));$('historyTitle').textContent=student?student.hoTen:'Quá trình xếp lớp';$('historyCount').textContent=`${data.length} bản ghi`;
  $('historyTimeline').innerHTML=data.length?'<div class="timeline">'+data.map(e=>`<article class="timeline-item"><span class="timeline-dot ${e.trangThaiDuyet==='CHO_DUYET'?'pending':e.trangThaiDuyet==='TU_CHOI'?'rejected':''}"></span><h3>${escapeHtml(className(e.maLop))} ${status(e.trangThaiDuyet)}</h3><p>${icon('clock')} ${date(e.tuNgay)} → ${date(e.denNgay)}</p><p>${e.maXepLopNguon?`Chuyển từ phân lớp #${e.maXepLopNguon}`:'Yêu cầu xếp lớp'} · Mã #${e.maXepLop}</p>${e.lyDoChuyen?`<p>Lý do chuyển: ${escapeHtml(e.lyDoChuyen)}</p>`:''}${e.lyDoKetThuc?`<p>Kết thúc: ${escapeHtml(e.lyDoKetThuc)}</p>`:''}${e.duyetLuc?`<p>Đã xử lý ngày ${date(e.duyetLuc)} · Tài khoản #${e.maTaiKhoanDuyet}</p>`:''}</article>`).join('')+'</div>':`<div class="empty-state"><div class="empty-symbol">${icon('history')}</div><strong>Chưa có lịch sử xếp lớp</strong><p>Tạo yêu cầu xếp lớp để bắt đầu hành trình học tập.</p></div>`;
 }catch(error){if(seq===state.seq.history)$('historyTimeline').innerHTML=`<div class="empty-state">${escapeHtml(error.message)}</div>`;}
}
async function route() {
 const view=location.hash.slice(1);state.view=['classes','roster','requests','history'].includes(view)?view:'classes';
 const names={classes:'Lớp học',roster:'Danh sách lớp',requests:'Yêu cầu & phê duyệt',history:'Lịch sử học sinh'};
 $('breadcrumb').textContent=names[state.view];document.title=`${names[state.view]} · Schoolspace`;
 document.querySelectorAll('.view-panel').forEach(el=>el.hidden=el.id!==`view-${state.view}`);
 document.querySelectorAll('[data-view]').forEach(el=>el.classList.toggle('active',el.dataset.view===state.view));
 bootstrap.Offcanvas.getInstance($('sidebar'))?.hide();
 if(!state.catalog)return;
 if(state.view==='classes')await loadClasses();if(state.view==='roster')await loadRoster();if(state.view==='requests')await loadRequests();
 if(state.view==='history'){if(!$('historyStudent').options.length||$('historyStudent').options.length===1)await loadStudents('historyStudent');await loadHistory();}
}
async function afterChange() { await loadAllClasses();await route(); }
function openClass(id=null) {
 if(!state.catalog){globalError(new Error("Chưa kết nối hệ thống. Vui lòng tải lại dữ liệu trước."));return;}
 const form=$('classForm');form.reset();clearErrors(form);$('editClassId').value=id||'';
 const edit=Boolean(id);$('classModalTitle').textContent=edit?'Chỉnh sửa lớp học':'Thêm lớp học';$('saveClassBtn').textContent=edit?'Lưu thay đổi':'Tạo lớp học';$('editStatusField').hidden=!edit;
 $('classYearInput').disabled=edit;$('classGradeInput').disabled=edit;
 if(edit){const c=state.classRows.find(c=>c.maLop===id)||state.classes.find(c=>c.maLop===id);$('classNameInput').value=c.tenLop;$('classYearInput').value=c.maNamHoc;$('classGradeInput').value=c.maKhoi;$('classStatusInput').value=c.trangThai;}
 else if(state.catalog){const y=state.catalog.namHoc.find(y=>y.batDau<=state.catalog.homNay&&y.ketThuc>=state.catalog.homNay);if(y)$('classYearInput').value=y.maNamHoc;}
 modal('classModal').show();
}
function updateEnrollmentDate() {
 const c=state.classes.find(c=>c.maLop===Number($('enrollmentClassInput').value));const year=state.catalog.namHoc.find(y=>y.maNamHoc===c?.maNamHoc);const input=$('enrollmentDateInput');
 input.min=year&&year.batDau>state.catalog.homNay?year.batDau:state.catalog.homNay;input.max=year?.ketThuc||'';
 if(input.value<input.min)input.value=input.min;
}
async function openEnrollment(kind,student=null) {
 if(!state.catalog){globalError(new Error("Chưa kết nối hệ thống. Vui lòng tải lại dữ liệu trước."));return;}
 const form=$('enrollmentForm');form.reset();clearErrors(form);$('enrollmentKind').value=kind;
 $('enrollmentModalTitle').textContent=kind==='transfer'?'Tạo yêu cầu chuyển lớp':'Xếp học sinh vào lớp';$('transferReasonField').hidden=kind!=='transfer';$('transferReasonInput').required=kind==='transfer';
 classOptions('enrollmentClassInput',true,kind==='transfer'?'Chọn lớp đích':'Chọn lớp học');
 if(kind==='enroll'&&$('rosterClass').value)$('enrollmentClassInput').value=$('rosterClass').value;
 $('enrollmentDateInput').value=kind==='transfer'?addDay(state.catalog.homNay):state.catalog.homNay;updateEnrollmentDate();
 modal('enrollmentModal').show();
 try {await loadStudents('studentInput','',student);}catch(error){setError(form,error.message);}
}
let confirmTask=null;
function confirmAction(title,message,action,danger=false) {
 clearErrors($('confirmModal'));$('confirmTitle').textContent=title;$('confirmMessage').textContent=message;confirmTask=action;
 $('confirmActionBtn').className=`btn ${danger?'btn-danger':'btn-primary'}`;$('confirmActionBtn').textContent=title;modal('confirmModal').show();
}
async function detail(id) {
 $('detailBody').textContent='Đang tải…';$('detailTitle').textContent=`Yêu cầu #${id}`;modal('detailModal').show();
 try {
  const e=await api(`/enrollments/${id}`);const s=state.requestRows.find(r=>r.maXepLop===id);
  const fields=[['Học sinh',s?`${s.hoTen} · ${s.maSoHocSinh}`:`Hồ sơ #${e.maHocSinh}`],['Lớp đích',className(e.maLop)],['Loại yêu cầu',e.maXepLopNguon?'Chuyển lớp':'Xếp lớp'],['Trạng thái',e.trangThaiDuyet],['Ngày bắt đầu',date(e.tuNgay)],['Mốc kết thúc',date(e.denNgay)],['Phân lớp nguồn',e.maXepLopNguon?`#${e.maXepLopNguon}`:'Không có'],['Người duyệt',e.maTaiKhoanDuyet?`Tài khoản #${e.maTaiKhoanDuyet}`:'Chưa xử lý'],['Lý do chuyển',e.lyDoChuyen||'—'],['Lý do kết thúc',e.lyDoKetThuc||'—']];
  $('detailBody').innerHTML='<dl class="detail-grid">'+fields.map(([k,v])=>`<div><dt>${escapeHtml(k)}</dt><dd>${k==='Trạng thái'?status(v):escapeHtml(v)}</dd></div>`).join('')+'</dl><p class="form-text mb-0">Mốc kết thúc là ngày học sinh không còn thuộc phân lớp.</p>';
 }catch(error){$('detailBody').textContent=error.message;}
}
function debounce(fn,delay=300){let timer;return(...args)=>{clearTimeout(timer);timer=setTimeout(()=>fn(...args).catch(globalError),delay);};}
$('classForm').addEventListener('submit',async event=>{
 event.preventDefault();const form=event.currentTarget;clearErrors(form);
 await busy($('saveClassBtn'),async()=>{try{
  const id=$('editClassId').value;const body=id?{tenLop:$('classNameInput').value.trim(),trangThai:$('classStatusInput').value}:{tenLop:$('classNameInput').value.trim(),maNamHoc:Number($('classYearInput').value),maKhoi:Number($('classGradeInput').value)};
  await api(id?`/classes/${id}`:'/classes',{method:id?'PUT':'POST',body:JSON.stringify(body)});modal('classModal').hide();toast(id?'Đã cập nhật thông tin lớp.':'Đã tạo lớp học mới.');await afterChange();
 }catch(error){setError(form,error.message);}});
});
$('enrollmentForm').addEventListener('submit',async event=>{
 event.preventDefault();const form=event.currentTarget;clearErrors(form);
 await busy(form.querySelector('[type="submit"]'),async()=>{try{
  const transfer=$('enrollmentKind').value==='transfer';const body={maHocSinh:Number($('studentInput').value)};
  if(transfer){body.maLopDich=Number($('enrollmentClassInput').value);body.ngayChuyen=$('enrollmentDateInput').value;body.lyDo=$('transferReasonInput').value.trim();}
  else {body.maLop=Number($('enrollmentClassInput').value);body.tuNgay=$('enrollmentDateInput').value;}
  const result=await api(transfer?'/enrollments/transfers':'/enrollments',{method:'POST',body:JSON.stringify(body)});modal('enrollmentModal').hide();toast(`Đã tạo yêu cầu #${result.maXepLop}. Yêu cầu đang chờ duyệt.`);state.requestStatus='CHO_DUYET';state.requestPage=0;if(location.hash==='#requests')await loadRequests(0);else location.hash='requests';
 }catch(error){setError(form,error.message);}});
});
$('confirmActionBtn').addEventListener('click',async()=>{
 if(!confirmTask)return;await busy($('confirmActionBtn'),async()=>{try{await confirmTask();modal('confirmModal').hide();await afterChange();}catch(error){setError($('confirmModal'),error.message);}});
});
document.addEventListener('click',async event=>{
 const target=event.target.closest('[data-action]');if(!target)return;const id=Number(target.dataset.id);
 try {
  switch(target.dataset.action){
   case 'roster':$('rosterClass').value=id;state.rosterPage=0;if(location.hash==='#roster')await loadRoster(0);else location.hash='roster';break;
   case 'edit-class':openClass(id);break;
   case 'delete-class':{const c=state.classRows.find(c=>c.maLop===id);confirmAction('Xóa lớp học',`Bạn muốn xóa lớp ${c.tenLop}? Chỉ lớp chưa có xếp lớp, phân công, môn học hoặc điểm danh mới được xóa.`,async()=>{await api(`/classes/${id}`,{method:'DELETE'});toast('Đã xóa lớp học.');},true);break;}
   case 'detail':await detail(id);break;
   case 'approve':case 'reject':{const approve=target.dataset.action==='approve';const r=state.requestRows.find(r=>r.maXepLop===id);confirmAction(approve?'Duyệt yêu cầu':'Từ chối yêu cầu',`${approve?'Phê duyệt':'Từ chối'} yêu cầu #${id} của ${r.hoTen}? ${approve&&r.maXepLopNguon?'Phân lớp cũ sẽ kết thúc tại ngày chuyển.':''}`,async()=>{await api(`/enrollments/${id}/${approve?'approval':'rejection'}`,{method:'PUT'});toast(approve?'Đã phê duyệt yêu cầu.':'Đã từ chối yêu cầu.');},!approve);break;}
   case 'student-transfer':{const s=state.rosterRows.find(s=>s.maHocSinh===id);await openEnrollment('transfer',s);const next=addDay(s.tuNgay);if($('enrollmentDateInput').value<next)$('enrollmentDateInput').value=next;break;}
   case 'student-history':{const s=state.rosterRows.find(s=>s.maHocSinh===id);await loadStudents('historyStudent','',s);location.hash='history';if(state.view==='history')await loadHistory();break;}
  }
 }catch(error){globalError(error);}
});
$('addClassBtn').addEventListener('click',()=>openClass());
$('newEnrollBtn').addEventListener('click',()=>openEnrollment('enroll'));
$('newTransferBtn').addEventListener('click',()=>openEnrollment('transfer'));
$('rosterEnrollBtn').addEventListener('click',()=>openEnrollment('enroll'));
$('classFilters').addEventListener('submit',e=>{e.preventDefault();loadClasses(0);});
$('classKeyword').addEventListener('input',debounce(()=>loadClasses(0)));
['classYear','classGrade','classStatus'].forEach(id=>$(id).addEventListener('change',()=>loadClasses(0)));
$('resetClassFilters').addEventListener('click',()=>{$('classFilters').reset();loadClasses(0);});
$('requestFilters').addEventListener('submit',e=>{e.preventDefault();loadRequests(0);});
$('requestKeyword').addEventListener('input',debounce(()=>loadRequests(0)));
 document.querySelectorAll('[data-request-status]').forEach(el=>el.addEventListener('click',()=>{state.requestStatus=el.dataset.requestStatus;loadRequests(0);}));
$('rosterFilters').addEventListener('submit',e=>{e.preventDefault();loadRoster(0);});
$('rosterClass').addEventListener('change',()=>loadRoster(0));$('rosterDate').addEventListener('change',()=>loadRoster(0));
$('studentSearchInput').addEventListener('input',debounce(()=>loadStudents('studentInput',$('studentSearchInput').value)));
$('historySearch').addEventListener('input',debounce(()=>loadStudents('historyStudent',$('historySearch').value)));
$('historyLoadBtn').addEventListener('click',()=>loadHistory());
$('enrollmentClassInput').addEventListener('change',updateEnrollmentDate);
$('refreshBtn').addEventListener('click',()=>busy($('refreshBtn'),()=>init()).catch(globalError));
window.addEventListener('hashchange',()=>route().catch(globalError));
$('classModal').addEventListener('shown.bs.modal',()=>$('classNameInput').focus());
async function init() {
 $('globalError').classList.add('d-none');
 try {
  const user=await currentUser();if(!user)return;state.user=user;
  $('actorLabel').textContent=user.hoTen;
  const roleNames={ADMIN:'Quản trị viên',BGH:'Ban Giám hiệu',GIAO_VU:'Nhân viên giáo vụ',GIAO_VIEN:'Giáo viên'};
  $('modeLabel').textContent=user.vaiTro.map(r=>roleNames[r]||r).join(' · ');
  $('userAvatar').textContent=user.hoTen.split(' ').filter(Boolean).slice(-2).map(s=>s[0]).join('');
  const canManage=user.vaiTro.some(r=>['ADMIN','BGH','GIAO_VU'].includes(r));
  ['addClassBtn','newEnrollBtn','newTransferBtn','rosterEnrollBtn'].forEach(id=>$(id).disabled=!canManage);
  const c=await api('/classes/catalog');state.catalog=c;
  $('connection').innerHTML='<i></i> Đã kết nối';$('connection').className='connection-pill connected';
  $('todayLabel').textContent=new Intl.DateTimeFormat('vi-VN',{weekday:'long',day:'2-digit',month:'2-digit',year:'numeric'}).format(new Date(c.homNay+'T12:00:00'));
  $('rosterDate').value=$('rosterDate').value||c.homNay;
  selectOptions('classYear',c.namHoc,y=>y.ten,y=>y.maNamHoc,'Tất cả năm học');selectOptions('classGrade',c.khoi,g=>`Khối ${g.soKhoi}`,g=>g.maKhoi,'Tất cả khối');
  selectOptions('classYearInput',c.namHoc,y=>y.ten,y=>y.maNamHoc,'Chọn năm học');selectOptions('classGradeInput',c.khoi,g=>`Khối ${g.soKhoi}`,g=>g.maKhoi,'Chọn khối');
  const active=c.namHoc.find(y=>y.batDau<=c.homNay&&y.ketThuc>=c.homNay);$('schoolYearLabel').textContent=active?`Năm học hiện tại ${active.ten}`:'';
  await loadAllClasses();await route();
 }catch(error){$('connection').innerHTML='<i></i> Chưa kết nối';$('connection').className='connection-pill disconnected';globalError(error);empty('classRows',6,'Kết nối hệ thống để xem dữ liệu','Chạy Spring Boot với profile local rồi nhấn nút làm mới.');}
}
$('logoutBtn').addEventListener('click',()=>logout());
icons();route();init();
