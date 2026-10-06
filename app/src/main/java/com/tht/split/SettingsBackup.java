package com.tht.split;

import android.content.Context;
import android.content.SharedPreferences;
import com.tht.split.core.SettingsSnapshot;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.*;

public final class SettingsBackup {
    private SettingsBackup(){}
    public static final AtomicBoolean RESTORING=new AtomicBoolean(false);
    private static Map<String,Object> map(JSONObject object)throws JSONException{
        Map<String,Object> result=new LinkedHashMap<>();Iterator<String> keys=object.keys();while(keys.hasNext()){String key=keys.next();result.put(key,object.get(key));}return result;
    }
    public static String export(Context c)throws JSONException{
        Store store=new Store(c);Map<String,Object> values=new LinkedHashMap<>();Map<String,?> all=store.prefs.getAll();
        for(String key:SettingsSnapshot.KEYS)if(all.containsKey(key))values.put(key,all.get(key));
        List<Map<String,Object>> pairs=new ArrayList<>();JSONArray saved=store.pairs();for(int n=0;n<saved.length();n++)pairs.add(map(saved.getJSONObject(n)));
        SettingsSnapshot checked=new SettingsSnapshot(values,pairs);
        String result=new JSONObject().put("schema",1).put("app","com.tht.split").put("version",BuildConfig.VERSION_NAME)
            .put("settings",new JSONObject(checked.settings)).put("pairs",new JSONArray(checked.pairs)).toString(2);
        if(result.getBytes(StandardCharsets.UTF_8).length>SettingsSnapshot.MAX_BYTES)throw new IllegalArgumentException("Bản sao lưu vượt 1MB");return result;
    }
    public static SettingsSnapshot read(InputStream input)throws IOException,JSONException{
        if(input==null)throw new IOException("Không mở được tệp sao lưu");ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
        while((n=input.read(buffer))!=-1){if(bytes.size()+n>SettingsSnapshot.MAX_BYTES)throw new IOException("Tệp sao lưu vượt 1MB");bytes.write(buffer,0,n);}
        String text=StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes.toByteArray())).toString();
        int depth=0;boolean quoted=false,escaped=false;for(int i=0;i<text.length();i++){
            char ch=text.charAt(i);if(quoted){if(escaped)escaped=false;else if(ch=='\\')escaped=true;else if(ch=='"')quoted=false;}
            else if(ch=='"')quoted=true;else if(ch=='{'||ch=='['){if(++depth>16)throw new IOException("Tệp sao lưu lồng quá nhiều cấp");}else if(ch=='}'||ch==']')depth--;
        }
        JSONObject root=new JSONObject(text);
        if(!Integer.valueOf(1).equals(root.get("schema"))||!"com.tht.split".equals(root.get("app")))throw new IOException("Tệp không phải bản sao lưu THT Split được hỗ trợ");
        Set<String> rootKeys=new HashSet<>();Iterator<String> it=root.keys();while(it.hasNext())rootKeys.add(it.next());
        if(!new HashSet<>(Arrays.asList("schema","app","version","settings","pairs")).equals(rootKeys))throw new IOException("Cấu trúc bản sao lưu không hợp lệ");
        if(root.getString("version").length()>80)throw new IOException("Phiên bản trong bản sao lưu không hợp lệ");
        JSONArray saved=root.getJSONArray("pairs");if(saved.length()>SettingsSnapshot.MAX_PAIRS)throw new IOException("Tối đa 100 cặp ứng dụng");
        List<Map<String,Object>> pairs=new ArrayList<>();for(int i=0;i<saved.length();i++)pairs.add(map(saved.getJSONObject(i)));
        return new SettingsSnapshot(map(root.getJSONObject("settings")),pairs);
    }
    /** Called on the IO worker after explicit confirmation, with RESTORING already held. */
    public static void restore(Context c,SettingsSnapshot checked)throws IOException{
        Store store=new Store(c);if(store.b("session_active",false)||store.b("session_busy",false))throw new IOException("Tắt chia màn hình trước khi khôi phục");
        SharedPreferences.Editor edit=store.prefs.edit();
        boolean saved=checked.apply(new SettingsSnapshot.Editor(){
            public void remove(String key){edit.remove(key);}
            public void put(String key,Object value){if(value instanceof String)edit.putString(key,(String)value);else if(value instanceof Integer)edit.putInt(key,(Integer)value);else if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);}
            public void pairs(List<Map<String,Object>> values){edit.putString("pairs",new JSONArray(values).toString());}
            public boolean commit(){return edit.commit();}
        });
        if(!saved)throw new IOException("Không lưu được cấu hình khôi phục");
    }
}
