package com.tht.split;

import android.app.*;
import android.content.*;
import android.graphics.Point;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import com.tht.split.core.LayoutMath;
import org.json.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SplitService extends Service {
    private Store store;private SplitEngine engine;private WindowManager wm;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final AtomicBoolean busy=new AtomicBoolean(false);
    private View toolbar,mini,divider;private boolean visible=true,full=false,active=false;
    private final Runnable autoHide=()->send("hide");
    @Override public void onCreate(){super.onCreate();store=new Store(this);engine=new SplitEngine(this);wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);n.createNotificationChannel(new NotificationChannel("split","THT Split",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        startForeground(1,new Notification.Builder(this,"split").setSmallIcon(R.drawable.icon_foreground).setContentTitle("THT Split").setContentText("Chạm để mở ứng dụng / khôi phục cặp").setContentIntent(open).setOngoing(true).build());
    }
    public static void command(Context c,String cmd){c.startForegroundService(new Intent(c,SplitService.class).putExtra("command",cmd));}
    private void send(String cmd){command(this,cmd);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public int onStartCommand(Intent i,int flags,int id){if(i==null){stopSelf();return START_NOT_STICKY;}
        String cmd=i.getStringExtra("command");if(cmd==null)cmd="start";
        if(!Settings.canDrawOverlays(this)){toast("Cần cấp quyền hiển thị trên ứng dụng khác");stopSelf();return START_NOT_STICKY;}
        if(!busy.compareAndSet(false,true)){toast("Đang xử lý, vui lòng đợi");return START_NOT_STICKY;}
        final String action=cmd;main.removeCallbacks(autoHide);
        worker.execute(()->{
            String oldL=store.s("left",""),oldR=store.s("right","");int oldRatio=store.i("ratio",50);
            try {
                boolean nextVisible=visible,nextFull=full,nextActive=active,stop=false;
                if(action.equals("preset")){String pid=i.getStringExtra("presetId");JSONObject pair=null;JSONArray pairs=store.pairs();for(int k=0;k<pairs.length();k++){JSONObject p=pairs.optJSONObject(k);if(p!=null&&p.optString("id").equals(pid)){pair=p;break;}}
                    if(pair==null)throw new IllegalArgumentException("Không tìm thấy cặp đã lưu");store.set("left",pair.getString("left"));store.set("right",pair.getString("right"));store.set("ratio",LayoutMath.clamp(pair.optInt("ratio",50)));engine.layout(visible);nextActive=true;nextFull=false;
                }else if(action.equals("start")||action.equals("restore")){
                    if(i.hasExtra("left"))store.set("left",i.getStringExtra("left"));if(i.hasExtra("right"))store.set("right",i.getStringExtra("right"));if(i.hasExtra("ratio"))store.set("ratio",LayoutMath.clamp(i.getIntExtra("ratio",50)));engine.layout(visible);nextActive=true;nextFull=false;
                }else if(action.equals("stop")){if(!store.s("left","").isEmpty()&&!store.s("right","").isEmpty())engine.stop();stop=true;nextActive=false;
                }else if(action.equals("home")){engine.home();nextActive=false;nextFull=false;
                }else {if(!active)throw new IllegalStateException("Chưa có cặp đang chạy; bấm Bắt đầu chia");
                    switch(action){
                        case "ratio":if(full)throw new IllegalStateException("Khôi phục cặp trước khi đổi tỷ lệ");store.set("ratio",LayoutMath.clamp(i.getIntExtra("ratio",50)));engine.resize(visible);break;
                        case "swap":store.set("left",oldR);store.set("right",oldL);engine.layout(visible);nextFull=false;break;
                        case "replace":String side=i.getStringExtra("side"),pkg=i.getStringExtra("package");if(!"left".equals(side)&&!"right".equals(side))throw new IllegalArgumentException("Chọn bên trái/phải");if(pkg==null)throw new IllegalArgumentException("Thiếu package");store.set(side,pkg);engine.layout(visible);nextFull=false;break;
                        case "fullscreen":String focus=i.getStringExtra("side");engine.fullscreen(focus==null?"left":focus);nextFull=true;nextVisible=false;break;
                        case "hide":nextVisible=false;if(!full)engine.resize(false);break;
                        case "show":nextVisible=true;if(!full)engine.resize(true);break;
                        default:throw new IllegalArgumentException("Lệnh không hợp lệ");
                    }
                }
                final boolean v=nextVisible,f=nextFull,a=nextActive,end=stop;
                main.post(()->{visible=v;full=f;active=a;busy.set(false);store.set("session_active",active);store.set("session_full",full);if(end)stopSelf();else{render();scheduleHide();}});
            }catch(Exception e){store.set("left",oldL);store.set("right",oldR);store.set("ratio",oldRatio);android.util.Log.e("THTSplit",action+" failed",e);
                main.post(()->{busy.set(false);toast("Không thực hiện được: "+e.getMessage());store.set("last_error",e.getClass().getSimpleName()+": "+e.getMessage());if(!active)stopSelf();else{render();scheduleHide();}});
            }
        });return START_NOT_STICKY;
    }
    private void scheduleHide(){main.removeCallbacks(autoHide);if(active&&visible&&store.b("auto_hide",true)&&!full)main.postDelayed(autoHide,store.i("hide_seconds",5)*1000L);}
    private WindowManager.LayoutParams params(int w,int h,int x,int y){WindowManager.LayoutParams p=new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,android.graphics.PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.LEFT;p.x=x;p.y=y;p.alpha=store.i("opacity",90)/100f;return p;}
    private void remove(View v){if(v!=null)try{wm.removeView(v);}catch(IllegalArgumentException ignored){}}
    private void clear(){remove(toolbar);remove(mini);remove(divider);toolbar=mini=divider=null;}
    private Ui.Icon icon(LinearLayout row,String kind,int color,String label,Runnable click){Ui.Icon v=new Ui.Icon(this,kind,color);v.setContentDescription(label);int s=engine.toolbarPx()-Ui.dp(this,8);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(s,s);p.setMargins(Ui.dp(this,3),0,Ui.dp(this,3),0);row.addView(v,p);v.setOnClickListener(w->{scheduleHide();click.run();});return v;}
    private void render(){clear();if(!active)return;Point size=new Point();wm.getDefaultDisplay().getRealSize(size);LayoutMath g=engine.geometry(visible&&!full);
        if(visible&&!full){LinearLayout bar=Ui.row(this);bar.setBackgroundColor(Ui.BG);bar.setPadding(Ui.dp(this,8),Ui.dp(this,4),Ui.dp(this,8),Ui.dp(this,4));
            TextView brand=Ui.text(this,"THT Split",16,Ui.CYAN);brand.setTypeface(null,android.graphics.Typeface.BOLD);bar.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
            for(int ratio:new int[]{30,40,50,60,70}){Button b=Ui.button(this,ratio+":"+(100-ratio),ratio==store.i("ratio",50));b.setTextSize(13);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(0,0,0,0);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(Ui.dp(this,66),-1);p.setMargins(Ui.dp(this,3),0,Ui.dp(this,3),0);bar.addView(b,p);b.setOnClickListener(v->startForegroundService(new Intent(this,SplitService.class).putExtra("command","ratio").putExtra("ratio",ratio)));}
            icon(bar,"apps",Ui.TEXT,"Đổi ứng dụng",this::pickSide);icon(bar,"swap",Ui.TEXT,"Đổi bên",()->send("swap"));icon(bar,"full",Ui.TEXT,"Toàn màn hình",this::chooseFullscreen);icon(bar,"home",Ui.TEXT,"Về Home",()->send("home"));icon(bar,"up",Ui.CYAN,"Ẩn thanh",()->send("hide"));icon(bar,"close",Ui.RED,"Tắt chia",()->send("stop"));
            toolbar=bar;wm.addView(toolbar,params(size.x,engine.toolbarPx(),0,store.i("top_px",72)));
        }else{Ui.Icon open=new Ui.Icon(this,full?"apps":"down",Ui.CYAN);open.setContentDescription(full?"Khôi phục chia màn hình":"Hiện thanh điều khiển");int w=Ui.dp(this,44),h=Ui.dp(this,32),x=size.x/2-w/2;String pos=store.s("mini_position","center");if(pos.equals("left"))x=Ui.dp(this,8);if(pos.equals("right"))x=size.x-w-Ui.dp(this,8);
            open.setOnClickListener(v->send(full?"restore":"show"));mini=open;wm.addView(mini,params(w,h,x,store.i("top_px",72)));}
        if(!full){Ui.Icon grip=new Ui.Icon(this,"grip",Ui.TEXT);grip.setContentDescription("Kéo để đổi tỷ lệ");int w=Ui.dp(this,24),h=Ui.dp(this,56);WindowManager.LayoutParams p=params(w,h,g.splitX-w/2,(g.left.top+g.left.bottom)/2-h/2);divider=grip;wm.addView(divider,p);
            grip.setOnTouchListener(new View.OnTouchListener(){int ratio;public boolean onTouch(View v,android.view.MotionEvent event){switch(event.getActionMasked()){case MotionEvent.ACTION_DOWN:main.removeCallbacks(autoHide);return true;case MotionEvent.ACTION_MOVE:ratio=LayoutMath.clamp(Math.round(event.getRawX()*100/size.x));p.x=size.x*ratio/100-w/2;wm.updateViewLayout(v,p);return true;case MotionEvent.ACTION_UP:v.performClick();ratio=LayoutMath.clamp(Math.round(event.getRawX()*100/size.x));startForegroundService(new Intent(SplitService.this,SplitService.class).putExtra("command","ratio").putExtra("ratio",ratio));return true;case MotionEvent.ACTION_CANCEL:render();scheduleHide();return true;}return false;}});
        }
    }
    private void showDialog(AlertDialog d){if(d.getWindow()!=null)d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);d.show();if(d.getWindow()!=null)d.getWindow().setBackgroundDrawable(Ui.bg(Ui.CARD,Ui.dp(this,12)));}
    private void pickSide(){main.removeCallbacks(autoHide);AlertDialog d=new AlertDialog.Builder(this).setTitle("Đổi ứng dụng").setItems(new String[]{"Bên trái: "+AppPicker.label(this,store.s("left","")),"Bên phải: "+AppPicker.label(this,store.s("right",""))},(dialog,n)->AppPicker.show(this,"Chọn ứng dụng "+(n==0?"trái":"phải"),true,pkg->startForegroundService(new Intent(this,SplitService.class).putExtra("command","replace").putExtra("side",n==0?"left":"right").putExtra("package",pkg)))).setNegativeButton("Hủy",(x,y)->scheduleHide()).create();showDialog(d);}
    private void chooseFullscreen(){main.removeCallbacks(autoHide);AlertDialog d=new AlertDialog.Builder(this).setTitle("Toàn màn hình").setItems(new String[]{AppPicker.label(this,store.s("left","")),AppPicker.label(this,store.s("right",""))},(x,n)->startForegroundService(new Intent(this,SplitService.class).putExtra("command","fullscreen").putExtra("side",n==0?"left":"right"))).setNegativeButton("Hủy",(x,y)->scheduleHide()).create();showDialog(d);}
    @Override public void onDestroy(){main.removeCallbacksAndMessages(null);clear();worker.shutdownNow();store.set("session_active",false);super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
