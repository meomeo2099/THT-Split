package com.tht.split.core;

/** Off has no authority to reconcile existing tasks; only open and overlay actions. */
public final class TransportPolicy {
    private TransportPolicy() {}
    public static boolean allows(String transport,String command) {
        if(!"off".equals(transport))return true;
        switch(command) {
            case "ratio":case "swap":case "replace":case "fullscreen":case "restore":return false;
            default:return true;
        }
    }
}
