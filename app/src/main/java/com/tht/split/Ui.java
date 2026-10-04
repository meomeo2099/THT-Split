package com.tht.split;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

public final class Ui {
    public static final int BG=Color.rgb(7,18,31),CARD=Color.rgb(17,34,51),LINE=Color.rgb(47,74,96),CYAN=Color.rgb(16,203,229),TEXT=Color.rgb(238,245,250),MUTED=Color.rgb(156,179,198),RED=Color.rgb(244,83,100);
    public static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    public static GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);d.setStroke(1,LINE);return d;}
    public static TextView text(Context c,String s,int size,int color){TextView t=new TextView(c);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    public static Button button(Context c,String s,boolean primary){Button b=new Button(c);b.setText(s);b.setAllCaps(false);b.setTextSize(16);b.setTextColor(primary?BG:TEXT);b.setBackground(bg(primary?CYAN:CARD,dp(c,10)));b.setMinHeight(dp(c,48));return b;}
    public static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    public static LinearLayout row(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    public static void pad(View v,int n){int p=dp(v.getContext(),n);v.setPadding(p,p,p,p);}
    public static final class Icon extends View {
        private final String kind;private final int color;private final Paint p=new Paint(3);private final Path path=new Path();
        private static final int[] GRID={12,22}, CORNERS={12,28}, DOTS={14,20,26};
        public Icon(Context c){this(c,"brand",CYAN);}
        @Override public boolean performClick(){return super.performClick();}
        public Icon(Context c,String k,int col){super(c);kind=k;color=col;setContentDescription(k);setBackground(bg(CARD,dp(c,9)));}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.save();float scale=Math.min(getWidth(),getHeight())/40f;canvas.translate((getWidth()-40*scale)/2,(getHeight()-40*scale)/2);canvas.scale(scale,scale);p.setColor(color);p.setStrokeWidth(2);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            Path a=path;a.reset();switch(kind){
                case "apps":for(int x:GRID)for(int y:GRID)canvas.drawRoundRect(x,y,x+6,y+6,1,1,p);break;
                case "swap":canvas.drawLine(11,16,29,16,p);canvas.drawLine(11,24,29,24,p);a.moveTo(25,12);a.lineTo(29,16);a.lineTo(25,20);a.moveTo(15,20);a.lineTo(11,24);a.lineTo(15,28);canvas.drawPath(a,p);break;
                case "full":for(int x:CORNERS)for(int y:CORNERS){canvas.drawLine(x,y,x+(x==12?5:-5),y,p);canvas.drawLine(x,y,x,y+(y==12?5:-5),p);}break;
                case "home":a.moveTo(10,19);a.lineTo(20,10);a.lineTo(30,19);a.moveTo(13,17);a.lineTo(13,29);a.lineTo(27,29);a.lineTo(27,17);a.moveTo(18,29);a.lineTo(18,22);a.lineTo(22,22);a.lineTo(22,29);canvas.drawPath(a,p);break;
                case "up":case "down":boolean up=kind.equals("up");a.moveTo(13,up?23:17);a.lineTo(20,up?16:24);a.lineTo(27,up?23:17);canvas.drawPath(a,p);break;
                case "close":canvas.drawLine(13,13,27,27,p);canvas.drawLine(27,13,13,27,p);break;
                case "grip":p.setStyle(Paint.Style.FILL);for(int y:DOTS)canvas.drawCircle(20,y,1.7f,p);break;
                case "gear":canvas.drawCircle(20,20,8,p);canvas.drawCircle(20,20,3,p);for(int d=0;d<360;d+=45){double n=Math.toRadians(d);canvas.drawLine(20+(float)Math.cos(n)*8,20+(float)Math.sin(n)*8,20+(float)Math.cos(n)*11,20+(float)Math.sin(n)*11,p);}break;
                default:p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(13);p.setTextAlign(Paint.Align.CENTER);canvas.drawText("THT",20,25,p);p.setStrokeWidth(2);canvas.drawLine(20,9,20,31,p);
            }canvas.restore();
        }
    }
}
