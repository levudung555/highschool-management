// Access token chỉ lưu trong sessionStorage của tab; không dùng cookie đăng nhập.
const TOKEN='school.accessToken';
export function clearSession(){sessionStorage.removeItem(TOKEN);}
export function accessToken(){return sessionStorage.getItem(TOKEN);}
export async function authorizedFetch(url, options={}) {
 const token=accessToken();
 const response=await fetch(url,{...options,cache:'no-store',headers:{...options.headers,...(token?{Authorization:'Bearer '+token}:{})}});
 if(response.status===401){clearSession();if(!location.pathname.endsWith('/login.html')){sessionStorage.setItem('school.returnHash',location.hash);location.replace('/login.html');}}
 return response;
}
export async function currentUser(){
 if(!accessToken()){sessionStorage.setItem('school.returnHash',location.hash);location.replace('/login.html');return null;}
 const response=await authorizedFetch('/api/v1/auth/me');
 if(!response.ok){if(response.status!==401)throw new Error('Không tải được tài khoản đăng nhập.');return null;}
 return response.json();
}
export async function login(tenDangNhap,matKhau){
 const response=await fetch('/api/v1/auth/login',{method:'POST',cache:'no-store',headers:{'Content-Type':'application/json'},body:JSON.stringify({tenDangNhap,matKhau})});
 const data=await response.json().catch(()=>({}));
 if(!response.ok)throw new Error(data.message||'Đăng nhập chưa thành công. Vui lòng thử lại.');
 sessionStorage.setItem(TOKEN,data.accessToken);return data;
}
export function enterApp(){const hash=sessionStorage.getItem('school.returnHash');sessionStorage.removeItem('school.returnHash');location.replace('/'+(['#classes','#roster','#requests','#history'].includes(hash)?hash:'#classes'));}
export async function logout(){
 try{await authorizedFetch('/api/v1/auth/logout',{method:'POST'});}finally{clearSession();sessionStorage.removeItem('school.returnHash');location.replace('/login.html');}
}
