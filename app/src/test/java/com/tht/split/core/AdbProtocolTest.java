package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.*;
import java.security.*;
import java.security.interfaces.*;
import javax.crypto.Cipher;
import java.util.Arrays;
import java.math.BigInteger;

public class AdbProtocolTest {
    @Test public void readsIndependentLittleEndianFixture()throws Exception{
        // AUTH (token) from an independently built byte fixture, not write()/read() roundtrip.
        byte[] header={65,85,84,72,1,0,0,0,0,0,0,0,3,0,0,0,6,0,0,0,(byte)190,(byte)170,(byte)171,(byte)183,1,2,3};
        AdbWire.Packet p=AdbWire.read(new ByteArrayInputStream(header));assertEquals(AdbWire.AUTH,p.command);assertEquals(1,p.arg0);assertArrayEquals(new byte[]{1,2,3},p.data);
    }
    @Test public void outputHeaderMatchesLegacyProtocol()throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();AdbWire.write(out,new AdbWire.Packet(AdbWire.OPEN,1,0,new byte[]{'s',0}));
        ByteBuffer b=ByteBuffer.wrap(out.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);assertEquals(0x4e45504f,b.getInt());assertEquals(1,b.getInt());assertEquals(0,b.getInt());assertEquals(2,b.getInt());assertEquals(115,b.getInt());assertEquals(~0x4e45504f,b.getInt());assertEquals(26,out.size());
    }
    @Test public void corruptChecksumRejected()throws Exception{
        byte[] bytes=ByteBuffer.allocate(25).order(ByteOrder.LITTLE_ENDIAN).putInt(AdbWire.WRTE).putInt(1).putInt(1).putInt(1).putInt(2).putInt(~AdbWire.WRTE).put((byte)1).array();
        try{AdbWire.read(new ByteArrayInputStream(bytes));fail();}catch(IOException expected){assertTrue(expected.getMessage().contains("checksum"));}
    }
    @Test public void oversizedAndInvalidMagicRejectedBeforeAllocation()throws Exception{
        for(int length:new int[]{-1,1048577}){byte[] bytes=ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN).putInt(AdbWire.AUTH).putInt(1).putInt(0).putInt(length).putInt(0).putInt(~AdbWire.AUTH).array();try{AdbWire.read(new ByteArrayInputStream(bytes));fail();}catch(IOException expected){}}
        try{AdbWire.read(new ByteArrayInputStream(new byte[24]));fail();}catch(IOException expected){}
    }
    @Test public void androidPublicKeyHasCorrectMontgomeryValues()throws Exception{
        KeyPairGenerator g=KeyPairGenerator.getInstance("RSA");g.initialize(2048);RSAPrivateCrtKey key=(RSAPrivateCrtKey)g.generateKeyPair().getPrivate();ByteBuffer b=ByteBuffer.wrap(AdbRsa.publicKey(key)).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(64,b.getInt());long inverse=Integer.toUnsignedLong(b.getInt());BigInteger base=BigInteger.ONE.shiftLeft(32);assertEquals(base.subtract(BigInteger.ONE),key.getModulus().multiply(BigInteger.valueOf(inverse)).mod(base));
        byte[] modulus=new byte[256];b.get(modulus);assertEquals(key.getModulus(),unsignedLittle(modulus));byte[] rr=new byte[256];b.get(rr);assertEquals(BigInteger.ONE.shiftLeft(4096).mod(key.getModulus()),unsignedLittle(rr));assertEquals(65537,b.getInt());assertEquals(524,b.position());
    }
    private BigInteger unsignedLittle(byte[] data){byte[] reversed=new byte[data.length];for(int i=0;i<data.length;i++)reversed[i]=data[data.length-1-i];return new BigInteger(1,reversed);}
    @Test public void authSignsDigestWithoutHashingChallengeAgain()throws Exception{
        KeyPairGenerator g=KeyPairGenerator.getInstance("RSA");g.initialize(2048);KeyPair k=g.generateKeyPair();byte[] token=new byte[20];new SecureRandom().nextBytes(token);
        byte[] signed=AdbRsa.sign((RSAPrivateCrtKey)k.getPrivate(),token);Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");cipher.init(Cipher.DECRYPT_MODE,k.getPublic());byte[] digest=cipher.doFinal(signed);
        assertEquals(35,digest.length);assertArrayEquals(token,Arrays.copyOfRange(digest,15,35));assertArrayEquals(new byte[]{0x30,0x21,0x30,0x09,0x06,0x05,0x2b,0x0e,0x03,0x02,0x1a,0x05,0x00,0x04,0x14},Arrays.copyOf(digest,15));
        try{AdbRsa.sign((RSAPrivateCrtKey)k.getPrivate(),new byte[19]);fail();}catch(IllegalArgumentException expected){}
    }
}
