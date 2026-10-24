package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public final class DividerCrashGuardTest {
    private static final String OLD="10-22 08:00:01.1 1000 900 E AndroidRuntime: at com.android.server.wm.DockedStackDividerController.x\n";
    private static final String NEW="10-22 09:15:02.3 1000 900 E AndroidRuntime: at com.android.server.wm.WindowState.getFrameLw\n";
    @Test public void newMarkerLineIsACrash() {
        assertTrue(DividerCrashGuard.crashedSince(OLD,OLD+NEW));
        assertTrue(DividerCrashGuard.crashedSince("",NEW));
    }
    @Test public void oldOrUnrelatedLinesAreNot() {
        assertFalse(DividerCrashGuard.crashedSince(OLD,OLD));
        assertFalse(DividerCrashGuard.crashedSince(OLD,OLD+"10-22 09:15 E AndroidRuntime: FATAL EXCEPTION: main\n"));
        assertFalse(DividerCrashGuard.crashedSince(null,NEW));
        assertFalse(DividerCrashGuard.crashedSince(OLD,null));
    }
    @Test public void cooldownLastsTenMinutes() {
        long t=1_000_000L;
        assertFalse(DividerCrashGuard.coolingDown(0,t));
        assertTrue(DividerCrashGuard.coolingDown(t,t+60_000));
        assertFalse(DividerCrashGuard.coolingDown(t,t+DividerCrashGuard.COOLDOWN_MS));
        assertFalse(DividerCrashGuard.coolingDown(t+5,t));
    }
}
