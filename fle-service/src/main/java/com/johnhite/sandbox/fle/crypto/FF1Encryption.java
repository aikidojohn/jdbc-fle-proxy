package com.johnhite.sandbox.fle.crypto;

import org.checkerframework.checker.units.qual.A;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of FF1 algorithm from NIST SP 800-38G REV. 1 (DRAFT)
 */
public class FF1Encryption {
    private static final IvParameterSpec iv = new IvParameterSpec(new byte[16]);

    public static String encrypt(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        char[] X = value.toCharArray();
        int u = X.length/2;
        int v = X.length - u;
        int b = MoreMath.celining(MoreMath.celining((double)v * MoreMath.log(10, (double)domain.getRadix()))/8.0);
        int d = 4 * MoreMath.celining(b/4.0) + 4;

        char[] A = Arrays.copyOfRange(X, 0, u);
        char[] B = Arrays.copyOfRange(X, u, X.length);

        ByteBuffer P = ByteBuffer.allocate(16);
        P.put(new byte[] {1,2,1,});
        //3 bytes of the radix
        P.put(ByteBuffer.allocate(4).putInt((int)domain.getRadix()).array(), 0, 3);
        P.put((byte)10);
        P.put((byte)(u%256));
        P.putInt(X.length);
        P.putInt(tweak.length);

        //This pads the length of the string to a multiple of 16
        int numZeros = (16 - ((b + tweak.length + 1) % 16)) % 16;

        byte[] Q0 = ByteBuffer.allocate(tweak.length + numZeros).put(tweak).array();
        byte[] Q2 = Arrays.copyOfRange(numRadix(B, domain).toByteArray(), 0, b);
        for (int i=0; i < 10; i++) {
            ByteBuffer Q = ByteBuffer.allocate(Q0.length + Q2.length +1);
            Q.put(Q0);
            Q.put((byte)i);
            Q.put(Q2);

            byte[] R = prf(key, ByteBuffer.allocate(P.capacity() + Q.capacity()).put(P).put(Q).array());
            ByteBuffer S = ByteBuffer.allocate(d);
            if (d < R.length) {
                S.put(Arrays.copyOfRange(R, 0, d));
            } else {
                S.put(R);
                for (int j = R.length; j < d; j += 16) {
                    byte[] pad = new byte[R.length];
                    Arrays.fill(pad, (byte) j);
                    byte[] mac = cbcmac(key, xor(R, pad, 0));
                    if (d - j < 16) {
                        S.put(Arrays.copyOfRange(mac, 0, (d - j)));
                    }
                    else {
                        S.put(mac);
                    }
                }
            }
            BigInteger y = new BigInteger(S.array());

            int m = i % 2 == 0 ? u : v;
            BigInteger c = numRadix(A, domain).add(y).mod(BigInteger.valueOf(domain.getRadix()).pow(m));
            char[] C = strRadix(c, domain);
            A = B;
            B = C;
        }
        //640 5. Return A || B.
        StringBuilder sb = new StringBuilder();
        sb.append(A);
        sb.append(B);
        return sb.toString();
    }

    public static String decrypt(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        char[] X = value.toCharArray();
        int u = X.length/2;
        int v = X.length - u;
        int b = MoreMath.celining(MoreMath.celining((double)v * MoreMath.log(10, (double)domain.getRadix()))/8.0);
        int d = 4 * MoreMath.celining(b/4.0) + 4;

        char[] A = Arrays.copyOfRange(X, 0, u);
        char[] B = Arrays.copyOfRange(X, u, X.length);

        ByteBuffer P = ByteBuffer.allocate(16);
        P.put(new byte[] {1,2,1,});
        //3 bytes of the radix
        P.put(ByteBuffer.allocate(4).putInt((int)domain.getRadix()).array(), 0, 3);
        P.put((byte)10);
        P.put((byte)(u%256));
        P.putInt(X.length);
        P.putInt(tweak.length);

        //This pads the length of the string to a multiple of 16
        int numZeros = (16 - ((b + tweak.length + 1) % 16)) % 16;

        byte[] Q0 = ByteBuffer.allocate(tweak.length + numZeros).put(tweak).array();
        byte[] Q2 = Arrays.copyOfRange(numRadix(A, domain).toByteArray(), 0, b);
        for (int i=9; i >= 0; i--) {
            ByteBuffer Q = ByteBuffer.allocate(Q0.length + Q2.length +1);
            Q.put(Q0);
            Q.put((byte)i);
            Q.put(Q2);

            byte[] R = prf(key, ByteBuffer.allocate(P.capacity() + Q.capacity()).put(P).put(Q).array());
            ByteBuffer S = ByteBuffer.allocate(d);
            if (d < R.length) {
                S.put(Arrays.copyOfRange(R, 0, d));
            } else {
                S.put(R);
                for (int j = R.length; j < d; j += 16) {
                    byte[] pad = new byte[R.length];
                    Arrays.fill(pad, (byte) j);
                    byte[] mac = cbcmac(key, xor(R, pad, 0));
                    if (d - j < 16) {
                        S.put(Arrays.copyOfRange(mac, 0, (d - j)));
                    }
                    else {
                        S.put(mac);
                    }
                }
            }
            BigInteger y = new BigInteger(S.array());

            int m = i % 2 == 0 ? u : v;
            BigInteger c = numRadix(B, domain).subtract(y).mod(BigInteger.valueOf(domain.getRadix()).pow(m));
            char[] C = strRadix(c, domain);
            B = A;
            A = C;
        }
        //640 5. Return A || B.
        StringBuilder sb = new StringBuilder();
        sb.append(A);
        sb.append(B);
        return sb.toString();
    }

