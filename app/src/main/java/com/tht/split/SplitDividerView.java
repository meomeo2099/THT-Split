package com.tht.split;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** A thin visible divider inside a transparent, full-height touch strip. */
public final class SplitDividerView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean dragging;private int ratio=50;private float touchY,handleY=-1;
    public SplitDividerView(Context c){super(c);setContentDescription("Thanh chia: kéo ngang ở bất kỳ điểm nào để đổi tỷ lệ");}
    public void feedback(boolean active,int value,float y){dragging=active;ratio=value;touchY=y;invalidate();}
    public void handleAt(float y){handleY=y;invalidate();}
    @Override public boolean performClick(){return super.performClick();}
    private float dp(float value){return Ui.dp(getContext(),value);}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);float x=getWidth()/2f,h=getHeight(),halfHeight=Math.max(1,Math.min(dp(36),h/2f-dp(4)));
        float cy=handleY<0?h/2f:Math.max(halfHeight,Math.min(h-halfHeight,handleY));
        // No second rail, dots or arrows: extend the native-looking white pill at its center.
        paint.setStyle(Paint.Style.FILL);paint.setColor(Color.WHITE);
        canvas.drawRoundRect(x-dp(3),cy-halfHeight,x+dp(3),cy+halfHeight,dp(3),dp(3),paint);
        if(dragging){
            float y=Math.max(dp(16),Math.min(h-dp(16),touchY-dp(40)));paint.setStyle(Paint.Style.FILL);paint.setColor(Ui.CYAN);
            canvas.drawRoundRect(dp(2),y-dp(12),getWidth()-dp(2),y+dp(12),dp(6),dp(6),paint);
            paint.setColor(Ui.BG);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextSize(dp(11));paint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics font=paint.getFontMetrics();canvas.drawText(ratio+":"+(100-ratio),x,y-(font.ascent+font.descent)/2,paint);
        }
    }
}
