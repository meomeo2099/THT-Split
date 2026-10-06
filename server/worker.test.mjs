import {test} from 'node:test';
import assert from 'node:assert/strict';
import {webcrypto} from 'node:crypto';
import {Script} from 'node:vm';
import worker,{Control,validateConfig} from './worker.mjs';
import {adminPage} from './admin.mjs';
globalThis.crypto??=webcrypto;
class Storage {
 data=new Map();writes=0;
 async get(k){return structuredClone(this.data.get(k));}
 async put(k,v){this.writes++;this.data.set(k,structuredClone(v));}
}
const request=(path,b)=>new Request('https://control.test'+path,{method:b?'POST':'GET',...(b?{body:JSON.stringify(b)}:{})});
const config=(extra={})=>({state:'enabled',reason:'',minimumVersionCode:0,latest:null,...extra});
function setup(){const c=new Control({storage:new Storage()});return {c,env:{CONTROL:{idFromName:()=>'',get:()=>c},ADMIN_TOKEN:'secret-admin-password-at-least-32-characters'}};}
const authorized=(path,b,env)=>{const r=request(path,b);r.headers.set('Authorization','Bearer '+env.ADMIN_TOKEN);return r;};
test('fresh install reads public configuration without authentication, enrollment or storage writes',async()=>{
 const {c,env}=setup();delete env.ADMIN_TOKEN;
 for(let n=0;n<3;n++){const r=await worker.fetch(request('/api/config'),env);assert.equal(r.status,200);assert.deepEqual(await r.json(),config());}
 assert.equal(c.storage.writes,0);assert.equal(c.storage.data.size,0);
});
test('all enrollment and per-device admin routes are retired without modifying records',async()=>{
 const {c,env}=setup();
 for(const path of ['/api/device','/api/admin/device','/api/admin/reenroll'])assert.equal((await worker.fetch(request(path,{deviceId:'a'.repeat(64),credential:'b'.repeat(64)}),env)).status,410);
 assert.equal(c.storage.writes,0);
});
test('admin token protects writes and public reads never overwrite a global lock',async()=>{
 const {c,env}=setup();
 assert.equal((await worker.fetch(request('/api/admin/config',config({state:'locked'})),env)).status,401);
 assert.equal(c.storage.writes,0);
 assert.equal((await worker.fetch(authorized('/api/admin/config',config({state:'locked',reason:'Bảo trì'}),env),env)).status,200);
 for(let n=0;n<5;n++)assert.equal((await (await worker.fetch(request('/api/config'),env)).json()).state,'locked');
 assert.equal(c.storage.writes,1);
 await worker.fetch(authorized('/api/admin/config',config(),env),env);
 assert.equal((await (await worker.fetch(request('/api/config'),env)).json()).state,'enabled');
});
test('mandatory update metadata is validated and public clients can read it anonymously',async()=>{
 const {env}=setup();const latest={versionCode:19,versionName:'0.3.7',apkUrl:'https://apk.test/app.apk',sha256:'d'.repeat(64),notes:'Mới'};
 assert.throws(()=>validateConfig(config({minimumVersionCode:19})));
 assert.throws(()=>validateConfig(config({latest:{...latest,apkUrl:'http://apk.test/app.apk'}})));
 assert.throws(()=>validateConfig(config({latest:{...latest,sha256:'bad'}})));
 assert.throws(()=>validateConfig(config({state:'pending'})));
 const cfg=config({minimumVersionCode:19,latest});
 assert.equal((await worker.fetch(authorized('/api/admin/config',cfg,env),env)).status,200);
 assert.deepEqual(await (await worker.fetch(request('/api/config'),env)).json(),cfg);
});
test('legacy enrollment records and lease fields do not enter public policy or admin output',async()=>{
 const {c,env}=setup();await c.storage.put('device:old',{id:'old',state:'locked',credentialHash:'secret'});
 await c.storage.put('config',{minimumVersionCode:0,latest:null,offlineSeconds:3600,autoApprove:false});
 const r=await worker.fetch(request('/api/config'),env);assert.deepEqual(await r.json(),config());
 const admin=await worker.fetch(authorized('/api/admin/state',null,env),env);assert.deepEqual(await admin.json(),{config:config()});
 assert.equal(c.storage.data.get('device:old').state,'locked'); // Retired data is neither deleted nor used.
});
test('health exposes protocol2/no registration and missing storage returns update error only',async()=>{
 const r=await worker.fetch(request('/api/health'),{});assert.deepEqual(await r.json(),{service:'tht-split-control',protocol:2,registration:false,storage:false,adminConfigured:false});
 assert.equal((await worker.fetch(request('/api/config'),{})).status,503);
});
test('admin password accepts 8..256 characters, keeps old passwords and rejects wrong credentials',async()=>{
 for(const [length,valid] of [[7,false],[8,true],[32,true],[256,true],[257,false]]){
   const {env}=setup();env.ADMIN_TOKEN='x'.repeat(length);
   const health=await (await worker.fetch(request('/api/health'),env)).json();assert.equal(health.adminConfigured,valid);
   const response=await worker.fetch(authorized('/api/admin/state',null,env),env);assert.equal(response.status,valid?200:503);
   if(valid){const wrong=request('/api/admin/state');wrong.headers.set('Authorization','Bearer '+'y'.repeat(length));assert.equal((await worker.fetch(wrong,env)).status,401);}
 }
});
test('short ADMIN_PASSWORD login preserves stored lock, mandatory, source, cache and legacy records',async()=>{
 const {c,env}=setup();env.ADMIN_PASSWORD='test1234';delete env.ADMIN_TOKEN;
 await c.storage.put('config',config({state:'locked',reason:'Giữ trạng thái',minimumVersionCode:21,
   latest:{versionCode:21,versionName:'0.3.9-test',apkUrl:'https://download.test/app.apk',sha256:'a'.repeat(64),notes:''}}));
 await c.storage.put('update-settings',{source:'manual',includePrerelease:true});
 await c.storage.put('github-cache',{checkedAt:123,snapshot:{assetId:11}});
 await c.storage.put('device:old',{state:'locked',note:'Giữ hồ sơ cũ'});
 const before=structuredClone([...c.storage.data]),writes=c.storage.writes;
 const login=request('/api/admin/state');login.headers.set('Authorization','Bearer '+env.ADMIN_PASSWORD);
 assert.equal((await worker.fetch(login,env)).status,200);
 assert.equal((await (await worker.fetch(request('/api/config'),env)).json()).state,'locked');
 assert.deepEqual([...c.storage.data],before);assert.equal(c.storage.writes,writes);
});
test('admin screen has shared lock/update controls with no device list or offline lease',async()=>{
 const html=await adminPage().text();assert.ok(html.includes('Mở cho mọi người'));assert.ok(html.includes('Tạm khóa toàn ứng dụng'));
 assert.ok(!html.includes('id="devices"'));assert.ok(!html.includes('id="offline"'));assert.ok(!html.includes('reenroll'));
 const script=html.match(/<script nonce="[^"]+">([\s\S]*?)<\/script>/)[1];assert.doesNotThrow(()=>new Script(script));
});
test('single-module Worker bundle serves the same anonymous public policy',async()=>{
 await import('./pack.mjs');const {default:bundle,Control:BundledControl}=await import('./build/distribution/worker.mjs');
 const c=new BundledControl({storage:new Storage()});const env={CONTROL:{idFromName:()=>'',get:()=>c}};
 assert.deepEqual(await (await bundle.fetch(request('/api/config'),env)).json(),config());assert.equal(c.storage.writes,0);
});
