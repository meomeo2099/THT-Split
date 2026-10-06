package com.tht.split.core;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class SettingsSnapshotTest {
    private static Map<String,Object> settings(){Map<String,Object> m=new LinkedHashMap<>();m.put("left","com.google.android.apps.maps");m.put("right","com.google.android.youtube");m.put("ratio",50);m.put("adb_port",5555);m.put("auto_hide",true);return m;}
    private static Map<String,Object> pair(String id){Map<String,Object> m=new LinkedHashMap<>();m.put("id",id);m.put("name","Bản đồ & YouTube");m.put("left","com.google.android.apps.maps");m.put("right","com.google.android.youtube");m.put("ratio",45);return m;}
    private static void reject(Runnable run){try{run.run();fail("Must reject invalid snapshot");}catch(IllegalArgumentException expected){}}
    @Test public void preservesPairsAndMakesIndependentImmutableSnapshot(){
        Map<String,Object> s=settings(),p=pair("one");SettingsSnapshot snapshot=new SettingsSnapshot(s,Arrays.asList(p));s.put("ratio",70);p.put("name","changed");
        assertEquals(50,snapshot.settings.get("ratio"));assertEquals("Bản đồ & YouTube",snapshot.pairs.get(0).get("name"));assertEquals(45,snapshot.pairs.get(0).get("ratio"));
        try{snapshot.pairs.get(0).put("left","bad");fail();}catch(UnsupportedOperationException expected){}
    }
    @Test public void rejectsCredentialsPolicySessionAndRollbackImports(){
        for(String key:Arrays.asList("voice_token","bridge_token","policy","session_active","download_version","freeform_backup","old_enable_freeform_support","admin_password")){
            Map<String,Object> s=settings();s.put(key,"injected");reject(()->new SettingsSnapshot(s,Collections.emptyList()));
        }
    }
    @Test public void rejectsIncorrectTypesEnumsAndBounds(){
        Object[][] invalid={{"ratio",29},{"ratio",71},{"ratio",50d},{"adb_port",0},{"adb_port",65536},{"opacity",39},{"top_px",401},{"launch_wait_ms",3001},{"auto_hide","true"},{"mode","unknown"},{"adb_host","host\ncommand"}};
        for(Object[] v:invalid){Map<String,Object> s=settings();s.put((String)v[0],v[1]);reject(()->new SettingsSnapshot(s,Collections.emptyList()));}
    }
    @Test public void acceptsBoundarySettingsAndUnsetSelection(){
        Map<String,Object> s=settings();s.put("left","");s.put("right","");s.put("ratio",70L);s.put("adb_port",65535);s.put("opacity",100);s.put("top_px",400);s.put("transport","off");
        SettingsSnapshot out=new SettingsSnapshot(s,Collections.emptyList());assertEquals(70,out.settings.get("ratio"));assertEquals("off",out.settings.get("transport"));
    }
    @Test public void rejectsDuplicateIdsSameAppUnknownPairFieldsAndTooManyPairs(){
        reject(()->new SettingsSnapshot(settings(),Arrays.asList(pair("same"),pair("same"))));
        Map<String,Object> same=pair("one");same.put("right",same.get("left"));reject(()->new SettingsSnapshot(settings(),Arrays.asList(same)));
        Map<String,Object> extra=pair("one");extra.put("token","bad");reject(()->new SettingsSnapshot(settings(),Arrays.asList(extra)));
        List<Map<String,Object>> many=new ArrayList<>();for(int n=0;n<101;n++)many.add(pair("p"+n));reject(()->new SettingsSnapshot(settings(),many));
    }
    @Test public void restoreReplacesOnlyPortableKeysInOneCommitAndKeepsSecrets(){
        Map<String,Object> current=new LinkedHashMap<>();current.put("voice_token","private-voice");current.put("bridge_token","private-bridge");current.put("freeform_backup",true);current.put("last_error","keep");current.put("ratio",30);current.put("top_px",200);
        int[] commits={0};SettingsSnapshot snapshot=new SettingsSnapshot(settings(),Arrays.asList(pair("one")));
        assertTrue(snapshot.apply(new SettingsSnapshot.Editor(){public void remove(String k){current.remove(k);}public void put(String k,Object v){current.put(k,v);}public void pairs(List<Map<String,Object>> p){current.put("pairs",p);}public boolean commit(){commits[0]++;return true;}}));
        assertEquals(1,commits[0]);assertEquals("private-voice",current.get("voice_token"));assertEquals("private-bridge",current.get("bridge_token"));assertEquals(true,current.get("freeform_backup"));assertEquals("keep",current.get("last_error"));assertEquals(50,current.get("ratio"));assertFalse(current.containsKey("top_px"));
    }
}
