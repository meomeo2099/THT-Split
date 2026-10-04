package com.tht.split.connection;
import android.content.*;
import com.tht.split.Store;
public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){if(!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())&&!Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction()))return;Store s=new Store(c);s.set("session_busy",false);s.set("session_active",false);s.set("session_full",false);if(s.s("transport","adb").equals("adb")&&s.b("adb_at_boot",true))try{c.startForegroundService(new Intent(c,ConnectionService.class));}catch(RuntimeException e){s.set("connection_status","Không được chuẩn bị ADB ở nền; sẽ thử khi chia màn.");}}
}
