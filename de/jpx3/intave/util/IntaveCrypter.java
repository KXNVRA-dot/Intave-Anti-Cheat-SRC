// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

public final class IntaveCrypter
{
    private static List<String> cached_list;
    private static String source;
    private static String[] target;
    
    public static String encrypt(final String text, final String key) {
        String strData = "FAILED";
        try {
            final SecretKeySpec skeyspec = new SecretKeySpec(key.getBytes(), "Blowfish");
            final Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(1, skeyspec);
            final byte[] encrypted = cipher.doFinal(text.getBytes());
            strData = new String(encrypted);
        }
        catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return strData;
    }
    
    public static String decrypt(final String text, final String key) throws InvalidKeyException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException {
        final SecretKeySpec skeyspec = new SecretKeySpec(key.getBytes(), "Blowfish");
        final Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(2, skeyspec);
        final byte[] decrypted = cipher.doFinal(text.getBytes());
        return new String(decrypted);
    }
    
    public static String obfuscate(final String s) {
        final char[] result = new char[s.length()];
        final String local_target = IntaveCrypter.target[ThreadLocalRandom.current().nextInt(0, IntaveCrypter.target.length)];
        final char c;
        int index;
        final Object o;
        final String s2;
        IntStream.range(0, s.length()).forEachOrdered(i -> {
            c = s.charAt(i);
            index = IntaveCrypter.source.indexOf(c);
            if (index < 0) {
                index = 37;
            }
            o[i] = s2.charAt(index);
            return;
        });
        return new String(result);
    }
    
    public static List<String> unobfuescate(final String s) {
        final List<String> strings = IntaveCrypter.cached_list;
        strings.clear();
        Arrays.stream(IntaveCrypter.target).map(loc_target -> unobfuscate(s, loc_target)).forEachOrdered(strings::add);
        return strings;
    }
    
    private static String unobfuscate(final String s, final String local_target) {
        final char[] result = new char[s.length()];
        final int bound = s.length();
        final char c;
        int index;
        final Object o;
        IntStream.range(0, bound).forEachOrdered(i -> {
            c = s.charAt(i);
            index = local_target.indexOf(c);
            if (index < 0) {
                index = 37;
            }
            o[i] = IntaveCrypter.source.charAt(index);
            return;
        });
        return new String(result);
    }
    
    static {
        IntaveCrypter.cached_list = new ArrayList<String>();
        IntaveCrypter.source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!/";
        IntaveCrypter.target = new String[] { "Q5A8ZWS0XEDC6RFVT9GBY4HNU3J_MI1KO7L2P/", "FW472TCNIS5BRVHY9LJ38QPEDU6AGOMXK0Z_1/", "1_8DI4K0U3XHFGJBZACOWY6TQNRL5S92MEVP7/", "MZHWD47N_BI89TEGCF12Y0QO35AXSUP6LVRKJ/", "HSXWN8DAZIB3R0Q1KTLM4EJY7UG6C5F_92VOP/" };
    }
}
