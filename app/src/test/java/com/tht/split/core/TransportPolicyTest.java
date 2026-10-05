package com.tht.split.core;

import org.junit.Test;
import static org.junit.Assert.*;

public final class TransportPolicyTest {
    @Test public void offRejectsTaskCommandsFromVoiceAsWellAsToolbar() {
        for(String command:new String[]{"ratio","swap","replace","fullscreen","restore"}) {
            assertFalse(TransportPolicy.allows("off",command));
            assertTrue(TransportPolicy.allows("adb",command));
            assertTrue(TransportPolicy.allows("system",command));
        }
    }
    @Test public void offKeepsOpenAndOverlayControls() {
        for(String command:new String[]{"start","preset","hide","show","home","stop","status"})
            assertTrue(TransportPolicy.allows("off",command));
    }
}
