package com.tht.split.core;

/** Pure pixel geometry shared by overlays and the shell backend. */
public final class LayoutMath {
    public static final class Box {
        public final int left, top, right, bottom;
        public Box(int l,int t,int r,int b) { left=l;top=t;right=r;bottom=b; }
        public int width(){return right-left;} public int height(){return bottom-top;}
        public int[] array(){return new int[]{left,top,right,bottom};}
    }
    public final Box left, right;
    public final int splitX,areaLeft,areaWidth;
    public LayoutMath(int width,int height,int top,int bottom,int toolbar,int divider,int ratio) {
        this(width,height,0,top,bottom,toolbar,divider,ratio);
    }
    public LayoutMath(int width,int height,int sideLeft,int top,int bottom,int toolbar,int divider,int ratio) {
        if(width<320 || height<240 || sideLeft<0 || sideLeft>width-320 || top<0 || bottom<0 || toolbar<0 || divider<0 || divider>40)
            throw new IllegalArgumentException("Kích thước màn hình không hợp lệ");
        int y=top+toolbar, end=height-bottom;
        if(end-y<160)throw new IllegalArgumentException("Khoảng chừa quá lớn");
        areaLeft=sideLeft;areaWidth=width-sideLeft;
        splitX=splitAt(ratio);
        left=new Box(sideLeft,y,splitX-divider/2,end);
        right=new Box(splitX+(divider-divider/2),y,width,end);
    }
    public int splitAt(int ratio){return areaLeft+areaWidth*clamp(ratio)/100;}
    public int draggedRatio(int startRatio,float deltaX){return clamp(startRatio+Math.round(deltaX*100/areaWidth));}
    public static int clamp(int ratio){return Math.max(30,Math.min(70,ratio));}
}

