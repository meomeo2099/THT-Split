package com.tht.split.core;

import java.math.BigInteger;
import java.nio.*;
import java.security.interfaces.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.Cipher;

/** Original implementation of Android's 2048-bit ADB public-key format. */
public final class AdbRsa {
    private AdbRsa() {}
    public static PrivateKey decodePrivate(byte[] encoded)throws GeneralSecurityException {
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }
    public static byte[] publicKey(PrivateKey key)throws GeneralSecurityException {
        RSAPrivateKeySpec spec=KeyFactory.getInstance("RSA").getKeySpec(key,RSAPrivateKeySpec.class);
        BigInteger n=spec.getModulus(),base=BigInteger.ONE.shiftLeft(32);
        // This app's keys are generated with F4, including the legacy key-size-only
        // generator. Some Android providers expose only n/d, without the CRT interface.
        BigInteger exponent=key instanceof RSAPrivateCrtKey?((RSAPrivateCrtKey)key).getPublicExponent():RSAKeyGenParameterSpec.F4;
        if(n.bitLength()!=2048)throw new IllegalArgumentException("ADB cần RSA 2048");
        int inverse=n.mod(base).modInverse(base).negate().mod(base).intValue();
        ByteBuffer b=ByteBuffer.allocate(524).order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(64).putInt(inverse);putLittle(b,n);putLittle(b,BigInteger.ONE.shiftLeft(4096).mod(n));b.putInt(exponent.intValue());return b.array();
    }
    private static void putLittle(ByteBuffer b,BigInteger x){for(int i=0;i<256;i++){b.put(x.byteValue());x=x.shiftRight(8);}}
    public static byte[] sign(PrivateKey key,byte[] token)throws Exception {
        if(token.length!=20)throw new IllegalArgumentException("ADB AUTH token phải 20 byte");
        // adbd supplies an already hashed SHA-1 challenge: do not hash a second time.
        byte[] prefix={0x30,0x21,0x30,0x09,0x06,0x05,0x2b,0x0e,0x03,0x02,0x1a,0x05,0x00,0x04,0x14};
        byte[] digest=new byte[prefix.length+20];System.arraycopy(prefix,0,digest,0,prefix.length);System.arraycopy(token,0,digest,prefix.length,20);
        Cipher c=Cipher.getInstance("RSA/ECB/PKCS1Padding");c.init(Cipher.ENCRYPT_MODE,key);return c.doFinal(digest);
    }
}
