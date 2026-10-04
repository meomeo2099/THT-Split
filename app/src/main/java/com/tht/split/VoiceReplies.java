package com.tht.split;

import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import org.json.JSONObject;
import java.util.Arrays;

/** Responses go only to a caller-owned PendingIntent, never an arbitrary package extra. */
public final class VoiceReplies {
    private VoiceReplies() {}
    public static boolean allowed(PendingIntent pi){return pi!=null&&Arrays.asList("com.tht.voice","com.tht.dashboard").contains(pi.getCreatorPackage());}
    public static boolean validId(String id){return id!=null&&id.matches("[A-Za-z0-9_-]{1,80}");}
    public static void send(Context c,Intent request,String status,String code,String message,JSONObject result){
        PendingIntent callback=request.getParcelableExtra("result");String id=request.getStringExtra("requestId");if(!allowed(callback)||!validId(id))return;
        Intent reply=new Intent().putExtra("apiVersion",2).putExtra("requestId",id).putExtra("command",request.getStringExtra("command")).putExtra("status",status).putExtra("code",code).putExtra("message",message);
        if(result!=null){reply.putExtra("verified",result.optBoolean("verified",false));reply.putExtra("transport",result.optString("transport",new Store(c).s("transport","adb")));reply.putExtra("sdk",result.optInt("sdk",android.os.Build.VERSION.SDK_INT));}
        try{callback.send(c,0,reply);}catch(PendingIntent.CanceledException ignored){/* timed out caller */}
    }
}
