package com.tht.split.remote;

import android.content.Context;
import com.tht.split.*;
import com.tht.split.core.AccessRules;
import java.io.*;
import java.net.*;
import org.json.*;

/** Shared update/application policy; no device identity or enrollment request. */
public final class RemoteControl {
    private static final Object LOCK = new Object();
    private final Store store;
    public RemoteControl(Context c) {
        // A separate cache prevents legacy per-device locks/leases from blocking public installs.
        store = new Store(c, "tht_public_policy");
    }
    public boolean configured() { return !BuildConfig.CONTROL_URL.isEmpty(); }
    public JSONObject policy() { try{return new JSONObject(store.s("policy","{}"));}catch(JSONException e){return new JSONObject();} }
    public String denial() {
        JSONObject p=policy();
        String d=AccessRules.denial(configured(),p.optString("state","enabled"),p.optInt("minimumVersionCode",0),BuildConfig.VERSION_CODE);
        String reason=p.optString("reason","");
        return d+(d.isEmpty()||reason.isEmpty()?"":". "+reason);
    }
    public String status() {
        String d=denial();if(!d.isEmpty())return d;
        return "Dùng ngay, không đăng ký thiết bị • cập nhật khi có mạng";
    }
    public String lastError() { return store.s("error",""); }
    public void refresh(boolean force) throws Exception { if(!configured())return;synchronized(LOCK){
        long now=System.currentTimeMillis(),last=store.prefs.getLong("attempt_at",0);
        if(!force&&now>=last&&now-last<300000)return;
        store.prefs.edit().putLong("attempt_at",now).apply();
        try {
            String base=BuildConfig.CONTROL_URL;
            if(!base.matches("https://[^/]+"))throw new IOException("Địa chỉ dịch vụ phải là HTTPS, không có đường dẫn");
            HttpURLConnection con=(HttpURLConnection)new URL(base+"/api/config").openConnection();
            con.setConnectTimeout(8000);con.setReadTimeout(8000);con.setInstanceFollowRedirects(false);con.setRequestMethod("GET");
            con.setRequestProperty("Accept","application/json");
            try {
                int code=con.getResponseCode();if(code!=200)throw new IOException("Dịch vụ cập nhật trả HTTP "+code);
                String type=con.getContentType();
                if(type==null||!type.toLowerCase(java.util.Locale.US).contains("application/json"))throw new IOException("Worker chưa chạy API cập nhật THT Split");
                JSONObject p=new JSONObject(read(con.getInputStream(),65536));validate(p);
                JSONObject saved=new JSONObject().put("state",p.getString("state")).put("reason",p.optString("reason",""))
                    .put("minimumVersionCode",p.getInt("minimumVersionCode")).put("latest",p.optJSONObject("latest")==null?JSONObject.NULL:p.getJSONObject("latest"));
                if(!store.prefs.edit().putString("policy",saved.toString()).putString("error","").commit())throw new IOException("Không lưu được thông tin cập nhật");
            }finally{con.disconnect();}
        }catch(Exception e){store.set("error",com.tht.split.core.FailureText.describe(e)+". Giữ thông tin cập nhật đã lưu.");throw e;}
    } }
    public void require(String command) throws Exception {
        if(AccessRules.exitCommand(command))return;
        // Never wait for the network on a task command, including the first installation.
        String d=denial();if(!d.isEmpty())throw new AccessDenied(d);
    }
    public static void validate(JSONObject p) throws Exception {
        String state=p.getString("state");
        if(!state.equals("enabled")&&!state.equals("locked"))throw new IOException("Trạng thái ứng dụng không hợp lệ");
        int min=p.getInt("minimumVersionCode");if(min<0)throw new IOException("Phiên bản tối thiểu không hợp lệ");
        JSONObject latest=p.optJSONObject("latest");
        if(min>0&&(latest==null||latest.optInt("versionCode")<min))throw new IOException("Thiếu bản cập nhật bắt buộc");
        if(latest!=null&&(latest.optInt("versionCode")<=0||!latest.optString("sha256").matches("[a-fA-F0-9]{64}")||!https(latest.optString("apkUrl"))))
            throw new IOException("Bản cập nhật không hợp lệ");
    }
    public static boolean https(String s){try{URI u=new URI(s);return "https".equals(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null;}catch(Exception e){return false;} }
    public static String read(InputStream stream,int limit)throws IOException{try(InputStream in=stream;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){if(out.size()+n>limit)throw new IOException("Phản hồi quá lớn");out.write(b,0,n);}return out.toString("UTF-8");}}
    public static final class AccessDenied extends IOException { public AccessDenied(String message){super(message);} }
}
