package com.tht.split;

import android.content.Context;
import android.os.SystemClock;
import org.json.*;

/** Bounded local durations only; no command text, endpoint, token or network payload. */
public final class PerformanceLog {
    private PerformanceLog(){}
    public static final class Measure {
        private final Context context;private final String action,method;private final long started=SystemClock.elapsedRealtime();
        private long previous=started;private final JSONObject phases=new JSONObject();private boolean success,finished;
        public Measure(Context c,String action,String method){context=c.getApplicationContext();this.action=java.util.Arrays.asList("connect","start","preset","ratio","swap","replace","fullscreen","restore","hide","show","home","stop","status").contains(action)?action:"operation";this.method=java.util.Arrays.asList("adb","off","system").contains(method)?method:"other";}
        public void mark(String phase){long now=SystemClock.elapsedRealtime();try{phases.put(phase,Math.max(0,now-previous));}catch(JSONException ignored){}previous=now;}
        public void success(){success=true;}
        public void finish(){if(finished)return;finished=true;record(context,action,method,Math.max(0,SystemClock.elapsedRealtime()-started),success,phases);}
    }
    private static synchronized void record(Context c,String action,String method,long duration,boolean success,JSONObject phases){
        try{Store store=new Store(c,"tht_performance");JSONArray old=new JSONArray(store.s("samples","[]")),next=new JSONArray();
            for(int n=Math.max(0,old.length()-19);n<old.length();n++)next.put(old.get(n));
            next.put(new JSONObject().put("action",action).put("method",method).put("durationMs",duration).put("success",success).put("phasesMs",phases));store.set("samples",next.toString());
        }catch(JSONException|RuntimeException ignored){/* Diagnostics must not alter command outcome. */}
    }
    public static JSONArray samples(Context c){try{return new JSONArray(new Store(c,"tht_performance").s("samples","[]"));}catch(JSONException|RuntimeException ignored){return new JSONArray();}}
    private static String label(String action){switch(action){
        case "connect":return "Kết nối Bridge";case "start":return "Mở cặp";case "preset":return "Mở cặp đã lưu";case "ratio":return "Đổi tỷ lệ";
        case "swap":return "Đổi bên";case "replace":return "Đổi ứng dụng";case "fullscreen":return "Toàn màn hình";case "restore":return "Khôi phục cặp";
        case "hide":return "Ẩn thanh";case "show":return "Hiện thanh";case "home":return "Về Home";case "stop":return "Tắt chia";case "recover":return "Gỡ kẹt";case "status":return "Kiểm tra";default:return "Điều khiển cửa sổ";
    }}
    public static String summary(Context c){JSONArray values=samples(c);if(values.length()==0)return "Chưa có số đo. Kết nối hoặc chia màn hình để ghi thời gian.";
        StringBuilder text=new StringBuilder();for(int n=values.length()-1;n>=Math.max(0,values.length()-10);n--){JSONObject value=values.optJSONObject(n);if(value==null)continue;
            text.append(label(value.optString("action"))).append(" • ").append(value.optString("method")).append(" • ").append(value.optLong("durationMs")).append(" ms").append(value.optBoolean("success")?"":" • lỗi").append('\n');
        }return text.toString();
    }
}
