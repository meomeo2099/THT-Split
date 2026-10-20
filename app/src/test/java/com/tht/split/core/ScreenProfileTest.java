package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class ScreenProfileTest {
    @Test public void identifiesBothCoolrayIdentifiersWithoutMatchingEx2() {
        assertTrue(ScreenProfile.coolray2025("SX11-LA","other"));
        assertTrue(ScreenProfile.coolray2025("other","IHU730P"));
        assertFalse(ScreenProfile.coolray2025("IHU629G","ex2"));
        assertFalse(ScreenProfile.coolray2025(null,null));
    }
    @Test public void usesAvailableHeightInsteadOfAssumingFullPanel() {
        assertTrue(ScreenProfile.shortLandscape(1920,720));
        assertTrue(ScreenProfile.shortLandscape(1920,600));
        assertFalse(ScreenProfile.shortLandscape(1920,1080));
        assertFalse(ScreenProfile.shortLandscape(800,600));
        assertFalse(ScreenProfile.shortLandscape(1920,0));
    }
    @Test public void largeFontsHaveRoomForTwoLinesAndPadding() {
        assertEquals(52,ScreenProfile.navigationHeight(true,1));
        assertEquals(60,ScreenProfile.navigationHeight(false,1));
        assertTrue(ScreenProfile.navigationHeight(true,2)>=76);
    }
}
