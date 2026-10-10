package com.tht.split.bridge;

import java.io.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public final class CommandRunnerTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private String run(File dir,String script,long timeout)throws Exception{return CommandRunner.run(new String[]{"/bin/sh","-c",script},dir,timeout);}
    private void clean(){assertEquals(0,temp.getRoot().list().length);}
    @Test public void fileModeReadsStdoutAndStderrAndCleans()throws Exception {
        assertEquals("out\nerr\n",run(temp.getRoot(),"echo out; echo err >&2",2000));clean();
    }
    @Test public void bothModesCloseStdinInsteadOfWaitingForever()throws Exception {
        for(File dir:new File[]{null,temp.getRoot()})assertEquals("eof\n",run(dir,"read line || echo eof",2000));clean();
    }
    @Test public void reportsFailureWithoutRepeatingTheCommand()throws Exception {
        File marker=temp.newFile("marker");
        try{run(temp.getRoot(),"echo once >> '"+marker+"'; echo denied >&2; exit 2",2000);fail();}
        catch(IOException e){assertTrue(e.getMessage().contains("denied"));}
        try(BufferedReader reader=new BufferedReader(new FileReader(marker))){assertEquals("once",reader.readLine());assertNull(reader.readLine());}
        assertEquals(1,temp.getRoot().list().length);
    }
    @Test public void binderFailureEvenWithZeroExitIsAnError()throws Exception {
        try{run(temp.getRoot(),"echo 'cmd: Failure calling service activity: Failed transaction (2147483646)'",2000);fail();}
        catch(IOException e){assertTrue(e.getMessage().contains("Failed transaction"));}clean();
    }
    @Test public void timeoutKillsProcessAndRemovesCapture()throws Exception {
        try{run(temp.getRoot(),"exec sleep 10",100);fail();}
        catch(IOException e){assertTrue(e.getMessage().contains("quá thời gian"));}clean();
    }
    @Test public void outputIsBoundedAndTemporaryFileIsPrivate()throws Exception {
        String s=run(temp.getRoot(),"ls -l '"+temp.getRoot()+"'; head -c 30000 /dev/zero",2000);
        assertTrue(s.contains("-rw-------"));assertEquals(24000,s.getBytes("UTF-8").length);clean();
    }
    @Test public void excessiveOutputIsRejectedAndCleaned()throws Exception {
        try{run(temp.getRoot(),"head -c 300000 /dev/zero",2000);fail();}
        catch(IOException e){assertTrue(e.getMessage().contains("vượt giới hạn"));}clean();
    }
}
