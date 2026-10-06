package com.tht.split.core;

import java.io.*;
import java.security.*;
import java.util.Locale;

public final class DownloadStream {
    private DownloadStream(){}
    public interface Progress {void update(long received,long total);}
    public static String copy(InputStream in,OutputStream out,long declared,long maximum,Progress progress)throws IOException,NoSuchAlgorithmException{
        if(declared>maximum)throw new IOException("APK vượt 64MB");MessageDigest digest=MessageDigest.getInstance("SHA-256");
        byte[] buffer=new byte[32768];long size=0;int n;progress.update(0,declared);
        while((n=in.read(buffer))!=-1){
            if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Đã dừng tải cập nhật");
            size+=n;if(size>maximum)throw new IOException("APK vượt 64MB");
            if(declared>=0&&size>declared)throw new IOException("Kích thước APK không khớp phản hồi tải");
            out.write(buffer,0,n);digest.update(buffer,0,n);progress.update(size,declared);
        }
        if(declared>=0&&size!=declared)throw new IOException("Tải APK chưa đủ dữ liệu. Kiểm tra mạng rồi thử lại");
        StringBuilder hash=new StringBuilder();for(byte b:digest.digest())hash.append(String.format(Locale.US,"%02x",b&255));return hash.toString();
    }
}
