package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class NativeUiPolicyTest {
    @Test public void compatibleHideShowNeverReachesTaskBackend()throws Exception{
        int[] calls={0};NativeUiPolicy.Resize resize=shown->{calls[0]++;throw new AssertionError("Must not resize native stacks");};
        NativeUiPolicy.visibility(true,"native",false,resize);
        NativeUiPolicy.visibility(true,"native",true,resize);
        assertEquals(0,calls[0]);assertTrue(NativeUiPolicy.blocksSwap(true,"native"));
    }
    @Test public void ex2AndFreeformRetainBothResizeTransitions()throws Exception{
        for(boolean compat:new boolean[]{false,true})for(String mode:new String[]{"native","freeform"}){
            if(compat&&mode.equals("native"))continue;
            java.util.List<Boolean> calls=new java.util.ArrayList<>();
            NativeUiPolicy.visibility(compat,mode,false,calls::add);
            NativeUiPolicy.visibility(compat,mode,true,calls::add);
            assertEquals(java.util.Arrays.asList(false,true),calls);
            assertFalse(NativeUiPolicy.blocksSwap(compat,mode));
        }
    }
    @Test public void foreignLauncherOrMixedStackIsNeverOwned(){
        assertFalse(StackScope.owns(new String[]{"com.flyme.auto.launcher/.Home"},"com.maps","com.video"));
        assertFalse(StackScope.owns(new String[]{"com.maps/.Main","com.flyme.auto.launcher/.Home"},"com.maps","com.video"));
        assertFalse(StackScope.owns(new String[]{"com.maps.fake/.Main"},"com.maps","com.video"));
        assertFalse(StackScope.owns(null,"com.maps","com.video"));
        assertFalse(StackScope.owns(new String[0],"com.maps","com.video"));
        assertTrue(StackScope.owns(new String[]{"com.maps/.Main","com.video"},"com.maps","com.video"));
    }
}
