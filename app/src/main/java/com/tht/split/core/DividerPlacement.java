package com.tht.split.core;

/** Read-back seam alignment without changing the requested task geometry. */
public final class DividerPlacement {
    private DividerPlacement(){}
    public static int centerX(int fallback,int displayWidth,int maximumGap,LayoutMath.Box left,LayoutMath.Box right){
        if(left==null||right==null||displayWidth<=0||maximumGap<0)return fallback;
        if(left.left<0||right.right>displayWidth||left.right<=left.left||right.right<=right.left
            ||left.top<0||right.top<0||left.bottom<=left.top||right.bottom<=right.top)return fallback;
        int gap=right.left-left.right;if(gap<0||gap>maximumGap)return fallback;
        int center=left.right+gap/2;return center>0&&center<displayWidth?center:fallback;
    }
    public static int centerY(int displayHeight,int top,int bottom,boolean nativeMode){
        if(top<0||bottom<=top||bottom>displayHeight)throw new IllegalArgumentException("Vùng chia không hợp lệ");
        return nativeMode?Math.max(top,Math.min(bottom,displayHeight/2)):(top+(bottom-top)/2);
    }
}
