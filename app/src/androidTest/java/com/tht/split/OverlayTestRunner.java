package com.tht.split;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/** Test APK only: exercises real overlay windows, never shipped as an app receiver. */
public final class OverlayTestRunner extends Instrumentation {
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){
        Bundle result=new Bundle();
        try{
            Store profile=new Store(getTargetContext(),"tht_ci_profile");
            profile.prefs.edit().clear().commit();
            check(profile.i("bottom_px",112)==0,"Coolray bottom default");
            profile.set("bottom_px",44);check(profile.i("bottom_px",112)==44,"Keep custom bottom");
            profile.prefs.edit().clear().commit();
            for(int[] origin:new int[][]{{0,0},{160,0},{160,24}})testWindow(origin[0],origin[1]);
            result.putString("stream","THT_OVERLAY_TEST_OK: real windows, simulated origin 0/160, move/reuse, profile defaults\n");
            finish(-1,result);
        }catch(Throwable error){result.putString("stream","THT_OVERLAY_TEST_FAILED: "+error+"\n");finish(0,result);}
    }
    private void testWindow(int originX,int originY)throws Exception{
        Context context=getTargetContext();
        WindowManager manager=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        OverlayPositioner positioner=new OverlayPositioner(manager,null);
        View[] view=new View[1];
        try{
            runOnMainSync(()->{
                view[0]=new SplitDividerView(context);
                // Models an OEM screen origin without changing the production placement code.
                view[0].setTranslationX(originX);view[0].setTranslationY(originY);
                WindowManager.LayoutParams p=new WindowManager.LayoutParams(48,400,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
                p.gravity=Gravity.TOP|Gravity.LEFT;p.x=820;p.y=72;
                positioner.place(view[0],p);
            });
            await(view[0],820,72);
            runOnMainSync(()->positioner.move(view[0],1016,72));
            await(view[0],1016,72);
            runOnMainSync(()->positioner.move(view[0],660,96));
            await(view[0],660,96);
        }finally{
            runOnMainSync(()->{if(view[0]!=null){positioner.forget(view[0]);if(view[0].getParent()!=null)manager.removeViewImmediate(view[0]);}});
        }
    }
    private void await(View view,int x,int y)throws Exception{
        int[] actual=new int[2];boolean[] ready=new boolean[1];
        for(int n=0;n<80;n++){
            runOnMainSync(()->{view.getLocationOnScreen(actual);ready[0]=view.getAlpha()==1&&Math.abs(actual[0]-x)<=1&&Math.abs(actual[1]-y)<=1;});
            if(ready[0])return;Thread.sleep(50);
        }
        throw new AssertionError("expected="+x+","+y+" actual="+actual[0]+","+actual[1]);
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
