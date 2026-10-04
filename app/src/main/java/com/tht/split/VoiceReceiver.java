package com.tht.split;
import android.app.PendingIntent;
import android.content.*;
import android.provider.Settings;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/** Legacy token API remains usable even when apps have different signing certificates. */
public final class VoiceReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent request){
        if(!"com.tht.split.COMMAND".equals(request.getAction()))return;
        Store s=new Store(c);String token=request.getStringExtra("token");
        if(token==null||token.length()!=64||!MessageDigest.isEqual(s.token("voice_token").getBytes(StandardCharsets.US_ASCII),token.getBytes(StandardCharsets.US_ASCII)))return;
        dispatch(c,request);
    }
    static void dispatch(Context c,Intent request){
        try{
            String cmd=request.getStringExtra("command");
            if(cmd==null||!Arrays.asList("start","preset","ratio","swap","replace","fullscreen","restore","hide","show","stop","home","status").contains(cmd)){VoiceReplies.send(c,request,"error","INVALID_COMMAND","Lệnh không hỗ trợ",null);return;}
            if(request.hasExtra("requestId")||request.hasExtra("result")){PendingIntent pi=request.getParcelableExtra("result");if(!VoiceReplies.validId(request.getStringExtra("requestId"))||!VoiceReplies.allowed(pi))return;}
            if(!new Store(c).b("voice_enabled",false)){VoiceReplies.send(c,request,"error","API_DISABLED","Bật nhận lệnh trong Cài đặt THT Split",null);return;}
            if(!cmd.equals("status")&&!Settings.canDrawOverlays(c)){VoiceReplies.send(c,request,"error","OVERLAY_REQUIRED","Cần cấp quyền hiển thị nổi cho Split",null);return;}
            Intent i=new Intent(c,SplitService.class).putExtra("command",cmd);
            for(String k:new String[]{"left","right","side","package","presetId","requestId"}){String v=request.getStringExtra(k);if(v!=null){if(v.length()>256){VoiceReplies.send(c,request,"error","INVALID_ARGUMENT","Tham số quá dài",null);return;}i.putExtra(k,v);}}
            if(request.hasExtra("result"))i.putExtra("result",(PendingIntent)request.getParcelableExtra("result"));
            if(request.hasExtra("ratio"))i.putExtra("ratio",request.getIntExtra("ratio",50));
            c.startForegroundService(i);
        }catch(RuntimeException e){VoiceReplies.send(c,request,"error","SERVICE_DENIED","Không khởi động được dịch vụ Split",null);}
    }
}
