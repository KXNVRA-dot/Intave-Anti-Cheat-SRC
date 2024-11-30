// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util;

import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import java.security.Key;
import sun.misc.BASE64Encoder;
import javax.crypto.Cipher;

public final class AESenc
{
    private static final String ALGO = "AES";
    private static final byte[] keyValue;
    
    public static String encrypt(final String data) throws Exception {
        final Key key = generateKey();
        final Cipher c = Cipher.getInstance("AES");
        c.init(1, key);
        final byte[] encVal = c.doFinal(data.getBytes());
        return new BASE64Encoder().encode(encVal);
    }
    
    public static String decrypt(final String encryptedData) throws Exception {
        final Key key = generateKey();
        final Cipher c = Cipher.getInstance("AES");
        c.init(2, key);
        final byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        final byte[] decValue = c.doFinal(decordedValue);
        return new String(decValue);
    }
    
    private static Key generateKey() {
        return new SecretKeySpec(AESenc.keyValue, "AES");
    }
    
    static {
        keyValue = "".getBytes();
    }
}
