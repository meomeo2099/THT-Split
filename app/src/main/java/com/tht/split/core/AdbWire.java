package com.tht.split.core;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;

/** AOSP ADB packet framing, independent of Android for protocol tests. */
public final class AdbWire {
    private AdbWire() {}
    public static int command(String text) { return ByteBuffer.wrap(text.getBytes(StandardCharsets.US_ASCII)).order(ByteOrder.LITTLE_ENDIAN).getInt(); }
    public static final int CNXN=command("CNXN"),AUTH=command("AUTH"),OPEN=command("OPEN"),OKAY=command("OKAY"),WRTE=command("WRTE"),CLSE=command("CLSE");
    public static final class Packet {
        public final int command,arg0,arg1; public final byte[] data;
        public Packet(int c,int a,int b,byte[] d){command=c;arg0=a;arg1=b;data=d;}
    }
    public static void write(OutputStream out,Packet p)throws IOException {
        if(p.data.length>4096)throw new IOException("ADB payload quá lớn");
        int sum=0;for(byte b:p.data)sum+=b&255;
        byte[] header=ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN).putInt(p.command).putInt(p.arg0).putInt(p.arg1).putInt(p.data.length).putInt(sum).putInt(p.command^(-1)).array();
        out.write(header);out.write(p.data);out.flush();
    }
    public static Packet read(InputStream in)throws IOException {
        byte[] header=new byte[24];new DataInputStream(in).readFully(header);
        ByteBuffer b=ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);int cmd=b.getInt(),a=b.getInt(),c=b.getInt(),len=b.getInt(),sum=b.getInt(),magic=b.getInt();
        if(magic!=(cmd^(-1))||len<0||len>1024*1024)throw new IOException("ADB packet không hợp lệ");
        byte[] data=new byte[len];new DataInputStream(in).readFully(data);int actual=0;for(byte v:data)actual+=v&255;
        // We negotiate the legacy protocol (1.0); checksums remain mandatory.
        if(sum!=actual)throw new IOException("ADB checksum không đúng");
        return new Packet(cmd,a,c,data);
    }
}
