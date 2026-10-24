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
            context=getTargetContext();
            context.getSharedPreferences("tht_split",0).edit().remove("transport_choices_v1").putString("transport","adb").putInt("opacity",87).commit();
            store=new Store(context);
            check(store.s("transport","").equals("system")&&store.i("opacity",0)==87,"Platform migration must preserve effective system method and settings");
            for(String method:new String[]{"off","adb","system"}){store.set("transport",method);check(new Store(context).s("transport","").equals(method),"Selection overwritten: "+method);}
            store.set("transport","off");JSONObject offPing=new Backend(context,"off").ping();
            check(offPing.getString("transport").equals("off")&&!offPing.getBoolean("verified"),"Platform signer forced Off back to system");
            store.set("transport","adb");store.set("adb_host","127.0.0.1");store.set("adb_port",1);
            boolean adbFailed=false;try{new Backend(context,"adb").ping();}catch(Exception expected){adbFailed=true;}
            check(adbFailed&&store.s("transport","").equals("adb"),"Unavailable ADB silently used system");store.set("adb_port",5555);
            Context denied=new ContextWrapper(context){@Override public int checkSelfPermission(String permission){return permission.equals("android.permission.MANAGE_ACTIVITY_STACKS")?android.content.pm.PackageManager.PERMISSION_DENIED:super.checkSelfPermission(permission);}};
            boolean systemFailed=false;try{new Backend(denied,"system").ping();}catch(java.io.IOException expected){systemFailed=expected.getMessage().contains("MANAGE_ACTIVITY_STACKS");}
            check(systemFailed,"System without permission silently used another transport");
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
            command("stop");waitFor(()->!store.b("session_active",false),"stop native fixture");
            store.set("mode","freeform");startPair(apps.get(0),apps.get(1));
            check(lastLayout().getString("usedMode").equals("freeform"),"Explicit freeform used native");
            command("swap");waitFor(()->store.s("left","").equals(apps.get(1))&&!store.b("session_busy",false),"freeform swap");
            JSONObject swapped=lastLayout();check(swapped.getInt("leftMode")==5&&swapped.getInt("rightMode")==5,"Freeform swap changed window mode");
            context.startForegroundService(new Intent(context,SplitService.class).putExtra("command","ratio").putExtra("ratio",40));
            waitFor(()->store.i("ratio",0)==40&&!store.b("session_busy",false),"freeform ratio");
            command("hide");waitFor(()->hasOverlay("Hiện thanh điều khiển"),"freeform hide");
            command("show");waitFor(()->hasOverlay("Ẩn thanh"),"freeform show");
            command("stop");waitFor(()->!store.b("session_active",false),"stop freeform");
            store.set("mode","auto");startPair(apps.get(0),apps.get(1));
            JSONObject automatic=lastLayout();check(automatic.getString("usedMode").equals("freeform"),"Auto did not prefer freeform");
            JSONObject cooldownRequest=new JSONObject().put("op","LAYOUT").put("mode","native").put("preserveOemStacks",true)
                .put("left",apps.get(0)).put("right",apps.get(1)).put("leftComponent",component(apps.get(0))).put("rightComponent",component(apps.get(1)))
                .put("leftBounds",automatic.getJSONArray("actualLeftBounds")).put("rightBounds",automatic.getJSONArray("actualRightBounds"));
            context.getSharedPreferences("tht_split_guard",0).edit().putLong("divider_crash_at",System.currentTimeMillis()).commit();
            try{boolean cooldown=false;try{new Backend(context,"system").call(cooldownRequest);}catch(Exception expected){cooldown=String.valueOf(expected.getMessage()).contains("10 phút");}check(cooldown,"Persisted cooldown not applied to native");
                check(new Backend(context,"system").call(cooldownRequest.put("mode","freeform")).getString("usedMode").equals("freeform"),"Cooldown blocked freeform");
            }finally{context.getSharedPreferences("tht_split_guard",0).edit().remove("divider_crash_at").commit();}
            command("stop");waitFor(()->!store.b("session_active",false),"stop auto");
            store.set("transport","off");startPair(apps.get(0),apps.get(1));
            waitFor(()->hasOverlay("Dừng điều khiển Off"),"Off toolbar");
            check(new Store(context,"tht_split_session").s("transport","").equals("off"),"Off session rerouted");
            result.putString("stream","THT_NATIVE_UI_TEST_OK: transport migration and strict routing; native hide/show and wake; freeform swap/ratio; auto freeform; Off session; persisted system cooldown\n");
            passed=true;
        }catch(Throwable error){result.putString("stream","THT_NATIVE_UI_TEST_FAILED: "+error+" last_error="+(store==null?"":store.s("last_error",""))+"\n");}
        finally{if(context!=null&&store!=null&&store.b("session_active",false))try{
            command("stop");waitFor(()->!store.b("session_active",false),"fixture cleanup");
        }catch(Exception cleanup){passed=false;result.putString("stream","THT_NATIVE_UI_TEST_FAILED: cleanup "+cleanup+"\n");}}
        finish(passed?-1:0,result);
    }
    private JSONObject lastLayout()throws Exception{return new JSONObject(store.s("last_layout","{}"));}
    private void startPair(String left,String right)throws Exception{
        context.startForegroundService(new Intent(context,SplitService.class).putExtra("command","start").putExtra("left",left).putExtra("right",right).putExtra("ratio",50));
        waitFor(()->store.b("session_active",false)&&!store.b("session_busy",false),"start "+store.s("transport","")+"/"+store.s("mode",""));
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

