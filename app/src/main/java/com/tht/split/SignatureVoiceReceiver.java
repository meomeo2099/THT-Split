package com.tht.split;
import android.content.*;
/** Android checks CONTROL_SPLIT signature permission before delivering the broadcast. */
public final class SignatureVoiceReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){if("com.tht.split.COMMAND".equals(i.getAction()))VoiceReceiver.dispatch(c,i);}
}
