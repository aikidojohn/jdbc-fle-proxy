package com.johnhite.sandbox.fle.crypto;

import com.johnhite.sandbox.fle.db.proxy.FieldConf;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class EncryptionManager {

    public static <T> Encryption<T> getEncryption(FieldConf.FieldFormat type, Class<T> rt) {
        switch(type) {
            case BINARY: return (Encryption<T>)new ByteEncryption();
            case EMAIL: return (Encryption<T>)new EmailEncryption();
            case DOMAIN: return (Encryption<T>)new DomainEncryption();
            case ENGLISH_NAME: return (Encryption<T>)new NameEncryption();
            case ENGLISH_TEXT: return (Encryption<T>)new TextEncryption();
            case CREDIT_CARD: return (Encryption<T>)new NumberEncryption();
            case NUMBER: return (Encryption<T>)new NumberEncryption();
            case NUMBER_FIXED_WIDTH: return (Encryption<T>)new FixedWidthNumberEncryption();
        }
        return null;
    }

    public static class NumberEncryption implements Encryption<BigInteger> {
        public BigInteger encrypt(SecretKey key, byte[] tweak, BigInteger value) {
            String str = value.toString();
            String enc = FFXEncryption.encrypt(key, tweak, str);
            while (enc.startsWith("0")) {
                enc = FFXEncryption.encrypt(key, tweak, enc);
            }
            return new BigInteger(enc);
        }

        public BigInteger decrypt(SecretKey key, byte[] tweak, BigInteger value) {
            String str = value.toString();
            String dec = FFXEncryption.decrypt(key, tweak, str);
            while (dec.startsWith("0")) {
                dec = FFXEncryption.decrypt(key, tweak, dec);
            }
            return new BigInteger(dec);
        }
    }

    public static class FixedWidthNumberEncryption implements Encryption<String> {
        public String encrypt(SecretKey key, byte[] tweak, String value) {
            String enc = FFXEncryption.encrypt(key, tweak, value);
            return enc;
        }

        public String decrypt(SecretKey key, byte[] tweak, String value) {
            String dec = FFXEncryption.decrypt(key, tweak, value);
            return dec;
        }
    }

    public static class TextEncryption implements Encryption<String> {
        public String encrypt(SecretKey key, byte[] tweak, String value) {
            BigInteger encoded = Alpha.toBase10(value, Alpha.ASCII_PRINTABLE);
            BigInteger enc = new NumberEncryption().encrypt(key, tweak, encoded);
            return Alpha.fromBase10(enc, Alpha.ASCII_PRINTABLE);
        }

        public String decrypt(SecretKey key, byte[] tweak, String value) {
            BigInteger encoded = Alpha.toBase10(value, Alpha.ASCII_PRINTABLE);
            BigInteger dec = new NumberEncryption().decrypt(key, tweak, encoded);
            return Alpha.fromBase10(dec, Alpha.ASCII_PRINTABLE);
        }
    }

    public static class NameEncryption implements Encryption<String> {
        public String encrypt(SecretKey key, byte[] tweak, String value) {
            BigInteger encoded = Alpha.toBase10(value, Alpha.ASCII_NAME);
            BigInteger enc = new NumberEncryption().encrypt(key, tweak, encoded);
            return Alpha.fromBase10(enc, Alpha.ASCII_NAME);
        }

        public String decrypt(SecretKey key, byte[] tweak, String value) {
            BigInteger encoded = Alpha.toBase10(value, Alpha.ASCII_NAME);
            BigInteger dec = new NumberEncryption().decrypt(key, tweak, encoded);
            return Alpha.fromBase10(dec, Alpha.ASCII_NAME);
        }
    }

    public static class DomainEncryption implements Encryption<String> {
        public String encrypt(SecretKey key, byte[] tweak, String value) {
            int lastDot = value.lastIndexOf('.');
            String d = value.substring(0, lastDot);
            String suffix = value.substring(lastDot);

            BigInteger encoded = Alpha.toBase10(d, Alpha.ASCII_DOMAIN);
            BigInteger enc = new NumberEncryption().encrypt(key, tweak, encoded);
            return Alpha.fromBase10(enc, Alpha.ASCII_DOMAIN) + suffix;
        }

        public String decrypt(SecretKey key, byte[] tweak, String value) {
            int lastDot = value.lastIndexOf('.');
            String d = value.substring(0, lastDot);
            String suffix = value.substring(lastDot);

            BigInteger encoded = Alpha.toBase10(d, Alpha.ASCII_DOMAIN);
            BigInteger dec = new NumberEncryption().decrypt(key, tweak, encoded);
            return Alpha.fromBase10(dec, Alpha.ASCII_DOMAIN) + suffix;
        }
    }

    public static class EmailEncryption implements Encryption<String> {
        public String encrypt(SecretKey key, byte[] tweak, String value) {
            int atSign = value.indexOf('@');
            String local = value.substring(0, atSign);
            String domain = value.substring(atSign+1);

            BigInteger encoded = Alpha.toBase10(local, Alpha.ASCII_EMAIL);
            BigInteger enc = new NumberEncryption().encrypt(key, tweak, encoded);

            String encDomain = new DomainEncryption().encrypt(key, tweak, domain);
            return Alpha.fromBase10(enc, Alpha.ASCII_EMAIL) + "@" + encDomain;
        }

        public String decrypt(SecretKey key, byte[] tweak, String value) {
            int atSign = value.indexOf('@');
            String local = value.substring(0, atSign);
            String domain = value.substring(atSign+1);

            BigInteger encoded = Alpha.toBase10(local, Alpha.ASCII_EMAIL);
            BigInteger dec = new NumberEncryption().decrypt(key, tweak, encoded);

            String decDomain = new DomainEncryption().decrypt(key, tweak, domain);
            return Alpha.fromBase10(dec, Alpha.ASCII_EMAIL) + "@" + decDomain;
        }
    }

    public static class ByteEncryption implements Encryption<byte[]>{
        private IvParameterSpec constructIV(byte[] tweak) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(tweak);
                return new IvParameterSpec(digest, 0, 16);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public byte[] encrypt(SecretKey key, byte[] tweak, byte[] value) {
            try {
                CipherParameters parameters = CipherParameters.instance("AES", key, constructIV(tweak));
                return BlockEncryption.encrypt(parameters, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] decrypt(SecretKey key, byte[] tweak, byte[] value) {
            try {
                CipherParameters parameters = CipherParameters.instance("AES", key, constructIV(tweak));
                return BlockEncryption.decrypt(parameters, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String... args) throws Exception {
        SecureRandom random = SecureRandom.getInstanceStrong();
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128, random);
        SecretKey key = keyGen.generateKey();

        byte[] tweak = new byte[]{0, 5,-8, 124};
        String enc = EncryptionManager.getEncryption(FieldConf.FieldFormat.DOMAIN, String.class)
                .encrypt(key, tweak, "gmail.com");
        System.out.println(enc);
        System.out.println(new DomainEncryption().decrypt(key, tweak, enc));

        enc = new EmailEncryption().encrypt(key, tweak, "aikidojohn@gmail.com");
        System.out.println(enc);
        System.out.println(new EmailEncryption().decrypt(key, tweak, enc));

        enc = new NameEncryption().encrypt(key, tweak, "John");
        System.out.println(enc);
        System.out.println(new NameEncryption().decrypt(key, tweak, enc));

        enc = new TextEncryption().encrypt(key, tweak, "John! was here?");
        System.out.println(enc);
        System.out.println(new TextEncryption().decrypt(key, tweak, enc));
    }
}
