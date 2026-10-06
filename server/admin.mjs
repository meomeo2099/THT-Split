export function adminPage(){
 const nonce=crypto.randomUUID();
 return new Response(`<!doctype html><html lang="vi"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Quản trị THT Split</title>
 <style>body{background:#07121f;color:#eef5fa;font:16px system-ui;max-width:1100px;margin:auto;padding:24px}h1{color:#10cbe5}section{background:#112233;padding:20px;border-radius:14px;margin:20px 0}input,textarea,select,button{font:inherit;border-radius:7px;padding:10px;box-sizing:border-box}input,textarea,select{background:#07121f;color:white;border:1px solid #658096;width:100%;margin:6px 0}button{background:#10cbe5;color:#07121f;border:0;margin:5px;cursor:pointer}label{display:block;margin:10px 0}small{color:#b4c6d4}#message{white-space:pre-wrap}</style>
 <h1>THT Split · Quản trị</h1><section id="login"><label>Mật khẩu quản trị<input id="token" type="password" autocomplete="current-password"></label><button id="signin">Đăng nhập</button><small>Mật khẩu chỉ giữ trong phiên đang mở, không lưu trên trình duyệt.</small></section>
 <p id="message" role="status"></p><main id="panel" hidden>
 <button id="refresh">Làm mới cấu hình</button><button id="logout">Đăng xuất</button>
 <section><h2>Ứng dụng</h2><label>Trạng thái chung<select id="state"><option value="enabled">Mở cho mọi người</option><option value="locked">Tạm khóa toàn ứng dụng</option></select></label><label>Thông báo<textarea id="reason" rows="2"></textarea></label><small>Ai cài cũng dùng ngay, không đăng ký thiết bị. Khóa chung có hiệu lực khi app kiểm tra mạng; máy đã nhận khóa giữ trạng thái khi offline.</small></section>
 <section><h2>Cập nhật</h2><label>Version code mới nhất<input id="version" type="number" min="0"></label><label>Tên phiên bản<input id="name"></label><label>URL APK công khai (HTTPS)<input id="apk" type="url"></label><label>SHA256 APK<input id="sha"></label><label>Nội dung cập nhật<textarea id="notes" rows="3"></textarea></label><label>Version code tối thiểu bắt buộc (0 = không bắt buộc)<input id="minimum" type="number" min="0"></label><small>Chỉ đặt bắt buộc sau khi APK cùng chữ ký đã tải và cài thử được. Không dùng link artifact cần đăng nhập.</small></section>
 <button id="save">Lưu cấu hình</button></main>
 <script nonce="${nonce}">
 let token='';const el=id=>document.getElementById(id),msg=s=>el('message').textContent=s;
 async function api(path,body){const r=await fetch('/api/admin/'+path,{method:body?'POST':'GET',headers:{'Authorization':'Bearer '+token,'Content-Type':'application/json'},...(body?{body:JSON.stringify(body)}:{})});const data=await r.json();if(!r.ok)throw Error(data.error||'Lỗi '+r.status);return data;}
 const guarded=fn=>async()=>{try{await fn();}catch(e){msg(e.message);}};
 function fill(cfg){el('state').value=cfg.state;el('reason').value=cfg.reason||'';el('minimum').value=cfg.minimumVersionCode;const l=cfg.latest||{};for(const [id,key] of [['version','versionCode'],['name','versionName'],['apk','apkUrl'],['sha','sha256'],['notes','notes']])el(id).value=l[key]||'';}
 async function load(){const data=await api('state');fill(data.config);}
 el('signin').addEventListener('click',guarded(async()=>{token=el('token').value;el('token').value='';await load();el('login').hidden=true;el('panel').hidden=false;msg('Đã đăng nhập');}));
 el('logout').addEventListener('click',()=>{token='';el('panel').hidden=true;el('login').hidden=false;msg('Đã đăng xuất');});
 el('refresh').addEventListener('click',guarded(load));
 el('save').addEventListener('click',guarded(async()=>{const versionCode=Number(el('version').value);const latest=versionCode>0?{versionCode,versionName:el('name').value,apkUrl:el('apk').value,sha256:el('sha').value,notes:el('notes').value}:null;await api('config',{state:el('state').value,reason:el('reason').value,minimumVersionCode:Number(el('minimum').value),latest});msg('Đã lưu trạng thái ứng dụng và cập nhật');}));
 </script></html>`,{headers:{'Content-Type':'text/html; charset=utf-8','Cache-Control':'no-store','X-Content-Type-Options':'nosniff','Referrer-Policy':'no-referrer','Content-Security-Policy':`default-src 'none'; script-src 'nonce-${nonce}'; style-src 'unsafe-inline'; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'`}});
}
