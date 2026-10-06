package com.tht.split;

import com.tht.split.core.SettingsSnapshot;
import org.junit.Test;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class SettingsBackupFileTest {
    private static JSONObject file()throws Exception{return new JSONObject().put("schema",1).put("app","com.tht.split").put("version","0.3.11-test").put("settings",new JSONObject().put("ratio",50)).put("pairs",new JSONArray());}
    private static SettingsSnapshot read(String value)throws Exception{return SettingsBackup.read(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));}
    private static void rejects(JSONObject value)throws Exception{try{read(value.toString());fail();}catch(IOException|JSONException|IllegalArgumentException expected){}}
    @Test public void readsActualJsonAndNormalizesNumericPreferences()throws Exception{assertEquals(50,read(file().toString()).settings.get("ratio"));}
    @Test public void wrongAppSchemaUnknownRootAndSecretsAreRejected()throws Exception{
        rejects(file().put("app","com.tht.voice"));rejects(file().put("schema",2));rejects(file().put("voice_token","injected"));
        rejects(file().put("settings",new JSONObject().put("bridge_token","injected")));rejects(file().put("pairs",new JSONArray().put("not a pair")));
    }
    @Test public void brokenAndOversizedFilesAreRejectedWithoutApplyingPreferences()throws Exception{
        try{read("{not-json");fail();}catch(JSONException expected){}
        byte[] large=new byte[SettingsSnapshot.MAX_BYTES+1];try{SettingsBackup.read(new ByteArrayInputStream(large));fail();}catch(IOException expected){}
    }
    @Test public void excessiveNestingAndInvalidUtf8AreRejected()throws Exception{
        StringBuilder deep=new StringBuilder();for(int n=0;n<32;n++)deep.append('[');for(int n=0;n<32;n++)deep.append(']');
        try{read(deep.toString());fail();}catch(IOException expected){}
        try{SettingsBackup.read(new ByteArrayInputStream(new byte[]{(byte)0xc3,(byte)0x28}));fail();}catch(IOException expected){}
    }
}
