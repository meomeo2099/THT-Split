package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class AccessRulesTest {
    @Test public void freshInstallAndAbsentWorkerAllowImmediateUse(){
        assertEquals("",AccessRules.denial(true,"enabled",0,18));
        assertEquals("",AccessRules.denial(false,"locked",100,18));
    }
    @Test public void publicPolicyHasNoEnrollmentOrOfflineExpiryGate(){
        assertEquals("",AccessRules.denial(true,"",0,18));
        assertEquals("",AccessRules.denial(true,"enabled",18,18));
    }
    @Test public void receivedGlobalLockAndMandatoryUpdateStillBlock(){
        assertFalse(AccessRules.denial(true,"locked",0,18).isEmpty());
        assertFalse(AccessRules.denial(true,"enabled",19,18).isEmpty());
        assertEquals("",AccessRules.denial(true,"enabled",19,19));
    }
    @Test public void exitIsAlwaysAvailable(){
        for(String action:new String[]{"stop","home","hide","recover"})assertTrue(AccessRules.exitCommand(action));
        for(String action:new String[]{"start","restore","ratio","prepare","status"})assertFalse(AccessRules.exitCommand(action));
    }
}
