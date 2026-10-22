package com.tht.split.core;

import java.util.*;

/** Portable preferences only. Never accept credentials, policy, session or freeform rollback keys. */
public final class SettingsSnapshot {
    public static final int MAX_BYTES=1048576,MAX_PAIRS=100;
    public static final Set<String> KEYS=Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
        "left","right","ratio","mode","top_px","bottom_px","toolbar_side","auto_hide","hide_seconds",
        "button_size","opacity","mini_position","launch_wait_ms","transport","adb_host","adb_port","adb_at_boot","voice_enabled","oem_ui_compat")));
    public final Map<String,Object> settings;
    public final List<Map<String,Object>> pairs;
    public interface Editor {void remove(String key);void put(String key,Object value);void pairs(List<Map<String,Object>> pairs);boolean commit();}
    public boolean apply(Editor editor){
        for(String key:KEYS)editor.remove(key);for(Map.Entry<String,Object> e:settings.entrySet())editor.put(e.getKey(),e.getValue());
        editor.pairs(pairs);return editor.commit();
    }
    public SettingsSnapshot(Map<String,?> input,List<? extends Map<String,?>> inputPairs){
        if(inputPairs.size()>MAX_PAIRS)throw invalid("Tối đa 100 cặp ứng dụng");
        Map<String,Object> checked=new LinkedHashMap<>();
        for(Map.Entry<String,?> e:input.entrySet()){
            String k=e.getKey();Object v=e.getValue();if(!KEYS.contains(k))throw invalid("Cài đặt không được hỗ trợ: "+k);
            switch(k){
                case "left":case "right":v=pkg(v,true);break;
                case "ratio":v=number(v,30,70);break;
                case "top_px":case "bottom_px":v=number(v,0,400);break;
                case "hide_seconds":v=number(v,3,30);break;
                case "opacity":v=number(v,40,100);break;
                case "launch_wait_ms":v=number(v,0,3000);break;
                case "adb_port":v=number(v,1,65535);break;
                case "auto_hide":case "adb_at_boot":case "voice_enabled":case "oem_ui_compat":if(!(v instanceof Boolean))throw invalid("Tùy chọn bật/tắt không hợp lệ");break;
                case "mode":v=choice(v,"native","freeform");break;
                case "transport":v=choice(v,"adb","off","system");break;
                case "toolbar_side":v=choice(v,"left","right");break;
                case "button_size":v=choice(v,"small","large");break;
                case "mini_position":v=choice(v,"center","left","right");break;
                case "adb_host":v=text(v,64,false);if(!((String)v).matches("[a-zA-Z0-9.:%_\\[\\]-]+"))throw invalid("Địa chỉ ADB không hợp lệ");break;
                default:throw invalid("Cài đặt không hợp lệ");
            }
            checked.put(k,v);
        }
        if(checked.containsKey("left")&&!checked.get("left").equals("")&&checked.get("left").equals(checked.get("right")))throw invalid("Hai ứng dụng phải khác nhau");
        List<Map<String,Object>> checkedPairs=new ArrayList<>();Set<String> ids=new HashSet<>();
        for(Map<String,?> pair:inputPairs){
            if(!pair.keySet().equals(new HashSet<>(Arrays.asList("id","name","left","right","ratio"))))throw invalid("Cấu trúc cặp ứng dụng không hợp lệ");
            Map<String,Object> next=new LinkedHashMap<>();String id=text(pair.get("id"),80,false);
            if(!ids.add(id))throw invalid("Mã cặp ứng dụng bị trùng");next.put("id",id);next.put("name",text(pair.get("name"),80,false));
            Object left=pkg(pair.get("left"),false),right=pkg(pair.get("right"),false);if(left.equals(right))throw invalid("Hai ứng dụng trong cặp phải khác nhau");
            next.put("left",left);next.put("right",right);next.put("ratio",number(pair.get("ratio"),30,70));checkedPairs.add(Collections.unmodifiableMap(next));
        }
        settings=Collections.unmodifiableMap(checked);pairs=Collections.unmodifiableList(checkedPairs);
    }
    private static IllegalArgumentException invalid(String message){return new IllegalArgumentException(message);}
    private static int number(Object v,int min,int max){
        if(!(v instanceof Integer||v instanceof Long))throw invalid("Giá trị phải là số nguyên");long n=((Number)v).longValue();
        if(n<min||n>max)throw invalid("Giá trị ngoài phạm vi "+min+"–"+max);return (int)n;
    }
    private static String text(Object v,int max,boolean empty){
        if(!(v instanceof String))throw invalid("Giá trị chữ không hợp lệ");String s=(String)v;
        if(s.length()>max||(!empty&&s.trim().isEmpty())||s.matches("(?s).*\\p{Cntrl}.*"))throw invalid("Giá trị chữ vượt giới hạn hoặc chứa ký tự điều khiển");return s;
    }
    private static String pkg(Object v,boolean empty){String s=text(v,180,empty);if(s.isEmpty()&&empty)return s;
        if(!s.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)*")||s.equals("com.tht.split"))throw invalid("Tên ứng dụng không hợp lệ");return s;}
    private static String choice(Object v,String...allowed){for(String s:allowed)if(s.equals(v))return s;throw invalid("Lựa chọn không được hỗ trợ");}
}

