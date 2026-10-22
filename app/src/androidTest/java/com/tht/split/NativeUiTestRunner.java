package com.tht.split;

import android.app.Instrumentation;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.*;
import com.tht.split.connection.Backend;
import org.json.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/** CI test APK only: native task pair, overlay hide/show, blocked swap and actual sleep/wake. */
public final class NativeUiTestRunner extends Instrumentation {
    private Context context;private Store store;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){
        Bundle result=new Bundle();boolean passed=false;
        try{
            context=getTargetContext();store=new Store(context);
            AccessibilityServiceInfo info=getUiAutomation().getServiceInfo();
            info.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;getUiAutomation().setServiceInfo(info);
            List<String> apps=new ArrayList<>();
            for(String pkg:new String[]{"com.android.settings","com.android.dialer","com.android.browser","com.android.contacts","com.android.calendar","com.android.calculator2"})
                if(context.getPackageManager().getLaunchIntentForPackage(pkg)!=null)apps.add(pkg);
            check(apps.size()>=2,"Two stock fixture apps required");
            store.set("oem_ui_compat",true);store.set("auto_hide",false);store.set("mode","native");store.set("transport","system");
            store.set("left",apps.get(0));store.set("right",apps.get(1));store.set("ratio",50);
            context.startForegroundService(new Intent(context,SplitService.class).putExtra("command","start").putExtra("left",apps.get(0)).putExtra("right",apps.get(1)).putExtra("ratio",50));
            waitFor(()->store.b("session_active",false)&&!store.b("session_busy",false),"start: "+store.s("last_error",""));
            JSONObject layout=new JSONObject(store.s("last_layout","{}"));
            int left=layout.getInt("leftTask"),right=layout.getInt("rightTask");
            Rect beforeLeft=bounds(left),beforeRight=bounds(right);
            waitFor(()->hasOverlay("Ẩn thanh"),"toolbar initially visible");
            command("hide");waitFor(()->hasOverlay("Hiện thanh điều khiển"),"hidden toolbar has mini");
            check(beforeLeft.equals(bounds(left))&&beforeRight.equals(bounds(right)),"Hide changed native task bounds");
            command("show");waitFor(()->hasOverlay("Ẩn thanh"),"toolbar shown");
            check(beforeLeft.equals(bounds(left))&&beforeRight.equals(bounds(right)),"Show changed native task bounds");
            store.set("last_error","");command("swap");
            waitFor(()->store.s("last_error","").contains("tạm khóa"),"swap rejected before backend");
            check(store.s("left","").equals(apps.get(0))&&store.s("right","").equals(apps.get(1)),"Rejected swap changed selection");
            check(beforeLeft.equals(bounds(left))&&beforeRight.equals(bounds(right)),"Rejected swap changed task bounds");
            if(apps.size()>=3){
                JSONObject request=new JSONObject().put("op","LAYOUT").put("mode","native").put("preserveOemStacks",true)
                    .put("left",apps.get(1)).put("right",apps.get(2))
                    .put("leftComponent",component(apps.get(1))).put("rightComponent",component(apps.get(2)))
                    .put("leftBounds",layout.getJSONArray("actualLeftBounds")).put("rightBounds",layout.getJSONArray("actualRightBounds"));
                boolean blocked=false;
                try{new Backend(context,"system").call(request);}catch(Exception expected){blocked=String.valueOf(expected.getMessage()).contains("Giữ nguyên stack");}
                check(blocked,"Foreign native stack not rejected");
                check(beforeLeft.equals(bounds(left))&&beforeRight.equals(bounds(right)),"Foreign guard mutated tasks");
            }
            command("hide");waitFor(()->hasOverlay("Hiện thanh điều khiển"),"hide before actual sleep");
            shell("input keyevent 223");waitFor(()->store.s("last_screen_event","").equals("screen_off"),"SCREEN_OFF observed");
            shell("input keyevent 224");shell("input keyevent 82");
            waitFor(()->store.s("last_screen_event","").equals("screen_on")&&hasOverlay("Ẩn thanh"),"toolbar recreated after wake");
            check(beforeLeft.equals(bounds(left))&&beforeRight.equals(bounds(right)),"Wake rebuilt native task bounds");
            Bitmap bitmap=getUiAutomation().takeScreenshot();
            if(bitmap!=null){try(OutputStream out=new FileOutputStream(new File(context.getExternalFilesDir(null),"native-ui-awake.png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
            result.putString("stream","THT_NATIVE_UI_TEST_OK: hide/show preserve bounds, swap blocked, native scope guarded, screen sleep/wake restores THT overlay\n");
            passed=true;
        }catch(Throwable error){result.putString("stream","THT_NATIVE_UI_TEST_FAILED: "+error+" last_error="+(store==null?"":store.s("last_error",""))+"\n");}
        finally{if(context!=null&&store!=null&&store.b("session_active",false))try{
            command("stop");waitFor(()->!store.b("session_active",false),"fixture cleanup");
        }catch(Exception cleanup){passed=false;result.putString("stream","THT_NATIVE_UI_TEST_FAILED: cleanup "+cleanup+"\n");}}
        finish(passed?-1:0,result);
    }
    private interface Condition{boolean test()throws Exception;}
    private void waitFor(Condition condition,String message)throws Exception{
        for(int n=0;n<100;n++){if(condition.test())return;SystemClock.sleep(100);}
        throw new AssertionError(message+" last_error="+store.s("last_error",""));
    }
    private void command(String action){SplitService.command(context,action);}
    private String component(String pkg){return context.getPackageManager().getLaunchIntentForPackage(pkg).getComponent().flattenToString();}
    private Rect bounds(int task)throws Exception{
        Method service=Class.forName("android.app.ActivityManager").getDeclaredMethod("getService");service.setAccessible(true);
        Object manager=service.invoke(null);Method read=manager.getClass().getMethod("getTaskBounds",int.class);read.setAccessible(true);
        return (Rect)read.invoke(manager,task);
    }
    private boolean hasOverlay(String description){
        for(AccessibilityWindowInfo window:getUiAutomation().getWindows()){
            AccessibilityNodeInfo root=window.getRoot();if(root!=null&&contains(root,description))return true;
        }return false;
    }
    private boolean contains(AccessibilityNodeInfo node,String description){
        if(description.contentEquals(node.getContentDescription()==null?"":node.getContentDescription()))return true;
        for(int n=0;n<node.getChildCount();n++){AccessibilityNodeInfo child=node.getChild(n);if(child!=null&&contains(child,description))return true;}
        return false;
    }
    private void shell(String command)throws Exception{
        try(InputStream input=new ParcelFileDescriptor.AutoCloseInputStream(getUiAutomation().executeShellCommand(command))){byte[] buffer=new byte[256];while(input.read(buffer)>=0){}}
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
