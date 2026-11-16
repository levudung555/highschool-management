// Run: node src/test/js/teaching-ui.cjs (mock API; no database writes)
const fs = require('node:fs');
const assert = require('node:assert/strict');
const { JSDOM } = require('../../../target/ui-tools/node_modules/jsdom');
async function main() {
  const dom = new JSDOM(fs.readFileSync('src/main/resources/static/index.html', 'utf8'), {url:'http://localhost/#teaching', runScripts:'outside-only'});
  const w = dom.window, el = id => w.document.getElementById(id);
  const tick = () => new Promise(r => setTimeout(r, 10));
  w.bootstrap = {Modal:{getOrCreateInstance:()=>({show(){},hide(){}})}};
  const option = {maLopMonHoc:5,maLop:2,maNamHoc:2,namHoc:'2026-2027',tenLop:'10A1',tenMonHoc:'Toán',soHocKy:1,batDau:'2026-09-01',ketThuc:'2027-01-15',trangThaiLop:'HOAT_DONG'};
  const row = {maPhanCong:10,maLopMonHoc:5,tenLop:'10A1',tenMonHoc:'Toán',soHocKy:1,namHoc:'2026-2027',tenGiaoVien:'Cô Lan <script>',tuNgay:'2026-09-01',denNgay:null,trangThaiDuyet:'DA_DUYET',tenNguoiDuyet:'Giáo vụ'};
  const calls=[]; let fail=false, failOptions=false, slowResolve;
  w.request=async(path,opts={})=>{
    calls.push({path,...opts});
    if(path.endsWith('/options')) { if(failOptions) throw Error('Lỗi tải lựa chọn'); return [option,{...option,maLopMonHoc:6,tenMonHoc:'Văn'},{...option,maLopMonHoc:7,maLop:3,tenLop:'Lớp ngừng',trangThaiLop:'NGUNG_HOAT_DONG'}]; }
    if(path==='/teacher') return [{maGV:1,maSoGV:'GV1',hoTen:'Cô Lan',trangThai:'DANG_CONG_TAC'},{maGV:2,hoTen:'Đã nghỉ',trangThai:'NGUNG_CONG_TAC'}];
    if(opts.method==='POST') {if(fail) throw Error('Phân công trùng thời gian');return {};}
    if(path.endsWith('/10')) return row;
    if(path.endsWith('?maLopMonHoc=5')) return [row];
    if(path.endsWith('?maLopMonHoc=6')) return new Promise(resolve=>{slowResolve=resolve;});
    return [];
  };
  w.escapeHtml=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  w.formatDate=v=>v||'—';
  w.eval(fs.readFileSync('src/main/resources/static/js/teaching.js','utf8').replace(/^import .*;\r?\n/gm,'').replace(/^export /gm,'')+'\nwindow.app={mountTeaching,configureTeaching,loadTeaching,loadHistory,openTeaching};');
  const app=w.app; app.mountTeaching();
  app.configureTeaching({vaiTro:['GIAO_VU']},{homNay:'2026-11-16'});
  await app.loadTeaching();
  assert.equal(el('ta-year').value,'2');
  assert.equal(el('ta-create').disabled,true);
  el('ta-class').value='2'; el('ta-class').dispatchEvent(new w.Event('change'));
  assert.equal(el('ta-subject').options.length,3);
  el('ta-subject').value='5'; await app.loadHistory();
  assert.match(el('ta-rows').textContent,/Cô Lan/);
  assert.equal(el('ta-rows').querySelector('script'),null);
  assert.match(el('ta-rows').textContent,/Đang hiệu lực/);
  el('ta-rows').querySelector('button').click(); await tick();
  assert.match(el('ta-detail-body').textContent,/Toán/);
  await app.openTeaching();
  assert.deepEqual([...el('ta-teacher').options].map(o=>o.value),['','1']);
  assert.equal(el('ta-start').min,'2026-09-01');
  assert.equal(el('ta-start').max,'2027-01-15');
  el('ta-teacher').value='1'; el('ta-end').value=el('ta-start').value;
  assert.equal(el('ta-form').checkValidity(),false);
  el('ta-end').value='2027-02-01'; assert.equal(el('ta-form').checkValidity(),false);
  el('ta-end').value='';
  const submit=()=>el('ta-form').dispatchEvent(new w.Event('submit',{bubbles:true,cancelable:true}));
  submit();submit();await tick();
  const writes=calls.filter(c=>c.method==='POST');assert.equal(writes.length,1);
  assert.deepEqual(JSON.parse(writes[0].body),{maLopMonHoc:5,maGiaoVien:1,tuNgay:'2026-11-16',denNgay:null});
  assert.equal(el('ta-success').hidden,false);
  fail=true;await app.openTeaching();el('ta-teacher').value='1';submit();await tick();
  assert.match(el('ta-form-error').textContent,/trùng thời gian/);assert.equal(el('ta-save').disabled,false);
  el('ta-subject').value='6';const slow=app.loadHistory();
  el('ta-subject').value='5';await app.loadHistory();slowResolve([]);await slow;
  assert.match(el('ta-rows').textContent,/Cô Lan/);
  el('ta-class').value='3';el('ta-class').dispatchEvent(new w.Event('change'));
  el('ta-subject').value='7';await app.loadHistory();assert.equal(el('ta-create').disabled,true);
  failOptions=true;await app.loadTeaching();assert.match(el('ta-error').textContent,/Lỗi tải/);assert.equal(el('ta-create').disabled,true);
  app.configureTeaching({vaiTro:['GIAO_VIEN']},{homNay:'2026-11-16'});
  const before=calls.length;await app.loadTeaching();assert.equal(calls.length,before);
  assert.match(el('ta-error').textContent,/không có quyền/);
  dom.window.close();
  console.log('PASS: teaching filters, history, detail, dates, POST, double submit, API errors, stale responses and permissions');
}
main().catch(err=>{console.error(err);process.exitCode=1;});
