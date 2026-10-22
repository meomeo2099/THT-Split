package com.tht.split;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/** EX2 base remains unchanged; recognize the reported IHU689G/Flyme combination. */
public final class OemUiCompat {
    private OemUiCompat() {}
    public static boolean detected(Context context){
        if(!"IHU689G".equalsIgnoreCase(Build.MODEL))return false;
        try{context.getPackageManager().getApplicationInfo("com.flyme.auto.launcher",0);return true;}
        catch(PackageManager.NameNotFoundException missing){return false;}
    }
    public static boolean enabled(Context context,Store store){return store.b("oem_ui_compat",detected(context));}
}
