package com.tht.split;
import android.content.*;
import android.provider.Settings;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/** Small explicit broadcast contract; caller must know the user-shared API token. */
public final class VoiceReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent request){Store s=new Store(c);if(!s.b("voice_enabled",false))return;
        String token=request.getStringExtra("token");if(token==null||!MessageDigest.isEqual(s.token("voice_token").getBytes(StandardCharsets.US_ASCII),token.getBytes(StandardCharsets.US_ASCII)))return;
        String cmd=request.getStringExtra("command");if(cmd==null||!Arrays.asList("start","preset","ratio","swap","replace","fullscreen","restore","hide","show","stop","home").contains(cmd))return;
        if(!Settings.canDrawOverlays(c))return;
        Intent i=new Intent(c,SplitService.class).putExtra("command",cmd);
        // Copy only known typed extras, never forward arbitrary service intents.
        for(String k:new String[]{"left","right","side","package","presetId"}){String v=request.getStringExtra(k);if(v!=null&&v.length()<=256)i.putExtra(k,v);}
        if(request.hasExtra("ratio"))i.putExtra("ratio",request.getIntExtra("ratio",50));
        try{c.startForegroundService(i);}catch(RuntimeException e){android.util.Log.w("THTSplit","Voice service start denied",e);}
    }
}
