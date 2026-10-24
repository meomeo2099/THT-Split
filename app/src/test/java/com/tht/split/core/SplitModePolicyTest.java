package com.tht.split.core;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class SplitModePolicyTest {
    @Test public void explicitModesNeverTryAnotherMode()throws Exception {
        for(String mode:new String[]{"native","freeform"}){
            List<Boolean> attempts=new ArrayList<>();
            try{SplitModePolicy.run(mode,false,n->{attempts.add(n);throw new java.io.IOException("fixture");});fail();}catch(java.io.IOException expected){}
            assertEquals(Collections.singletonList(mode.equals("native")),attempts);
        }
    }
    @Test public void autoUsesFreeformBeforeNative()throws Exception {
        List<Boolean> attempts=new ArrayList<>();
        assertEquals("native",SplitModePolicy.run("auto",false,n->{attempts.add(n);if(!n)throw new java.io.IOException("freeform unavailable");return "native";}));
        assertEquals(Arrays.asList(false,true),attempts);
    }
    @Test public void compatibleAutoNeverFallsBackToNative()throws Exception {
        List<Boolean> attempts=new ArrayList<>();
        try{SplitModePolicy.run("auto",true,n->{attempts.add(n);throw new java.io.IOException("fixture");});fail();}catch(java.io.IOException expected){assertTrue(expected.getMessage().contains("không tự chuyển"));}
        assertEquals(Collections.singletonList(false),attempts);
    }
    @Test public void compatibleAutoSuccessDoesNotProbeNative()throws Exception {
        List<Boolean> attempts=new ArrayList<>();
        assertEquals("freeform",SplitModePolicy.run("auto",true,n->{attempts.add(n);return "freeform";}));
        assertEquals(Collections.singletonList(false),attempts);
    }
    @Test public void selectedTransportIsNotReplacedByPlatformSigner(){
        for(String method:new String[]{"system","adb","off"})assertEquals(method,TransportPolicy.select(false,method));
        assertEquals("system",TransportPolicy.select(true,"adb"));
        assertFalse(TransportPolicy.allows("off","recover"));
    }
}
