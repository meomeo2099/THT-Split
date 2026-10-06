package com.tht.split.core;

import org.junit.Test;
import static org.junit.Assert.*;

public final class FailureTextTest {
    @Test public void adbEndpointAndStageSurviveNestedRefusal() {
        java.net.ConnectException refused=new java.net.ConnectException("Connection refused");
        refused.initCause(new java.io.IOException("isConnected failed: ECONNREFUSED"));
        String text=FailureText.describe(new ConnectionFailure("Không kết nối được cổng ADB 127.0.0.1:5555.",refused));
        assertTrue(text.contains("127.0.0.1:5555"));assertTrue(text.contains("ECONNREFUSED"));
    }
    @Test public void contextualFailureRedactsBothContextAndCause() {
        String token=new String(new char[64]).replace('\0','a');
        String text=FailureText.describe(new ConnectionFailure("ADB token="+token,new java.io.IOException("key="+token)));
        assertFalse(text.contains(token));assertTrue(text.contains("[đã ẩn]"));
    }
    @Test public void wrappedFailureShowsUnderlyingCause() {
        assertEquals("ClassCastException: incompatible RSA key",FailureText.describe(new java.io.IOException("Generic ADB failure",new ClassCastException("incompatible RSA key"))));
    }
    @Test public void diagnosticRedactsTokenAndEncodedKeyBeforeTruncating() {
        String token=new String(new char[64]).replace('\0','a');
        String encoded=new String(new char[120]).replace('\0','Q');
        String message=FailureText.describe(new java.io.IOException("token="+token+" key="+encoded+"\nfailed"));
        assertFalse(message.contains(token));assertFalse(message.contains(encoded));assertFalse(message.contains("\n"));assertTrue(message.contains("failed"));
    }
}
