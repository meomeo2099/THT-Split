package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class AccessRulesTest {
    @Test public void lockedAndMandatoryRemainBlockedOffline(){assertFalse(AccessRules.denial(true,true,"locked",0,12,1,999999999,3600).isEmpty());assertFalse(AccessRules.denial(true,true,"enabled",13,12,1,999999999,3600).isEmpty());}
    @Test public void firstRegistrationNeedsNetworkButLegacyPendingHasNoApprovalGate(){assertFalse(AccessRules.denial(true,false,"",0,12,0,10,3600).isEmpty());assertEquals("",AccessRules.denial(true,true,"pending",0,12,1,10,3600));assertFalse(AccessRules.denial(true,true,"unknown",0,12,1,10,3600).isEmpty());}
    @Test public void enabledDeviceHasBoundedOfflineLease(){assertEquals("",AccessRules.denial(true,true,"enabled",12,12,1000,2000,3600));assertFalse(AccessRules.denial(true,true,"enabled",12,12,1000,3601001,3600).isEmpty());assertFalse(AccessRules.denial(true,true,"enabled",12,12,1000,999,3600).isEmpty());}
    @Test public void exitIsAlwaysAvailable(){assertTrue(AccessRules.exitCommand("stop"));assertTrue(AccessRules.exitCommand("home"));assertTrue(AccessRules.exitCommand("hide"));assertFalse(AccessRules.exitCommand("start"));assertFalse(AccessRules.exitCommand("restore"));}
    private static final class CachedPolicy implements AccessRules.Policy {
        String state="enabled";boolean known=true,fail;int minimum=0,calls;long now=2000;
        public String denial(){return AccessRules.denial(true,known,state,minimum,17,1000,now,3600);}
        public void refresh()throws Exception{calls++;if(fail)throw new java.io.IOException("offline");known=true;state="enabled";minimum=0;now=2000;}
    }
    @Test public void validCacheDoesNotWaitForNetworkOnAnyTaskCommand(){
        CachedPolicy p=new CachedPolicy();p.fail=true;
        for(String action:new String[]{"start","preset","ratio","swap","replace","fullscreen","restore","show","status"})assertEquals("",AccessRules.commandDenial(action,p));
        assertEquals(0,p.calls);
    }
    @Test public void unknownAndExpiredMustRefreshBeforeAllowing(){
        CachedPolicy p=new CachedPolicy();p.known=false;assertEquals("",AccessRules.commandDenial("start",p));assertEquals(1,p.calls);
        p.now=3601001;assertEquals("",AccessRules.commandDenial("ratio",p));assertEquals(2,p.calls);
    }
    @Test public void networkFailureCannotClearLockExpiryMandatoryOrUnknown(){
        for(int scenario=0;scenario<4;scenario++){
            CachedPolicy p=new CachedPolicy();p.fail=true;
            if(scenario==0)p.state="locked";if(scenario==1)p.now=3601001;if(scenario==2)p.minimum=18;if(scenario==3)p.known=false;
            assertFalse(AccessRules.commandDenial("start",p).isEmpty());assertEquals(1,p.calls);
            assertEquals("",AccessRules.commandDenial("stop",p));assertEquals("",AccessRules.commandDenial("home",p));assertEquals("",AccessRules.commandDenial("hide",p));assertEquals(1,p.calls);
        }
    }
}
