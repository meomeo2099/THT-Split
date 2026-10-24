package com.tht.split.connection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.tht.split.Store;
import com.tht.split.bridge.*;
import org.json.*;
import java.io.IOException;


public final class Backend {
    private final Context context;private final Store settings;private final String method;
    public Backend(Context c,String selected){context=c;settings=new Store(c);method=com.tht.split.core.TransportPolicy.select(com.tht.split.BuildConfig.SYSTEM_ONLY,selected);}
    public static boolean systemGranted(Context c){return c.checkSelfPermission("android.permission.MANAGE_ACTIVITY_STACKS")==PackageManager.PERMISSION_GRANTED;}
    public JSONObject call(JSONObject q)throws Exception {
        if(method.equals("off"))return new DirectWindows(context).call(q).put("transport",method);
        if(method.equals("system")){
            if(!systemGranted(context))throw new IOException("APK chưa được cấp MANAGE_ACTIVITY_STACKS. Cài được không có nghĩa đã có quyền hệ thống.");
            HiddenAccess.allow("Landroid/app/ActivityOptions;","Landroid/app/ActivityManager","Landroid/app/ActivityTaskManager;","Landroid/app/IActivityManager","Landroid/app/IActivityTaskManager","Landroid/content/res/Configuration;","Landroid/app/WindowConfiguration;");
            JSONObject result=ShellServer.systemCall(context,q);return result.put("verified",result.optBoolean("verified",true)).put("transport",method);
        }
        if(!method.equals("adb"))throw new IOException("Phương thức điều khiển không hợp lệ");
        ConnectionManager.ensure(context);JSONObject result=new ShellClient(settings).call(q);return result.put("verified",result.optBoolean("verified",true)).put("transport",method);
    }
    public JSONObject ping()throws Exception{return call(new JSONObject().put("op","PING"));}
    public boolean resizeSupported(){return !method.equals("off");}
}

