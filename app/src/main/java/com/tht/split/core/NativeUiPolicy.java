package com.tht.split.core;

/** Avoid native task transitions while only hiding/showing the control overlay. */
public final class NativeUiPolicy {
    private NativeUiPolicy() {}
    public interface Resize { void apply(boolean toolbar) throws Exception; }
    public static boolean keepsLayout(boolean compatible,String mode){return compatible&&"native".equals(mode);}
    public static void visibility(boolean compatible,String mode,boolean shown,Resize resize)throws Exception{
        if(!keepsLayout(compatible,mode))resize.apply(shown);
    }
    public static boolean blocksSwap(boolean compatible,String mode){return keepsLayout(compatible,mode);}
}

