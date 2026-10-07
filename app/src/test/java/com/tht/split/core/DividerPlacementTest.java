package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;

public class DividerPlacementTest {
    @Test public void nativeHandleStaysAtDisplayCenterWhenToolbarIsHidden(){
        assertEquals(540,DividerPlacement.centerY(1080,120,968,true));
        assertEquals(540,DividerPlacement.centerY(1080,72,968,true));
        assertEquals(544,DividerPlacement.centerY(1080,120,968,false));
        assertEquals(520,DividerPlacement.centerY(1080,72,968,false));
    }
    @Test public void seamFollowsReadBackInsteadOfUnverifiedRequestedRatio(){
        assertEquals(900,DividerPlacement.centerX(960,1920,40,new LayoutMath.Box(0,120,896,968),new LayoutMath.Box(904,120,1920,968)));
        assertEquals(901,DividerPlacement.centerX(960,1920,40,new LayoutMath.Box(0,120,898,968),new LayoutMath.Box(905,120,1920,968)));
    }
    @Test public void invalidOrMissingReadBackFallsBackWithoutMovingTasks(){
        LayoutMath.Box left=new LayoutMath.Box(0,120,956,968);
        assertEquals(960,DividerPlacement.centerX(960,1920,40,left,null));
        assertEquals(960,DividerPlacement.centerX(960,1920,40,left,new LayoutMath.Box(950,120,1920,968)));
        assertEquals(960,DividerPlacement.centerX(960,1920,40,left,new LayoutMath.Box(1100,120,1920,968)));
        assertEquals(960,DividerPlacement.centerX(960,1920,40,left,new LayoutMath.Box(964,120,2000,968)));
    }
    @Test public void largeInsetsClampHandleToVisibleArea(){
        assertEquals(600,DividerPlacement.centerY(1080,600,968,true));
        assertEquals(400,DividerPlacement.centerY(1080,72,400,true));
    }
}
