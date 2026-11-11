// DOM regression checks for the current API contracts. No real database writes.
// Dependency: npm install --prefix target/ui-tools jsdom
const fs = require('node:fs');
const assert = require('node:assert/strict');
const { JSDOM } = require('../../../target/ui-tools/node_modules/jsdom');

async function main() {
  const dom = new JSDOM(fs.readFileSync('src/main/resources/static/index.html', 'utf8'), {
    url: 'http://localhost/#requests', runScripts: 'outside-only'
  });
  const w = dom.window;
  const students = [1, 2, 3].map(id => ({maHS:id, maSoHS:`HS${id}`, hoTen:`Học sinh ${id}`, ngaySinh:'2010-01-01'}));
  const classes = [
    {maLop:1, maNamHoc:1, maKhoi:1, tenLop:'10 cũ', namHoc:'2025-2026', trangThai:'HOAT_DONG'},
    {maLop:2, maNamHoc:2, maKhoi:1, tenLop:'10A1', namHoc:'2026-2027', trangThai:'HOAT_DONG'},
    {maLop:3, maNamHoc:2, maKhoi:1, tenLop:'10A2', namHoc:'2026-2027', trangThai:'HOAT_DONG'},
    {maLop:4, maNamHoc:2, maKhoi:2, tenLop:'11A1', namHoc:'2026-2027', trangThai:'HOAT_DONG'},
  ];
  const row = (id, student, cls, day, end=null) => ({maXepLop:id, maHocSinh:student, maLop:cls, tuNgay:day, denNgay:end,
    hoTen:`Học sinh ${student}`, maSoHocSinh:`HS${student}`, tenLop:classes.find(c=>c.maLop===cls).tenLop});
  const histories = {
    1:[row(11,1,1,'2025-09-01')],
    2:[row(12,2,2,'2026-09-01')],
    3:[row(13,3,2,'2026-12-01')],
  };
  const catalog = {homNay:'2026-11-11', khoi:[{maKhoi:1,soKhoi:10},{maKhoi:2,soKhoi:11}], namHoc:[
    {maNamHoc:1,ten:'2025-2026',batDau:'2025-09-01',ketThuc:'2026-05-31'},
    {maNamHoc:2,ten:'2026-2027',batDau:'2026-09-01',ketThuc:'2027-05-31'}
  ]};
  const writes = [], reads = [];
  w.request = async path => {
    if(path === '/student') return structuredClone(students);
    const match = path.match(/^\/student\/(\d+)\/enrollments$/);
    if(match) return structuredClone(histories[match[1]] || []);
    throw Error('Unexpected endpoint: '+path);
  };
  w.authorizedFetch = async (path, options={}) => {
    reads.push(path);
    let data;
    if(path === '/api/v1/classes/catalog') data = catalog;
    else if(path.startsWith('/api/v1/classes?')) data = {danhSach:classes,trang:0,kichThuoc:100,tongSoPhanTu:4,tongSoTrang:1};
    else if(path.startsWith('/api/v1/classes/2/students?')) data = {danhSach:[histories[2][0]],trang:0,kichThuoc:10,tongSoPhanTu:1,tongSoTrang:1};
    else if(path === '/api/v1/enrollments/12') data = histories[2][0];
    else if(path === '/api/v1/enrollments/transfers' || path === '/api/v1/enrollments') {
      const body=JSON.parse(options.body); writes.push({path,body});
      data={maXepLop:99,tuNgay:body.ngayChuyen || body.tuNgay};
    } else throw Error('Unexpected endpoint: '+path);
    return {ok:true,status:200,json:async()=>structuredClone(data)};
  };
  w.currentUser = async()=>({hoTen:'Giáo vụ',vaiTro:['GIAO_VU']});
  w.logout=()=>{};
  w.mountRecords=w.configureRecords=w.renderRecords=()=>{};
  w.bootstrap={Modal:{getOrCreateInstance:()=>({show(){},hide(){}})},Toast:{getOrCreateInstance:()=>({show(){}})},Offcanvas:{getInstance:()=>null}};
  const strip = file => fs.readFileSync(file,'utf8').replace(/^import .*;\r?\n/gm,'').replace(/^export /gm,'');
  const adapter = strip('src/main/resources/static/js/enrollment-api.js');
  const script = strip('src/main/resources/static/js/school.js').replace(/icons\(\);\s*route\(\);\s*init\(\);\s*$/, '');
  w.eval(adapter+'\n'+script+'\nwindow.app={init,openEnrollment,loadRequests,loadStudents,loadHistory,loadRoster,detail,updateTransferSource,state};');
  const app=w.app, el=id=>w.document.getElementById(id);
  await app.init();
  assert.ok(el('globalError').classList.contains('d-none'),el('globalError').textContent);
  assert.equal(el('requestRows').querySelectorAll('tr').length,3);
  assert.equal([...el('enrollmentYear').options].some(o=>o.value==='1'),false);
  el('enrollmentListYear').value='2';
  await app.loadRequests(0);
  assert.equal(el('requestRows').querySelectorAll('tr').length,2);
  el('requestKeyword').value='HS2';
  await app.loadRequests(0);
  assert.match(el('requestRows').textContent,/HS2/);
  assert.doesNotMatch(el('requestRows').textContent,/HS3/);

  await app.openEnrollment('enroll');
  // Old-year open records do not block; current-year future records do block.
  assert.deepEqual([...el('studentInput').options].map(o=>o.value),['','1']);
  await app.loadStudents('historyStudent','', {maHocSinh:2,hoTen:'Học sinh 2',maSoHocSinh:'HS2'});
  await app.loadHistory();
  assert.match(el('historyTimeline').textContent,/10A1/);
  el('rosterClass').value='2';
  el('rosterDate').value='2026-11-14';
  await app.loadRoster(0);
  assert.ok(reads.includes('/api/v1/classes/2/students?ngay=2026-11-14&trang=0&kichThuoc=10'));
  assert.match(el('rosterRows').textContent,/Học sinh 2/);
  await app.detail(12);
  assert.match(el('detailBody').textContent,/10A1/);

  await app.openEnrollment('transfer');
  el('studentInput').value='2';
  el('enrollmentDateInput').value='2026-11-15';
  await app.updateTransferSource();
  assert.equal(app.state.transferSource.maXepLop,12);
  assert.deepEqual([...el('enrollmentClassInput').options].map(o=>o.value),['','3']);
  histories[2][0].denNgay='2026-11-20';
  await app.updateTransferSource();
  assert.equal(app.state.transferSource,null);
  histories[2][0].denNgay=null;
  histories[2][0].tuNgay='2026-11-15';
  await app.updateTransferSource();
  assert.equal(app.state.transferSource,null);
  histories[2][0].tuNgay='2026-09-01';
  await app.updateTransferSource();
  el('enrollmentClassInput').value='3';
  el('transferReasonInput').value='Chuyển theo nguyện vọng';
  el('enrollmentForm').dispatchEvent(new w.Event('submit',{bubbles:true,cancelable:true}));
  for(let i=0;i<50 && !writes.length;i++) await new Promise(resolve=>setTimeout(resolve,10));
  assert.equal(writes.length,1);
  assert.deepEqual(writes[0].body,{maXepLopHienTai:12,maLopDich:3,ngayChuyen:'2026-11-15',lyDo:'Chuyển theo nguyện vọng'});
  // Let refresh after successful submit complete before closing the DOM.
  await new Promise(resolve=>setTimeout(resolve,30));
  dom.window.close();
  console.log('PASS: current routes, year/search filters, unassigned students, history, transfer restrictions and POST payload');
}
main().catch(error=>{console.error(error);process.exitCode=1;});
