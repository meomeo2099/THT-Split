import {test} from 'node:test';
import assert from 'node:assert/strict';
import {webcrypto} from 'node:crypto';
import worker,{Control,validateConfig} from './worker.mjs';
import {adminPage} from './admin.mjs';
import {Script} from 'node:vm';
globalThis.crypto??=webcrypto;
class Storage {
  data=new Map();tail=Promise.resolve();
  async get(k){return structuredClone(this.data.get(k));}
  async put(k,v){this.data.set(k,structuredClone(v));}
  async list({prefix,limit,startAfter}){return new Map([...this.data].filter(([k])=>k.startsWith(prefix)&&(!startAfter||k>startAfter)).sort(([a],[b])=>a.localeCompare(b)).slice(0,limit));}
  async transaction(fn){const run=this.tail.then(()=>fn(this));this.tail=run.catch(()=>{});return run;}
}
const id='a'.repeat(64),credential='b'.repeat(64);
const device=(extra={})=>({deviceId:id,credential,model:'LDPlayer',sdk:28,versionCode:12,versionName:'0.3.0-test',...extra});
const request=(path,b)=>new Request('https://control.test'+path,{method:b?'POST':'GET',...(b?{body:JSON.stringify(b)}:{})});
const config=(extra={})=>({minimumVersionCode:0,offlineSeconds:604800,latest:null,...extra});
function setup(){return new Control({storage:new Storage()});}
test('fresh devices are enabled without approval and wrong credentials cannot replace identity',async()=>{
 const c=setup();const r=await c.fetch(request('/api/device',device()));assert.equal(r.status,200);assert.equal((await r.json()).state,'enabled');
 assert.equal((await c.fetch(request('/api/device',device({credential:'c'.repeat(64)})))).status,403);
});
test('admin lock survives repeated enrollment and concurrent heartbeats',async()=>{
 const c=setup();await c.fetch(request('/api/device',device()));
 await Promise.all([c.fetch(request('/api/device',device())),c.fetch(request('/api/admin/device',{id,state:'locked',reason:'Khóa kiểm thử'})),c.fetch(request('/api/device',device()))]);
 const r=await c.fetch(request('/api/device',device()));assert.equal((await r.json()).state,'locked');
});
test('reenroll preserves locked and enabled states without another approval',async()=>{
 const c=setup();await c.fetch(request('/api/device',device()));await c.fetch(request('/api/admin/device',{id,state:'locked'}));await c.fetch(request('/api/admin/reenroll',{id}));
 const r=await c.fetch(request('/api/device',device({credential:'c'.repeat(64)})));assert.equal((await r.json()).state,'locked');
 await c.fetch(request('/api/admin/device',{id,state:'enabled'}));await c.fetch(request('/api/admin/reenroll',{id}));assert.equal((await (await c.fetch(request('/api/device',device()))).json()).state,'enabled');
});
test('mandatory policy requires downloadable metadata and rejects non-HTTPS/checksum errors',()=>{
 assert.throws(()=>validateConfig(config({minimumVersionCode:13})));assert.throws(()=>validateConfig(config({latest:{versionCode:13,apkUrl:'http://apk.test/app.apk',sha256:'d'.repeat(64)}})));
 assert.throws(()=>validateConfig(config({latest:{versionCode:13,apkUrl:'https://apk.test/app.apk',sha256:'bad'}})));
 const ok=validateConfig(config({minimumVersionCode:13,latest:{versionCode:13,versionName:'0.3.1',apkUrl:'https://apk.test/app.apk',sha256:'d'.repeat(64)}}));assert.equal(ok.minimumVersionCode,13);
});
test('admin API requires server-only token; admin device list never exposes credentials',async()=>{
 const c=setup();let forwarded=0;const env={ADMIN_TOKEN:'secret-admin-password-at-least-32-characters',CONTROL:{idFromName:()=>'',get:()=>({fetch:r=>{forwarded++;return c.fetch(r);}})}};
 assert.equal((await worker.fetch(request('/api/admin/state'),env)).status,401);assert.equal(forwarded,0);
 await c.fetch(request('/api/device',device()));const req=request('/api/admin/state');req.headers.set('Authorization','Bearer '+env.ADMIN_TOKEN);const result=await worker.fetch(req,env);assert.equal(result.status,200);assert.equal((await result.json()).devices[0].credentialHash,undefined);
});
test('legacy autoApprove=false and pending records migrate without touching a lock',async()=>{
 const c=setup();await c.storage.put('config',{...config(),autoApprove:false});
 assert.equal((await (await c.fetch(request('/api/device',device()))).json()).state,'enabled');
 const old=await c.storage.get('device:'+id);old.state='pending';await c.storage.put('device:'+id,old);
 assert.equal((await (await c.fetch(request('/api/device',device()))).json()).state,'enabled');
 assert.equal((await c.fetch(request('/api/admin/device',{id,state:'pending'}))).status,400);
});
test('admin screen has lock/unlock without approval controls and valid emitted script',async()=>{
 const html=await adminPage().text();assert.ok(html.includes('Mở khóa'));assert.ok(html.includes('Khóa'));
 assert.ok(!html.includes('Tự duyệt'));assert.ok(!html.includes('Duyệt / Mở'));assert.ok(!html.includes('Khôi phục cài lại'));
 const script=html.match(/<script nonce="[^"]+">([\s\S]*?)<\/script>/)[1];assert.doesNotThrow(()=>new Script(script));
});
