package com.tht.split;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Indeterminate preparation UI. Never claims that another app has drawn its first frame. */
public final class SplitLoadingView extends FrameLayout {
    public SplitLoadingView(Context c,int width,int height){
        super(c);
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Ui.BG,0xFF102A3D,Ui.BG}));
        setContentDescription("THT Split đang chuẩn bị hai ứng dụng");setOnTouchListener((v,event)->true);
        boolean compact=height<Ui.dp(c,240);
        LinearLayout card=Ui.column(c);card.setGravity(Gravity.CENTER_HORIZONTAL);Ui.pad(card,compact?12:24);
        card.setBackground(Ui.bg(Ui.CARD,Ui.dp(c,20)));
        FrameLayout.LayoutParams frame=new FrameLayout.LayoutParams(Math.min(Ui.dp(c,520),Math.max(1,width-Ui.dp(c,48))),-2,Gravity.CENTER);
        addView(card,frame);
        if(!compact){
            TextView brand=label(c,"THT SPLIT",13,Ui.CYAN);brand.setLetterSpacing(0.14f);card.addView(brand);gap(card,12);
            card.addView(new Emblem(c),new LinearLayout.LayoutParams(Ui.dp(c,72),Ui.dp(c,48)));gap(card,14);
        }
        TextView title=label(c,"Đang mở hai ứng dụng",compact?18:22,Ui.TEXT);title.setTypeface(null,Typeface.BOLD);card.addView(title,new LinearLayout.LayoutParams(-1,-2));gap(card,8);
        if(!compact){card.addView(label(c,"Đang chuẩn bị bố cục chia màn hình…",14,Ui.MUTED),new LinearLayout.LayoutParams(-1,-2));gap(card,18);}
        ProgressBar progress=new ProgressBar(c,null,android.R.attr.progressBarStyleHorizontal);progress.setIndeterminate(true);
        progress.setIndeterminateTintList(ColorStateList.valueOf(Ui.CYAN));
        card.addView(progress,new LinearLayout.LayoutParams(Math.min(Ui.dp(c,240),Math.max(1,width-Ui.dp(c,96))),Ui.dp(c,6)));
    }
    private static TextView label(Context c,String value,int size,int color){TextView text=Ui.text(c,value,size,color);text.setGravity(Gravity.CENTER);return text;}
    private void gap(LinearLayout parent,int size){parent.addView(new View(getContext()),new LinearLayout.LayoutParams(1,Ui.dp(getContext(),size)));}
    private static final class Emblem extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        Emblem(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);float scale=Math.min(getWidth()/72f,getHeight()/48f);canvas.save();canvas.translate((getWidth()-72*scale)/2,(getHeight()-48*scale)/2);canvas.scale(scale,scale);
            paint.setStyle(Paint.Style.FILL);paint.setColor(0xFF183A50);canvas.drawRoundRect(3,5,32,43,6,6,paint);canvas.drawRoundRect(40,5,69,43,6,6,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);paint.setColor(Ui.LINE);canvas.drawRoundRect(3,5,32,43,6,6,paint);canvas.drawRoundRect(40,5,69,43,6,6,paint);
            paint.setStrokeWidth(3);paint.setColor(Ui.CYAN);paint.setStrokeCap(Paint.Cap.ROUND);canvas.drawLine(36,14,36,34,paint);canvas.restore();
        }
    }
}
