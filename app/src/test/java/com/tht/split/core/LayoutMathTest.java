package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class LayoutMathTest {
 @Test public void coolrayReservesTheLeftDockAndCentersInsideRemainingArea(){
   LayoutMath g=new LayoutMath(1920,720,160,72,112,48,8,50);
   assertEquals(160,g.areaLeft);assertEquals(1760,g.areaWidth);assertEquals(1040,g.splitX);
   assertEquals(160,g.left.left);assertEquals(1920,g.right.right);
   assertEquals(g.left.width(),g.right.width());assertEquals(8,g.right.left-g.left.right);
 }
 @Test public void coolrayDragUsesRemainingWidthAndPreservesEdgeGaps(){
   LayoutMath g=new LayoutMath(1920,720,160,72,112,0,8,50);
   assertEquals(60,g.draggedRatio(50,176));assertEquals(40,g.draggedRatio(50,-176));
   assertEquals(30,g.draggedRatio(50,-5000));assertEquals(70,g.draggedRatio(50,5000));
   for(int r:new int[]{30,40,50,60,70}){
     LayoutMath n=new LayoutMath(1920,720,160,72,112,0,8,r);
     assertEquals(g.splitAt(r),n.splitX);assertEquals(160,n.left.left);
     assertEquals(1760,n.left.width()+8+n.right.width());
   }
 }
 @Test(expected=IllegalArgumentException.class) public void rejectsLeftInsetThatLeavesNoUsableArea(){new LayoutMath(1920,720,1800,0,0,0,8,50);}
 @Test(expected=IllegalArgumentException.class) public void rejectsNegativeLeftInset(){new LayoutMath(1920,720,-1,0,0,0,8,50);}
 @Test public void ex2KeepsOemBarsAndToolbarOutOfAppBounds(){LayoutMath g=new LayoutMath(1920,1080,72,112,48,8,50);assertEquals(120,g.left.top);assertEquals(968,g.left.bottom);assertEquals(956,g.left.right);assertEquals(964,g.right.left);assertEquals(1920,g.right.right);}
 @Test public void hidingToolbarGivesBothAppsAllFreedPixels(){LayoutMath shown=new LayoutMath(1920,1080,72,112,48,8,40),hidden=new LayoutMath(1920,1080,72,112,0,8,40);assertEquals(48,hidden.left.height()-shown.left.height());assertEquals(48,hidden.right.height()-shown.right.height());assertEquals(shown.left.width(),hidden.left.width());}
 @Test public void ratiosAndOddDividerDoNotOverlap(){for(int r=30;r<=70;r++){LayoutMath g=new LayoutMath(1921,1080,72,112,0,7,r);assertEquals(7,g.right.left-g.left.right);assertEquals(1921,g.left.width()+g.right.width()+7);assertTrue(g.left.width()>0);assertTrue(g.right.width()>0);}}
 @Test public void clampsDragToSafeRange(){assertEquals(30,LayoutMath.clamp(-50));assertEquals(70,LayoutMath.clamp(999));assertEquals(42,LayoutMath.clamp(42));}
 @Test(expected=IllegalArgumentException.class) public void invalidInsetsNeverProduceNegativeViewport(){new LayoutMath(1920,1080,800,300,48,8,50);}
 @Test(expected=IllegalArgumentException.class) public void rejectsNegativeInsets(){new LayoutMath(1920,1080,-1,112,48,8,50);}
}

