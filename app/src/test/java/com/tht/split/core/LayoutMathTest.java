package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class LayoutMathTest {
 @Test public void ex2KeepsOemBarsAndToolbarOutOfAppBounds(){LayoutMath g=new LayoutMath(1920,1080,72,112,48,8,50);assertEquals(120,g.left.top);assertEquals(968,g.left.bottom);assertEquals(956,g.left.right);assertEquals(964,g.right.left);assertEquals(1920,g.right.right);}
 @Test public void hidingToolbarGivesBothAppsAllFreedPixels(){LayoutMath shown=new LayoutMath(1920,1080,72,112,48,8,40),hidden=new LayoutMath(1920,1080,72,112,0,8,40);assertEquals(48,hidden.left.height()-shown.left.height());assertEquals(48,hidden.right.height()-shown.right.height());assertEquals(shown.left.width(),hidden.left.width());}
 @Test public void ratiosAndOddDividerDoNotOverlap(){for(int r=30;r<=70;r++){LayoutMath g=new LayoutMath(1921,1080,72,112,0,7,r);assertEquals(7,g.right.left-g.left.right);assertEquals(1921,g.left.width()+g.right.width()+7);assertTrue(g.left.width()>0);assertTrue(g.right.width()>0);}}
 @Test public void clampsDragToSafeRange(){assertEquals(30,LayoutMath.clamp(-50));assertEquals(70,LayoutMath.clamp(999));assertEquals(42,LayoutMath.clamp(42));}
 @Test(expected=IllegalArgumentException.class) public void invalidInsetsNeverProduceNegativeViewport(){new LayoutMath(1920,1080,800,300,48,8,50);}
 @Test(expected=IllegalArgumentException.class) public void rejectsNegativeInsets(){new LayoutMath(1920,1080,-1,112,48,8,50);}
}
