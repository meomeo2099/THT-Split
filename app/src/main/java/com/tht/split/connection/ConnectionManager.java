package com.tht.split.connection;

import android.content.Context;
import com.tht.split.Store;
import com.tht.split.bridge.ShellClient;
import com.tht.split.core.FailureText;
import org.json.JSONObject;
import java.io.IOException;

/** Serialized bootstrap avoids two app_process servers racing for the same port. */
public final class ConnectionManager {
    private static final Object LOCK=new Object();
    private ConnectionManager() {}
    public static JSONObject ensure(Context c)throws Exception {
        synchronized(LOCK){Store s=new Store(c);ShellClient shell=new ShellClient(s);
            try {JSONObject p=shell.ping();checkUid(p);checkVersion(p);s.set("connection_status","Bridge đã sẵn sàng");return p;}catch(java.net.ConnectException e){/* start only when no listener */}
            s.set("connection_status","Đang kết nối ADB nội bộ…");
            try(LocalAdb adb=new LocalAdb(c,s.s("adb_host","127.0.0.1"),s.i("adb_port",5555))){
                // Key and bridge secret never leave this device or appear in an API/log response.
                String output=adb.bootstrap(shell.startCommand()+" echo THT_BOOTSTRAP_SENT");
                if(!output.contains("THT_BOOTSTRAP_SENT"))throw new IOException("Không gửi được lệnh mở bridge");
            }catch(Exception e){String detail="Không kết nối được ADB nội bộ. "+FailureText.describe(e);s.set("connection_status",detail);s.set("last_error",detail);throw new IOException(detail,e);}
            Exception last=null;for(int i=0;i<12;i++){try{JSONObject p=shell.ping();checkUid(p);checkVersion(p);s.set("connection_status","ADB nội bộ → bridge sẵn sàng");return p;}catch(java.net.ConnectException e){last=e;Thread.sleep(250);}}
            throw new IOException("Bridge chưa chạy. Xem log hỗ trợ hoặc dùng lệnh ADB thủ công.",last);
        }
    }
    private static void checkVersion(JSONObject p)throws IOException{if(p.optInt("bridgeVersion",1)!=6)throw new IOException("Bridge phiên bản cũ còn chạy. Khởi động lại màn/LDPlayer rồi kết nối để nạp bridge v6; nếu dùng lệnh thủ công, chạy lại lệnh sau khởi động.");}
    private static void checkUid(JSONObject p)throws Exception{int uid=p.getInt("uid");if(uid!=2000&&uid!=0)throw new IOException("Bridge không có quyền shell");}
}
