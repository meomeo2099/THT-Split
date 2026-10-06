package com.tht.split;

import android.app.*;
import android.content.*;
import android.graphics.Point;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import com.tht.split.core.LayoutMath;
import com.tht.split.core.TransportPolicy;
import org.json.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SplitService extends Service {
    private Store store,session;private SplitEngine engine;private WindowManager wm;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final ExecutorService policyIo=Executors.newSingleThreadExecutor();
    private final AtomicBoolean busy=new AtomicBoolean(false);
    private volatile boolean destroyed;
    private View toolbar,mini,divider,loading;private boolean visible=true,full=false,active=false;
    private final java.util.Map<Integer,Button> ratioViews=new java.util.HashMap<>();
    private String overlayKey="";private boolean miniFull;
    private final Runnable autoHide=()->send("hide");
    private final Runnable policyPoll=new Runnable(){public void run(){if(destroyed)return;main.postDelayed(this,60000);policyIo.execute(()->{com.tht.split.remote.RemoteControl r=new com.tht.split.remote.RemoteControl(SplitService.this);try{r.refresh(false);}catch(Exception ignored){}String denied=r.denial();if(!denied.isEmpty())main.post(()->{if(!destroyed){toast(denied);stopSelf();}});});}};
    @Override public void onCreate(){super.onCreate();store=new Store(this);store.set("session_busy",false);session=new Store(this,"tht_split_session");
        session.set("left",store.s("left",""));session.set("right",store.s("right",""));session.set("ratio",store.i("ratio",50));captureLayoutSettings();engine=new SplitEngine(this,session);wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);n.createNotificationChannel(new NotificationChannel("split","THT Split",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        startForeground(1,new Notification.Builder(this,"split").setSmallIcon(R.drawable.icon_foreground).setContentTitle("THT Split").setContentText("Chạm để mở ứng dụng / khôi phục cặp").setContentIntent(open).setOngoing(true).build());
        main.post(policyPoll);
    }
    public static void command(Context c,String cmd){c.startForegroundService(new Intent(c,SplitService.class).putExtra("command",cmd));}
    private void send(String cmd){command(this,cmd);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public int onStartCommand(Intent i,int flags,int id){if(i==null){stopSelf();return START_NOT_STICKY;}
        String cmd=i.getStringExtra("command");if(cmd==null)cmd="start";
        if(SettingsBackup.RESTORING.get()&&!com.tht.split.core.AccessRules.exitCommand(cmd)){VoiceReplies.send(this,i,"error","BUSY","Đang khôi phục cấu hình; thử lại sau",null);if(!active)stopSelf();return START_NOT_STICKY;}
        if(!cmd.equals("status")&&!Settings.canDrawOverlays(this)){VoiceReplies.send(this,i,"error","OVERLAY_REQUIRED","Cần quyền hiển thị nổi",null);toast("Cần cấp quyền hiển thị trên ứng dụng khác");stopSelf();return START_NOT_STICKY;}
        String transport=(cmd.equals("start")||cmd.equals("preset"))?store.s("transport","adb"):session.s("transport","adb");
        if(!TransportPolicy.allows(transport,cmd)){String message="Off không điều khiển được task đang chạy. Chọn ADB nội bộ để dùng chức năng này.";VoiceReplies.send(this,i,"error","UNSUPPORTED_OPERATION",message,null);toast(message);if(!active&&!busy.get())stopSelf();return START_NOT_STICKY;}
        if(!busy.compareAndSet(false,true)){VoiceReplies.send(this,i,"error","BUSY","Split đang xử lý lệnh khác; thử lại sau",null);toast("Đang xử lý, vui lòng đợi");return START_NOT_STICKY;}
        store.set("session_busy",true);final String action=cmd;main.removeCallbacks(autoHide);
        final boolean opening=java.util.Arrays.asList("start","restore","preset","swap","replace").contains(action);
        worker.execute(()->{
            PerformanceLog.Measure measure=new PerformanceLog.Measure(this,action,session.s("transport","adb"));
            String oldL=session.s("left",""),oldR=session.s("right","");int oldRatio=session.i("ratio",50),oldTop=session.i("top_px",72),oldBottom=session.i("bottom_px",112);String oldMode=session.s("mode","native"),oldTransport=session.s("transport","adb");
            try {
                new com.tht.split.remote.RemoteControl(this).require(action);
                if(opening){FutureTask<Void> prepare=new FutureTask<>(()->{if(destroyed)throw new IllegalStateException("Phiên đã dừng");showLoading();dismissConfiguration();return null;});main.post(prepare);prepare.get(5,TimeUnit.SECONDS);}
                measure.mark("prepare");
                boolean nextVisible=visible,nextFull=full,nextActive=active,stop=false;
                if(action.equals("status")){engine.status();measure.mark("backend");measure.success();VoiceReplies.send(this,i,"success","READY","Phương thức điều khiển sẵn sàng",engine.lastResult);main.post(()->{busy.set(false);store.set("session_busy",false);if(!active)stopSelf();else scheduleHide();});return;}
                if(action.equals("preset")){String pid=i.getStringExtra("presetId");JSONObject pair=null;JSONArray pairs=store.pairs();for(int k=0;k<pairs.length();k++){JSONObject p=pairs.optJSONObject(k);if(p!=null&&p.optString("id").equals(pid)){pair=p;break;}}
                    if(pair==null)throw new IllegalArgumentException("Không tìm thấy cặp đã lưu");session.set("left",pair.getString("left"));session.set("right",pair.getString("right"));session.set("ratio",LayoutMath.clamp(pair.optInt("ratio",50)));captureLayoutSettings();engine.layout(visible);nextActive=true;nextFull=false;
                }else if(action.equals("start")||action.equals("restore")){
                    captureLayoutSettings();
                    if(i.hasExtra("left"))session.set("left",i.getStringExtra("left"));if(i.hasExtra("right"))session.set("right",i.getStringExtra("right"));if(i.hasExtra("ratio"))session.set("ratio",LayoutMath.clamp(i.getIntExtra("ratio",50)));engine.layout(visible);nextActive=true;nextFull=false;
                }else if(action.equals("stop")){if(!session.s("left","").isEmpty()&&!session.s("right","").isEmpty())engine.stop();stop=true;nextActive=false;
                }else if(action.equals("home")){engine.home();nextActive=false;nextFull=false;
                }else {if(!active)throw new IllegalStateException("Chưa có cặp đang chạy; bấm Bắt đầu chia");
                    switch(action){
                        case "ratio":if(full)throw new IllegalStateException("Khôi phục cặp trước khi đổi tỷ lệ");session.set("ratio",LayoutMath.clamp(i.getIntExtra("ratio",50)));engine.resize(visible);break;
                        case "swap":session.set("left",oldR);session.set("right",oldL);engine.layout(visible);nextFull=false;break;
                        case "replace":String side=i.getStringExtra("side"),pkg=i.getStringExtra("package");if(!"left".equals(side)&&!"right".equals(side))throw new IllegalArgumentException("Chọn bên trái/phải");if(pkg==null)throw new IllegalArgumentException("Thiếu package");session.set(side,pkg);engine.layout(visible);nextFull=false;break;
                        case "fullscreen":String focus=i.getStringExtra("side");engine.fullscreen(focus==null?"left":focus);nextFull=true;nextVisible=false;break;
                        case "hide":nextVisible=false;if(!full&&engine.supportsTaskResize())engine.resize(false);break;
                        case "show":nextVisible=true;if(!full&&engine.supportsTaskResize())engine.resize(true);break;
                        default:throw new IllegalArgumentException("Lệnh không hợp lệ");
                    }
                }
                // Off cannot observe another app's first drawn frame. This is a bounded,
                // configurable settling interval, not a readiness assertion.
                measure.mark("backend");
                if(opening)Thread.sleep(Math.max(0,Math.min(3000,store.i("launch_wait_ms",0))));
                measure.mark("settle");measure.success();
                final boolean v=nextVisible,f=nextFull,a=nextActive,end=stop;
                main.post(()->{if(destroyed)return;visible=v;full=f;active=a;if(java.util.Arrays.asList("start","restore","preset","ratio","swap","replace").contains(action)){store.set("left",session.s("left",""));store.set("right",session.s("right",""));store.set("ratio",session.i("ratio",50));}busy.set(false);store.set("session_busy",false);store.set("session_active",active);store.set("session_full",full);try{if(end)stopSelf();else{render(action);scheduleHide();}if(end&&!engine.supportsTaskResize())toast("Đã dừng điều khiển Off. Cửa sổ app vẫn do Android quản lý.");boolean verified=engine.lastResult.optBoolean("verified",false);if((engine.lastResult.has("actualLeftBounds")||engine.lastResult.has("actualFullscreenBounds"))&&!verified){String note=engine.lastResult.optString("note","Android chưa giữ đúng vùng chia yêu cầu");toast(note);store.set("last_error",note);}if(action.equals("hide")||action.equals("show")){verified=true;try{engine.lastResult.put("verified",true);}catch(JSONException ignored){}}VoiceReplies.send(this,i,verified?"success":"requested",verified?"OK":"LAYOUT_UNVERIFIED",verified?"Đã thực hiện lệnh":engine.lastResult.optString("note","Đã gửi yêu cầu cửa sổ, chưa xác minh bố cục"),engine.lastResult);}catch(RuntimeException overlayError){clear();store.set("last_error","Không hiện được thanh điều khiển");VoiceReplies.send(this,i,"error","OVERLAY_FAILED","Không hiện được thanh điều khiển",null);stopSelf();}});
            }catch(Exception e){session.set("left",oldL);session.set("right",oldR);session.set("ratio",oldRatio);session.set("top_px",oldTop);session.set("bottom_px",oldBottom);session.set("mode",oldMode);session.set("transport",oldTransport);VoiceReplies.send(this,i,"error",e instanceof com.tht.split.remote.RemoteControl.AccessDenied?"ACCESS_BLOCKED":"OPERATION_FAILED",e.getMessage()==null?"Không thực hiện được lệnh":e.getMessage(),null);android.util.Log.e("THTSplit",action+" failed",e);
                main.post(()->{if(destroyed)return;busy.set(false);store.set("session_busy",false);toast("Không thực hiện được: "+e.getMessage());store.set("last_error",e.getClass().getSimpleName()+": "+e.getMessage());if(!active||e instanceof com.tht.split.remote.RemoteControl.AccessDenied)stopSelf();else{render();scheduleHide();}});
            }finally{measure.finish();}
        });return START_NOT_STICKY;
    }
    private void captureLayoutSettings(){session.set("transport",store.s("transport","adb"));session.set("mode",store.s("mode","native"));session.set("top_px",store.i("top_px",72));session.set("bottom_px",store.i("bottom_px",112));}
    private void scheduleHide(){main.removeCallbacks(autoHide);if(active&&visible&&store.b("auto_hide",true)&&!full)main.postDelayed(autoHide,store.i("hide_seconds",5)*1000L);}
    private WindowManager.LayoutParams params(int w,int h,int x,int y){WindowManager.LayoutParams p=new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,android.graphics.PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.LEFT;p.x=x;p.y=y;p.alpha=store.i("opacity",90)/100f;return p;}
    private void remove(View v){if(v!=null&&v.getParent()!=null)try{wm.removeView(v);}catch(IllegalArgumentException ignored){}}
    private void clear(){remove(toolbar);remove(mini);remove(divider);remove(loading);toolbar=mini=divider=loading=null;ratioViews.clear();overlayKey="";}
    private void render(String action){render();}
    private void dismissConfiguration(){
        // Only tasks owned by THT Split; foreground service and app data stay alive.
        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);
        for(ActivityManager.AppTask task:am.getAppTasks())task.finishAndRemoveTask();
    }
    private void showLoading(){
        clear();Point size=new Point();wm.getDefaultDisplay().getRealSize(size);
        int top=session.i("top_px",72),bottom=session.i("bottom_px",112);
        if(size.y-top-bottom<160)throw new IllegalArgumentException("Khoảng chừa quá lớn");
        LinearLayout panel=Ui.column(this);panel.setGravity(Gravity.CENTER);panel.setBackgroundColor(Ui.BG);
        panel.addView(new ProgressBar(this));TextView text=Ui.text(this,"Đang mở hai ứng dụng…",20,Ui.TEXT);Ui.pad(text,16);panel.addView(text);
        panel.setOnTouchListener((v,e)->true);loading=panel;
        WindowManager.LayoutParams p=params(size.x,size.y-top-bottom,0,top);p.alpha=1f;wm.addView(panel,p);
    }
    private Ui.Icon icon(LinearLayout row,String kind,int color,String label,Runnable click){Ui.Icon v=new Ui.Icon(this,kind,color);v.setContentDescription(label);int s=engine.toolbarPx()-Ui.dp(this,8);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(s,s);p.setMargins(Ui.dp(this,3),0,Ui.dp(this,3),0);row.addView(v,p);v.setOnClickListener(w->{scheduleHide();click.run();});return v;}
    private void attach(View view,WindowManager.LayoutParams params){
        if(view.getParent()==null)wm.addView(view,params);else wm.updateViewLayout(view,params);
    }
    private void render(){
        if(!active){clear();return;}Point size=new Point();wm.getDefaultDisplay().getRealSize(size);
        String key=size.x+":"+size.y+":"+getResources().getDisplayMetrics().density+":"+engine.toolbarPx()+":"+engine.supportsTaskResize()+":"+session.i("top_px",72)+":"+session.i("bottom_px",112)+":"+store.s("toolbar_side","right")+":"+store.s("mini_position","center")+":"+store.i("opacity",90);
        if(!key.equals(overlayKey)||loading!=null){clear();overlayKey=key;}
        LayoutMath g=engine.geometry(visible&&!full);boolean controls=engine.supportsTaskResize();
        if(visible&&!full){
            remove(mini);
            if(toolbar==null){
                boolean leftControls=store.s("toolbar_side","right").equals("left");LinearLayout bar=Ui.row(this);bar.setBackgroundColor(Ui.BG);bar.setPadding(Ui.dp(this,leftControls?40:8),Ui.dp(this,4),Ui.dp(this,leftControls?8:40),Ui.dp(this,4));
                TextView brand=Ui.text(this,controls?"THT Split":"THT • Off",16,Ui.CYAN);brand.setTypeface(null,android.graphics.Typeface.BOLD);bar.addView(brand,leftControls?new LinearLayout.LayoutParams(Ui.dp(this,104),-1):new LinearLayout.LayoutParams(0,-1,1));
                for(int ratio:new int[]{30,40,50,60,70}){
                    Button b=Ui.button(this,ratio+":"+(100-ratio),false);ratioViews.put(ratio,b);b.setTextSize(13);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(0,0,0,0);
                    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(Ui.dp(this,66),-1);p.setMargins(Ui.dp(this,3),0,Ui.dp(this,3),0);bar.addView(b,p);
                    b.setOnClickListener(v->startForegroundService(new Intent(this,SplitService.class).putExtra("command","ratio").putExtra("ratio",ratio)));
                }
                lock(icon(bar,"apps",Ui.TEXT,"Đổi ứng dụng",this::pickSide),controls);lock(icon(bar,"swap",Ui.TEXT,"Đổi bên",()->send("swap")),controls);lock(icon(bar,"full",Ui.TEXT,"Toàn màn hình",this::chooseFullscreen),controls);
                icon(bar,"home",Ui.TEXT,"Về Home",()->send("home"));icon(bar,"up",Ui.CYAN,"Ẩn thanh",()->send("hide"));icon(bar,"close",Ui.RED,controls?"Tắt chia":"Dừng điều khiển Off",()->send("stop"));
                if(leftControls)bar.addView(new View(this),new LinearLayout.LayoutParams(0,-1,1));toolbar=bar;
            }
            for(java.util.Map.Entry<Integer,Button> item:ratioViews.entrySet()){
                boolean selected=controls&&item.getKey()==session.i("ratio",50);Button b=item.getValue();
                b.setEnabled(controls&&!busy.get());b.setAlpha(controls?1f:0.35f);b.setTextColor(selected?Ui.BG:Ui.TEXT);b.setBackground(Ui.bg(selected?Ui.CYAN:Ui.CARD,Ui.dp(this,10)));
            }
            attach(toolbar,params(size.x,engine.toolbarPx(),0,session.i("top_px",72)));
        }else{
            remove(toolbar);
            if(mini==null||miniFull!=full){remove(mini);Ui.Icon open=new Ui.Icon(this,full?"apps":"down",Ui.CYAN);
                open.setContentDescription(full?"Khôi phục chia màn hình":"Hiện thanh điều khiển");open.setOnClickListener(v->send(full?"restore":"show"));mini=open;miniFull=full;
            }
            int w=Ui.dp(this,44),h=Ui.dp(this,32),x=size.x/2-w/2;String pos=store.s("mini_position","center");
            if(pos.equals("left"))x=Ui.dp(this,8);if(pos.equals("right"))x=size.x-w-Ui.dp(this,8);attach(mini,params(w,h,x,session.i("top_px",72)));
        }
        if(!full&&controls){
            int w=Ui.dp(this,48),h=Ui.dp(this,80);WindowManager.LayoutParams p=params(w,h,g.splitX-w/2,(g.left.top+g.left.bottom)/2-h/2);
            if(divider==null){
                Ui.Icon grip=new Ui.Icon(this,"grip",Ui.TEXT);grip.setContentDescription("Kéo để đổi tỷ lệ");divider=grip;
                grip.setOnTouchListener(new View.OnTouchListener(){
                    int startRatio,ratio;float downX;boolean dragging;
                    public boolean onTouch(View v,MotionEvent event){switch(event.getActionMasked()){
                        case MotionEvent.ACTION_DOWN:
                            if(busy.get())return false;main.removeCallbacks(autoHide);downX=event.getRawX();startRatio=session.i("ratio",50);ratio=startRatio;dragging=false;return true;
                        case MotionEvent.ACTION_MOVE:
                            float delta=event.getRawX()-downX;if(!dragging&&Math.abs(delta)<ViewConfiguration.get(SplitService.this).getScaledTouchSlop())return true;
                            dragging=true;ratio=LayoutMath.clamp(startRatio+Math.round(delta*100/size.x));WindowManager.LayoutParams current=(WindowManager.LayoutParams)v.getLayoutParams();
                            current.x=size.x*ratio/100-w/2;wm.updateViewLayout(v,current);return true;
                        case MotionEvent.ACTION_UP:
                            if(dragging&&ratio!=startRatio)startForegroundService(new Intent(SplitService.this,SplitService.class).putExtra("command","ratio").putExtra("ratio",ratio));
                            else{v.performClick();scheduleHide();}return true;
                        case MotionEvent.ACTION_CANCEL:render();scheduleHide();return true;
                    }return false;}
                });
            }
            attach(divider,p);
        }else remove(divider);
    }
    private void lock(View view,boolean enabled){view.setEnabled(enabled);view.setAlpha(enabled?1f:0.35f);}
    private void showDialog(AlertDialog d){if(d.getWindow()!=null)d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);d.show();if(d.getWindow()!=null)d.getWindow().setBackgroundDrawable(Ui.bg(Ui.CARD,Ui.dp(this,12)));}
    private void pickSide(){main.removeCallbacks(autoHide);AlertDialog d=Ui.dialog(this).setTitle("Đổi ứng dụng").setItems(new String[]{"Bên trái: "+AppPicker.label(this,session.s("left","")),"Bên phải: "+AppPicker.label(this,session.s("right",""))},(dialog,n)->AppPicker.show(this,"Chọn ứng dụng "+(n==0?"trái":"phải"),true,pkg->startForegroundService(new Intent(this,SplitService.class).putExtra("command","replace").putExtra("side",n==0?"left":"right").putExtra("package",pkg)))).setNegativeButton("Hủy",(x,y)->scheduleHide()).create();showDialog(d);}
    private void chooseFullscreen(){main.removeCallbacks(autoHide);AlertDialog d=Ui.dialog(this).setTitle("Toàn màn hình").setItems(new String[]{AppPicker.label(this,session.s("left","")),AppPicker.label(this,session.s("right",""))},(x,n)->startForegroundService(new Intent(this,SplitService.class).putExtra("command","fullscreen").putExtra("side",n==0?"left":"right"))).setNegativeButton("Hủy",(x,y)->scheduleHide()).create();showDialog(d);}
    @Override public void onDestroy(){destroyed=true;main.removeCallbacksAndMessages(null);clear();worker.shutdownNow();policyIo.shutdownNow();store.set("session_active",false);store.set("session_busy",false);super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
