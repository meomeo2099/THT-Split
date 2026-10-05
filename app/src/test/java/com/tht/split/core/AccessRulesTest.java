package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class AccessRulesTest {
    @Test public void lockedAndMandatoryRemainBlockedOffline(){assertFalse(AccessRules.denial(true,true,"locked",0,12,1,999999999,3600).isEmpty());assertFalse(AccessRules.denial(true,true,"enabled",13,12,1,999999999,3600).isEmpty());}
    @Test public void newAndPendingDevicesNeedApproval(){assertFalse(AccessRules.denial(true,false,"",0,12,0,10,3600).isEmpty());assertFalse(AccessRules.denial(true,true,"pending",0,12,1,10,3600).isEmpty());}
    @Test public void approvedDeviceHasBoundedOfflineLease(){assertEquals("",AccessRules.denial(true,true,"enabled",12,12,1000,2000,3600));assertFalse(AccessRules.denial(true,true,"enabled",12,12,1000,3601001,3600).isEmpty());assertFalse(AccessRules.denial(true,true,"enabled",12,12,1000,999,3600).isEmpty());}
    @Test public void exitIsAlwaysAvailable(){assertTrue(AccessRules.exitCommand("stop"));assertTrue(AccessRules.exitCommand("home"));assertTrue(AccessRules.exitCommand("hide"));assertFalse(AccessRules.exitCommand("start"));assertFalse(AccessRules.exitCommand("restore"));}
}
