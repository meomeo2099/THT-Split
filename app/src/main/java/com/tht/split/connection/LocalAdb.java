package com.tht.split.connection;

import android.content.Context;
import android.util.AtomicFile;
import android.util.Base64;
import com.tht.split.core.AdbWire;
import com.tht.split.core.AdbRsa;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

/** One short lived authenticated shell stream; no arbitrary shell API exposed to callers. */
public final class LocalAdb implements AutoCloseable {
    private final Socket socket=new Socket();private final RSAPrivateCrtKey key;
    private InputStream in;private OutputStream out;
    public LocalAdb(Context c,String host,int port)throws Exception {
        if(port<1||port>65535)throw new IOException("Cổng ADB từ 1 đến 65535");
        InetAddress address=InetAddress.getByName(host);
        if(!address.isLoopbackAddress()&&NetworkInterface.getByInetAddress(address)==null)throw new IOException("Chỉ cho phép địa chỉ của chính màn xe");
        key=loadKey(c);try{socket.connect(new InetSocketAddress(address,port),3500);socket.setSoTimeout(12000);in=socket.getInputStream();out=socket.getOutputStream();connect();}catch(Exception e){close();throw e;}
    }
    private static synchronized RSAPrivateCrtKey loadKey(Context c)throws Exception {
        AtomicFile file=new AtomicFile(new File(c.getNoBackupFilesDir(),"adb-private.pk8"));byte[] encoded;
        if(file.getBaseFile().exists()){encoded=file.readFully();}
        else {KeyPairGenerator gen=KeyPairGenerator.getInstance("RSA");gen.initialize(2048);encoded=gen.generateKeyPair().getPrivate().getEncoded();FileOutputStream output=null;try{output=file.startWrite();output.write(encoded);file.finishWrite(output);}catch(IOException e){if(output!=null)file.failWrite(output);throw e;}}
        return (RSAPrivateCrtKey)KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }
    private void send(int cmd,int a,int b,byte[] bytes)throws IOException{AdbWire.write(out,new AdbWire.Packet(cmd,a,b,bytes));}
    private void connect()throws Exception {
        send(AdbWire.CNXN,0x01000000,4096,"host::\0".getBytes(StandardCharsets.UTF_8));boolean signed=false,offered=false;
        long deadline=System.nanoTime()+TimeUnitSeconds(20);
        while(System.nanoTime()<deadline){AdbWire.Packet p=AdbWire.read(in);
            if(p.command==AdbWire.CNXN)return;
            if(p.command!=AdbWire.AUTH||p.arg0!=1)throw new IOException("Cổng không dùng ADB TCP RSA; chưa hỗ trợ ghép đôi ADB TLS");
            if(!signed){send(AdbWire.AUTH,2,0,AdbRsa.sign(key,p.data));signed=true;}
            else if(!offered){String pub=Base64.encodeToString(AdbRsa.publicKey(key),Base64.NO_WRAP)+" tht@split\0";send(AdbWire.AUTH,3,0,pub.getBytes(StandardCharsets.UTF_8));offered=true;}
            else throw new IOException("Chưa cho phép khóa ADB của THT Split. Xác nhận hộp thoại trên màn xe rồi thử lại.");
        }throw new IOException("Hết thời gian xác thực ADB");
    }
    private static long TimeUnitSeconds(int seconds){return seconds*1000000000L;}
    String bootstrap(String fixedCommand)throws Exception {
        send(AdbWire.OPEN,1,0,("shell:"+fixedCommand+"\0").getBytes(StandardCharsets.UTF_8));int remote=0;ByteArrayOutputStream bytes=new ByteArrayOutputStream();long deadline=System.nanoTime()+TimeUnitSeconds(20);
        while(System.nanoTime()<deadline){AdbWire.Packet p=AdbWire.read(in);
            if(p.arg1!=1)throw new IOException("ADB stream không khớp");
            if(p.command==AdbWire.OKAY){remote=p.arg0;}
            else if(p.command==AdbWire.WRTE){if(remote==0)remote=p.arg0;if(p.arg0!=remote)throw new IOException("ADB stream không khớp");if(bytes.size()+p.data.length>32768)throw new IOException("ADB output quá lớn");bytes.write(p.data);send(AdbWire.OKAY,1,remote,new byte[0]);}
            else if(p.command==AdbWire.CLSE){send(AdbWire.CLSE,1,p.arg0,new byte[0]);return bytes.toString("UTF-8");}
            else throw new IOException("ADB phản hồi không hợp lệ");
        }throw new IOException("Hết thời gian chạy ADB");
    }
    @Override public void close(){try{socket.close();}catch(IOException ignored){}}
}
