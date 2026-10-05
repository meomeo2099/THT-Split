package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public final class BoundsCheckTest {
    @Test public void rejectsSecondaryThatFillsDisplayDespiteCorrectPrimary() {
        LayoutMath g=new LayoutMath(1920,1080,72,112,48,8,40);
        assertTrue(BoundsCheck.matches(g.left,new LayoutMath.Box(0,120,764,968),2));
        assertFalse(BoundsCheck.matches(g.right,new LayoutMath.Box(772,0,1920,1080),2));
        assertTrue(BoundsCheck.matches(g.right,new LayoutMath.Box(772,120,1920,968),2));
    }
    @Test public void rejectsSnappedRatioDifferentFromRequest() {
        LayoutMath g=new LayoutMath(1920,1080,72,112,0,8,30);
        assertFalse(BoundsCheck.matches(g.left,new LayoutMath.Box(0,72,676,968),2));
    }
    @Test public void acceptsOnlySmallPixelRounding() {
        LayoutMath.Box b=new LayoutMath.Box(0,120,956,968);
        assertTrue(BoundsCheck.matches(b,new LayoutMath.Box(0,121,957,967),2));
        assertFalse(BoundsCheck.matches(b,new LayoutMath.Box(0,117,956,968),2));
    }
}
