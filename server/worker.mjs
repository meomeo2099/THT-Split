import { adminPage } from './admin.mjs';

const headers = {'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store','X-Content-Type-Options':'nosniff'};
export const json = (value, status=200) => new Response(JSON.stringify(value), {status,headers});
const text = (v, max=300) => typeof v==='string' ? v.trim().slice(0,max) : '';
const idOK = id => typeof id==='string' && /^(?:[a-f0-9]{64}|[a-f0-9-]{36})$/.test(id);
const https = s => {try {const u=new URL(s);return u.protocol==='https:'&&!u.username&&!u.password;}catch{return false;}};
export async function hash(value) { return [...new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(value)))].map(b=>b.toString(16).padStart(2,'0')).join(''); }
async function same(a,b){const x=await hash(a),y=await hash(b);let diff=0;for(let i=0;i<x.length;i++)diff|=x.charCodeAt(i)^y.charCodeAt(i);return diff===0;}
async function body(request){const raw=await request.text();if(raw.length>16384)throw new Error('Nội dung vượt giới hạn');return JSON.parse(raw);}
const defaults = () => ({minimumVersionCode:0,offlineSeconds:604800,latest:null});
export function validateConfig(input){
  const result=defaults();
  if(!Number.isInteger(input.minimumVersionCode)||input.minimumVersionCode<0)throw new Error('Phiên bản tối thiểu không hợp lệ');result.minimumVersionCode=input.minimumVersionCode;
  if(!Number.isInteger(input.offlineSeconds)||input.offlineSeconds<3600||input.offlineSeconds>2592000)throw new Error('Ngoại tuyến từ 1 giờ đến 30 ngày');result.offlineSeconds=input.offlineSeconds;
  if(input.latest){const l=input.latest;if(!Number.isInteger(l.versionCode)||l.versionCode<=0||!https(l.apkUrl)||!/^[a-fA-F0-9]{64}$/.test(l.sha256))throw new Error('APK phải có versionCode, HTTPS và SHA256');
    result.latest={versionCode:l.versionCode,versionName:text(l.versionName,80),apkUrl:l.apkUrl,sha256:l.sha256.toLowerCase(),notes:text(l.notes,4000)};}
  if(result.minimumVersionCode>(result.latest?.versionCode||0))throw new Error('Phải có APK đạt phiên bản tối thiểu trước khi bắt buộc cập nhật');
  return result;
}

export default {
  async fetch(request,env){
    const url=new URL(request.url);
    if(url.pathname==='/'&&request.method==='GET')return adminPage();
    if(url.pathname==='/api/device'&&request.method==='POST')return env.CONTROL.get(env.CONTROL.idFromName('control')).fetch(request);
    if(url.pathname.startsWith('/api/admin/')){
      if(!env.ADMIN_TOKEN||env.ADMIN_TOKEN.length<32)return json({error:'Chưa cấu hình ADMIN_TOKEN'},503);
      const token=request.headers.get('Authorization')?.replace(/^Bearer /,'')||'';
      if(token.length>256||!await same(token,env.ADMIN_TOKEN))return json({error:'Không được phép'},401);
      return env.CONTROL.get(env.CONTROL.idFromName('control')).fetch(request);
    }
    return json({error:'Không tìm thấy'},404);
  }
};

/** One strongly-consistent Durable Object; transaction preserves an admin lock during enrollment. */
export class Control {
  constructor(ctx){this.storage=ctx.storage;}
  async fetch(request){
    try {
      const path=new URL(request.url).pathname;
      if(path==='/api/device'&&request.method==='POST'){
        const b=await body(request);if(!idOK(b.deviceId)||!/^[a-f0-9]{64}$/.test(b.credential||'')||!Number.isInteger(b.versionCode)||b.versionCode<1)return json({error:'Sai thiết bị'},400);
        const credentialHash=await hash(b.credential);
        return await this.storage.transaction(async tx=>{
          const cfg=await tx.get('config')||defaults(),key='device:'+b.deviceId;let d=await tx.get(key);
          if(d&&d.credentialHash&&d.credentialHash!==credentialHash)return json({error:'Thông tin xác thực thay đổi; liên hệ nhà phát hành để khôi phục thiết bị'},403);
          if(!d)d={id:b.deviceId,credentialHash,state:'enabled',reason:'',note:'',createdAt:Date.now()};
          if(d.state==='pending')d.state='enabled'; // Migrate old approval records without changing a lock.
          d.credentialHash=credentialHash;d.lastSeen=Date.now();d.model=text(b.model,120);d.sdk=Number.isInteger(b.sdk)?b.sdk:0;d.versionCode=b.versionCode;d.versionName=text(b.versionName,80);
          await tx.put(key,d);
          return json({deviceId:d.id,state:d.state,reason:d.reason,minimumVersionCode:cfg.minimumVersionCode,offlineSeconds:cfg.offlineSeconds,latest:cfg.latest});
        });
      }
      if(path==='/api/admin/state'&&request.method==='GET'){
        const cursor=new URL(request.url).searchParams.get('cursor')||'';if(cursor&&!idOK(cursor))return json({error:'Cursor không hợp lệ'},400);
        const records=await this.storage.list({prefix:'device:',limit:200,...(cursor?{startAfter:'device:'+cursor}:{})});
        const devices=[...records.values()].map(({credentialHash,...d})=>({...d,state:d.state==='pending'?'enabled':d.state}));
        return json({config:validateConfig(await this.storage.get('config')||defaults()),devices,nextCursor:devices.length===200?devices[199].id:null});
      }
      if(path==='/api/admin/config'&&request.method==='POST'){
        const cfg=validateConfig(await body(request));await this.storage.put('config',cfg);return json({ok:true});
      }
      if(path==='/api/admin/device'&&request.method==='POST'){
        const b=await body(request);if(!idOK(b.id)||!['enabled','locked'].includes(b.state))return json({error:'Thiết bị/trạng thái không hợp lệ'},400);
        return await this.storage.transaction(async tx=>{const key='device:'+b.id,d=await tx.get(key);if(!d)return json({error:'Không tìm thấy thiết bị'},404);
          d.state=b.state;d.reason=text(b.reason);d.note=text(b.note,500);await tx.put(key,d);return json({ok:true});});
      }
      if(path==='/api/admin/reenroll'&&request.method==='POST'){
        const b=await body(request);if(!idOK(b.id))return json({error:'Sai thiết bị'},400);
        return await this.storage.transaction(async tx=>{const key='device:'+b.id,d=await tx.get(key);if(!d)return json({error:'Không tìm thấy'},404);d.credentialHash=null;if(d.state==='pending')d.state='enabled';await tx.put(key,d);return json({ok:true});});
      }
      return json({error:'Không tìm thấy'},404);
    }catch(e){return json({error:e instanceof SyntaxError?'JSON không hợp lệ':e.message},400);}
  }
}
