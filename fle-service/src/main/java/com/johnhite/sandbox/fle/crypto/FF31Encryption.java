package com.johnhite.sandbox.fle.crypto;

import org.checkerframework.checker.units.qual.C;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of FF3-1 algorithm from NIST SP 800-38G REV. 1 (DRAFT)
 */
public class FF31Encryption {
    private static final IvParameterSpec iv = new IvParameterSpec(new byte[16]);

    public static String encrypt(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        char[] X = value.toCharArray();
        int v = X.length/2;
        int u = X.length - v;
        //Let A = X[1..u]; B = X[u + 1..n]
        char[] A = reverseChar(Arrays.copyOfRange(X, 0, u));
        char[] B = reverseChar(Arrays.copyOfRange(X, u, X.length));
        //System.out.println(new String(A) + " " + new String(B));

        //Let TL = T[0..27] || 04 and TR = T[32..55] || T[28..31] || 04 630 .
        ByteBuffer TL = ByteBuffer.allocate(4);
        TL.put(tweak,0,3);
        TL.put((byte)(tweak[3] & (byte)0xF0));
        ByteBuffer TR = ByteBuffer.allocate(4);
        TR.put(tweak, 4,3);
        TR.put((byte)(tweak[3] << 4));
        //631 4. For i from 0 to 7:
        for (int i=0; i < 8; i++) {
        //632 i. If i is even, let m = u and W = TR, else let m = v and W = TL.
            int m =0;
            ByteBuffer W;
            if (i %2 == 0) {
                m = u;
                W = TR;
            } else {
                m = v;
                W = TL;
            }
        //        ii. Let P = W ⊕ [i]
        //4 || [NUMradix (REV(B))]12 633 .
            ByteBuffer P = ByteBuffer.allocate(16);
            P.putInt(W.getInt(0) ^ i);
            BigInteger numRad = numRadix(B, domain);
            P.put(numRad.toByteArray());
            //System.out.println(" P = " + Arrays.toString(P.array()));

        //634 iii Let S = REVB(CIPHREVB(K)REVB(P)).
            byte[] S = cbcmac(key, reverseBytes(P.array()));

        //635 iv. Let y = NUM(S).
            BigInteger y = new BigInteger(S);
        //        v. Let c = (NUMradix (REV(A)) + y) mod radix m 636 .
            BigInteger c = (numRadix(A, domain).add(y)).mod(BigInteger.valueOf(domain.getRadix()).pow(m));
            //System.out.println("y = " + y + ", c = " + c);
        //        vi. Let C = REV(STRm 637 radix (c)).
            char[] C = strRadix(c, domain);
            A = B;
            B = C;
        //638 vii. Let A = B.
        //639 viii. Let B = C.
        }
        //640 5. Return A || B.
        StringBuilder sb = new StringBuilder();
        sb.append(reverseChar(A));
        sb.append(reverseChar(B));
        return sb.toString();
    }

    public static String decrypt(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        //681 1. Let u = ⌈n/2⌉; v = n – u.
        char[] X = value.toCharArray();
        int v = X.length/2;
        int u = X.length - v;

        //682 2. Let A = X[1..u]; B = X[u + 1..n].
        char[] A = reverseChar(Arrays.copyOfRange(X, 0, u));
        char[] B = reverseChar(Arrays.copyOfRange(X, u, X.length));
        //System.out.println(new String(A) + " " + new String(B));

        //3. Let TL = T[0..27] || 04 and TR = T[32..55] || T[28..31] || 04 683 .
        ByteBuffer TL = ByteBuffer.allocate(4);
        TL.put(tweak,0,3);
        TL.put((byte)(tweak[3] & (byte)0xF0));
        ByteBuffer TR = ByteBuffer.allocate(4);
        TR.put(tweak, 4,3);
        TR.put((byte)(tweak[3] << 4));

        //684 4. For i from 7 to 0:
        for (int i=7; i >= 0; i--) {
            //685 i. If i is even, let m = u and W = TR, else let m = v and W =TL.
            int m =0;
            ByteBuffer W;
            if (i %2 == 0) {
                m = u;
                W = TR;
            } else {
                m = v;
                W = TL;
            }
            //ii. P = W ⊕ [i]
            //4 || [NUMradix (REV(A))]12 686 .
            ByteBuffer P = ByteBuffer.allocate(16);
            P.putInt(W.getInt(0) ^ i);
            P.put(numRadix(A, domain).toByteArray());
            //System.out.println(" P = " + Arrays.toString(P.array()));
            //687 iii Let S = REVB(CIPHREVB(K)REVB(P)).
            byte[] S = cbcmac(key, reverseBytes(P.array()));
            //688 iv. Let y = NUM(S).
            BigInteger y = new BigInteger(S);
            //v. Let c = (NUMradix (REV(B))–y) mod radix m 689 .
            BigInteger c = (numRadix(B, domain).subtract(y)).mod(BigInteger.valueOf(domain.getRadix()).pow(m));
            //System.out.println("y = " + y + ", c = " + c);
            //vi. Let C = REV(STRm 690 radix (c)).
            char[] C = strRadix(c, domain);
            //691 vii. Let B = A.
            //692 viii. Let A = C.
            B = A;
            A = C;
        }
        //693 5. Return A || B.
        StringBuilder sb = new StringBuilder();
        sb.append(reverseChar(A));
        sb.append(reverseChar(B));
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
