package com.tht.split.connection;

import android.app.*;
import android.content.*;
import android.os.*;
import com.tht.split.*;

/** Prepares privilege only; never launches saved apps during boot. */
public final class ConnectionService extends Service {
    @Override public void onCreate(){super.onCreate();NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);n.createNotificationChannel(new NotificationChannel("connection","Kết nối THT Split",NotificationManager.IMPORTANCE_LOW));startForeground(2,new Notification.Builder(this,"connection").setSmallIcon(R.drawable.icon_foreground).setContentTitle("THT Split").setContentText("Đang chuẩn bị ADB nội bộ").build());}
    @Override public int onStartCommand(Intent i,int flags,int id){new Thread(()->{try{Store s=new Store(this);if(s.s("transport","adb").equals("adb"))ConnectionManager.ensure(this);}catch(Exception e){new Store(this).set("connection_status","Chưa kết nối sau khởi động. App sẽ thử lại khi nhận lệnh chia.");}finally{stopSelf(id);}},"THT-boot-connect").start();return START_NOT_STICKY;}
    @Override public IBinder onBind(Intent i){return null;}
}
