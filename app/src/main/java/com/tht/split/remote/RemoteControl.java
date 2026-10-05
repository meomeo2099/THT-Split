package com.tht.split.remote;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import java.security.MessageDigest;
import com.tht.split.*;
import com.tht.split.core.AccessRules;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.json.*;

/** No publisher credential in the APK. Each installation owns a private random credential. */
public final class RemoteControl {
    private static final Object LOCK = new Object();
    private final Store store;
    private final Context context;
    public RemoteControl(Context c) { context=c.getApplicationContext();store = new Store(c, "tht_remote"); }
    public boolean configured() { return !BuildConfig.CONTROL_URL.isEmpty(); }
    public String deviceId() { synchronized (LOCK) { String id=store.s("device_id","");
        if(id.isEmpty()){
            String scoped=Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
            if(scoped==null||scoped.isEmpty())id=UUID.randomUUID().toString();
            else try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest((BuildConfig.APPLICATION_ID+":"+scoped).getBytes(StandardCharsets.UTF_8));StringBuilder hex=new StringBuilder();for(byte b:bytes)hex.append(String.format(java.util.Locale.US,"%02x",b&255));id=hex.toString();}catch(Exception e){throw new IllegalStateException(e);}
            store.prefs.edit().putString("device_id",id).commit();}return id; } }
    private String credential() { String t=store.s("credential","");if(t.isEmpty()){t=Store.randomToken();store.prefs.edit().putString("credential",t).commit();}return t; }
    public JSONObject policy() { try{return new JSONObject(store.s("policy","{}"));}catch(JSONException e){return new JSONObject();} }
    public String denial() { JSONObject p=policy();String s=AccessRules.denial(configured(),p.has("state"),p.optString("state"),p.optInt("minimumVersionCode"),BuildConfig.VERSION_CODE,
        store.prefs.getLong("checked_at",0),System.currentTimeMillis(),p.optLong("offlineSeconds",604800));
        String reason=p.optString("reason","");return s+(s.isEmpty()||reason.isEmpty()?"":". "+reason); }
    public String status() { if(!configured())return "Quản lý từ xa chưa được cấu hình trong bản test này";
        String d=denial();return d.isEmpty()?"Thiết bị đã được duyệt • tự kiểm tra khi có mạng":d; }
    public String lastError() { return store.s("error",""); }
    public void refresh(boolean force) throws Exception { if(!configured())return;synchronized(LOCK){
        long now=System.currentTimeMillis(),last=store.prefs.getLong("attempt_at",0);
        if(!force&&now>=last&&now-last<300000)return;
        store.prefs.edit().putLong("attempt_at",now).apply();
        try {
            String base=BuildConfig.CONTROL_URL;
            if(!base.matches("https://[^/]+"))throw new IOException("Địa chỉ dịch vụ phải là HTTPS, không có đường dẫn");
            String secret=credential();JSONObject body=new JSONObject().put("deviceId",deviceId()).put("credential",secret)
                .put("versionCode",BuildConfig.VERSION_CODE).put("versionName",BuildConfig.VERSION_NAME)
                .put("model",(Build.MANUFACTURER+" "+Build.MODEL).substring(0,Math.min(120,(Build.MANUFACTURER+" "+Build.MODEL).length())))
                .put("sdk",Build.VERSION.SDK_INT);
            HttpURLConnection con=(HttpURLConnection)new URL(base+"/api/device").openConnection();
            con.setConnectTimeout(8000);con.setReadTimeout(8000);con.setInstanceFollowRedirects(false);con.setRequestMethod("POST");con.setDoOutput(true);
            con.setRequestProperty("Content-Type","application/json");con.setRequestProperty("Accept","application/json");
            try {try(OutputStream out=con.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}
                if(con.getResponseCode()!=200)throw new IOException("Dịch vụ trả HTTP "+con.getResponseCode());
                JSONObject p=new JSONObject(read(con.getInputStream(),65536));validate(p);
                if(!deviceId().equals(p.getString("deviceId")))throw new IOException("Phản hồi sai thiết bị");
                if(!store.prefs.edit().putString("policy",p.toString()).putLong("checked_at",System.currentTimeMillis()).putString("error","").commit())throw new IOException("Không lưu được chính sách");
            }finally{con.disconnect();}
        }catch(Exception e){store.set("error","Không liên hệ được dịch vụ. Giữ chính sách đã lưu.");throw e;}
    } }
    public void require(String command) throws Exception {if(AccessRules.exitCommand(command))return;
        try{refresh(false);}catch(Exception ignored){}String d=denial();if(!d.isEmpty())throw new AccessDenied(d); }
    public static void validate(JSONObject p) throws Exception {
        String state=p.getString("state");if(!state.equals("enabled")&&!state.equals("pending")&&!state.equals("locked"))throw new IOException("Trạng thái không hợp lệ");
        int min=p.getInt("minimumVersionCode");long grace=p.getLong("offlineSeconds");if(min<0||grace<3600||grace>2592000)throw new IOException("Chính sách không hợp lệ");
        JSONObject latest=p.optJSONObject("latest");if(min>0&&(latest==null||latest.optInt("versionCode")<min))throw new IOException("Thiếu bản cập nhật bắt buộc");
        if(latest!=null&&latest.optInt("versionCode")>0){if(!latest.optString("sha256").matches("[a-fA-F0-9]{64}")||!https(latest.optString("apkUrl")))throw new IOException("Bản cập nhật không hợp lệ");}
    }
    public static boolean https(String s){try{URI u=new URI(s);return "https".equals(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null;}catch(Exception e){return false;} }
    public static String read(InputStream stream,int limit)throws IOException{try(InputStream in=stream;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){if(out.size()+n>limit)throw new IOException("Phản hồi quá lớn");out.write(b,0,n);}return out.toString("UTF-8");}}
    public static final class AccessDenied extends IOException { public AccessDenied(String message){super(message);} }
}
