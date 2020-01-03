package com.johnhite.sandbox.fle.crypto;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class FF1Test {

    @Test
    public void testEncryptWithRadix10TestVector() {

        byte[] keyBytes = DatatypeConverter.parseHexBinary("2B7E151628AED2A6ABF7158809CF4F3C");
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        String encrypted = FF1Encryption.encrypt(key, new byte[]{}, "0123456789", Alpha.NUMBER );
        System.out.println(encrypted);
        Assert.assertEquals("2433477484", encrypted);
    }

    @Test
    public void testDecryptWithRadix10TestVector() {

        byte[] keyBytes = DatatypeConverter.parseHexBinary("2B7E151628AED2A6ABF7158809CF4F3C");
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        String encrypted = FF1Encryption.decrypt(key, new byte[]{}, "2433477484", Alpha.NUMBER );
        System.out.println(encrypted);
        Assert.assertEquals("0123456789", encrypted);
    }

    @Test
    public void testEncryptWithRadix10TestVectorAndTweak() {

        byte[] keyBytes = DatatypeConverter.parseHexBinary("2B7E151628AED2A6ABF7158809CF4F3C");
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        String encrypted = FF1Encryption.encrypt(key, new byte[]{57, 56, 55, 54, 53, 52, 51, 50, 49, 48}, "0123456789", Alpha.NUMBER );
        System.out.println(encrypted);
        Assert.assertEquals("6124200773", encrypted);
    }

    @Test
    public void testDecryptWithRadix10TestVectorAndTweak() {

        byte[] keyBytes = DatatypeConverter.parseHexBinary("2B7E151628AED2A6ABF7158809CF4F3C");
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        String encrypted = FF1Encryption.decrypt(key, new byte[]{57, 56, 55, 54, 53, 52, 51, 50, 49, 48}, "6124200773", Alpha.NUMBER );
        System.out.println(encrypted);
        Assert.assertEquals("0123456789", encrypted);
    }

    @Test
    public void testEncryptWithRadix36TestVector() {

        byte[] keyBytes = DatatypeConverter.parseHexBinary("2B7E151628AED2A6ABF7158809CF4F3C");
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        String encrypted = FF1Encryption.encrypt(key, new byte[]{55, 55, 55, 55, 112, 113, 114, 115, 55, 55, 55}, "0123456789abcdefghi", Alpha.BASE36 );
        System.out.println(encrypted);
        Assert.assertEquals("a9tv40mll9kdu509eum", encrypted);
    }

    @Test
    public void testDecryptWithRadix36TestVector() {

        byte[] keyBytes = DatatypeConverter.parseHexBinary("2B7E151628AED2A6ABF7158809CF4F3C");
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        String encrypted = FF1Encryption.decrypt(key, new byte[]{55, 55, 55, 55, 112, 113, 114, 115, 55, 55, 55}, "a9tv40mll9kdu509eum", Alpha.BASE36 );
        System.out.println(encrypted);
        Assert.assertEquals("0123456789abcdefghi", encrypted);
    }
}
