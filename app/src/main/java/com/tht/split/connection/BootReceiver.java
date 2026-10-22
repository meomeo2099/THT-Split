package com.tht.split.connection;
import android.content.*;
import com.tht.split.Store;
public final class BootReceiver extends BroadcastReceiver {
    /**
     * Screen restarted while a pair was open: if the crash log has a new ROM divider crash, leave split
     * and go home so the car UI comes back (DividerCrashGuard). Only for the system transport.
     */
    private static void afterDividerCrash(Context c,Store s,PendingResult pending){
        new Thread(()->{try{
            org.json.JSONObject out=new Backend(c,"system").call(new org.json.JSONObject().put("op","BOOT_CHECK").put("seen",s.s("divider_seen","")));
            s.set("divider_seen",out.optString("log",""));
            if(out.optBoolean("dismissed",false))s.set("last_error","Màn đã khởi động lại vì lỗi thanh chia đôi của ROM; đã thoát chia. Nếu vẫn mất thanh trạng thái, nói \"gỡ kẹt màn hình\" hoặc bấm Gỡ kẹt giao diện xe.");
        }catch(Exception ignored){}finally{pending.finish();}},"THT-boot-divider").start();
    }
    @Override public void onReceive(Context c,Intent i){if(!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())&&!Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction()))return;Store s=new Store(c);
        if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())&&s.b("session_active",false)&&(com.tht.split.BuildConfig.PLATFORM_TEST||s.s("transport","adb").equals("system")))afterDividerCrash(c,s,goAsync());
        s.set("session_busy",false);s.set("session_active",false);s.set("session_full",false);if(s.s("transport","adb").equals("adb")&&s.b("adb_at_boot",true))try{c.startForegroundService(new Intent(c,ConnectionService.class));}catch(RuntimeException e){s.set("connection_status","Không được chuẩn bị ADB ở nền; sẽ thử khi chia màn.");}}
}
