package com.tht.split.connection;
import android.os.Build;
import java.io.IOException;
import org.lsposed.hiddenapibypass.HiddenApiBypass;
public final class HiddenAccess {
    private HiddenAccess() {}
    public static synchronized void allow(String... classes)throws IOException{
        if(Build.VERSION.SDK_INT<28)return;
        try{if(!HiddenApiBypass.addHiddenApiExemptions(classes))throw new IOException("Firmware không cho truy cập API cửa sổ");}
        catch(LinkageError|RuntimeException e){throw new IOException("Không khởi tạo được API cửa sổ trên firmware này",e);}
    }
}
