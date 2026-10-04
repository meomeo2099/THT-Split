package com.tht.split.bridge;

import android.graphics.Rect;
import android.os.Process;
import com.tht.split.core.TaskParser;
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
    private static final ExecutorService IO=Executors.newCachedThreadPool();
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
    private static void setMode(int id,int mode)throws Exception{invoke("setTaskWindowingMode",new Class<?>[]{int.class,int.class,boolean.class},id,mode,true);}
    private static void launch(String comp,int mode)throws Exception{String output=exec("am","start","-W","--user","current","--windowingMode",String.valueOf(mode),"-a","android.intent.action.MAIN","-c","android.intent.category.LAUNCHER","-f","0x10000000","-n",comp);if(output.contains("Error:")||output.contains("Exception"))throw new IOException(output);}
    private static String exec(String...command)throws Exception{
        java.lang.Process p=new ProcessBuilder(command).redirectErrorStream(true).start();
        Future<String> output=IO.submit(()->{ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] b=new byte[2048];int n;try(InputStream in=p.getInputStream()){while((n=in.read(b))!=-1){if(bytes.size()<24000)bytes.write(b,0,Math.min(n,24000-bytes.size()));}}return bytes.toString("UTF-8");});
        try{if(!p.waitFor(12,TimeUnit.SECONDS)){p.destroyForcibly();throw new IOException("Lệnh quá thời gian");}String s=output.get(2,TimeUnit.SECONDS);if(p.exitValue()!=0)throw new IOException(s);return s;}finally{output.cancel(true);}
    }
    private static void resize(int leftTask,int rightTask,String mode,Rect left,Rect right)throws Exception{
        if(mode.equals("native")){
            invoke("resizeDockedStack",new Class<?>[]{Rect.class,Rect.class,Rect.class,Rect.class,Rect.class},left,left,left,right,right);
        }else{
            invoke("resizeTask",new Class<?>[]{int.class,Rect.class,int.class},leftTask,left,0);
            invoke("resizeTask",new Class<?>[]{int.class,Rect.class,int.class},rightTask,right,0);
        }
    }
    private static String mode(JSONObject q){String m=q.optString("mode","native");if(!m.equals("native")&&!m.equals("freeform"))throw new IllegalArgumentException("Mode");return m;}
    private static JSONObject handle(JSONObject q)throws Exception{
        String op=q.getString("op");JSONObject out=new JSONObject();
        if(op.equals("PING"))return out.put("sdk",android.os.Build.VERSION.SDK_INT).put("uid",Process.myUid()).put("manager",manager().getClass().getName());
        if(op.equals("HOME")){exec("am","start","-a","android.intent.action.MAIN","-c","android.intent.category.HOME","-f","0x10000000");return out;}
        if(op.equals("FREEFORM_ENABLE")||op.equals("FREEFORM_RESTORE")){
            for(String k:new String[]{"enable_freeform_support","force_resizable_activities"}){
                if(op.equals("FREEFORM_ENABLE")){out.put(k,exec("settings","get","global",k).trim());exec("settings","put","global",k,"1");}
                else {String v=q.optString(k,"null");if(v.equals("null"))exec("settings","delete","global",k);else{if(!v.matches("[0-9]+"))throw new IllegalArgumentException("Setting");exec("settings","put","global",k,v);}}
            }return out;
        }
        String leftPkg=pkg(q,"left"),rightPkg=pkg(q,"right");if(leftPkg.equals(rightPkg))throw new IllegalArgumentException("Chọn hai ứng dụng khác nhau");
        String mode=mode(q);
        if(op.equals("LAYOUT")){
            String lc=component(q,"leftComponent",leftPkg),rc=component(q,"rightComponent",rightPkg);
            Rect l=rect(q,"leftBounds"),r=rect(q,"rightBounds");
            launch(lc,mode.equals("native")?3:5);int lt=awaitTask(leftPkg);setMode(lt,mode.equals("native")?3:5);
            launch(rc,mode.equals("native")?4:5);int rt=awaitTask(rightPkg);setMode(rt,mode.equals("native")?4:5);
            resize(lt,rt,mode,l,r);return out.put("leftTask",lt).put("rightTask",rt);
        }
        int lt=awaitTask(leftPkg),rt=awaitTask(rightPkg);
        if(op.equals("RESIZE")){resize(lt,rt,mode,rect(q,"leftBounds"),rect(q,"rightBounds"));return out;}
        if(op.equals("FULLSCREEN")||op.equals("STOP")){
            String side=q.optString("side","left");if(!side.equals("left")&&!side.equals("right"))throw new IllegalArgumentException("Side");
            // Collapse the primary first so the secondary becomes a regular window; then focus chosen task.
            if(mode.equals("native"))setMode(lt,1);
            else {setMode(lt,1);setMode(rt,1);}
            setMode(side.equals("right")?rt:lt,1);return out;
        }
        throw new IllegalArgumentException("Lệnh không hỗ trợ");
    }
}
