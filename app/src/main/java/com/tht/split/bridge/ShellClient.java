package com.tht.split.bridge;
import com.tht.split.Store;
import org.json.*;
import java.io.*;
import java.net.*;

public final class ShellClient {
    public static final int PORT=39871;
    private final Store store;
    public ShellClient(Store s){store=s;}
    public JSONObject call(JSONObject request)throws Exception {
        request.put("token",store.token("bridge_token"));
        try(Socket socket=new Socket()){
            socket.connect(new InetSocketAddress("127.0.0.1",PORT),1500);socket.setSoTimeout(request.optString("op").equals("PING")?2000:45000);
            DataOutputStream out=new DataOutputStream(socket.getOutputStream());out.writeUTF(request.toString());out.flush();
            JSONObject response=new JSONObject(new DataInputStream(socket.getInputStream()).readUTF());
            if(!response.optBoolean("ok"))throw new IOException(response.optString("error","Lỗi bridge"));return response;
        }catch(java.net.ConnectException e){throw e;}
    }
    public JSONObject ping()throws Exception{return call(new JSONObject().put("op","PING"));}
    public String startCommand(){return "CLASSPATH=$(pm path com.tht.split | head -n 1 | cut -d: -f2) nohup app_process /system/bin com.tht.split.bridge.ShellServer "+store.token("bridge_token")+" >/data/local/tmp/tht-split-bridge.log 2>&1 < /dev/null &";}
}
