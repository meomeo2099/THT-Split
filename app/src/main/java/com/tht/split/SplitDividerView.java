package com.tht.split;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** A thin visible divider inside a transparent, full-height touch strip. */
public final class SplitDividerView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path arrows=new Path();
    private static final int[] DOTS={-7,0,7};
    private boolean dragging;private int ratio=50;private float touchY;
    public SplitDividerView(Context c){super(c);setContentDescription("Thanh chia: kéo ngang ở bất kỳ điểm nào để đổi tỷ lệ");}
    public void feedback(boolean active,int value,float y){dragging=active;ratio=value;touchY=y;invalidate();}
    @Override public boolean performClick(){return super.performClick();}
    private float dp(float value){return Ui.dp(getContext(),value);}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float x=getWidth()/2f,h=getHeight(),cy=h/2f;
        paint.setStyle(Paint.Style.FILL);paint.setColor(dragging?0xCC112B3C:0x99112233);
        canvas.drawRoundRect(x-dp(5),0,x+dp(5),h,dp(5),dp(5),paint);
        paint.setColor(dragging?Ui.TEXT:0x99EEF5FA);float half=dp(dragging?2:1);
        canvas.drawRoundRect(x-half,dp(4),x+half,h-dp(4),half,half,paint);
        // White handle and its touch strip share the same center; keep white while dragging.
        paint.setColor(Ui.TEXT);canvas.drawRoundRect(x-dp(5),cy-dp(36),x+dp(5),cy+dp(36),dp(5),dp(5),paint);
        paint.setColor(Ui.BG);for(int y:DOTS)canvas.drawCircle(x,cy+dp(y),dp(1.2f),paint);
        paint.setColor(dragging?Ui.TEXT:Ui.MUTED);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1.5f));paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
        arrows.reset();arrows.moveTo(x-dp(14),cy-dp(4));arrows.lineTo(x-dp(18),cy);arrows.lineTo(x-dp(14),cy+dp(4));
        arrows.moveTo(x+dp(14),cy-dp(4));arrows.lineTo(x+dp(18),cy);arrows.lineTo(x+dp(14),cy+dp(4));canvas.drawPath(arrows,paint);
        if(dragging){
            float y=Math.max(dp(16),Math.min(h-dp(16),touchY-dp(40)));paint.setStyle(Paint.Style.FILL);paint.setColor(Ui.CYAN);
            canvas.drawRoundRect(dp(2),y-dp(12),getWidth()-dp(2),y+dp(12),dp(6),dp(6),paint);
            paint.setColor(Ui.BG);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextSize(dp(11));paint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics font=paint.getFontMetrics();canvas.drawText(ratio+":"+(100-ratio),x,y-(font.ascent+font.descent)/2,paint);
        }
    }
}
