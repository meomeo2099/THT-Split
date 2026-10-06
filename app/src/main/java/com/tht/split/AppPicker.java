package com.tht.split;

import android.app.AlertDialog;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.LruCache;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AppPicker {
    public interface Chosen {void select(String pkg);}
    private static final ExecutorService LOADERS=Executors.newFixedThreadPool(2);
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    // ConstantState avoids retaining an Activity or sharing mutable Drawables across rows.
    private static final LruCache<String,Drawable.ConstantState> ICONS=new LruCache<>(64);
    public static final class App {public String pkg,label,key,search;public ResolveInfo info;}
    public static List<App> installed(Context c){
        PackageManager pm=c.getPackageManager();Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        Map<String,App> map=new HashMap<>();
        for(ResolveInfo i:pm.queryIntentActivities(query,0)){
            String pkg=i.activityInfo.packageName;if(pkg.equals(c.getPackageName())||map.containsKey(pkg))continue;
            try{PackageInfo p=pm.getPackageInfo(pkg,0);App a=new App();a.pkg=pkg;a.label=i.loadLabel(pm).toString();a.info=i;
                a.key=pkg+":"+p.lastUpdateTime;a.search=(a.label+" "+pkg).toLowerCase(Locale.ROOT);map.put(pkg,a);
            }catch(PackageManager.NameNotFoundException ignored){/* Removed during enumeration. */}
        }
        List<App> apps=new ArrayList<>(map.values());apps.sort(Comparator.comparing(a->a.label.toLowerCase(Locale.ROOT)));return apps;
    }
    private static final class Row {ImageView icon;TextView name,pkg;}
    public static void show(Context c,String title,boolean overlay,Chosen chosen){
        Context appContext=c.getApplicationContext();AtomicBoolean alive=new AtomicBoolean(true);
        List<App> all=new ArrayList<>(),shown=new ArrayList<>();Set<String> pending=new HashSet<>();
        LinearLayout box=Ui.column(c);Ui.pad(box,12);EditText search=Ui.input(c);search.setSingleLine(true);search.setHint("Tìm ứng dụng");box.addView(search);
        TextView status=Ui.text(c,"Đang đọc danh sách ứng dụng…",15,Ui.MUTED);Ui.pad(status,8);box.addView(status);
        ListView list=new ListView(c);int height=Math.max(Ui.dp(c,96),Math.min(Ui.dp(c,300),Math.round(c.getResources().getDisplayMetrics().heightPixels*0.45f)-Ui.dp(c,48)));
        box.addView(list,new LinearLayout.LayoutParams(-1,height));
        android.widget.BaseAdapter adapter=new BaseAdapter(){
            public int getCount(){return shown.size();}public Object getItem(int i){return shown.get(i);}public long getItemId(int i){return i;}
            public View getView(int i,View old,ViewGroup parent){
                App a=shown.get(i);Row holder;LinearLayout row;
                if(old==null){row=Ui.row(c);Ui.pad(row,12);row.setMinimumHeight(Ui.dp(c,64));holder=new Row();holder.icon=new ImageView(c);
                    row.addView(holder.icon,new LinearLayout.LayoutParams(Ui.dp(c,36),Ui.dp(c,36)));LinearLayout texts=Ui.column(c);
                    LinearLayout.LayoutParams labels=new LinearLayout.LayoutParams(0,-2,1);labels.leftMargin=Ui.dp(c,12);row.addView(texts,labels);
                    holder.name=Ui.text(c,"",17,Ui.TEXT);holder.name.setMaxLines(2);holder.name.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    holder.pkg=Ui.text(c,"",13,Ui.MUTED);holder.pkg.setSingleLine(true);holder.pkg.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    texts.addView(holder.name);texts.addView(holder.pkg);row.setTag(holder);
                }else{row=(LinearLayout)old;holder=(Row)row.getTag();}
                holder.name.setText(a.label);holder.pkg.setText(a.pkg);holder.icon.setTag(a.key);
                Drawable.ConstantState state=ICONS.get(a.key);
                holder.icon.setImageDrawable(state==null?null:state.newDrawable(c.getResources()).mutate());
                if(state==null&&pending.add(a.key))LOADERS.execute(()->{
                    Drawable icon=null;try{icon=a.info.loadIcon(appContext.getPackageManager());if(icon!=null&&icon.getConstantState()!=null)ICONS.put(a.key,icon.getConstantState());}catch(RuntimeException ignored){}
                    Drawable loaded=icon;MAIN.post(()->{pending.remove(a.key);if(!alive.get())return;
                        for(int n=0;n<list.getChildCount();n++){Row h=(Row)list.getChildAt(n).getTag();if(a.key.equals(h.icon.getTag())){
                            Drawable.ConstantState cs=loaded==null?null:loaded.getConstantState();h.icon.setImageDrawable(cs==null?loaded:cs.newDrawable(c.getResources()).mutate());
                        }}
                    });
                });
                return row;
            }
        };list.setAdapter(adapter);
        Runnable filter=()->{shown.clear();String q=search.getText().toString().toLowerCase(Locale.ROOT);for(App a:all)if(a.search.contains(q))shown.add(a);
            adapter.notifyDataSetChanged();status.setText(shown.isEmpty()?"Không tìm thấy ứng dụng":"");status.setVisibility(shown.isEmpty()?View.VISIBLE:View.GONE);};
        AlertDialog dialog=Ui.dialog(c).setTitle(title).setView(box).setNegativeButton("Hủy",null).create();
        if(overlay&&dialog.getWindow()!=null)dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        dialog.setOnDismissListener(d->alive.set(false));list.setOnItemClickListener((a,v,i,id)->{chosen.select(shown.get(i).pkg);dialog.dismiss();});
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){filter.run();}public void afterTextChanged(android.text.Editable e){}});
        dialog.setOnShowListener(d->{if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawable(Ui.bg(Ui.CARD,Ui.dp(c,14)));dialog.getWindow().setLayout(Math.min(Ui.dp(c,620),Ui.dialogWidth(c)),-2);}});dialog.show();
        // Re-enumerate each opening so installation/removal/update is reflected without a receiver.
        LOADERS.execute(()->{try{List<App> apps=installed(appContext);MAIN.post(()->{if(alive.get()){all.addAll(apps);filter.run();}});}
            catch(RuntimeException e){MAIN.post(()->{if(alive.get()){status.setVisibility(View.VISIBLE);status.setText("Không đọc được danh sách. Đóng và mở lại để thử.");}});}});
    }
    public static String label(Context c,String pkg){try{return c.getPackageManager().getApplicationLabel(c.getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(PackageManager.NameNotFoundException e){return pkg.isEmpty()?"Chọn ứng dụng":pkg+" (chưa cài)";}}
}
