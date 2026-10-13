package com.tht.split;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import java.security.MessageDigest;

/** Read-only evidence; the firmware decides whether the requested permission is granted. */
public final class PlatformDiagnostics {
    private PlatformDiagnostics() {}
    @SuppressWarnings("deprecation") private static String certificate(Context c,String pkg)throws Exception {
        Signature[] signatures=c.getPackageManager().getPackageInfo(pkg,PackageManager.GET_SIGNATURES).signatures;
        if(signatures==null||signatures.length!=1)throw new IllegalStateException("Không có một chứng chỉ duy nhất");
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(signatures[0].toByteArray());
        StringBuilder result=new StringBuilder();for(byte b:digest)result.append(String.format(java.util.Locale.US,"%02x",b&255));return result.toString();
    }
    public static String describe(Context c){
        String match;
        try{match=certificate(c,c.getPackageName()).equals(certificate(c,"android"))?"Trùng chứng chỉ hệ thống":"KHÔNG trùng chứng chỉ hệ thống";}
        catch(Exception e){match="Chưa đọc được chứng chỉ hệ thống";}
        return "Package: "+c.getPackageName()+"\nUID: "+android.os.Process.myUid()+"\n"+match+"\nMANAGE_ACTIVITY_STACKS: "+(com.tht.split.connection.Backend.systemGranted(c)?"ĐÃ CẤP":"CHƯA CẤP")+"\nChỉ kiểm tra trên máy hiện tại, không bảo đảm firmware khác.";
    }
}
