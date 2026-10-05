package com.tht.split;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

/** Styles for the configuration activity only. The running split overlay keeps Ui. */
public final class AppUi {
    public static final int BG=Ui.BG, CARD=Ui.CARD, LINE=Ui.LINE, CYAN=Ui.CYAN,
            TEXT=Ui.TEXT, MUTED=Ui.MUTED, RED=Ui.RED;
    private AppUi() {}
    public static int dp(Context c,float value){return Ui.dp(c,value);}
    public static LinearLayout row(Context c){return Ui.row(c);}
    public static LinearLayout column(Context c){return Ui.column(c);}
    public static void pad(View v,int value){Ui.pad(v,value);}
    public static TextView text(Context c,String value,int size,int color){return Ui.text(c,value,size,color);}
    public static android.app.AlertDialog.Builder dialog(Context c){return Ui.dialog(c);}
    public static EditText input(Context c){return Ui.input(c);}
    public static Spinner spinner(Context c,String[] labels){return Ui.spinner(c,labels);}
    public static GradientDrawable bg(int color,int radius){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{color,color==CARD?Color.rgb(12,27,42):color});
        d.setCornerRadius(radius);d.setStroke(1,LINE);return d;
    }
    public static Drawable surface(Context c,boolean selected){
        GradientDrawable d=bg(selected?Color.rgb(15,49,65):CARD,dp(c,12));
        if(selected)d.setStroke(dp(c,1),CYAN);
        return new RippleDrawable(ColorStateList.valueOf(0x3310CBE5),d,null);
    }
    public static Button button(Context c,String value,boolean primary){
        Button b=new Button(c);b.setText(value);b.setAllCaps(false);b.setTextSize(16);
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        b.setTextColor(primary?BG:TEXT);b.setMinHeight(dp(c,48));
        b.setMinimumWidth(0);b.setMinWidth(0);b.setPadding(dp(c,12),0,dp(c,12),0);
        b.setElevation(0);b.setStateListAnimator(null);
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                primary?new int[]{0xFF24D9ED,CYAN}:new int[]{0xFF152B3E,0xFF0C1B2A});
        d.setCornerRadius(dp(c,10));d.setStroke(dp(c,1),primary?CYAN:LINE);
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x3310CBE5),d,null));return b;
    }
    public static LinearLayout navigation(Context c,String label,String kind,boolean selected,boolean compact){
        LinearLayout row=compact?column(c):row(c);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(c,compact?4:12),dp(c,compact?4:8),dp(c,8),dp(c,compact?4:8));
        row.setBackground(surface(c,selected));row.setContentDescription(label);
        row.setFocusable(true);row.setClickable(true);
        Icon icon=new Icon(c,kind,selected?CYAN:TEXT);
        row.addView(icon,new LinearLayout.LayoutParams(dp(c,28),dp(c,28)));
        TextView text=text(c,label,compact?12:15,selected?CYAN:TEXT);
        text.setMaxLines(2);text.setEllipsize(android.text.TextUtils.TruncateAt.END);
        text.setTypeface(Typeface.create(selected?"sans-serif-medium":"sans-serif",Typeface.NORMAL));
        text.setGravity(compact?Gravity.CENTER:Gravity.CENTER_VERTICAL);
        if(compact){row.setGravity(Gravity.CENTER);row.addView(text,new LinearLayout.LayoutParams(-1,-2));}
        else{LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.leftMargin=dp(c,12);row.addView(text,p);}
        return row;
    }
    public static final class Icon extends View {
        private final String kind;private final int color;
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path path=new Path();
        public Icon(Context c,String kind,int color){super(c);this.kind=kind;this.color=color;setContentDescription(kind);}
        @Override public boolean performClick(){return super.performClick();}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);canvas.save();float s=Math.min(getWidth(),getHeight())/40f;
            canvas.translate((getWidth()-40*s)/2,(getHeight()-40*s)/2);canvas.scale(s,s);
            p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);path.reset();
            switch(kind){
                case "split":canvas.drawRoundRect(7,9,33,31,2,2,p);canvas.drawLine(20,9,20,31,p);break;
                case "bookmark":path.moveTo(12,7);path.lineTo(28,7);path.lineTo(28,33);path.lineTo(20,27);path.lineTo(12,33);path.close();canvas.drawPath(path,p);break;
                case "swap":canvas.drawLine(9,15,31,15,p);canvas.drawLine(9,25,31,25,p);path.moveTo(26,10);path.lineTo(31,15);path.lineTo(26,20);path.moveTo(14,20);path.lineTo(9,25);path.lineTo(14,30);canvas.drawPath(path,p);break;
                case "sliders":for(int y:new int[]{10,20,30})canvas.drawLine(7,y,33,y,p);canvas.drawCircle(15,10,3,p);canvas.drawCircle(25,20,3,p);canvas.drawCircle(15,30,3,p);break;
                case "voice":canvas.drawRoundRect(16,7,24,24,4,4,p);path.moveTo(11,18);path.lineTo(11,22);path.cubicTo(11,34,29,34,29,22);path.lineTo(29,18);canvas.drawPath(path,p);canvas.drawLine(20,31,20,35,p);break;
                case "link":path.moveTo(22,12);path.cubicTo(30,1,42,13,29,23);path.moveTo(18,28);path.cubicTo(10,39,-2,27,11,17);path.moveTo(14,26);path.lineTo(26,14);canvas.drawPath(path,p);break;
                case "update":canvas.drawArc(8,8,32,32,210,270,false,p);path.moveTo(6,15);path.lineTo(7,7);path.lineTo(15,9);canvas.drawPath(path,p);break;
                case "heart":path.moveTo(20,32);path.cubicTo(4,22,4,10,12,9);path.cubicTo(16,8,19,11,20,14);path.cubicTo(21,11,24,8,28,9);path.cubicTo(36,10,36,22,20,32);canvas.drawPath(path,p);break;
                case "gear":canvas.drawCircle(20,20,8,p);canvas.drawCircle(20,20,3,p);for(int a=0;a<360;a+=45){double n=Math.toRadians(a);canvas.drawLine(20+(float)Math.cos(n)*8,20+(float)Math.sin(n)*8,20+(float)Math.cos(n)*11,20+(float)Math.sin(n)*11,p);}break;
                default:p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD_ITALIC));p.setTextSize(24);p.setTextAlign(Paint.Align.CENTER);canvas.drawText("THT",20,28,p);
            }
            canvas.restore();
        }
    }
}
