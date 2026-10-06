import {test} from 'node:test';
import assert from 'node:assert/strict';
import {webcrypto, createHash} from 'node:crypto';
import worker, {Control} from './worker.mjs';
import {githubLatest, githubSettings, normalizeCertSha256} from './github.mjs';
globalThis.crypto ??= webcrypto;
const cert='e7874ffe498bfc9badbb0880194efdb5b1f14093c6a9e37ee536027378c3c5d2';
const fakeToken='test-token-never-forwarded-to-client-or-cdn';
const apkBytes=new TextEncoder().encode('apk-data');
const sha=createHash('sha256').update(apkBytes).digest('hex');
const cfg=(extra={})=>({state:'enabled',reason:'',minimumVersionCode:0,latest:null,...extra});
class Storage{
 data=new Map();
 async get(k){return structuredClone(this.data.get(k));}
 async put(k,v){this.data.set(k,structuredClone(v));}
 async delete(k){this.data.delete(k);}
}
function setup(){
 const env={GITHUB_REPO:'meomeo2099/THT-Split',GITHUB_TOKEN:fakeToken,RELEASE_CERT_SHA256:cert,
   GITHUB_INCLUDE_PRERELEASE:'true',ADMIN_TOKEN:'admin-secret-with-at-least-32-characters'};
 const storage=new Storage(),control=new Control({storage},env);
 env.CONTROL={idFromName:()=>'',get:()=>control};return {env,storage,control};
}
function req(path,body,env){const r=new Request('https://split.test'+path,{method:body?'POST':'GET',...(body?{body:JSON.stringify(body)}:{})});if(env)r.headers.set('Authorization','Bearer '+(env.ADMIN_TOKEN||env.ADMIN_PASSWORD));return r;}
function fixtures(){
 const manifest={schema:1,packageName:'com.tht.split',versionCode:21,versionName:'0.3.9-test',apkAsset:'THT-Split.apk',
   sizeBytes:apkBytes.length,sha256:sha,certificateSha256:cert,notes:'Bản mới'};
 const release={id:1,draft:false,prerelease:true,published_at:'2026-10-06T07:00:00Z',tag_name:'v0.3.9-test',assets:[
   {id:11,name:'THT-Split.apk',state:'uploaded',size:apkBytes.length,digest:'sha256:'+sha},
   {id:12,name:'update.json',state:'uploaded'}, {id:13,name:'THT-Split.apk.sha256',state:'uploaded'}]};
 return {manifest,release};
}
function mockGithub(t,fixture=fixtures(),options={}){
 const calls=[];
 t.mock.method(globalThis,'fetch',async(url,init)=>{
   calls.push({url:String(url),headers:new Headers(init.headers)});
   if(options.before)await options.before(String(url));
   if(options.status)return new Response('Do not expose token '+fakeToken,{status:options.status});
   const u=new URL(url);
   if(u.hostname==='api.github.com'&&u.pathname.endsWith('/releases'))return Response.json(options.releases||[fixture.release]);
   if(u.hostname==='api.github.com'&&u.pathname.endsWith('/assets/12'))return Response.json(fixture.manifest);
   if(u.hostname==='api.github.com'&&u.pathname.endsWith('/assets/13'))return new Response(options.checksum||sha+'  THT-Split.apk\n');
   if(u.hostname==='api.github.com'&&u.pathname.endsWith('/assets/11'))return new Response(null,{status:302,headers:{Location:options.redirect||'https://release-assets.githubusercontent.com/test/file.apk?temporary=1'}});
   if(u.hostname==='release-assets.githubusercontent.com')return new Response(options.bytes||apkBytes,{headers:{...(options.length===false?{}:{'Content-Length':String(options.length||apkBytes.length)}),'Set-Cookie':fakeToken}});
   throw Error('Unexpected mocked request');
 });return calls;
}
test('certificate normalization accepts uppercase colon fingerprint and distinguishes APK hash',()=>{
 assert.equal(normalizeCertSha256(cert.match(/../g).join(':').toUpperCase()),cert);
 assert.throws(()=>normalizeCertSha256('invalid'));
 assert.notEqual(cert,sha);
});
test('private release metadata becomes same-origin APK URL, token stays only on GitHub API',async t=>{
 const {env}=setup(),calls=mockGithub(t);
 const response=await worker.fetch(req('/api/config'),env);assert.equal(response.status,200);
 const config=await response.json();assert.equal(config.latest.versionCode,21);assert.equal(config.latest.apkUrl,'https://split.test/api/apk/11');assert.equal(config.latest.sha256,sha);
 const download=await worker.fetch(req('/api/apk/11'),env);
 assert.equal(download.status,200);assert.deepEqual(new Uint8Array(await download.arrayBuffer()),apkBytes);
 assert.equal(download.headers.get('Location'),null);assert.equal(download.headers.get('Set-Cookie'),null);
 assert.equal(download.headers.get('Authorization'),null);
 for(const call of calls)assert.equal(call.headers.get('Authorization'),new URL(call.url).hostname==='api.github.com'?'Bearer '+fakeToken:null);
 assert.ok(!JSON.stringify(config).includes(fakeToken));
});
test('draft and prerelease selection is explicit, legacy missing manifest is skipped',async t=>{
 const f=fixtures();mockGithub(t,f,{releases:[{...f.release,draft:true},{...f.release,assets:[]},f.release]});
 const {env}=setup();assert.equal((await githubLatest(githubSettings(env,true))).versionCode,21);
 assert.equal(await githubLatest(githubSettings(env,false)),null);
});
test('wrong certificate, package, version tag and oversize APK are not approved',async t=>{
 for(const mutate of [f=>f.manifest.certificateSha256='a'.repeat(64),f=>f.manifest.packageName='com.tht.voice',
   f=>f.release.tag_name='v0.3.8-test',f=>f.release.assets[0].size=67108865]){
   const f=fixtures();mutate(f);mockGithub(t,f);const {env,storage}=setup();
   const response=await worker.fetch(req('/api/config'),env);assert.equal((await response.json()).latest,null);
   assert.equal(await storage.get('approved-apk:11'),undefined);
 }
});
test('both GitHub digest and checksum must agree with update.json',async t=>{
 const {env}=setup();const f=fixtures();f.release.assets[0].digest='sha256:'+'b'.repeat(64);mockGithub(t,f);
 await assert.rejects(githubLatest(githubSettings(env,true)),/SHA256 asset/);
 mockGithub(t,fixtures(),{checksum:'c'.repeat(64)+'  THT-Split.apk'});
 await assert.rejects(githubLatest(githubSettings(env,true)),/checksum/);
});
test('cache survives new Durable Object instance and concurrent requests share one sync',async t=>{
 const {env,storage}=setup(),calls=mockGithub(t);
 const results=await Promise.all(Array.from({length:6},()=>worker.fetch(req('/api/config'),env)));
 for(const response of results)assert.equal((await response.json()).latest.sha256,sha);
 assert.equal(calls.filter(c=>c.url.includes('/releases?')).length,1);
 const replacement=new Control({storage},env);env.CONTROL.get=()=>replacement;
 await worker.fetch(req('/api/config'),env);assert.equal(calls.filter(c=>c.url.includes('/releases?')).length,1);
});
test('GitHub failure preserves last good update, lock and minimum without leaking upstream errors',async t=>{
 const {env,storage}=setup();mockGithub(t);const latest=(await (await worker.fetch(req('/api/config'),env)).json()).latest;
 await storage.put('config',cfg({state:'locked',reason:'Bảo trì',minimumVersionCode:21,latest}));
 mockGithub(t,fixtures(),{status:403});
 const refresh=await worker.fetch(req('/api/admin/github/refresh',{},env),env),result=await refresh.json();
 assert.equal(result.config.state,'locked');assert.equal(result.config.minimumVersionCode,21);assert.equal(result.config.latest.sha256,sha);
 assert.match(result.error,/GitHub từ chối/);assert.ok(!JSON.stringify(result).includes(fakeToken));
 assert.equal((await storage.get('config')).state,'locked');
});
test('in-flight GitHub refresh cannot return an outdated enabled policy after admin lock',async t=>{
 const {env,storage}=setup();let start,finish;
 const started=new Promise(r=>start=r),gate=new Promise(r=>finish=r);
 mockGithub(t,fixtures(),{before:async url=>{if(url.includes('/releases?')){start();await gate;}}});
 const pending=worker.fetch(req('/api/config'),env);await started;
 await storage.put('config',cfg({state:'locked',reason:'Khóa trong lúc tải'}));finish();
 assert.equal((await (await pending).json()).state,'locked');assert.equal((await storage.get('config')).reason,'Khóa trong lúc tải');
});
test('missing GitHub credential still allows admin lock/unlock and rejects mandatory without APK',async()=>{
 const {env}=setup();delete env.GITHUB_TOKEN;
 const input=cfg({state:'locked'});input.updateSource='github';
 assert.equal((await worker.fetch(req('/api/admin/config',input,env),env)).status,200);
 assert.equal((await (await worker.fetch(req('/api/config'),env)).json()).state,'locked');
 assert.equal((await worker.fetch(req('/api/admin/config',{...input,minimumVersionCode:21},env),env)).status,400);
 assert.equal((await worker.fetch(req('/api/admin/config',{...input,state:'enabled'},env),env)).status,200);
});
test('client cannot choose arbitrary private assets, redirect destinations or forward authorization',async t=>{
 const {env}=setup();const calls=mockGithub(t);
 assert.equal((await worker.fetch(req('/api/apk/9999'),env)).status,404);assert.equal(calls.length,0);
 assert.equal((await worker.fetch(req('/api/admin/github/refresh',{}),env)).status,401);assert.equal(calls.length,0);
 await worker.fetch(req('/api/config'),env);mockGithub(t,fixtures(),{redirect:'https://attacker.test/collect'});
 const response=await worker.fetch(req('/api/apk/11'),env);assert.equal(response.status,502);assert.match((await response.json()).error,/không được phép/);
});
test('APK stream rejects truncated or excessive bytes and wrong Content-Length',async t=>{
 for(const options of [{bytes:apkBytes.slice(0,-1),length:false},{bytes:new Uint8Array(9),length:false},{length:99}]){
   const {env}=setup();mockGithub(t);await worker.fetch(req('/api/config'),env);mockGithub(t,fixtures(),options);
   const response=await worker.fetch(req('/api/apk/11'),env);
   if(options.length===99)assert.equal(response.status,502);else await assert.rejects(response.arrayBuffer());
 }
});
test('changing certificate invalidates cached approved download and does not auto-unlock',async t=>{
 const {env,storage}=setup();mockGithub(t);await worker.fetch(req('/api/config'),env);
 await storage.put('config',cfg({state:'locked'}));env.RELEASE_CERT_SHA256='a'.repeat(64);
 assert.equal((await worker.fetch(req('/api/apk/11'),env)).status,404);
 assert.equal((await (await worker.fetch(req('/api/config'),env)).json()).state,'locked');
});
test('automatic source ignores submitted update metadata, keeps mandatory within approved version',async t=>{
 const {env}=setup();mockGithub(t);
 const input=cfg({minimumVersionCode:21,latest:{versionCode:999,versionName:'fake',apkUrl:'https://attacker.test/app.apk',sha256:'f'.repeat(64)}});
 input.updateSource='github';input.includePrerelease=true;
 assert.equal((await worker.fetch(req('/api/admin/config',input,env),env)).status,200);
 const result=await (await worker.fetch(req('/api/config'),env)).json();assert.equal(result.latest.versionCode,21);assert.equal(result.minimumVersionCode,21);
 assert.equal((await worker.fetch(req('/api/admin/config',{...input,minimumVersionCode:22},env),env)).status,400);
});
test('manual updates remain available, ADMIN_PASSWORD alias protects configuration',async()=>{
 const {env}=setup();env.ADMIN_PASSWORD=env.ADMIN_TOKEN;delete env.ADMIN_TOKEN;
 const latest={versionCode:21,versionName:'manual',apkUrl:'https://download.test/app.apk',sha256:sha,notes:''};
 assert.equal((await worker.fetch(req('/api/admin/config',{...cfg({latest}),updateSource:'manual'},env),env)).status,200);
 assert.deepEqual((await (await worker.fetch(req('/api/config'),env)).json()).latest,latest);
 assert.equal((await worker.fetch(req('/api/admin/config',cfg()),env)).status,401);
});
