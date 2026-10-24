package com.tht.split.core;

/** Off has no authority to reconcile existing tasks; only open and overlay actions. */
public final class TransportPolicy {
    private TransportPolicy() {}
    public static String select(boolean systemOnly,String selected){
        if(systemOnly)return "system";
        if(!"system".equals(selected)&&!"adb".equals(selected)&&!"off".equals(selected))throw new IllegalArgumentException("Phương thức điều khiển không hợp lệ");
        return selected;
    }
    public static boolean allows(String transport,String command) {
        if(!"off".equals(transport))return true;
        switch(command) {
            case "ratio":case "swap":case "replace":case "fullscreen":case "restore":case "recover":return false;
            default:return true;
        }
    }
}

