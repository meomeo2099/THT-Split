package com.tht.split;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import java.util.IdentityHashMap;

/** Main-thread placement in physical screen pixels, including OEM window-frame offsets. */
public final class OverlayPositioner {
    public interface Reporter { void report(String value); }
    private final WindowManager manager;
    private final Reporter reporter;
    private final IdentityHashMap<View,Entry> entries=new IdentityHashMap<>();
    public OverlayPositioner(WindowManager manager,Reporter reporter){this.manager=manager;this.reporter=reporter;}
    public void place(View view,WindowManager.LayoutParams params){
        Entry entry=entries.get(view);
        if(entry==null){entry=new Entry(view);entries.put(view,entry);view.getViewTreeObserver().addOnPreDrawListener(entry);}
        entry.x=params.x;entry.y=params.y;entry.attempts=0;
        params.x-=entry.originX;params.y-=entry.originY;
        boolean fresh=view.getParent()==null;
        if(fresh){view.setAlpha(0);manager.addView(view,params);}else manager.updateViewLayout(view,params);
    }
    public void move(View view,int screenX,int screenY){
        WindowManager.LayoutParams params=(WindowManager.LayoutParams)view.getLayoutParams();
        params.x=screenX;params.y=screenY;place(view,params);
    }
    public void forget(View view){
        Entry entry=entries.remove(view);
        if(entry!=null&&view.getViewTreeObserver().isAlive())view.getViewTreeObserver().removeOnPreDrawListener(entry);
    }
    private final class Entry implements ViewTreeObserver.OnPreDrawListener {
        final View view;int x,y,originX,originY,attempts;String last="";
        Entry(View view){this.view=view;}
        @Override public boolean onPreDraw(){
            if(entries.get(view)!=this||view.getParent()==null||!view.isLaidOut())return true;
            int[] actual=new int[2];view.getLocationOnScreen(actual);
            WindowManager.LayoutParams params=(WindowManager.LayoutParams)view.getLayoutParams();
            originX=actual[0]-params.x;originY=actual[1]-params.y;
            int dx=x-actual[0],dy=y-actual[1];
            if((Math.abs(dx)>1||Math.abs(dy)>1)&&attempts++<3){
                params.x+=dx;params.y+=dy;manager.updateViewLayout(view,params);
                return false;
            }
            view.setAlpha(1);
            String value=view.getContentDescription()+": expected="+x+","+y+" actual="+actual[0]+","+actual[1]+" origin="+originX+","+originY;
            if(!value.equals(last)){last=value;if(reporter!=null)reporter.report(value);}
            return true;
        }
    }
}
