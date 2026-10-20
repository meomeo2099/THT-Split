package com.tht.split.core;

/** Configuration UI only. Never changes display density, task bounds or permissions. */
public final class ScreenProfile {
    private ScreenProfile() {}
    public static boolean coolray2025(String model,String product) {
        return "SX11-LA".equalsIgnoreCase(model)||"IHU730P".equalsIgnoreCase(product);
    }
    public static boolean shortLandscape(int widthDp,int heightDp) {
        return widthDp>=900&&heightDp>0&&heightDp<=720;
    }
    public static int navigationHeight(boolean shortLandscape,float fontScale) {
        return Math.max(shortLandscape?52:60,(int)Math.ceil(36*Math.max(1,fontScale))+16);
    }
}
