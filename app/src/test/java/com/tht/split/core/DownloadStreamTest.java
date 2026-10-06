package com.tht.split.core;

import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;

public class DownloadStreamTest {
    private final byte[] data="hello".getBytes(StandardCharsets.UTF_8);
    @Test public void knownLengthReportsBytesAndComputesSha256()throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();List<Long> bytes=new ArrayList<>();String sha=DownloadStream.copy(new ByteArrayInputStream(data),out,5,64,(n,t)->{assertEquals(5,t);bytes.add(n);});
        assertArrayEquals(data,out.toByteArray());assertEquals(Arrays.asList(0L,5L),bytes);assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",sha);
    }
    @Test public void unknownLengthReportsIndeterminateTotal()throws Exception{
        DownloadStream.copy(new ByteArrayInputStream(data),new ByteArrayOutputStream(),-1,64,(n,t)->assertEquals(-1,t));
    }
    @Test public void rejectsTruncatedOrExcessAdvertisedLength()throws Exception{
        for(int declared:new int[]{4,6})try{DownloadStream.copy(new ByteArrayInputStream(data),new ByteArrayOutputStream(),declared,64,(n,t)->{});fail();}catch(IOException expected){}
    }
    @Test public void rejectsOversizedHeaderAndStreamingBodyBeforeWritingOverflow()throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();try{DownloadStream.copy(new ByteArrayInputStream(data),out,5,4,(n,t)->{});fail();}catch(IOException expected){}assertEquals(0,out.size());
        try{DownloadStream.copy(new ByteArrayInputStream(data),out,-1,4,(n,t)->{});fail();}catch(IOException expected){}assertEquals(0,out.size());
    }
    @Test public void interruptedWorkerDoesNotContinueWriting()throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();Thread.currentThread().interrupt();
        try{DownloadStream.copy(new ByteArrayInputStream(data),out,5,64,(n,t)->{});fail();}catch(InterruptedIOException expected){assertEquals(0,out.size());}finally{Thread.interrupted();}
    }
}