    public static byte[] cipher(SecretKey key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] ciphertext = cipher.doFinal(data);
            return ciphertext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] prf(SecretKey key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            int m = data.length / 16;
            byte[] y = new byte[16];
            for (int i=0; i < m; i++) {
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
                y = Arrays.copyOfRange(cipher.doFinal(xor(y, data, i*16)), 0 , 16);
            }
            return y;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] xor(byte[] a, byte[] b, int bOffset) {
        byte[] c = new byte[a.length];
        for (int i=0; i < a.length; i++) {
            c[i] = (byte)(a[i] ^ b[i+bOffset]);
        }
        return c;
    }

    //rough cbc-mac
    private static byte[] cbcmac(SecretKey key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            cipher.update(ByteBuffer.allocate(4).putInt(data.length).array());
            byte[] ciphertext = cipher.doFinal(data);
            return Arrays.copyOfRange(ciphertext, ciphertext.length -16, ciphertext.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BigInteger numRadix(char[] symbols, Alpha.CharacterDomain domain) {
        BigInteger rad = BigInteger.valueOf(domain.getRadix());
        BigInteger x = BigInteger.valueOf(0);
        for (int i=0; i < symbols.length; i++) {
            x = x.add(rad.pow(i).multiply(BigInteger.valueOf(domain.getIndex(symbols[i]))));
        }
        return x;
    }

    private static char[] strRadix(BigInteger n, Alpha.CharacterDomain domain) {
        List<Character> chars = new ArrayList<>();
        BigInteger rad = BigInteger.valueOf(domain.getRadix());
        BigInteger val = n;
        while (val.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divAndRem = val.divideAndRemainder(rad);
            int ind = divAndRem[1].intValue();
            chars.add(domain.getChar(ind));
            val = divAndRem[0];
        }
        char[] result = new char[chars.size()];
        for (int i=0; i < chars.size(); i++) {
            result[i] = chars.get(i).charValue();
        }
        return result;
    }

    private static byte[] reverseBytes(byte[] x) {
        byte[] y = new byte[x.length];
        for (int i= 0; i < x.length; i++) {
            y[i] = x[x.length - 1 - i];
        }
        return y;
    }

    private static char[] reverseChar(char[] x) {
        char[] y = new char[x.length];
        for (int i= 0; i < x.length; i++) {
            y[i] = x[x.length - 1 - i];
        }
        return y;
    }


    public static void main(String... args) throws Exception {
        SecureRandom random = SecureRandom.getInstanceStrong();
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128, random);
        SecretKey key = keyGen.generateKey();

        String message = "1234567890987654321";

        //String out = F_A10(message.length(), new byte[]{5, 19, -25}, 1, new BigInteger("11111111"), key);
        String enc = encrypt(key, new byte[]{5,19, -8, 28, 34,122,4}, message, Alpha.NUMBER);
        System.out.println(enc);

        String dec = decrypt(key, new byte[]{5,19, -8, 28, 34,122,4}, enc, Alpha.NUMBER);
        System.out.println(dec);

        message = "The quick brown fox.";
        enc = encrypt(key, new byte[]{5,19, -8, 28, 34,122,4}, message, Alpha.ASCII_PRINTABLE);
        System.out.println(enc);

        dec = decrypt(key, new byte[]{5,19, -8, 28, 34,122,4}, enc, Alpha.ASCII_PRINTABLE);
        System.out.println(dec);
    }
}
