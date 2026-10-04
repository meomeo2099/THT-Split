package com.tht.split;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class AppPicker {
    public interface Chosen {void select(String pkg);}
    public static final class App {public String pkg,label;public ResolveInfo info;}
    public static List<App> installed(Context c){Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);Map<String,App> map=new HashMap<>();for(ResolveInfo i:c.getPackageManager().queryIntentActivities(query,0)){
        if(i.activityInfo.packageName.equals(c.getPackageName()))continue;App a=new App();a.pkg=i.activityInfo.packageName;a.label=i.loadLabel(c.getPackageManager()).toString();a.info=i;map.put(a.pkg,a);}
        List<App> apps=new ArrayList<>(map.values());apps.sort(Comparator.comparing(a->a.label.toLowerCase(Locale.ROOT)));return apps;}
    public static void show(Context c,String title,boolean overlay,Chosen chosen){List<App> all=installed(c), shown=new ArrayList<>(all);LinearLayout box=Ui.column(c);Ui.pad(box,12);EditText search=new EditText(c);search.setSingleLine(true);search.setHint("Tìm ứng dụng");search.setTextColor(Ui.TEXT);search.setHintTextColor(Ui.MUTED);box.addView(search);
        ListView list=new ListView(c);box.addView(list,new LinearLayout.LayoutParams(-1,Ui.dp(c,300)));
        android.widget.BaseAdapter adapter=new BaseAdapter(){public int getCount(){return shown.size();}public Object getItem(int i){return shown.get(i);}public long getItemId(int i){return i;}public View getView(int i,View old,ViewGroup parent){App a=shown.get(i);LinearLayout row=Ui.row(c);Ui.pad(row,8);ImageView icon=new ImageView(c);icon.setImageDrawable(a.info.loadIcon(c.getPackageManager()));row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(c,36),Ui.dp(c,36)));TextView text=Ui.text(c,"  "+a.label+"\n  "+a.pkg,16,Ui.TEXT);row.addView(text);return row;}};list.setAdapter(adapter);
        AlertDialog dialog=new AlertDialog.Builder(c).setTitle(title).setView(box).setNegativeButton("Hủy",null).create();if(overlay&&dialog.getWindow()!=null)dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        list.setOnItemClickListener((a,v,i,id)->{chosen.select(shown.get(i).pkg);dialog.dismiss();});
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){shown.clear();String q=s.toString().toLowerCase(Locale.ROOT);for(App a:all)if((a.label+" "+a.pkg).toLowerCase(Locale.ROOT).contains(q))shown.add(a);adapter.notifyDataSetChanged();}public void afterTextChanged(android.text.Editable e){}});
        dialog.setOnShowListener(x->{if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawable(Ui.bg(Ui.CARD,Ui.dp(c,14)));dialog.getWindow().setLayout(Ui.dp(c,620),-2);}});dialog.show();
    }
    public static String label(Context c,String pkg){try{return c.getPackageManager().getApplicationLabel(c.getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(PackageManager.NameNotFoundException e){return pkg.isEmpty()?"Chọn ứng dụng":pkg+" (chưa cài)";}}
}
