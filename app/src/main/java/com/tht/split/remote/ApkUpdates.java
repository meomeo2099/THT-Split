package com.tht.split.remote;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.tht.split.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
import org.json.JSONObject;

public final class ApkUpdates {
    private ApkUpdates() {}
    /** Download never receives the device/admin credential, including after redirects. */
    public static synchronized File download(Context c,JSONObject latest)throws Exception{
        File file=new File(c.getCacheDir(),"verified-update.apk"),part=new File(c.getCacheDir(),"update.part");
        int expected=latest.getInt("versionCode");if(expected<=BuildConfig.VERSION_CODE)throw new IOException("Đã dùng bản mới nhất");
        String url=latest.getString("apkUrl"),hash=latest.getString("sha256");if(!hash.matches("[a-fA-F0-9]{64}"))throw new IOException("Thiếu checksum");
        MessageDigest digest=MessageDigest.getInstance("SHA-256");long size=0;
        try{
            HttpURLConnection con=null;
            for(int redirects=0;redirects<=5;redirects++){
                if(!RemoteControl.https(url))throw new IOException("Chỉ tải APK qua HTTPS");
                con=(HttpURLConnection)new URL(url).openConnection();con.setConnectTimeout(10000);con.setReadTimeout(15000);con.setInstanceFollowRedirects(false);
                int code=con.getResponseCode();if(code>=300&&code<400){String next=con.getHeaderField("Location");con.disconnect();if(next==null||redirects==5)throw new IOException("Chuyển hướng APK không hợp lệ");url=new URL(new URL(url),next).toString();continue;}
                if(code!=200){con.disconnect();throw new IOException("Tải APK lỗi HTTP "+code);}break;
            }
            if(con==null)throw new IOException("Không có kết nối");
            try(InputStream in=con.getInputStream();FileOutputStream out=new FileOutputStream(part)){
                byte[] buf=new byte[32768];int n;while((n=in.read(buf))!=-1){size+=n;if(size>67108864)throw new IOException("APK vượt 64MB");out.write(buf,0,n);digest.update(buf,0,n);}out.getFD().sync();
            }finally{con.disconnect();}
            StringBuilder actual=new StringBuilder();for(byte b:digest.digest())actual.append(String.format(Locale.US,"%02x",b&255));
            if(!hash.equalsIgnoreCase(actual.toString()))throw new IOException("Checksum APK không khớp");
            verify(c,part,expected);
            if(file.exists()&&!file.delete())throw new IOException("Không thay được tệp cập nhật");if(!part.renameTo(file))throw new IOException("Không lưu được APK");
            new Store(c,"tht_remote").set("download_version",expected);return file;
        }finally{if(part.exists())part.delete();}
    }
    @SuppressWarnings("deprecation") private static void verify(Context c,File f,int expected)throws Exception{
        PackageManager pm=c.getPackageManager();PackageInfo next=pm.getPackageArchiveInfo(f.getAbsolutePath(),PackageManager.GET_SIGNATURES);
        PackageInfo current=pm.getPackageInfo(c.getPackageName(),PackageManager.GET_SIGNATURES);
        if(next==null||!c.getPackageName().equals(next.packageName)||next.versionCode!=expected||expected<=current.versionCode)throw new IOException("Sai package hoặc phiên bản APK");
        if(next.applicationInfo==null||next.applicationInfo.minSdkVersion>Build.VERSION.SDK_INT)throw new IOException("APK không hỗ trợ phiên bản Android trên máy này");
        if(next.signatures==null||current.signatures==null||next.signatures.length==0||current.signatures.length==0||!new HashSet<>(Arrays.asList(next.signatures)).equals(new HashSet<>(Arrays.asList(current.signatures))))throw new IOException("APK khác chữ ký. Cần bản phát hành cùng khóa ký; không gỡ app tự động");
    }
    public static synchronized boolean install(Activity a)throws Exception{
        if(!a.getPackageManager().canRequestPackageInstalls()){a.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+a.getPackageName())));return false;}
        File f=new File(a.getCacheDir(),"verified-update.apk");int version=new Store(a,"tht_remote").i("download_version",0);verify(a,f,version);
        PackageInstaller installer=a.getPackageManager().getPackageInstaller();PackageInstaller.SessionParams params=new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);params.setAppPackageName(a.getPackageName());params.setSize(f.length());
        int id=installer.createSession(params);boolean committed=false;
        try(PackageInstaller.Session s=installer.openSession(id)){
            try(InputStream in=new FileInputStream(f);OutputStream out=s.openWrite("base.apk",0,f.length())){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)out.write(b,0,n);s.fsync(out);}
            Intent status=new Intent(a,InstallResult.class);int flags=PendingIntent.FLAG_UPDATE_CURRENT;if(Build.VERSION.SDK_INT>=31)flags|=PendingIntent.FLAG_MUTABLE;
            s.commit(PendingIntent.getBroadcast(a,id,status,flags).getIntentSender());committed=true;
        }finally{if(!committed)installer.abandonSession(id);}return true;
    }
}
