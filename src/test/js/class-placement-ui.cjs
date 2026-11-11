// DOM test of the real school.js against the isolated H2 application on port 18080.
// Install the temporary test dependency: npm install --prefix target/ui-tools jsdom
const fs = require('node:fs');
const assert = require('node:assert/strict');
const { JSDOM } = require('../../../target/ui-tools/node_modules/jsdom');
const base = 'http://127.0.0.1:18080';

async function main() {
  const login = await fetch(base + '/api/v1/auth/login', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({tenDangNhap:'giaovu', matKhau:'Test@12345'})
  });
  assert.equal(login.status, 200);
  const token = (await login.json()).accessToken;
  const authorizedFetch = (path, options = {}) => fetch(base + path, {
    ...options, headers: {...options.headers, Authorization:'Bearer ' + token}
  });
  const user = await (await authorizedFetch('/api/v1/auth/me')).json();
  const dom = new JSDOM(fs.readFileSync('src/main/resources/static/index.html','utf8'), {
    url: base + '/#requests', runScripts:'outside-only'
  });
  const w = dom.window;
  w.authorizedFetch = authorizedFetch;
  w.request = async path => {
    const response = await authorizedFetch(path);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || `HTTP ${response.status}`);
    return data;
  };
  w.currentUser = async () => user;
  w.logout = () => {};
  w.mountRecords = w.configureRecords = w.renderRecords = () => {};
  const component = {show(){}, hide(){}};
  w.bootstrap = {
    Modal: {getOrCreateInstance:()=>component},
    Toast: {getOrCreateInstance:()=>component},
    Offcanvas: {getInstance:()=>null}
  };
  let script = fs.readFileSync('src/main/resources/static/js/school.js','utf8')
    .replace(/^import .*;\r?\n/gm,'')
    .replace(/icons\(\);\s*route\(\);\s*init\(\);\s*$/, '');
  const adapter = fs.readFileSync('src/main/resources/static/js/enrollment-api.js','utf8')
    .replace(/^import .*;\r?\n/gm,'').replace(/^export /gm,'');
  w.eval(adapter + '\n' + script + '\nwindow.testApp = {init, openEnrollment, loadHistory, state};');
  const app = w.testApp;
  const el = id => w.document.getElementById(id);
  const waitFor = async predicate => {
    for(let i=0;i<150;i++) {
      if(predicate()) return;
      await new Promise(resolve=>setTimeout(resolve,40));
    }
    throw new Error('Timed out: ' + el('globalError').textContent + ' / ' + w.document.querySelector('#enrollmentForm .form-error').textContent);
  };
  const submit = () => el('enrollmentForm').dispatchEvent(new w.Event('submit',{bubbles:true,cancelable:true}));
  await app.init();
  assert.ok(app.state.catalog, el('globalError').textContent);
  assert.equal(w.document.querySelectorAll('[data-action="approve"],[data-action="reject"],[data-request-status]').length,0);
  await app.openEnrollment('enroll');
  el('studentInput').value='1';
  el('enrollmentClassInput').value='1';
  const today=el('enrollmentDateInput').value;
  submit();
  await waitFor(()=>el('toastMessage').textContent.includes('Đã xếp lớp'));
  await waitFor(()=>app.state.requestRows.length===1);
  const student=app.state.students.get(1);
  await app.openEnrollment('enroll');
  assert.equal([...el('studentInput').options].some(o=>o.value==='1'),false);
  await app.openEnrollment('transfer',student);
  assert.ok(el('currentClassLabel').textContent.includes('10A1'));
  assert.ok(app.state.transferSource);
  assert.equal([...el('enrollmentClassInput').options].some(o=>o.value==='5'),false);
  el('enrollmentClassInput').value='2';
  el('transferReasonInput').value='UI transfer test';
  submit();
  await waitFor(()=>el('toastMessage').textContent.includes('Đã chuyển lớp'));
  const roster=await (await authorizedFetch('/api/v1/classes/1/students?ngay='+today)).json();
  assert.equal(roster.tongSoPhanTu,1); // Future transfer must not remove today's student.
  el('historyStudent').innerHTML='<option value="1">HS1</option>';
  await app.loadHistory();
  assert.equal(el('historyTimeline').querySelectorAll('article').length,2);
  assert.ok(el('historyTimeline').textContent.includes('UI transfer test'));
  assert.ok(el('historyTimeline').textContent.includes('Người thực hiện'));
  console.log('PASS UI: first placement, unassigned filter, source class, target filter, transfer, history, no approval controls');
  w.close();
}
main().catch(error=>{console.error(error);process.exitCode=1;});
