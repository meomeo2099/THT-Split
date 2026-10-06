package com.tht.split;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import com.tht.split.bridge.ShellClient;
import com.tht.split.connection.Backend;
import com.tht.split.core.LayoutMath;
import com.tht.split.remote.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public final class MainActivity extends Activity {
    private Store store;private LinearLayout root,body;private int screenWidth;private String page="home",section="layout";
    private final Handler refreshHandler=new Handler(Looper.getMainLooper());
    private boolean refreshing,downloading; private final Runnable refreshTick=new Runnable(){public void run(){refreshRemote(false,false);refreshHandler.postDelayed(this,60000);}};
    private final ExecutorService io=Executors.newSingleThreadExecutor();private String exportedLog;
    private final ExecutorService networkIo=Executors.newSingleThreadExecutor();
    private boolean connectionChecking;private AlertDialog connectionDialog;
    private final Runnable connectionTick=new Runnable(){public void run(){if(!connectionChecking)return;if(connectionDialog!=null&&connectionDialog.isShowing())connectionDialog.setMessage(store.s("connection_status","Đang kiểm tra kết nối…"));refreshHandler.postDelayed(this,500);}};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);store=new Store(this);if(saved!=null){page=saved.getString("page","home");section=saved.getString("section","layout");}}
    @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putString("page",page);b.putString("section",section);}
    @Override protected void onResume(){super.onResume();if(store!=null){render();refreshRemote(false,false);refreshHandler.postDelayed(refreshTick,60000);}}
    @Override protected void onPause(){refreshHandler.removeCallbacks(refreshTick);super.onPause();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private LinearLayout.LayoutParams lp(int width,int height){return new LinearLayout.LayoutParams(width,height);}
    private void space(LinearLayout l,int n){View v=new View(this);l.addView(v,lp(1,AppUi.dp(this,n)));}
    private TextView title(String s){TextView t=AppUi.text(this,s,22,AppUi.TEXT);t.setTypeface(null,Typeface.BOLD);return t;}
    private Button button(LinearLayout l,String s,boolean selected,Runnable click){Button b=AppUi.button(this,s,selected);b.setMinHeight(AppUi.dp(this,52));LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,AppUi.dp(this,4),0,AppUi.dp(this,4));l.addView(b,p);b.setOnClickListener(v->click.run());return b;}
    private boolean landscapeLayout(){return screenWidth>=900;}
    private void line(LinearLayout parent){View v=new View(this);v.setBackgroundColor(AppUi.LINE);parent.addView(v,lp(-1,AppUi.dp(this,1)));}
    private void scrollBody(LinearLayout parent){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);
        parent.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=AppUi.column(this);
        AppUi.pad(body,landscapeLayout()?24:16);scroll.addView(body);
    }
    private void navItem(LinearLayout nav,String label,String icon,boolean selected,boolean compact,Runnable action){
        LinearLayout item=AppUi.navigation(this,label,icon,selected,compact);
        LinearLayout.LayoutParams p=compact?new LinearLayout.LayoutParams(0,AppUi.dp(this,68),1):lp(-1,AppUi.dp(this,60));
        int gap=AppUi.dp(this,4);p.setMargins(gap,gap,gap,gap);nav.addView(item,p);item.setOnClickListener(v->action.run());
    }
    private void render(){
        screenWidth=getResources().getConfiguration().screenWidthDp;
        root=AppUi.column(this);root.setBackgroundColor(AppUi.BG);setContentView(root);
        LinearLayout header=AppUi.row(this);header.setPadding(AppUi.dp(this,16),0,AppUi.dp(this,16),0);
        View balance=new View(this);header.addView(balance,lp(AppUi.dp(this,48),1));
        LinearLayout brand=AppUi.row(this);brand.setGravity(Gravity.CENTER);
        header.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
        brand.addView(new AppUi.Icon(this,"brand",AppUi.CYAN),lp(AppUi.dp(this,54),AppUi.dp(this,36)));
        View divider=new View(this);divider.setBackgroundColor(AppUi.CYAN);LinearLayout.LayoutParams d=lp(AppUi.dp(this,2),AppUi.dp(this,28));d.setMargins(AppUi.dp(this,12),0,AppUi.dp(this,12),0);brand.addView(divider,d);
        brand.addView(title("THT Split"));AppUi.Icon gear=new AppUi.Icon(this,"gear",AppUi.TEXT);
        gear.setBackground(AppUi.surface(this,false));header.addView(gear,lp(AppUi.dp(this,48),AppUi.dp(this,48)));
        gear.setContentDescription("Cài đặt");gear.setOnClickListener(v->{page="settings";render();});root.addView(header,lp(-1,AppUi.dp(this,64)));line(root);
        LinearLayout content=landscapeLayout()?AppUi.row(this):AppUi.column(this);content.setGravity(Gravity.TOP);
        root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=landscapeLayout()?AppUi.column(this):AppUi.row(this);AppUi.pad(nav,8);
        content.addView(nav,lp(landscapeLayout()?AppUi.dp(this,184):-1,landscapeLayout()?-1:AppUi.dp(this,92)));
        navItem(nav,"Chia màn hình","split",page.equals("home"),!landscapeLayout(),()->{page="home";render();});
        navItem(nav,"Cặp đã lưu","bookmark",page.equals("pairs"),!landscapeLayout(),()->{page="pairs";render();});
        navItem(nav,"Cài đặt","gear",page.equals("settings"),!landscapeLayout(),()->{page="settings";render();});
        navItem(nav,"Ủng hộ","heart",page.equals("donate"),!landscapeLayout(),()->{page="donate";render();});
        LinearLayout area=AppUi.column(this);content.addView(area,landscapeLayout()?new LinearLayout.LayoutParams(0,-1,1):new LinearLayout.LayoutParams(-1,0,1));
        if(page.equals("settings"))settings(area);else{scrollBody(area);if(page.equals("donate"))donation();else if(page.equals("pairs"))pairs();else home();}
    }
    private void home(){
        String denied=new RemoteControl(this).denial();if(!denied.isEmpty()){note(denied);button(body,"Kiểm tra cập nhật",true,()->{page="settings";section="updates";render();refreshRemote(true,true);});}
        body.addView(title("Chọn hai ứng dụng"));space(body,16);
        LinearLayout cards=landscapeLayout()?AppUi.row(this):AppUi.column(this);body.addView(cards,lp(-1,-2));
        appCard(cards,"Ứng dụng bên trái","left");AppUi.Icon swap=new AppUi.Icon(this,"swap",AppUi.TEXT);swap.setBackground(AppUi.surface(this,false));
        LinearLayout.LayoutParams p=lp(AppUi.dp(this,56),AppUi.dp(this,56));p.setMargins(AppUi.dp(this,16),AppUi.dp(this,8),AppUi.dp(this,16),AppUi.dp(this,8));p.gravity=Gravity.CENTER;
        cards.addView(swap,p);swap.setContentDescription("Đổi bên hai ứng dụng");swap.setFocusable(true);swap.setOnClickListener(v->{String l=store.s("left","");store.set("left",store.s("right",""));store.set("right",l);render();});
        appCard(cards,"Ứng dụng bên phải","right");space(body,20);
        LinearLayout ratios=landscapeLayout()?AppUi.row(this):AppUi.column(this);body.addView(ratios,lp(-1,-2));
        TextView label=AppUi.text(this,"Tỷ lệ chia màn hình",16,AppUi.TEXT);ratios.addView(label,lp(landscapeLayout()?AppUi.dp(this,184):-1,AppUi.dp(this,48)));
        LinearLayout ratioArea=AppUi.column(this);ratios.addView(ratioArea,landscapeLayout()?new LinearLayout.LayoutParams(0,-2,1):lp(-1,-2));
        ratioRow(ratioArea,store.i("ratio",50),n->{store.set("ratio",n);render();});space(body,18);
        JSONArray saved=store.pairs();
        if(saved.length()>0){body.addView(AppUi.text(this,"Cặp đã lưu — mở nhanh",16,AppUi.MUTED));space(body,8);LinearLayout quick=landscapeLayout()?AppUi.row(this):AppUi.column(this);
            if(landscapeLayout()){HorizontalScrollView scroller=new HorizontalScrollView(this);scroller.setHorizontalScrollBarEnabled(false);scroller.addView(quick);body.addView(scroller,lp(-1,-2));}else body.addView(quick,lp(-1,-2));
            for(int n=0;n<Math.min(3,saved.length());n++){JSONObject pair=saved.optJSONObject(n);if(pair!=null){LinearLayout tile=AppUi.navigation(this,pair.optString("name"),"bookmark",false,false);LinearLayout.LayoutParams q=lp(landscapeLayout()?AppUi.dp(this,280):-1,AppUi.dp(this,56));q.setMargins(0,0,AppUi.dp(this,8),AppUi.dp(this,8));quick.addView(tile,q);tile.setOnClickListener(v->openPair(pair));}}}
        LinearLayout actions=landscapeLayout()?AppUi.row(this):AppUi.column(this);body.addView(actions,lp(-1,-2));
        Button check=AppUi.button(this,"Kiểm tra kết nối",false);actions.addView(check,landscapeLayout()?lp(AppUi.dp(this,190),AppUi.dp(this,52)):lp(-1,AppUi.dp(this,52)));check.setOnClickListener(v->checkConnection());
        if(landscapeLayout())actions.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));else space(actions,12);
        Button save=AppUi.button(this,"Lưu cặp",false);actions.addView(save,landscapeLayout()?lp(AppUi.dp(this,160),AppUi.dp(this,52)):lp(-1,AppUi.dp(this,52)));save.setOnClickListener(v->{if(validSelection())editPair(null);});
        if(landscapeLayout())actions.addView(new View(this),lp(AppUi.dp(this,12),1));else space(actions,12);
        Button start=AppUi.button(this,"Bắt đầu chia",true);actions.addView(start,landscapeLayout()?lp(AppUi.dp(this,224),AppUi.dp(this,52)):lp(-1,AppUi.dp(this,52)));start.setOnClickListener(v->{if(validSelection())startCurrent();});
        if(store.b("session_active",false)){space(body,12);if(!store.s("transport","adb").equals("off"))button(body,"Khôi phục cặp đang chạy",false,()->send("restore"));button(body,"Tắt chia màn hình",false,()->send("stop"));}
    }
    private void appCard(LinearLayout parent,String label,String side){
        LinearLayout card=AppUi.column(this);AppUi.pad(card,20);card.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,14)));
        parent.addView(card,landscapeLayout()?new LinearLayout.LayoutParams(0,-2,1):lp(-1,-2));
        TextView heading=AppUi.text(this,label,17,AppUi.TEXT);heading.setTypeface(null,Typeface.BOLD);card.addView(heading);space(card,12);
        String pkg=store.s(side,"");LinearLayout app=AppUi.row(this);ImageView icon=new ImageView(this);
        try{icon.setImageDrawable(getPackageManager().getApplicationIcon(pkg));}catch(Exception ignored){icon.setImageResource(R.mipmap.ic_launcher);}
        app.addView(icon,lp(AppUi.dp(this,64),AppUi.dp(this,64)));TextView name=AppUi.text(this,AppPicker.label(this,pkg),20,AppUi.TEXT);name.setMaxLines(2);name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams n=new LinearLayout.LayoutParams(0,-2,1);n.leftMargin=AppUi.dp(this,16);app.addView(name,n);card.addView(app);space(card,12);
        Button change=AppUi.button(this,"Đổi ứng dụng",false);card.addView(change,lp(-1,AppUi.dp(this,48)));change.setOnClickListener(v->AppPicker.show(this,label,false,p->{store.set(side,p);render();}));
    }
    private interface Ratio{void selected(int n);}
    private void ratioRow(LinearLayout parent,int value,Ratio changed){LinearLayout row=AppUi.row(this);parent.addView(row,lp(-1,AppUi.dp(this,50)));for(int n:new int[]{30,40,50,60,70}){Button b=AppUi.button(this,n+":"+(100-n),value==n);b.setSingleLine(true);b.setTextSize(15);b.setPadding(AppUi.dp(this,2),0,AppUi.dp(this,2),0);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,AppUi.dp(this,48),1);p.setMargins(AppUi.dp(this,3),0,AppUi.dp(this,3),0);row.addView(b,p);b.setOnClickListener(v->changed.selected(n));}}
    private boolean validSelection(){String l=store.s("left",""),r=store.s("right","");if(l.isEmpty()||r.isEmpty()||l.equals(r)){toast("Chọn hai ứng dụng khác nhau");return false;}if(getPackageManager().getLaunchIntentForPackage(l)==null||getPackageManager().getLaunchIntentForPackage(r)==null){toast("Một ứng dụng chưa cài hoặc không mở được");return false;}return true;}
    private boolean overlay(){if(Settings.canDrawOverlays(this))return true;AppUi.dialog(this).setTitle("Cấp quyền hiển thị nổi").setMessage("THT Split cần quyền này để hiện thanh điều khiển và nút kéo giữa.").setPositiveButton("Mở cài đặt",(d,n)->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())))).setNegativeButton("Để sau",null).show();return false;}
    private void send(String cmd){if(!overlay())return;try{SplitService.command(this,cmd);}catch(RuntimeException e){toast("Không khởi động được: "+e.getMessage());}}
    private boolean access(){String d=new RemoteControl(this).denial();if(d.isEmpty())return true;toast(d);page="settings";section="updates";render();refreshRemote(true,false);return false;}
    private void startCurrent(){if(!access()||!overlay())return;Intent i=new Intent(this,SplitService.class).putExtra("command","start").putExtra("left",store.s("left","")).putExtra("right",store.s("right","")).putExtra("ratio",store.i("ratio",50));try{startForegroundService(i);}catch(RuntimeException e){toast(e.getMessage());}}
    private void openPair(JSONObject p){if(!access()||!overlay())return;startForegroundService(new Intent(this,SplitService.class).putExtra("command","preset").putExtra("presetId",p.optString("id")));}
    private void pairApp(LinearLayout row,String pkg){
        LinearLayout app=AppUi.row(this);ImageView icon=new ImageView(this);try{icon.setImageDrawable(getPackageManager().getApplicationIcon(pkg));}catch(Exception ignored){icon.setImageResource(R.mipmap.ic_launcher);}
        app.addView(icon,lp(AppUi.dp(this,36),AppUi.dp(this,36)));TextView label=AppUi.text(this,AppPicker.label(this,pkg),16,AppUi.TEXT);label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams t=new LinearLayout.LayoutParams(0,-2,1);t.leftMargin=AppUi.dp(this,12);app.addView(label,t);row.addView(app,new LinearLayout.LayoutParams(0,-2,1));
    }
    private void pairs(){
        body.addView(title("Cặp đã lưu"));space(body,16);JSONArray pairs=store.pairs();
        if(pairs.length()==0){LinearLayout empty=AppUi.column(this);AppUi.pad(empty,24);empty.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,14)));body.addView(empty,lp(-1,-2));empty.addView(AppUi.text(this,"Chưa có cặp ứng dụng",20,AppUi.TEXT));space(empty,8);empty.addView(AppUi.text(this,"Chọn hai ứng dụng và tỷ lệ, rồi bấm Lưu cặp để mở nhanh lần sau.",16,AppUi.MUTED));space(empty,16);button(empty,"Chọn ứng dụng",true,()->{page="home";render();});return;}
        for(int n=0;n<pairs.length();n++){JSONObject pair=pairs.optJSONObject(n);if(pair==null)continue;LinearLayout card=AppUi.column(this);AppUi.pad(card,20);card.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,14)));body.addView(card,lp(-1,-2));
            LinearLayout top=AppUi.row(this);card.addView(top);TextView name=AppUi.text(this,pair.optString("name"),20,AppUi.TEXT);name.setTypeface(null,Typeface.BOLD);top.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            space(card,12);LinearLayout apps=AppUi.row(this);card.addView(apps,lp(-1,-2));pairApp(apps,pair.optString("left"));TextView plus=AppUi.text(this,"+",20,AppUi.MUTED);plus.setGravity(Gravity.CENTER);apps.addView(plus,lp(AppUi.dp(this,32),AppUi.dp(this,40)));pairApp(apps,pair.optString("right"));
            space(card,16);LinearLayout bottom=landscapeLayout()?AppUi.row(this):AppUi.column(this);card.addView(bottom,lp(-1,-2));TextView ratio=AppUi.text(this,"Tỷ lệ "+pair.optInt("ratio",50)+":"+(100-pair.optInt("ratio",50)),16,AppUi.CYAN);bottom.addView(ratio,landscapeLayout()?new LinearLayout.LayoutParams(0,-2,1):lp(-1,-2));if(!landscapeLayout())space(bottom,12);
            LinearLayout actions=AppUi.row(this);bottom.addView(actions,landscapeLayout()?lp(AppUi.dp(this,408),AppUi.dp(this,52)):lp(-1,AppUi.dp(this,52)));
            Button edit=AppUi.button(this,"Sửa cặp",false),delete=AppUi.danger(this,"Xóa cặp"),open=AppUi.button(this,"Mở cặp",true);
            Button[] buttons={edit,delete,open};for(int k=0;k<buttons.length;k++){Button b=buttons[k];b.setSingleLine(true);b.setTextSize(landscapeLayout()?16:14);b.setPadding(AppUi.dp(this,6),0,AppUi.dp(this,6),0);LinearLayout.LayoutParams action=new LinearLayout.LayoutParams(0,-1,1);if(k>0)action.leftMargin=AppUi.dp(this,8);actions.addView(b,action);}
            edit.setContentDescription("Sửa cặp "+pair.optString("name"));delete.setContentDescription("Xóa cặp "+pair.optString("name"));open.setContentDescription("Mở cặp "+pair.optString("name"));
            edit.setOnClickListener(v->editPair(pair));delete.setOnClickListener(v->confirmDelete(pair));open.setOnClickListener(v->openPair(pair));space(body,16);
        }
    }
    private void confirmDelete(JSONObject p){AlertDialog dialog=AppUi.dialog(this).setTitle("Xóa cặp đã lưu?").setMessage("Cặp: "+p.optString("name")+"\n\nChỉ xóa cặp khỏi danh sách. Hai ứng dụng vẫn được giữ trên máy.").setPositiveButton("Xóa cặp",(d,n)->{store.deletePair(p.optString("id"));render();}).setNegativeButton("Hủy",null).create();dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(AppUi.RED));dialog.show();}
    private void editPair(JSONObject original){LinearLayout form=AppUi.column(this);AppUi.pad(form,16);EditText name=AppUi.input(this);name.setTextColor(AppUi.TEXT);name.setHintTextColor(AppUi.MUTED);name.setSingleLine(true);name.setHint("Tên cặp");String[] pkgs={original==null?store.s("left",""):original.optString("left"),original==null?store.s("right",""):original.optString("right")};int[] ratio={original==null?store.i("ratio",50):original.optInt("ratio",50)};
        name.setText(original==null?AppPicker.label(this,pkgs[0])+" + "+AppPicker.label(this,pkgs[1]):original.optString("name"));form.addView(name);
        for(int n=0;n<2;n++){final int side=n;Button b=button(form,(n==0?"Trái: ":"Phải: ")+AppPicker.label(this,pkgs[n]),false,()->{});b.setOnClickListener(v->AppPicker.show(this,side==0?"Ứng dụng trái":"Ứng dụng phải",false,p->{pkgs[side]=p;b.setText((side==0?"Trái: ":"Phải: ")+AppPicker.label(this,p));}));}
        form.addView(AppUi.text(this,"Tỷ lệ",16,AppUi.MUTED));String[] values={"30:70","40:60","50:50","60:40","70:30"};Spinner ratios=AppUi.spinner(this,values);ratios.setSelection(Math.max(0,Math.min(4,(ratio[0]-30)/10)));form.addView(ratios);
        AlertDialog dialog=AppUi.dialog(this).setTitle(original==null?"Lưu cặp mới":"Sửa cặp").setView(form).setPositiveButton("Lưu",null).setNegativeButton("Hủy",null).create();dialog.setOnShowListener(d->{dialog.getWindow().setBackgroundDrawable(AppUi.bg(AppUi.CARD,AppUi.dp(this,12)));dialog.getButton(-1).setOnClickListener(v->{String nm=name.getText().toString().trim();if(nm.isEmpty()||nm.length()>80||pkgs[0].isEmpty()||pkgs[1].isEmpty()||pkgs[0].equals(pkgs[1])){toast("Tên tối đa 80 ký tự và hai app khác nhau");return;}try{JSONObject pair=new JSONObject().put("name",nm).put("left",pkgs[0]).put("right",pkgs[1]).put("ratio",30+ratios.getSelectedItemPosition()*10);if(original!=null)pair.put("id",original.getString("id"));store.savePair(pair);dialog.dismiss();render();}catch(JSONException e){toast(e.getMessage());}});});dialog.show();
    }
    private void settings(LinearLayout area){
        String[] keys={"layout","toolbar","voice","connection","updates"},labels={"Chia màn hình","Thanh điều khiển","THT Voice","Kết nối & quyền","Cập nhật"},icons={"split","sliders","voice","link","update"};
        LinearLayout frame=screenWidth>=900?AppUi.row(this):AppUi.column(this);frame.setGravity(Gravity.TOP);area.addView(frame,lp(-1,-1));
        LinearLayout tabs=screenWidth>=900?AppUi.column(this):AppUi.row(this);AppUi.pad(tabs,8);
        if(screenWidth>=900){LinearLayout.LayoutParams t=lp(AppUi.dp(this,224),-1);t.setMargins(AppUi.dp(this,8),AppUi.dp(this,16),0,AppUi.dp(this,16));frame.addView(tabs,t);tabs.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,12)));}
        else{HorizontalScrollView scroller=new HorizontalScrollView(this);scroller.setHorizontalScrollBarEnabled(false);scroller.addView(tabs);frame.addView(scroller,lp(-1,AppUi.dp(this,80)));}
        for(int n=0;n<keys.length;n++){String key=keys[n];LinearLayout tab=AppUi.navigation(this,labels[n],icons[n],section.equals(key),false);LinearLayout.LayoutParams t=lp(screenWidth>=900?-1:AppUi.dp(this,192),AppUi.dp(this,60));t.setMargins(0,0,AppUi.dp(this,4),AppUi.dp(this,6));tabs.addView(tab,t);tab.setOnClickListener(v->{section=key;render();});}
        LinearLayout panel=AppUi.column(this);frame.addView(panel,screenWidth>=900?new LinearLayout.LayoutParams(0,-1,1):new LinearLayout.LayoutParams(-1,0,1));scrollBody(panel);
        LinearLayout settingsCard=AppUi.column(this);settingsCard.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,14)));AppUi.pad(settingsCard,16);body.addView(settingsCard,lp(-1,-2));body=settingsCard;
        switch(section){case "toolbar":toolbarSettings();break;case "voice":voiceSettings();break;case "connection":connectionSettings();break;case "updates":updateSettings();break;default:layoutSettings();}
    }
    private void note(String s){TextView t=AppUi.text(this,s,15,AppUi.MUTED);AppUi.pad(t,8);body.addView(t,lp(-1,-2));}
    private void toggle(String label,String key,boolean def){
        Switch sw=new Switch(this);sw.setText(label);sw.setTextColor(AppUi.TEXT);sw.setTextSize(17);sw.setChecked(store.b(key,def));AppUi.pad(sw,10);sw.setMinHeight(AppUi.dp(this,56));
        android.content.res.ColorStateList colors=new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{AppUi.CYAN,AppUi.MUTED});sw.setThumbTintList(colors);sw.setTrackTintList(colors);
        body.addView(sw,lp(-1,-2));sw.setOnCheckedChangeListener((v,checked)->store.set(key,checked));
    }
    private LinearLayout settingRow(String label){
        LinearLayout row=screenWidth<600?AppUi.column(this):AppUi.row(this);AppUi.pad(row,8);row.setMinimumHeight(AppUi.dp(this,56));body.addView(row,lp(-1,-2));
        TextView text=AppUi.text(this,label,17,AppUi.TEXT);LinearLayout.LayoutParams t=screenWidth<600?lp(-1,-2):new LinearLayout.LayoutParams(0,-2,1);t.rightMargin=AppUi.dp(this,12);row.addView(text,t);if(screenWidth<600)space(row,8);return row;
    }
    private int selectorWidth(){return screenWidth>=1100?250:screenWidth>=900?200:150;}
    private LinearLayout.LayoutParams controlParams(){return lp(screenWidth<600?-1:AppUi.dp(this,selectorWidth()),-2);}
    private void choice(String label,String key,String[] values,String[] labels,String def){
        LinearLayout row=settingRow(label);Spinner spin=AppUi.spinner(this,labels);String current=store.s(key,def);for(int n=0;n<values.length;n++)if(values[n].equals(current))spin.setSelection(n);
        row.addView(spin,controlParams());spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> a){}public void onItemSelected(AdapterView<?> a,View v,int n,long id){store.set(key,values[n]);}});
    }
    private void intChoice(String label,String key,int[] values,String[] labels,int def){
        LinearLayout row=settingRow(label);Spinner spin=AppUi.spinner(this,labels);int val=store.i(key,def);for(int n=0;n<values.length;n++)if(values[n]==val)spin.setSelection(n);
        row.addView(spin,controlParams());spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> a){}public void onItemSelected(AdapterView<?> a,View v,int n,long id){store.set(key,values[n]);}});
    }
    private void number(String label,String key,int def,int max){
        LinearLayout row=settingRow(label);Button b=AppUi.button(this,store.i(key,def)+" px",false);row.addView(b,controlParams());
        b.setOnClickListener(v->{EditText input=AppUi.input(this);input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);input.setText(String.valueOf(store.i(key,def)));AppUi.dialog(this).setTitle(label).setView(input).setPositiveButton("Lưu",(d,n)->{try{int x=Integer.parseInt(input.getText().toString());if(x<0||x>max)throw new NumberFormatException();store.set(key,x);render();}catch(NumberFormatException e){toast("Giá trị từ 0 đến "+max);}}).setNegativeButton("Hủy",null).show();});
    }
    private void layoutSettings(){body.addView(title("Chia màn hình"));choice("Chế độ chia","mode",new String[]{"native","freeform"},new String[]{"Chia đôi (EX2)","Cửa sổ tự do"},"native");
        note("ADB nội bộ hoặc quyền hệ thống dùng chế độ đã chọn. Off luôn mở cửa sổ tự do, không xác minh bố cục trên firmware xe. App tự nhớ cặp và tỷ lệ lần trước; không tự mở app khi xe khởi động.");number("Chừa phía trên","top_px",72,400);number("Chừa điều hòa phía dưới","bottom_px",112,400);
        button(body,"Khôi phục khoảng chừa EX2",false,()->{store.set("top_px",72);store.set("bottom_px",112);render();});note("Thay đổi chế độ/khoảng chừa áp dụng khi Bắt đầu chia hoặc Khôi phục cặp.");}
    private void toolbarSettings(){
        body.addView(title("Thanh điều khiển"));space(body,12);
        choice("Vị trí nhóm nút","toolbar_side",new String[]{"left","right"},new String[]{"Bên trái","Bên phải"},"right");line(body);
        toggle("Tự ẩn thanh điều khiển","auto_hide",true);line(body);
        intChoice("Ẩn sau","hide_seconds",new int[]{3,5,8,10},new String[]{"3 giây","5 giây","8 giây","10 giây"},5);line(body);
        choice("Kích thước nút","button_size",new String[]{"small","large"},new String[]{"Nhỏ","Lớn"},"small");line(body);
        LinearLayout opacity=settingRow("Độ hiển thị: "+store.i("opacity",90)+"%");TextView alpha=(TextView)opacity.getChildAt(0);
        SeekBar slider=new SeekBar(this);slider.setProgressTintList(android.content.res.ColorStateList.valueOf(AppUi.CYAN));slider.setThumbTintList(android.content.res.ColorStateList.valueOf(AppUi.TEXT));slider.setMax(60);slider.setProgress(store.i("opacity",90)-40);
        slider.setMinimumHeight(AppUi.dp(this,48));opacity.addView(slider,controlParams());slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int value,boolean user){alpha.setText("Độ hiển thị: "+(40+value)+"%");if(user)store.set("opacity",40+value);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});line(body);
        choice("Vị trí nút mở lại","mini_position",new String[]{"center","left","right"},new String[]{"Giữa phía trên","Góc trái","Góc phải"},"center");line(body);
        intChoice("Chờ ổn định sau khi mở cặp","launch_wait_ms",new int[]{0,500,1500,3000},new String[]{"Không chờ","0,5 giây","1,5 giây","3 giây"},0);
        note("Ẩn thanh vẫn giữ hai app đang chạy. Nút X tắt chia và đưa app trái về toàn màn hình khi dùng ADB/quyền hệ thống. Với Off, X dừng thanh điều khiển và giữ cửa sổ hiện tại.");
        note("Màn chờ che quá trình mở hai app. Off không xác định được lúc app khác tải xong nội dung. Vị trí nhóm nút và giao diện thanh áp dụng khi hiện lại thanh hoặc mở cặp.");
    }
    private void voiceSettings(){body.addView(title("Kết nối THT Voice"));toggle("Cho phép nhận lệnh từ THT Voice","voice_enabled",false);note("THT Split chỉ nhận lệnh chia màn hình. Popup, micro, vị trí và thời gian tự tắt do THT Voice quản lý; không dành diện tích cố định.");note("API v2 có phản hồi. Voice cùng chữ ký có thể dùng SignatureVoiceReceiver không cần token; khác chữ ký dùng token được lưu qua reboot. Repo Voice cần tích hợp theo tài liệu.");button(body,"Xem / sao chép token kết nối",false,()->showSecret("voice_token","Token THT Voice"));button(body,"Đổi token Voice",false,()->AppUi.dialog(this).setTitle("Đổi token?").setMessage("THT Voice cần nhập token mới để tiếp tục điều khiển.").setPositiveButton("Đổi",(d,n)->{store.set("voice_token",Store.randomToken());toast("Đã đổi token");}).setNegativeButton("Hủy",null).show());}
    private void showSecret(String key,String title){String secret=store.token(key);TextView text=AppUi.text(this,secret,16,AppUi.TEXT);text.setTextIsSelectable(true);AppUi.pad(text,16);AppUi.dialog(this).setTitle(title).setView(text).setPositiveButton("Sao chép",(d,n)->copy(secret)).setNegativeButton("Đóng",null).show();}
    private void copy(String s){((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("THT Split",s));toast("Đã sao chép");}
    private void connectionSettings(){body.addView(title("Kết nối & quyền"));
        note("Quyền nổi: "+(Settings.canDrawOverlays(this)?"Đã cấp":"Chưa cấp")+" • Android "+Build.VERSION.RELEASE+" / API "+Build.VERSION.SDK_INT);
        String method=store.s("transport","adb");
        button(body,"Phương thức: "+methodLabel(method),true,this::chooseTransport);
        note("Trạng thái: "+store.s("connection_status","Chưa kiểm tra")+"\nQuyền hệ thống: "+(Backend.systemGranted(this)?"Đã cấp":"Chưa cấp"));
        button(body,"Cấp quyền hiển thị nổi",false,()->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))));
        button(body,"Kiểm tra / kết nối",true,this::checkConnection);
        if(method.equals("adb")){
            note("Tự kết nối ADB trên chính màn xe và mở bridge khi nhận lệnh. Lần đầu có thể cần xác nhận khóa ADB. Địa chỉ 127.0.0.1 không đổi khi đổi Wi-Fi. Không hỗ trợ ADB TLS/ghép đôi Android 11+ trong bản này.");
            button(body,"ADB: "+store.s("adb_host","127.0.0.1")+":"+store.i("adb_port",5555),false,this::adbEndpoint);
            toggle("Chuẩn bị ADB sau khởi động / cập nhật","adb_at_boot",true);
            button(body,"Lấy lệnh bridge thủ công dự phòng",false,()->{String cmd=new ShellClient(store).startCommand();TextView t=AppUi.text(this,cmd,15,AppUi.TEXT);t.setTextIsSelectable(true);AppUi.pad(t,16);AppUi.dialog(this).setTitle("Chạy adb shell rồi dán lệnh này").setView(t).setPositiveButton("Sao chép",(d,n)->copy(cmd)).setNegativeButton("Đóng",null).show();});
        }else if(method.equals("off"))note("Off mở bằng ActivityOptions, không kết nối ADB và không dùng bridge. Cần freeform bật sẵn. Khóa tỷ lệ, kéo, đổi bên, đổi app và toàn màn hình trên thanh công cụ. X dừng điều khiển, giữ cửa sổ hiện tại để tránh lần mở tiếp bị fullscreen. Android có thể bỏ qua bounds của task cũ; nếu cửa sổ từ phiên cũ đã sai, đóng chúng bằng nút của Android rồi mở cặp lại. Dùng ADB để điều khiển đầy đủ.");
        else note("Chỉ chạy khi Android cấp MANAGE_ACTIVITY_STACKS. Bản ký thông thường không tự có quyền này; không dùng shared UID trong bản cài thường.");
        button(body,"Bật hỗ trợ cửa sổ tự do",false,()->AppUi.dialog(this).setTitle("Bật freeform?").setMessage("Đổi enable_freeform_support và force_resizable_activities. Có thể cần reboot. Off không tự đổi được; dùng ADB nội bộ hoặc lệnh máy tính lần đầu.").setPositiveButton("Bật",(d,n)->freeform(true)).setNegativeButton("Hủy",null).show());
        button(body,"Khôi phục tùy chọn freeform",false,()->freeform(false));
    }
    private String methodLabel(String m){return m.equals("off")?"Off — mở trực tiếp":m.equals("system")?"Quyền hệ thống":"ADB nội bộ";}
    private void chooseTransport(){
        if(connectionChecking){toast("Đang kiểm tra kết nối, vui lòng đợi kết quả");return;}
        if(store.b("session_active",false)||store.b("session_busy",false)){toast("Tắt chia màn hiện tại trước khi chuyển phương thức");return;}
        String[] values={"adb","off","system"};AppUi.dialog(this).setTitle("Phương thức điều khiển").setItems(new String[]{"ADB nội bộ (mặc định)","Off — mở trực tiếp","Quyền hệ thống"},(d,n)->{
            if(values[n].equals("system")&&!Backend.systemGranted(this)){toast("Bản APK này chưa được cấp quyền hệ thống");return;}
            store.set("transport",values[n]);store.set("connection_status","Đã chọn "+methodLabel(values[n])+"; chưa kiểm tra");render();
        }).setNegativeButton("Hủy",null).show();
    }
    private void adbEndpoint(){
        if(connectionChecking){toast("Đang kiểm tra kết nối, vui lòng đợi kết quả");return;}
        if(store.b("session_active",false)||store.b("session_busy",false)){toast("Tắt chia trước khi đổi kết nối");return;}
        LinearLayout form=AppUi.column(this);AppUi.pad(form,16);EditText host=AppUi.input(this);host.setSingleLine(true);host.setHint("Địa chỉ chính màn xe");host.setText(store.s("adb_host","127.0.0.1"));form.addView(host);EditText port=AppUi.input(this);port.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);port.setText(String.valueOf(store.i("adb_port",5555)));form.addView(port);
        AlertDialog dialog=AppUi.dialog(this).setTitle("ADB nội bộ").setView(form).setPositiveButton("Lưu",null).setNegativeButton("Hủy",null).create();dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{try{int n=Integer.parseInt(port.getText().toString());String h=host.getText().toString().trim();if(n<1||n>65535||h.isEmpty()||h.length()>64)throw new IllegalArgumentException();store.set("adb_host",h);store.set("adb_port",n);store.set("connection_status","Đã lưu địa chỉ; chưa kiểm tra");dialog.dismiss();render();}catch(IllegalArgumentException e){toast("Nhập địa chỉ hợp lệ và cổng 1–65535");}}));dialog.show();
    }
    private void runIo(Callable<String> task){io.execute(()->{String result;try{result=task.call();}catch(Exception e){result="Không thành công: "+com.tht.split.core.FailureText.describe(e);store.set("last_error",result);android.util.Log.e("THTSplit",result);}final String msg=result;runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())AppUi.dialog(this).setTitle("THT Split").setMessage(msg).setPositiveButton("Đóng",null).show();});});}
    private void checkConnection(){
        if(connectionChecking){toast("Đang kiểm tra kết nối; không cần bấm lại");return;}
        connectionChecking=true;final String method=store.s("transport","adb");
        store.set("connection_status","Đang kiểm tra "+methodLabel(method)+"…");
        connectionDialog=AppUi.dialog(this).setTitle("Kiểm tra kết nối").setMessage(store.s("connection_status","")).setPositiveButton("Ẩn",null).create();connectionDialog.show();
        refreshHandler.post(connectionTick);
        io.execute(()->{String result;try{
            JSONObject p=new Backend(this,method).ping();result=methodLabel(method)+" sẵn sàng • UID "+p.getInt("uid")+" • API "+p.getInt("sdk")+(p.has("bridgeVersion")?" • bridge v"+p.getInt("bridgeVersion"):"")+"\nHãy mở cặp app để kiểm tra bố cục trên màn.";
        }catch(Exception e){result="Không thành công: "+com.tht.split.core.FailureText.describe(e);store.set("last_error",result);android.util.Log.e("THTSplit",result);}
            store.set("connection_status",result);final String message=result;
            runOnUiThread(()->{connectionChecking=false;refreshHandler.removeCallbacks(connectionTick);if(connectionDialog!=null){connectionDialog.dismiss();connectionDialog=null;}if(isFinishing()||isDestroyed())return;render();AppUi.dialog(this).setTitle("Kết quả kết nối").setMessage(message).setPositiveButton("Đóng",null).show();});
        });
    }
    private void freeform(boolean enable){runIo(()->{if(!enable&&!store.b("freeform_backup",false))return "Chưa có bản lưu tùy chọn cũ; không thay đổi.";JSONObject q=new JSONObject().put("op",enable?"FREEFORM_ENABLE":"FREEFORM_RESTORE");if(!enable)for(String key:new String[]{"enable_freeform_support","force_resizable_activities"})q.put(key,store.s("old_"+key,"null"));JSONObject out=new Backend(this,store.s("transport","adb")).call(q);if(enable&&!store.b("freeform_backup",false)){for(String key:new String[]{"enable_freeform_support","force_resizable_activities"})store.set("old_"+key,out.getString(key));store.set("freeform_backup",true);}if(!enable)store.set("freeform_backup",false);return enable?"Đã bật hỗ trợ freeform. Có thể cần reboot; ADB nội bộ tự thử kết nối lại khi dùng.":"Đã khôi phục tùy chọn cũ.";});}
    private void updateSettings(){
        body.addView(title("Phiên bản & cập nhật"));note("THT Split "+BuildConfig.VERSION_NAME);
        RemoteControl remote=new RemoteControl(this);note(remote.status());
        note("Không cần đăng ký hoặc nhập key. Cài xong có thể dùng ngay, kể cả khi chưa kết nối mạng.");
        button(body,"Kiểm tra cập nhật",true,()->refreshRemote(true,true));
        JSONObject latest=remote.policy().optJSONObject("latest");
        if(latest!=null&&latest.optInt("versionCode")>BuildConfig.VERSION_CODE){note("Có bản "+latest.optString("versionName")+(remote.policy().optInt("minimumVersionCode")>BuildConfig.VERSION_CODE?" • BẮT BUỘC CẬP NHẬT":""));note(latest.optString("notes"));button(body,"Tải và cài cập nhật",true,this::downloadUpdate);}
        if(new File(getCacheDir(),"verified-update.apk").exists())button(body,"Tiếp tục cài APK đã tải",false,this::installDownloaded);
        if(!remote.lastError().isEmpty())note(remote.lastError());
        button(body,"Xuất log hỗ trợ",false,this::exportLog);String error=store.s("last_error","");if(!error.isEmpty())note("Lỗi gần nhất: "+error);
    }
    private void refreshRemote(boolean force,boolean show){
        RemoteControl remote=new RemoteControl(this);if(!remote.configured()){if(show)toast(remote.status());return;}if(refreshing)return;refreshing=true;final String before=remote.policy().toString()+remote.status()+remote.lastError();
        networkIo.execute(()->{String result;try{remote.refresh(force);result=remote.status();}catch(Exception e){result=remote.lastError()+"\n"+remote.status();}final String message=result;
            runOnUiThread(()->{refreshing=false;if(isFinishing()||isDestroyed())return;if(show||!before.equals(remote.policy().toString()+remote.status()+remote.lastError()))render();if(show)AppUi.dialog(this).setTitle("Cập nhật ứng dụng").setMessage(message).setPositiveButton("Đóng",null).show();});});
    }
    private void downloadUpdate(){if(downloading){toast("Đang tải, vui lòng đợi");return;}downloading=true;toast("Đang tải APK…");networkIo.execute(()->{try{
        RemoteControl r=new RemoteControl(this);r.refresh(true);JSONObject latest=r.policy().getJSONObject("latest");ApkUpdates.download(this,latest);
        runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()){render();AppUi.dialog(this).setTitle("Đã kiểm tra APK").setMessage("Cài bản cập nhật "+latest.optString("versionName")+"?").setPositiveButton("Cài đặt",(d,n)->installDownloaded()).setNegativeButton("Để sau",null).show();}});
        }catch(Exception e){String msg=com.tht.split.core.FailureText.describe(e);runOnUiThread(()->{if(!isDestroyed())AppUi.dialog(this).setTitle("Không tải được cập nhật").setMessage(msg).setPositiveButton("Đóng",null).show();});}finally{runOnUiThread(()->downloading=false);}});
    }
    private void installDownloaded(){
        if(!getPackageManager().canRequestPackageInstalls()){try{startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName())));toast("Cho phép cài từ THT Split, quay lại rồi bấm Tiếp tục cài APK đã tải");}catch(ActivityNotFoundException e){toast("Firmware chưa có trang cấp quyền cài APK");}return;}
        io.execute(()->{try{ApkUpdates.install(this);}catch(Exception e){runOnUiThread(()->toast(com.tht.split.core.FailureText.describe(e)));}});
    }
    private void donation(){
        body.addView(title("Ủng hộ THT Split"));space(body,6);
        note("Cảm ơn bạn đã đồng hành và giúp THT Split ngày càng hoàn thiện.");space(body,12);
        boolean sideBySide=screenWidth>=900;
        LinearLayout content=sideBySide?AppUi.row(this):AppUi.column(this);content.setGravity(Gravity.TOP);body.addView(content,lp(-1,-2));
        LinearLayout bank=AppUi.column(this);AppUi.pad(bank,24);bank.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,16)));
        content.addView(bank,sideBySide?new LinearLayout.LayoutParams(0,-2,1):lp(-1,-2));
        LinearLayout heading=AppUi.row(this);bank.addView(heading);AppUi.Icon heart=new AppUi.Icon(this,"heart",AppUi.CYAN);
        heading.addView(heart,lp(AppUi.dp(this,40),AppUi.dp(this,40)));TextView bankName=AppUi.text(this,DonationProfile.BANK,22,AppUi.TEXT);bankName.setTypeface(null,Typeface.BOLD);
        LinearLayout.LayoutParams bankLabel=new LinearLayout.LayoutParams(0,-2,1);bankLabel.leftMargin=AppUi.dp(this,12);heading.addView(bankName,bankLabel);space(bank,20);line(bank);space(bank,20);
        bank.addView(AppUi.text(this,"SỐ TÀI KHOẢN",13,AppUi.MUTED));space(bank,6);TextView account=AppUi.text(this,DonationProfile.ACCOUNT,screenWidth>=900?32:26,AppUi.CYAN);account.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));account.setTextIsSelectable(true);bank.addView(account);space(bank,20);
        bank.addView(AppUi.text(this,"CHỦ TÀI KHOẢN",13,AppUi.MUTED));space(bank,6);TextView holder=AppUi.text(this,DonationProfile.HOLDER,20,AppUi.TEXT);holder.setTypeface(null,Typeface.BOLD);bank.addView(holder);space(bank,24);
        Button copy=AppUi.button(this,"Sao chép số tài khoản",true);bank.addView(copy,lp(-1,AppUi.dp(this,52)));copy.setOnClickListener(v->copy(DonationProfile.ACCOUNT));
        TextView voluntary=AppUi.text(this,"Ủng hộ hoàn toàn tự nguyện. Cảm ơn sự hỗ trợ của bạn!",15,AppUi.MUTED);voluntary.setLineSpacing(AppUi.dp(this,4),1);space(bank,16);bank.addView(voluntary);
        if(sideBySide)content.addView(new View(this),lp(AppUi.dp(this,24),1));else space(content,20);
        LinearLayout qr=AppUi.column(this);AppUi.pad(qr,24);qr.setGravity(Gravity.CENTER_HORIZONTAL);qr.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,16)));
        content.addView(qr,sideBySide?lp(AppUi.dp(this,348),-2):lp(-1,-2));TextView scan=AppUi.text(this,"Quét mã để ủng hộ",19,AppUi.TEXT);scan.setTypeface(null,Typeface.BOLD);qr.addView(scan);space(qr,16);
        int size=sideBySide?280:Math.min(280,Math.max(160,screenWidth-96));ImageView image=new ImageView(this);
        image.setContentDescription("QR ủng hộ "+DonationProfile.BANK+", tài khoản "+DonationProfile.ACCOUNT);image.setBackgroundColor(android.graphics.Color.WHITE);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setPadding(AppUi.dp(this,12),AppUi.dp(this,12),AppUi.dp(this,12),AppUi.dp(this,12));qr.addView(image,lp(AppUi.dp(this,size),AppUi.dp(this,size)));
        try(InputStream in=getAssets().open(DonationProfile.QR_ASSET)){android.graphics.Bitmap bmp=android.graphics.BitmapFactory.decodeStream(in);if(bmp==null)throw new IOException("Không đọc được QR");image.setImageBitmap(bmp);}
        catch(IOException e){image.setVisibility(View.GONE);qr.addView(AppUi.text(this,"Không đọc được QR. Hãy sao chép số tài khoản.",15,AppUi.MUTED));}
        space(qr,12);TextView hint=AppUi.text(this,"Mở ứng dụng ngân hàng trên điện thoại\nđể quét mã QR.",14,AppUi.MUTED);hint.setGravity(Gravity.CENTER);qr.addView(hint);
        contacts();
    }
    private void contacts(){
        space(body,28);body.addView(title("Liên hệ THT"));space(body,12);
        boolean wide=landscapeLayout();LinearLayout content=wide?AppUi.row(this):AppUi.column(this);content.setGravity(Gravity.TOP);body.addView(content,lp(-1,-2));
        LinearLayout facebook=AppUi.column(this);AppUi.pad(facebook,24);facebook.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,16)));content.addView(facebook,wide?new LinearLayout.LayoutParams(0,-2,1):lp(-1,-2));
        facebook.addView(title("Facebook"));space(facebook,8);facebook.addView(AppUi.text(this,"Trao đổi và gửi phản hồi cho THT.",16,AppUi.MUTED));space(facebook,16);
        button(facebook,"Mở Facebook",true,()->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(ContactProfile.FACEBOOK_URL)));}catch(ActivityNotFoundException e){toast("Máy chưa có ứng dụng mở liên kết. Hãy sao chép link Facebook.");}});
        button(facebook,"Sao chép link Facebook",false,()->copy(ContactProfile.FACEBOOK_URL));
        if(wide)content.addView(new View(this),lp(AppUi.dp(this,24),1));else space(content,20);
        LinearLayout zalo=AppUi.column(this);AppUi.pad(zalo,24);zalo.setGravity(Gravity.CENTER_HORIZONTAL);zalo.setBackground(AppUi.bg(AppUi.CARD,AppUi.dp(this,16)));content.addView(zalo,wide?lp(AppUi.dp(this,348),-2):lp(-1,-2));
        TextView label=AppUi.text(this,"Kết nối qua Zalo",19,AppUi.TEXT);label.setTypeface(null,Typeface.BOLD);zalo.addView(label);space(zalo,16);
        int size=wide?280:Math.min(280,Math.max(160,screenWidth-96));ImageView qr=new ImageView(this);qr.setContentDescription("QR Zalo liên hệ THT");qr.setBackgroundColor(android.graphics.Color.WHITE);qr.setScaleType(ImageView.ScaleType.FIT_CENTER);qr.setPadding(AppUi.dp(this,12),AppUi.dp(this,12),AppUi.dp(this,12),AppUi.dp(this,12));zalo.addView(qr,lp(AppUi.dp(this,size),AppUi.dp(this,size)));
        try(InputStream in=getAssets().open(ContactProfile.ZALO_QR_ASSET)){android.graphics.Bitmap bmp=android.graphics.BitmapFactory.decodeStream(in);if(bmp==null)throw new IOException("Không đọc được QR Zalo");qr.setImageBitmap(bmp);}catch(IOException e){qr.setVisibility(View.GONE);zalo.addView(AppUi.text(this,"Không đọc được QR Zalo. Bạn có thể liên hệ qua Facebook.",15,AppUi.MUTED));}
        space(zalo,12);TextView hint=AppUi.text(this,"Mở Zalo trên điện thoại để quét mã.",14,AppUi.MUTED);hint.setGravity(Gravity.CENTER);zalo.addView(hint);
    }
    private void exportLog(){exportedLog="THT Split "+BuildConfig.VERSION_NAME+"\nAndroid="+Build.VERSION.RELEASE+" sdk="+Build.VERSION.SDK_INT+"\ntransport="+store.s("transport","adb")+"\nconnection="+store.s("connection_status","")+"\nmode="+store.s("mode","native")+" ratio="+store.i("ratio",50)+"\ntop="+store.i("top_px",72)+" bottom="+store.i("bottom_px",112)+"\nlast_layout="+store.s("last_layout","")+"\nlast_error="+store.s("last_error","")+"\nKhông chứa token kết nối.\n";Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/plain").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"THT-Split-log.txt");try{startActivityForResult(intent,31);}catch(ActivityNotFoundException e){toast("Máy không có trình chọn tệp; xem lỗi gần nhất trong app");}}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==31&&result==RESULT_OK&&data!=null&&data.getData()!=null&&exportedLog!=null)try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new IOException("Không mở được tệp");out.write(exportedLog.getBytes("UTF-8"));toast("Đã xuất log");}catch(Exception e){toast("Không lưu được log: "+e.getMessage());}}
    @Override protected void onDestroy(){refreshHandler.removeCallbacksAndMessages(null);if(connectionDialog!=null){connectionDialog.dismiss();connectionDialog=null;}io.shutdownNow();networkIo.shutdownNow();super.onDestroy();}
}
