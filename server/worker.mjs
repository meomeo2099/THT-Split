import { adminPage } from './admin.mjs';

const headers = {'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store','X-Content-Type-Options':'nosniff'};
export const json = (value, status=200) => new Response(JSON.stringify(value), {status,headers});
const text = (v, max=300) => typeof v==='string' ? v.trim().slice(0,max) : '';
const https = s => {try {const u=new URL(s);return u.protocol==='https:'&&!u.username&&!u.password;}catch{return false;}};
export async function hash(value) { return [...new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(value)))].map(b=>b.toString(16).padStart(2,'0')).join(''); }
async function same(a,b){const x=await hash(a),y=await hash(b);let diff=0;for(let i=0;i<x.length;i++)diff|=x.charCodeAt(i)^y.charCodeAt(i);return diff===0;}
async function body(request){const raw=await request.text();if(raw.length>16384)throw new Error('Nội dung vượt giới hạn');return JSON.parse(raw);}
const defaults = () => ({state:'enabled',reason:'',minimumVersionCode:0,latest:null});
export function validateConfig(input){
  const result=defaults();
  if(input.state!==undefined&&!['enabled','locked'].includes(input.state))throw new Error('Trạng thái ứng dụng không hợp lệ');
  result.state=input.state||'enabled';result.reason=text(input.reason);
  if(!Number.isInteger(input.minimumVersionCode)||input.minimumVersionCode<0)throw new Error('Phiên bản tối thiểu không hợp lệ');result.minimumVersionCode=input.minimumVersionCode;
  if(input.latest){const l=input.latest;if(!Number.isInteger(l.versionCode)||l.versionCode<=0||!https(l.apkUrl)||!/^[a-fA-F0-9]{64}$/.test(l.sha256))throw new Error('APK phải có versionCode, HTTPS và SHA256');
    result.latest={versionCode:l.versionCode,versionName:text(l.versionName,80),apkUrl:l.apkUrl,sha256:l.sha256.toLowerCase(),notes:text(l.notes,4000)};}
  if(result.minimumVersionCode>(result.latest?.versionCode||0))throw new Error('Phải có APK đạt phiên bản tối thiểu trước khi bắt buộc cập nhật');
  return result;
}

export default {
  async fetch(request,env){
    const url=new URL(request.url);
    if(url.pathname==='/api/health'&&request.method==='GET')return json({service:'tht-split-control',protocol:2,registration:false,storage:!!env.CONTROL,adminConfigured:!!env.ADMIN_TOKEN&&env.ADMIN_TOKEN.length>=32});
    if(url.pathname==='/'&&request.method==='GET')return adminPage();
    if(['/api/device','/api/admin/device','/api/admin/reenroll'].includes(url.pathname))return json({error:'Đã bỏ quản lý và đăng ký từng thiết bị'},410);
    if(url.pathname==='/api/config'&&request.method==='GET'){
      if(!env.CONTROL)return json({error:'Chưa cấu hình kho cập nhật CONTROL'},503);
      return env.CONTROL.get(env.CONTROL.idFromName('control')).fetch(request);
    }
    if(url.pathname.startsWith('/api/admin/')){
      if(!env.ADMIN_TOKEN||env.ADMIN_TOKEN.length<32)return json({error:'Chưa cấu hình ADMIN_TOKEN'},503);
      const token=request.headers.get('Authorization')?.replace(/^Bearer /,'')||'';
      if(token.length>256||!await same(token,env.ADMIN_TOKEN))return json({error:'Không được phép'},401);
      if(!env.CONTROL)return json({error:'Chưa cấu hình kho cập nhật CONTROL'},503);
      return env.CONTROL.get(env.CONTROL.idFromName('control')).fetch(request);
    }
    return json({error:'Không tìm thấy'},404);
  }
};

/** The existing namespace now stores only the shared application/update configuration. */
export class Control {
  constructor(ctx){this.storage=ctx.storage;}
  async fetch(request){
    try {
      const path=new URL(request.url).pathname;
      if(path==='/api/config'&&request.method==='GET')return json(validateConfig(await this.storage.get('config')||defaults()));
      if(path==='/api/admin/state'&&request.method==='GET')return json({config:validateConfig(await this.storage.get('config')||defaults())});
      if(path==='/api/admin/config'&&request.method==='POST'){
        const cfg=validateConfig(await body(request));await this.storage.put('config',cfg);return json({ok:true});
      }
      return json({error:'Không tìm thấy'},404);
    }catch(e){return json({error:e instanceof SyntaxError?'JSON không hợp lệ':e.message},400);}
  }
}
