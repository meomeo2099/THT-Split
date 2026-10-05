package com.tht.split.core;

import org.junit.Test;
import static org.junit.Assert.*;

public final class FailureTextTest {
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
