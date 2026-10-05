package com.tht.split.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public final class PairControllerTest {
    private static final class Fake implements PairController.Tasks {
        final Map<Integer,Integer> modes=new HashMap<>();
        final List<String> calls=new ArrayList<>();
        int left=1,right=2;
        boolean refuseMode;
        public void clearPrimary(){calls.add("clear");for(Map.Entry<Integer,Integer> e:modes.entrySet())if(e.getValue()==3||e.getValue()==4)e.setValue(1);}
        public int launch(boolean isLeft,int mode){int id=isLeft?left:right;calls.add("launch:"+id+":"+mode);modes.put(id,mode);return id;}
        public int find(boolean isLeft){int id=isLeft?left:right;return modes.containsKey(id)?id:-1;}
        public int mode(int id){return modes.getOrDefault(id,-1);}
        public void setMode(int id,int mode){
            if(mode==3&&modes.containsValue(3)&&mode(id)!=3)throw new IllegalStateException("duplicate primary");
            calls.add("mode:"+id+":"+mode);if(!refuseMode)modes.put(id,mode);
        }
        public void resize(int l,int r){calls.add("resize:"+l+":"+r);}
    }
    @Test public void swapDismissesOldPrimaryBeforeAssigningNewPrimary()throws Exception{
        Fake f=new Fake();f.modes.put(1,3);f.modes.put(2,4);f.left=2;f.right=1;
        new PairController(f,true).layout();
        assertEquals(Arrays.asList("clear","launch:2:1","launch:1:1","mode:2:3","mode:1:4","resize:2:1"),f.calls);
        assertEquals(3,f.mode(2));assertEquals(4,f.mode(1));
    }
    @Test public void closedAppIsReopenedBeforeResize()throws Exception{
        Fake f=new Fake();f.modes.put(2,1);
        new PairController(f,true).resize();
        assertEquals(3,f.mode(1));assertEquals(4,f.mode(2));assertEquals("clear",f.calls.get(0));
    }
    @Test public void fullscreenPairIsRebuiltBeforeResize()throws Exception{
        Fake f=new Fake();f.modes.put(1,1);f.modes.put(2,1);
        new PairController(f,true).resize();
        assertEquals(3,f.mode(1));assertEquals(4,f.mode(2));
    }
    @Test public void healthyPairResizesWithoutRelaunch()throws Exception{
        Fake f=new Fake();f.modes.put(1,3);f.modes.put(2,4);
        new PairController(f,true).resize();assertEquals(Collections.singletonList("resize:1:2"),f.calls);
    }
    @Test public void replacingLeftClearsPrimaryOfPreviousApp()throws Exception{
        Fake f=new Fake();f.modes.put(9,3);f.modes.put(2,4);
        new PairController(f,true).layout();assertEquals(1,f.mode(9));assertEquals(3,f.mode(1));
    }
    @Test public void freeformRestoreReopensClosedWindow()throws Exception{
        Fake f=new Fake();f.modes.put(1,5);
        new PairController(f,false).resize();assertEquals(5,f.mode(1));assertEquals(5,f.mode(2));
    }
    @Test(expected=java.io.IOException.class) public void rejectedWindowModeDoesNotReportSuccess()throws Exception{
        Fake f=new Fake();f.refuseMode=true;new PairController(f,true).layout();
    }
}
