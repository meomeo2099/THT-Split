package com.tht.split;
import android.content.*;
import org.json.*;
import java.security.SecureRandom;
import java.util.*;

public final class Store {
    public final android.content.SharedPreferences prefs;
    public Store(Context c){this(c,"tht_split");}
    public Store(Context c,String namespace){prefs=c.getSharedPreferences(namespace,Context.MODE_PRIVATE);}
    public String s(String key,String def){if(BuildConfig.PLATFORM_TEST&&key.equals("transport"))return "system";return prefs.getString(key,def);}
    public int i(String key,int def){return prefs.getInt(key,def);}
    public boolean b(String key,boolean def){return prefs.getBoolean(key,def);}
    public void set(String key,String value){prefs.edit().putString(key,value).apply();}
    public void set(String key,int value){prefs.edit().putInt(key,value).apply();}
    public void set(String key,boolean value){prefs.edit().putBoolean(key,value).apply();}
    public synchronized String token(String key){String t=s(key,"");if(t.isEmpty()){t=randomToken();set(key,t);}return t;}
    public static String randomToken(){byte[] b=new byte[32];new SecureRandom().nextBytes(b);StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format(Locale.US,"%02x",v&255));return s.toString();}
    public JSONArray pairs(){try{return new JSONArray(s("pairs","[]"));}catch(JSONException e){return new JSONArray();}}
    public synchronized void savePair(JSONObject pair){JSONArray old=pairs(), next=new JSONArray();String id=pair.optString("id");if(id.isEmpty()){id=UUID.randomUUID().toString();try{pair.put("id",id);}catch(JSONException e){throw new IllegalStateException(e);}}
        boolean found=false;for(int n=0;n<old.length();n++){JSONObject o=old.optJSONObject(n);if(o==null)continue;if(id.equals(o.optString("id"))){next.put(pair);found=true;}else next.put(o);}if(!found)next.put(pair);set("pairs",next.toString());}
    public synchronized void deletePair(String id){JSONArray old=pairs(), next=new JSONArray();for(int n=0;n<old.length();n++){JSONObject o=old.optJSONObject(n);if(o!=null&&!id.equals(o.optString("id")))next.put(o);}set("pairs",next.toString());}
}
