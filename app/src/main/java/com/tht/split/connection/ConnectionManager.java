package com.tht.split.connection;

import android.content.Context;
import com.tht.split.Store;
import com.tht.split.PerformanceLog;
import com.tht.split.bridge.ShellClient;
import com.tht.split.core.FailureText;
import com.tht.split.core.ConnectionFailure;
import org.json.JSONObject;
import java.io.IOException;

/** Serialized bootstrap avoids two app_process servers racing for the same port. */
public final class ConnectionManager {
    private static final Object LOCK=new Object();
    private ConnectionManager() {}
    public static JSONObject ensure(Context c)throws Exception {
        synchronized(LOCK){Store s=new Store(c);ShellClient shell=new ShellClient(s);PerformanceLog.Measure measure=new PerformanceLog.Measure(c,"connect","adb");try{
            try {JSONObject p=shell.ping();checkUid(p);checkVersion(p);s.set("connection_status","Bridge đã sẵn sàng");measure.success();return p;}catch(java.net.ConnectException e){/* start only when no listener */}finally{measure.mark("ping");}
            s.set("connection_status","Đang kết nối ADB "+s.s("adb_host","127.0.0.1")+":"+s.i("adb_port",5555)+"; xác nhận khóa trên màn nếu được hỏi…");
            try(LocalAdb adb=new LocalAdb(c,s.s("adb_host","127.0.0.1"),s.i("adb_port",5555))){
                // Key and bridge secret never leave this device or appear in an API/log response.
                // Keep the remote shell alive while nohup/app_process starts, as in 0.3.4.
                // Polling after this shell closes does not provide the same startup grace.
                String output=adb.bootstrap(shell.startCommand()+" sleep 1; echo THT_BOOTSTRAP_SENT");
                if(!output.contains("THT_BOOTSTRAP_SENT"))throw new IOException("Không gửi được lệnh mở bridge");
            }catch(Exception e){Exception failure=e instanceof ConnectionFailure?e:new ConnectionFailure("Không mở được bridge qua ADB "+s.s("adb_host","127.0.0.1")+":"+s.i("adb_port",5555)+".",e);String detail=FailureText.describe(failure);s.set("connection_status",detail);s.set("last_error",detail);throw failure;}finally{measure.mark("bootstrap");}
            s.set("connection_status","Đã gửi lệnh ADB; đang chờ bridge khởi động…");
            // Restore the 0.3.4 sequence: 1s inside the shell, then ~3s of PING retries.
            Exception last=null;try{for(int i=0;i<12;i++){try{JSONObject p=shell.ping();checkUid(p);checkVersion(p);s.set("connection_status","ADB nội bộ → bridge sẵn sàng");measure.success();return p;}catch(java.net.ConnectException e){last=e;Thread.sleep(250);}}}finally{measure.mark("wait");}
            throw new ConnectionFailure("ADB đã nhận lệnh nhưng bridge chưa chạy ở 127.0.0.1:"+ShellClient.PORT+". Xem log hỗ trợ hoặc dùng lệnh bridge dự phòng.",last);
        }finally{measure.finish();}}
    }
    private static void checkVersion(JSONObject p)throws IOException{if(p.optInt("bridgeVersion",1)!=9)throw new IOException("Bridge phiên bản cũ còn chạy. Khởi động lại màn/LDPlayer một lần sau cập nhật rồi kết nối để nạp bridge v9; nếu dùng lệnh thủ công, lấy lại lệnh trong app mới.");}
    private static void checkUid(JSONObject p)throws Exception{int uid=p.getInt("uid");if(uid!=2000&&uid!=0)throw new IOException("Bridge không có quyền shell");}
}

