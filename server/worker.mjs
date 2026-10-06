import { adminPage } from './admin.mjs';
import { githubSettings, githubLatest, githubPublicLatest, githubApk } from './github.mjs';

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
    const adminToken=env.ADMIN_TOKEN||env.ADMIN_PASSWORD;
    if(url.pathname==='/api/health'&&request.method==='GET')return json({service:'tht-split-control',protocol:2,registration:false,storage:!!env.CONTROL,adminConfigured:!!adminToken&&adminToken.length>=32});
    if(url.pathname==='/'&&request.method==='GET')return adminPage();
    if(['/api/device','/api/admin/device','/api/admin/reenroll'].includes(url.pathname))return json({error:'Đã bỏ quản lý và đăng ký từng thiết bị'},410);
    if((url.pathname==='/api/config'||/^\/api\/apk\/[1-9][0-9]*$/.test(url.pathname))&&request.method==='GET'){
      if(!env.CONTROL)return json({error:'Chưa cấu hình kho cập nhật CONTROL'},503);
      return env.CONTROL.get(env.CONTROL.idFromName('control')).fetch(request);
    }
    if(url.pathname.startsWith('/api/admin/')){
      if(!adminToken||adminToken.length<32)return json({error:'Chưa cấu hình ADMIN_TOKEN hoặc ADMIN_PASSWORD ít nhất 32 ký tự'},503);
      const token=request.headers.get('Authorization')?.replace(/^Bearer /,'')||'';
      if(token.length>256||!await same(token,adminToken))return json({error:'Không được phép'},401);
      if(!env.CONTROL)return json({error:'Chưa cấu hình kho cập nhật CONTROL'},503);
      return env.CONTROL.get(env.CONTROL.idFromName('control')).fetch(request);
    }
    return json({error:'Không tìm thấy'},404);
  }
};

/** The existing namespace now stores only the shared application/update configuration. */
export class Control {
  constructor(ctx,env={}){this.storage=ctx.storage;this.env=env;this.githubPending=null;}
  async updateSettings(){return await this.storage.get('update-settings')||{source:this.env.GITHUB_REPO?'github':'manual',includePrerelease:this.env.GITHUB_INCLUDE_PRERELEASE==='true'};}
  async syncGithub(settings,force=false){
    const options=githubSettings(this.env,settings.includePrerelease);
    const cache=await this.storage.get('github-cache');
    if(this.githubPending){
      if(this.githubPending.key===options.key)return this.githubPending.promise;
      await this.githubPending.promise;return this.syncGithub(settings,force);
    }
    if(!force&&cache?.key===options.key&&Date.now()-cache.checkedAt<300000)return cache;
    const pending=(async()=>{
      let snapshot=cache?.key===options.key?cache.snapshot:null,error='';
      try{
        const found=await githubLatest(options);
        if(found){
          snapshot=found;
          const ids=await this.storage.get('approved-apk-ids')||[];
          await this.storage.put('approved-apk:'+found.assetId,found);
          const keep=[found.assetId,...ids.filter(id=>id!==found.assetId)].slice(0,5);
          for(const id of ids)if(!keep.includes(id))await this.storage.delete('approved-apk:'+id);
          await this.storage.put('approved-apk-ids',keep);
        }else error='Chưa có Release hợp lệ với APK, update.json và checksum';
      }catch(e){error=e.message;}
      const result={key:options.key,checkedAt:Date.now(),snapshot,error};
      await this.storage.put('github-cache',result);return result;
    })();
    this.githubPending={key:options.key,promise:pending};
    try{return await pending;}finally{this.githubPending=null;}
  }
  async current(request,force=false){
    const settings=await this.updateSettings();let github=null;
    if(settings.source==='github'){
      try{github=await this.syncGithub(settings,force);}catch(e){github={error:e.message,snapshot:null};}
    }
    // Re-read after network awaits: an admin lock/minimum change must never be overwritten by a refresh.
    const cfg=validateConfig(await this.storage.get('config')||defaults());
    const actualSettings=await this.updateSettings();
    if(actualSettings.source!==settings.source||actualSettings.includePrerelease!==settings.includePrerelease)return {config:cfg,settings:actualSettings,github:null};
    if(settings.source==='github'){
      const latest=githubPublicLatest(github?.snapshot,new URL(request.url).origin);
      if(latest&&latest.versionCode>=cfg.minimumVersionCode)cfg.latest=latest;
      else if(cfg.latest?.apkUrl?.includes('/api/apk/')){
        const id=Number(new URL(cfg.latest.apkUrl).pathname.split('/').pop());
        const approved=await this.storage.get('approved-apk:'+id);
        let valid=false;try{valid=approved?.key===githubSettings(this.env,settings.includePrerelease).key;}catch{}
        if(!valid&&cfg.minimumVersionCode===0)cfg.latest=null;
      }
    }
    return {config:cfg,settings,github};
  }
  async fetch(request){
    try {
      const path=new URL(request.url).pathname;
      if(path==='/api/config'&&request.method==='GET')return json((await this.current(request)).config);
      if(path==='/api/admin/state'&&request.method==='GET'){
        const data=await this.current(request);
        if(!this.env.GITHUB_REPO&&data.settings.source==='manual')return json({config:data.config});
        return json({config:data.config,updates:{source:data.settings.source,includePrerelease:data.settings.includePrerelease,
          repository:this.env.GITHUB_REPO||'',checkedAt:data.github?.checkedAt||0,error:data.github?.error||'',
          tag:data.github?.snapshot?.tag||''}});
      }
      if(path==='/api/admin/github/refresh'&&request.method==='POST'){
        const data=await this.current(request,true);return json({config:data.config,error:data.github?.error||'',checkedAt:data.github?.checkedAt||0});
      }
      if(/^\/api\/apk\/[1-9][0-9]*$/.test(path)&&request.method==='GET'){
        const id=Number(path.split('/').pop()),settings=await this.updateSettings();
        if(!Number.isSafeInteger(id)||settings.source!=='github')return json({error:'Không tìm thấy APK'},404);
        const options=githubSettings(this.env,settings.includePrerelease),approved=await this.storage.get('approved-apk:'+id);
        if(!approved||approved.key!==options.key)return json({error:'APK chưa được xác minh cho cập nhật'},404);
        try{return await githubApk(options,approved);}catch(e){return json({error:e.message},502);}
      }
      if(path==='/api/admin/config'&&request.method==='POST'){
        const input=await body(request),settings=await this.updateSettings();
        if(input.updateSource!==undefined){if(!['manual','github'].includes(input.updateSource))throw Error('Nguồn cập nhật không hợp lệ');settings.source=input.updateSource;}
        if(input.includePrerelease!==undefined){if(typeof input.includePrerelease!=='boolean')throw Error('Tùy chọn bản thử không hợp lệ');settings.includePrerelease=input.includePrerelease;}
        if(settings.source==='github'){
          let data=null;try{data=await this.syncGithub(settings);}catch{}
          input.latest=githubPublicLatest(data?.snapshot,new URL(request.url).origin);
          if(!input.latest){const old=validateConfig(await this.storage.get('config')||defaults());input.latest=old.latest;}
        }
        const cfg=validateConfig(input);await this.storage.put('config',cfg);
        if(input.updateSource!==undefined||input.includePrerelease!==undefined)await this.storage.put('update-settings',settings);
        return json({ok:true});
      }
      return json({error:'Không tìm thấy'},404);
    }catch(e){return json({error:e instanceof SyntaxError?'JSON không hợp lệ':e.message},400);}
  }
}
