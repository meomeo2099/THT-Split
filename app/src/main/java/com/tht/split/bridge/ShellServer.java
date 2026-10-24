package com.tht.split.bridge;

import android.graphics.Rect;
import android.os.Process;
import com.tht.split.core.TaskParser;
import com.tht.split.core.PairController;
import com.tht.split.core.BoundsCheck;
import com.tht.split.core.DividerCrashGuard;
import com.tht.split.core.LayoutMath;
import org.json.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/** Started by the user with adb/app_process; NOT by the unprivileged app UID. */
public final class ShellServer {
    private static String secret;
    private static Object manager;
    /** Last ROM divider crash seen after a native layout (0 = none); lives as long as the bridge/app process. */
    private static long dividerCrashAt;
    @android.annotation.SuppressLint("StaticFieldLeak") // Set only inside a synchronized call; cleared in finally.
    private static android.content.Context systemContext;
    public static synchronized JSONObject systemCall(android.content.Context context,JSONObject q)throws Exception {
        if(context.checkSelfPermission("android.permission.MANAGE_ACTIVITY_STACKS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)throw new SecurityException("Thiếu quyền hệ thống");
        systemContext=context.getApplicationContext();try{return handle(q).put("ok",true);}finally{systemContext=null;}
    }
    public static void main(String[] args)throws Exception {
        if(Process.myUid()!=2000 && Process.myUid()!=0)throw new SecurityException("Bridge cần uid shell (ADB)");
        if(args.length!=1||!args[0].matches("[0-9a-f]{64}"))throw new IllegalArgumentException("Thiếu token 64 ký tự");
        secret=args[0];
        // Loopback only; token required before any operation. One connection at a time prevents layout races.
        try(ServerSocket server=new ServerSocket()){
            server.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),ShellClient.PORT));
            while(true){try(Socket socket=server.accept()){
                socket.setSoTimeout(3000);
                JSONObject result;
                try {JSONObject req=new JSONObject(new DataInputStream(socket.getInputStream()).readUTF());
                    if(!MessageDigest.isEqual(secret.getBytes(StandardCharsets.US_ASCII),req.optString("token").getBytes(StandardCharsets.US_ASCII)))throw new SecurityException("Token bridge không đúng");
                    result=handle(req);result.put("ok",true);
                }catch(Exception e){Throwable cause=e instanceof InvocationTargetException?((InvocationTargetException)e).getCause():e;result=new JSONObject().put("ok",false).put("error",cause.getClass().getSimpleName()+": "+String.valueOf(cause.getMessage()));}
                new DataOutputStream(socket.getOutputStream()).writeUTF(result.toString());
            }catch(IOException ignored){/* disconnected peer must not kill bridge */}}
        }
    }
    private static Object manager()throws Exception {
        if(manager!=null)return manager;
        String cls=android.os.Build.VERSION.SDK_INT>=29?"android.app.ActivityTaskManager":"android.app.ActivityManager";
        Method m=Class.forName(cls).getDeclaredMethod("getService");m.setAccessible(true);manager=m.invoke(null);return manager;
    }
    private static Object invoke(String name,Class<?>[] types,Object...args)throws Exception{
        Method m=manager().getClass().getMethod(name,types);m.setAccessible(true);return m.invoke(manager(),args);
    }
    private static String pkg(JSONObject q,String key)throws Exception{String p=q.getString(key);if(!p.matches("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+")||p.equals("com.tht.split"))throw new IllegalArgumentException("Package không hợp lệ");return p;}
    private static String component(JSONObject q,String key,String pkg)throws Exception{String c=q.getString(key);if(!c.matches("[A-Za-z0-9_.$]+/[A-Za-z0-9_.$]+")||!c.startsWith(pkg+"/"))throw new IllegalArgumentException("Component không hợp lệ");return c;}
    private static Rect rect(JSONObject q,String key)throws Exception{JSONArray a=q.getJSONArray(key);if(a.length()!=4)throw new IllegalArgumentException("Bounds");int l=a.getInt(0),t=a.getInt(1),r=a.getInt(2),b=a.getInt(3);if(l<0||t<0||r<=l||b<=t||r>16384||b>16384)throw new IllegalArgumentException("Bounds");return new Rect(l,t,r,b);}
    private static int task(String p)throws Exception{
        try {List<?> infos=(List<?>)invoke("getAllStackInfos",new Class<?>[]{});
            for(Object info:infos){int[] ids=(int[])info.getClass().getField("taskIds").get(info);String[] names=(String[])info.getClass().getField("taskNames").get(info);
                for(int n=ids.length-1;n>=0;n--)if(names[n].equals(p)||names[n].startsWith(p+"/"))return ids[n];}
        }catch(ReflectiveOperationException ignored){}
        return TaskParser.find(exec("dumpsys","activity","activities"),p);
    }
    private static int awaitTask(String p)throws Exception{for(int i=0;i<20;i++){int id=task(p);if(id>=0)return id;Thread.sleep(100);}throw new IOException("Không tìm thấy task: "+p);}
    private static int windowMode(int taskId)throws Exception {
        List<?> infos=(List<?>)invoke("getAllStackInfos",new Class<?>[]{});
        for(Object info:infos){int[] ids=(int[])info.getClass().getField("taskIds").get(info);for(int id:ids)if(id==taskId){Object config=info.getClass().getField("configuration").get(info);Object wc=config.getClass().getField("windowConfiguration").get(config);return (Integer)wc.getClass().getMethod("getWindowingMode").invoke(wc);}}
        return -1;
    }
    private static void setMode(int id,int mode)throws Exception{
        if(mode==3){
            // The generic mode3 call opens Recents on the other side on Android 9.
            invoke("setTaskWindowingModeSplitScreenPrimary",new Class<?>[]{int.class,int.class,boolean.class,boolean.class,Rect.class,boolean.class},id,0,true,false,null,false);
        }else invoke("setTaskWindowingMode",new Class<?>[]{int.class,int.class,boolean.class},id,mode,true);
    }
    private static Object stackInfo(int taskId)throws Exception {
        List<?> infos=(List<?>)invoke("getAllStackInfos",new Class<?>[]{});
        for(Object info:infos)for(int id:(int[])info.getClass().getField("taskIds").get(info))if(id==taskId)return info;
        throw new IOException("Task đã đóng trong lúc chia: "+taskId);
    }
    private static int stackId(int taskId)throws Exception{Object info=stackInfo(taskId);return info.getClass().getField("stackId").getInt(info);}
    private static void isolate(int taskId)throws Exception {
        Object info=stackInfo(taskId);
        if(((int[])info.getClass().getField("taskIds").get(info)).length==1)return;
        int display=info.getClass().getField("displayId").getInt(info);
        int target=(Integer)invoke("createStackOnDisplay",new Class<?>[]{int.class},display);
        if(target<0)throw new IOException("Không tạo được stack riêng cho app");
        invoke("moveTaskToStack",new Class<?>[]{int.class,int.class,boolean.class},taskId,target,true);
        if(stackId(taskId)!=target)throw new IOException("Firmware không chuyển app sang stack riêng");
    }
    private static void clearPrimary()throws Exception {clearPrimary(null,null,false);}
    private static void clearPrimary(String leftPkg,String rightPkg,boolean preserveOemStacks)throws Exception {
        List<?> infos=(List<?>)invoke("getAllStackInfos",new Class<?>[]{});
        // Snapshot IDs before changing stacks; changing the primary also collapses secondaries.
        List<Integer> primary=new ArrayList<>();
        for(Object info:infos){
            if(info.getClass().getField("displayId").getInt(info)!=0)continue;
            Object config=info.getClass().getField("configuration").get(info);
            Object wc=config.getClass().getField("windowConfiguration").get(config);
            if((Integer)wc.getClass().getMethod("getWindowingMode").invoke(wc)!=3)continue;
            int[] ids=(int[])info.getClass().getField("taskIds").get(info);
            if(preserveOemStacks&&ids.length>0){
                String[] names=(String[])info.getClass().getField("taskNames").get(info);
                if(names==null||names.length!=ids.length||!com.tht.split.core.StackScope.owns(names,leftPkg,rightPkg))
                    throw new IOException("Giữ nguyên stack chia có ứng dụng ngoài cặp đang chọn. Thoát phiên chia cũ trước khi mở cặp khác.");
            }
            if(ids.length>0)primary.add(ids[ids.length-1]);
        }
        for(int id:primary)setMode(id,1);
    }
    private static void launch(String comp,int mode)throws Exception{
        if(systemContext!=null){android.app.ActivityOptions options=android.app.ActivityOptions.makeBasic();android.app.ActivityOptions.class.getMethod("setLaunchWindowingMode",int.class).invoke(options,mode);systemContext.startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER).setComponent(android.content.ComponentName.unflattenFromString(comp)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),options.toBundle());return;}
        String output=exec("am","start","-W","--user","current","--windowingMode",String.valueOf(mode),"-a","android.intent.action.MAIN","-c","android.intent.category.LAUNCHER","-f","0x10000000","-n",comp);if(output.contains("Error:")||output.contains("Exception"))throw new IOException(output);}
    private static String exec(String...command)throws Exception{
        // Preserve shell UID behavior; root ADB uses regular descriptors, not su-owned pipes.
        try{return CommandRunner.run(command,Process.myUid()==0?new File("/data/local/tmp"):null,12000);}
        catch(IOException e){throw new IOException(e.getMessage()+" [UID "+Process.myUid()+", context "+securityContext()+"]",e);}
    }
    /** Crash-buffer lines with divider markers; null when logs cannot be read (never treated as a crash). */
    private static String crashLog(){try{return exec("logcat","-b","crash","-d","-e",DividerCrashGuard.PATTERN);}catch(Exception e){return null;}}
    private static long lastDividerCrash(){return systemContext==null?dividerCrashAt:systemContext.getSharedPreferences("tht_split_guard",0).getLong("divider_crash_at",0);}
    private static void rememberDividerCrash(){dividerCrashAt=System.currentTimeMillis();if(systemContext!=null)systemContext.getSharedPreferences("tht_split_guard",0).edit().putLong("divider_crash_at",dividerCrashAt).commit();}
    private static String securityContext(){try(BufferedReader r=new BufferedReader(new FileReader("/proc/self/attr/current"))){String s=r.readLine();return s==null?"unknown":s.trim().replace("\u0000","");}catch(IOException e){return "unknown";}}
    private static void resize(int leftTask,int rightTask,String mode,Rect left,Rect right)throws Exception{
        if(mode.equals("native")){
            // Final bounds: no temporary task/inset rectangles from divider animation.
            invoke("resizeDockedStack",new Class<?>[]{Rect.class,Rect.class,Rect.class,Rect.class,Rect.class},left,null,null,null,null);
            // Android derives the secondary from the whole display, losing custom top/bottom
            // reservations. Apply its final stack bounds explicitly after the dock resize.
            invoke("resizeStack",new Class<?>[]{int.class,Rect.class,boolean.class,boolean.class,boolean.class,int.class},stackId(rightTask),right,true,false,false,-1);
        }else{
            invoke("resizeTask",new Class<?>[]{int.class,Rect.class,int.class},leftTask,left,0);
            invoke("resizeTask",new Class<?>[]{int.class,Rect.class,int.class},rightTask,right,0);
        }
    }
    private static void resetBounds(int taskId)throws Exception {
        // Null inherits display bounds. preserveWindows=false lets Android reconfigure the app.
        invoke("resizeStack",new Class<?>[]{int.class,Rect.class,boolean.class,boolean.class,boolean.class,int.class},stackId(taskId),null,true,false,false,-1);
    }
    /**
     * R02 1.3.0: Android 9 can drop a just-opened freeform window back to fullscreen or let one cover the other.
     * Re-apply mode/bounds with a 32 px tolerance and raise both windows, a few rounds at most.
     */
    private static void settleFreeform(int leftTask,int rightTask,Rect left,Rect right)throws Exception {
        for(int round=0;round<3;round++){
            boolean ok=true;
            for(int n=0;n<2;n++){int id=n==0?leftTask:rightTask;Rect want=n==0?left:right;
                if(windowMode(id)!=5){setMode(id,5);ok=false;}
                Rect actual=(Rect)invoke("getTaskBounds",new Class<?>[]{int.class},id);
                if(!BoundsCheck.matches(box(want),box(actual),32)){invoke("resizeTask",new Class<?>[]{int.class,Rect.class,int.class},id,want,0);ok=false;}
            }
            for(int id:new int[]{leftTask,rightTask})try{invoke("moveTaskToFront",new Class<?>[]{int.class,int.class,android.os.Bundle.class},id,0,null);}catch(ReflectiveOperationException ignored){}
            if(ok)return;
            Thread.sleep(250);
        }
    }
    /** System split exit (R02): dismissSplitScreenMode, else move the docked stack to fullscreen, else clear primaries. */
    private static void dismissSplit()throws Exception {
        try{invoke("dismissSplitScreenMode",new Class<?>[]{boolean.class},true);return;}catch(ReflectiveOperationException ignored){}
        try{
            for(Object info:(List<?>)invoke("getAllStackInfos",new Class<?>[]{})){
                Object config=info.getClass().getField("configuration").get(info);Object wc=config.getClass().getField("windowConfiguration").get(config);
                if((Integer)wc.getClass().getMethod("getWindowingMode").invoke(wc)==3){invoke("moveTasksToFullscreenStack",new Class<?>[]{int.class,boolean.class},info.getClass().getField("stackId").getInt(info),true);return;}
            }
        }catch(ReflectiveOperationException ignored){}
        clearPrimary();
    }
    /** SystemUI is persistent: Android starts it again, which redraws the car status bar after a broken split. */
    private static void restartSystemUi()throws Exception {
        if(systemContext!=null){invoke("forceStopPackage",new Class<?>[]{String.class,int.class},"com.android.systemui",-1);return;}
        exec("am","force-stop","--user","all","com.android.systemui");
    }
    private static LayoutMath.Box box(Rect r){return new LayoutMath.Box(r.left,r.top,r.right,r.bottom);}
    private static JSONArray bounds(Rect r)throws JSONException{return new JSONArray(new int[]{r.left,r.top,r.right,r.bottom});}
    private static JSONObject observedBounds(int lt,int rt,Rect left,Rect right,int tolerance)throws Exception {
        Rect actualLeft=null,actualRight=null;boolean match=false;
        for(int i=0;i<5;i++){
            actualLeft=(Rect)invoke("getTaskBounds",new Class<?>[]{int.class},lt);
            actualRight=(Rect)invoke("getTaskBounds",new Class<?>[]{int.class},rt);
            match=BoundsCheck.matches(box(left),box(actualLeft),tolerance)&&BoundsCheck.matches(box(right),box(actualRight),tolerance);
            if(match)break;
            if(i<4)Thread.sleep(100);
        }
        return new JSONObject().put("verified",match).put("expectedLeftBounds",bounds(left)).put("expectedRightBounds",bounds(right)).put("actualLeftBounds",bounds(actualLeft)).put("actualRightBounds",bounds(actualRight))
            .put("note",match?"Android giữ đúng bounds yêu cầu":"Android chưa giữ đúng tỷ lệ/khoảng chừa yêu cầu; kiểm log hoặc chọn cửa sổ tự do trong cài đặt");
    }
    private static JSONObject observedFullscreen(int taskId,Rect expected)throws Exception {
        Rect actual=null;int mode=-1;boolean match=false;
        for(int i=0;i<5;i++){
            actual=(Rect)invoke("getTaskBounds",new Class<?>[]{int.class},taskId);mode=windowMode(taskId);
            match=mode==1&&BoundsCheck.matches(box(expected),box(actual),2);
            if(match)break;if(i<4)Thread.sleep(100);
        }
        return new JSONObject().put("verified",match).put("fullscreenTask",taskId).put("fullscreenStack",stackId(taskId)).put("fullscreenMode",mode).put("expectedFullscreenBounds",bounds(expected)).put("actualFullscreenBounds",bounds(actual))
            .put("note",match?"Android giữ bounds toàn màn hình; nội dung do ứng dụng vẽ":"Android chưa giữ đúng vùng toàn màn hình; hãy xuất log hỗ trợ");
    }
    private static String mode(JSONObject q){String m=q.optString("mode","auto");if(!m.equals("native")&&!m.equals("freeform")&&!m.equals("auto"))throw new IllegalArgumentException("Mode");return m;}
    private static void writeSetting(String key,String value)throws Exception {
        if(systemContext!=null){
            if(!value.equals("null")&&!value.matches("[0-9]+"))throw new IllegalArgumentException("Setting");
            if(!android.provider.Settings.Global.putString(systemContext.getContentResolver(),key,value.equals("null")?null:value))throw new IOException("Không lưu được thiết lập cửa sổ tự do");
            return;
        }
        if(value.equals("null"))exec("settings","delete","global",key);
        else{if(!value.matches("[0-9]+"))throw new IllegalArgumentException("Setting");exec("settings","put","global",key,value);}
    }
    private static PairController controller(boolean nativeMode,String leftPkg,String rightPkg,String lc,String rc,Rect l,Rect r,boolean preserveOemStacks){
        String used=nativeMode?"native":"freeform";
        return new PairController(new PairController.Tasks(){
            public void clearPrimary()throws Exception{ShellServer.clearPrimary(leftPkg,rightPkg,preserveOemStacks);}
            public int launch(boolean left,int windowMode)throws Exception{ShellServer.launch(left?lc:rc,windowMode);return awaitTask(left?leftPkg:rightPkg);}
            public int find(boolean left)throws Exception{return task(left?leftPkg:rightPkg);}
            public int mode(int id)throws Exception{return windowMode(id);}
            public int stack(int id)throws Exception{return stackId(id);}
            public void isolate(int id)throws Exception{ShellServer.isolate(id);}
            public void setMode(int id,int windowMode)throws Exception{ShellServer.setMode(id,windowMode);}
            public void resize(int lt,int rt)throws Exception{ShellServer.resize(lt,rt,used,l,r);}
            public void resetBounds(int id)throws Exception{ShellServer.resetBounds(id);}
            public void focus(int id)throws Exception{invoke("setFocusedTask",new Class<?>[]{int.class},id);}
        },nativeMode);
    }
    /** One LAYOUT/RESIZE in one mode, with the divider crash guard (native) and freeform settling. */
    private static JSONObject pair(String op,boolean nativeSplit,String leftPkg,String rightPkg,String lc,String rc,Rect l,Rect r,boolean preserveOemStacks)throws Exception{
        PairController controller=controller(nativeSplit,leftPkg,rightPkg,lc,rc,l,r,preserveOemStacks);
        if(nativeSplit&&DividerCrashGuard.coolingDown(lastDividerCrash(),System.currentTimeMillis()))throw new IOException(DividerCrashGuard.COOLDOWN_MESSAGE);
        String crashBefore=nativeSplit?crashLog():null;
        if(op.equals("LAYOUT"))controller.layout();else controller.resize();
        if(crashBefore!=null){
            Thread.sleep(400);
            if(DividerCrashGuard.crashedSince(crashBefore,crashLog())){rememberDividerCrash();throw new IOException(DividerCrashGuard.CRASH_MESSAGE);}
        }
        int lt=task(leftPkg),rt=task(rightPkg);
        // Android can kill an app right after it is split; never report that as a layout.
        if(lt<0||rt<0)throw new IOException("Ứng dụng bị Android đóng ngay sau khi chia: "+(lt<0?leftPkg:"")+(lt<0&&rt<0?", ":"")+(rt<0?rightPkg:""));
        if(!nativeSplit)settleFreeform(lt,rt,l,r);
        return observedBounds(lt,rt,l,r,nativeSplit?2:32).put("usedMode",nativeSplit?"native":"freeform").put("leftTask",lt).put("rightTask",rt).put("leftStack",stackId(lt)).put("rightStack",stackId(rt)).put("leftMode",windowMode(lt)).put("rightMode",windowMode(rt));
    }
    private static JSONObject handle(JSONObject q)throws Exception{
        String op=q.getString("op");JSONObject out=new JSONObject();
        if(op.equals("PING")){
            out.put("sdk",android.os.Build.VERSION.SDK_INT).put("uid",Process.myUid()).put("manager",manager().getClass().getName());
            if(systemContext!=null)return out.put("transport","system").put("systemPermission",true);
            return out.put("bridgeVersion",9).put("context",securityContext()).put("commandIo",Process.myUid()==0?"file":"pipe");
        }
        if(op.equals("HOME")){if(systemContext!=null){systemContext.startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));return out;}exec("am","start","-a","android.intent.action.MAIN","-c","android.intent.category.HOME","-f","0x10000000");return out;}
        if(op.equals("FREEFORM_ENABLE")||op.equals("FREEFORM_RESTORE")){
            String[] keys={"enable_freeform_support","force_resizable_activities"};String[] before=new String[2];
            for(int n=0;n<2;n++)before[n]=systemContext==null?exec("settings","get","global",keys[n]).trim():android.provider.Settings.Global.getString(systemContext.getContentResolver(),keys[n]);
            for(int n=0;n<2;n++)if(before[n]==null)before[n]="null";
            try{for(int n=0;n<2;n++){String value=op.equals("FREEFORM_ENABLE")?"1":q.optString(keys[n],"null");writeSetting(keys[n],value);out.put(keys[n],before[n]);}}
            catch(Exception e){for(int n=0;n<2;n++)try{writeSetting(keys[n],before[n]);}catch(Exception ignored){}throw e;}
            return out;
        }
        if(op.equals("BOOT_CHECK")){
            // After a restart caused by the ROM divider: leave split and go home, nothing else.
            String log=crashLog();
            boolean crashed=log!=null&&DividerCrashGuard.crashedSince(q.optString("seen",""),log);
            if(crashed){
                rememberDividerCrash();
                try{dismissSplit();}catch(Exception ignored){}
                if(systemContext!=null)systemContext.startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
                else exec("am","start","-a","android.intent.action.MAIN","-c","android.intent.category.HOME","-f","0x10000000");
            }
            return out.put("verified",true).put("dismissed",crashed).put("log",log==null?q.optString("seen",""):log);
        }
        if(op.equals("RECOVER")){
            // R02 "Gỡ kẹt": leave split, go home, then let SystemUI redraw the status bar / divider.
            try{dismissSplit();}catch(Exception ignored){}
            if(systemContext!=null)systemContext.startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
            else exec("am","start","-a","android.intent.action.MAIN","-c","android.intent.category.HOME","-f","0x10000000");
            Thread.sleep(300);
            restartSystemUi();
            return out.put("verified",true).put("note","Đã thoát chia, về màn chính và dựng lại giao diện hệ thống");
        }
        String leftPkg=pkg(q,"left"),rightPkg=pkg(q,"right");if(leftPkg.equals(rightPkg))throw new IllegalArgumentException("Chọn hai ứng dụng khác nhau");
        String mode=mode(q);boolean preserveOemStacks=q.optBoolean("preserveOemStacks",false);
        if(op.equals("LAYOUT")||op.equals("RESIZE")||op.equals("FULLSCREEN")||op.equals("STOP")){
            String lc=component(q,"leftComponent",leftPkg),rc=component(q,"rightComponent",rightPkg);
            Rect l=rect(q,"leftBounds"),r=rect(q,"rightBounds");
            if(op.equals("FULLSCREEN")||op.equals("STOP")){
                String side=q.optString("side","left");if(!side.equals("left")&&!side.equals("right"))throw new IllegalArgumentException("Side");
                Rect expected=rect(q,"fullscreenBounds");
                if(op.equals("STOP"))try{if(preserveOemStacks)clearPrimary(leftPkg,rightPkg,true);else dismissSplit();}catch(Exception ignored){/* fullscreen below still clears both tasks */}
                int selected=controller(mode.equals("native"),leftPkg,rightPkg,lc,rc,l,r,preserveOemStacks).fullscreen(side.equals("right"),op.equals("STOP"));
                return selected<0?out.put("verified",true).put("note","Hai ứng dụng đã đóng"):observedFullscreen(selected,expected);
            }
            JSONObject result=com.tht.split.core.SplitModePolicy.run(mode,preserveOemStacks,
                nativeMode->pair(op,nativeMode,leftPkg,rightPkg,lc,rc,l,r,preserveOemStacks));
            if(mode.equals("auto")&&result.optString("usedMode").equals("native"))result.put("note",result.optString("note")+" • Cửa sổ tự do chưa chạy; đã dùng chia đôi hệ thống. Bật hỗ trợ cửa sổ tự do rồi khởi động lại màn.");
            return result;
        }
        throw new IllegalArgumentException("Lệnh không hỗ trợ");
    }
}


