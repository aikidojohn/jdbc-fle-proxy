package crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Implementation of FF1 algorithm from NIST SP 800-38G REV. 1 (DRAFT)
 */
public class FF1Encryption {
    private static final IvParameterSpec iv = new IvParameterSpec(new byte[16]);

    private static byte[] getBytes(byte[] source, int start, int length) {
        byte[] ret = new byte[length];
        int n = start + length > source.length ? source.length -1 : start + length -1;
        int ind = ret.length -1;
        for (int i= n; i >= start; i--) {
            ret[ind]= source[i];
            ind--;
        }
        return ret;
    }

    private static char[] getChars(char[] source, int start, int length, char pad) {
        char[] ret = new char[length];
        Arrays.fill(ret, pad);
        int n = start + length > source.length ? source.length -1 : start + length -1;
        int ind = ret.length -1;
        for (int i= n; i >= start; i--) {
            ret[ind]= source[i];
            ind--;
        }
        return ret;
    }
    public static String encrypt(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        if (minlen(domain) > value.length()) {
            throw new IllegalArgumentException("Message length " + value.length() + " is less than the minimum length " + minlen(domain));
        }
        if (maxlen(domain) < value.length()) {
            throw new IllegalArgumentException("Message length " + value.length() + " is greater than the maximum length " + maxlen(domain));
        }
        return  encryptOptimized(key, tweak, value, domain);
    }

    public static String encryptOptimized(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        char[] X = value.toCharArray();
        int u = X.length/2;
        int v = X.length - u;
        int b = MoreMath.celining(MoreMath.celining((double)v * MoreMath.log(2, (double)domain.getRadix()))/8.0);
        int d = 4 * MoreMath.celining(b/4.0) + 4;

        char[] A = Arrays.copyOfRange(X, 0, u);
        char[] B = Arrays.copyOfRange(X, u, X.length);
        BigInteger aNum = domain.toNumber(A);
        BigInteger bNum = domain.toNumber(B);

        ByteBuffer P = ByteBuffer.allocate(16);
        P.put(new byte[] {1,2,1,});
        //3 bytes of the radix
        P.put(ByteBuffer.allocate(4).putInt((int)domain.getRadix()).array(), 1, 3);
        P.put((byte)10);
        P.put((byte)(u%256));
        P.putInt(X.length);
        P.putInt(tweak.length);
        byte[] pArray = P.array();

        //This pads the length of the string to a multiple of 16
        int numZeros = (16 - ((b + tweak.length + 1) % 16)) % 16;

        final BigInteger radu = BigInteger.valueOf(domain.getRadix()).pow(u);
        final BigInteger radv = BigInteger.valueOf(domain.getRadix()).pow(v);

        byte[] QQ = new byte[tweak.length + numZeros + b + 1];
        System.arraycopy(tweak, 0, QQ, 0, tweak.length);
        for (int i=0; i < 10; i++) {
            QQ[tweak.length + numZeros] = (byte)i;
            byte[] bBytes = bNum.toByteArray();
            int bInd = bBytes.length - 1;
            for (int j = QQ.length-1; j > tweak.length + numZeros; j--) {
                if (bInd >= 0) {
                    QQ[j] = bBytes[bInd--];
                } else {
                    QQ[j] = 0;
                }
            }

            byte[] R = prf(key, ByteBuffer.allocate(pArray.length + QQ.length).put(pArray).put(QQ).array());

            ByteBuffer S = ByteBuffer.allocate(d);
            if (d < R.length) {
                S.put(Arrays.copyOfRange(R, 0, d));
            } else {
                S.put(R);
                for (int j = R.length; j < d; j += 16) {
                    byte[] pad = new byte[R.length];
                    Arrays.fill(pad, (byte) (j/16));
                    byte[] mac = cipher(key, xor(R, pad, 0));
                    if (d - j < 16) {
                        S.put(Arrays.copyOfRange(mac, 0, (d - j)));
                    }
                    else {
                        S.put(mac);
                    }
                }
            }
            BigInteger y = new BigInteger(1, S.array());

            BigInteger radm = i % 2 == 0 ? radu : radv;
            BigInteger c = aNum.add(y).mod(radm);

            aNum = bNum;
            bNum = c;
        }
        //640 5. Return A || B.
        StringBuilder sb = new StringBuilder();
        sb.append(getChars(domain.fromNumber(aNum), 0, u, domain.getChar(0)));
        sb.append(getChars(domain.fromNumber(bNum), 0, v, domain.getChar(0)));
        return sb.toString();
    }

    public static String encryptStandard(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        char[] X = value.toCharArray();
        int u = X.length/2;
        int v = X.length - u;
        int b = MoreMath.celining(MoreMath.celining((double)v * MoreMath.log(2, (double)domain.getRadix()))/8.0);
        int d = 4 * MoreMath.celining(b/4.0) + 4;

        char[] A = Arrays.copyOfRange(X, 0, u);
        char[] B = Arrays.copyOfRange(X, u, X.length);
        //System.out.println("A = " + Arrays.toString(A));
        //System.out.println("B = " + Arrays.toString(B));

        ByteBuffer P = ByteBuffer.allocate(16);
        P.put(new byte[] {1,2,1,});
        //3 bytes of the radix
        P.put(ByteBuffer.allocate(4).putInt((int)domain.getRadix()).array(), 1, 3);
        P.put((byte)10);
        P.put((byte)(u%256));
        P.putInt(X.length);
        P.putInt(tweak.length);
        //System.out.println("P = " + Arrays.toString(P.array()));
        //This pads the length of the string to a multiple of 16
        int numZeros = (16 - ((b + tweak.length + 1) % 16)) % 16;

        byte[] Q0 = ByteBuffer.allocate(tweak.length + numZeros).put(tweak).array();
        for (int i=0; i < 10; i++) {
            byte[] Q2 = getBytes(domain.toNumber(B).toByteArray(), 0, b);
            ByteBuffer Q = ByteBuffer.allocate(Q0.length + Q2.length +1);
            Q.put(Q0);
            Q.put((byte)i);
            Q.put(Q2);
            //System.out.println("Q = " + Arrays.toString(Q.array()));

            byte[] R = prf(key, ByteBuffer.allocate(P.capacity() + Q.capacity()).put(P.array()).put(Q.array()).array());
            //System.out.println("R= " + Arrays.toString(R));
            ByteBuffer S = ByteBuffer.allocate(d);
            if (d < R.length) {
                S.put(Arrays.copyOfRange(R, 0, d));
            } else {
                S.put(R);
                for (int j = R.length; j < d; j += 16) {
                    byte[] pad = new byte[R.length];
                    Arrays.fill(pad, (byte) (j/16));
                    byte[] mac = cipher(key, xor(R, pad, 0));
                    if (d - j < 16) {
                        S.put(Arrays.copyOfRange(mac, 0, (d - j)));
                    }
                    else {
                        S.put(mac);
                    }
                }
            }
            //System.out.println("S = " + DatatypeConverter.printHexBinary(S.array()));
            BigInteger y = new BigInteger(1, S.array());
            //System.out.println("y = " + y);

            int m = i % 2 == 0 ? u : v;
            //System.out.println("m = " + m);
            BigInteger c = domain.toNumber(A).add(y).mod(BigInteger.valueOf(domain.getRadix()).pow(m));
            char[] C = getChars(domain.fromNumber(c), 0, m, domain.getChar(0));
            A = B;
            B = C;
           // System.out.println("c = " + c);
           // System.out.println("C = " + Arrays.toString(C));
           // System.out.println("A = " + Arrays.toString(A));
           // System.out.println("B = " + Arrays.toString(B));
        }
        //640 5. Return A || B.
        StringBuilder sb = new StringBuilder();
        sb.append(A);
        sb.append(B);
        return sb.toString();
    }

    public static String decrypt(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        if (minlen(domain) > value.length()) {
            throw new IllegalArgumentException("Message length " + value.length() + " is less than the minimum length " + minlen(domain));
        }
        if (maxlen(domain) < value.length()) {
            throw new IllegalArgumentException("Message length " + value.length() + " is greater than the maximum length " + maxlen(domain));
        }
        return decryptOptimized(key, tweak, value, domain);
    }

    public static String decryptOptimized(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        char[] X = value.toCharArray();
        int u = X.length/2;
        int v = X.length - u;
        int b = MoreMath.celining(MoreMath.celining((double)v * MoreMath.log(2, (double)domain.getRadix()))/8.0);
        int d = 4 * MoreMath.celining(b/4.0) + 4;

        char[] A = Arrays.copyOfRange(X, 0, u);
        char[] B = Arrays.copyOfRange(X, u, X.length);
        BigInteger aNum = domain.toNumber(A);
        BigInteger bNum = domain.toNumber(B);


        ByteBuffer P = ByteBuffer.allocate(16);
        P.put(new byte[] {1,2,1,});
        //3 bytes of the radix
        P.put(ByteBuffer.allocate(4).putInt((int)domain.getRadix()).array(), 1, 3);
        P.put((byte)10);
        P.put((byte)(u%256));
        P.putInt(X.length);
        P.putInt(tweak.length);
        byte[] pArray = P.array();

        //This pads the length of the string to a multiple of 16
        int numZeros = (16 - ((b + tweak.length + 1) % 16)) % 16;

        final BigInteger radu = BigInteger.valueOf(domain.getRadix()).pow(u);
        final BigInteger radv = BigInteger.valueOf(domain.getRadix()).pow(v);

        byte[] QQ = new byte[tweak.length + numZeros + b + 1];
        System.arraycopy(tweak, 0, QQ, 0, tweak.length);
        for (int i=9; i >= 0; i--) {
            QQ[tweak.length + numZeros] = (byte)i;
            byte[] aBytes = aNum.toByteArray();
            int bInd = aBytes.length - 1;
            for (int j = QQ.length-1; j > tweak.length + numZeros; j--) {
                if (bInd >= 0) {
                    QQ[j] = aBytes[bInd--];
                } else {
                    QQ[j] = 0;
                }
            }

            byte[] R = prf(key, ByteBuffer.allocate(pArray.length + QQ.length).put(pArray).put(QQ).array());

            ByteBuffer S = ByteBuffer.allocate(d);
            if (d < R.length) {
                S.put(Arrays.copyOfRange(R, 0, d));
            } else {
                S.put(R);
                for (int j = R.length; j < d; j += 16) {
                    byte[] pad = new byte[R.length];
                    Arrays.fill(pad, (byte) (j/16));
                    byte[] mac = cipher(key, xor(R, pad, 0));
                    if (d - j < 16) {
                        S.put(Arrays.copyOfRange(mac, 0, (d - j)));
                    }
                    else {
                        S.put(mac);
                    }
                }
            }
            BigInteger y = new BigInteger(1, S.array());

            BigInteger radm = i % 2 == 0 ? radu : radv;
            BigInteger c = bNum.subtract(y).mod(radm);//aNum.add(y).mod(radm);

            bNum = aNum;
            aNum = c;
        }
        //640 5. Return A || B.
        StringBuilder sb = new StringBuilder();
        sb.append(getChars(domain.fromNumber(aNum), 0, u, domain.getChar(0)));
        sb.append(getChars(domain.fromNumber(bNum), 0, v, domain.getChar(0)));
        return sb.toString();
    }

    public static String decryptStandard(SecretKey key, byte[] tweak, String value, Alpha.CharacterDomain domain) {
        char[] X = value.toCharArray();
        int u = X.length/2;
        int v = X.length - u;
        int b = MoreMath.celining(MoreMath.celining((double)v * MoreMath.log(2, (double)domain.getRadix()))/8.0);
        int d = 4 * MoreMath.celining(b/4.0) + 4;

        char[] A = Arrays.copyOfRange(X, 0, u);
        char[] B = Arrays.copyOfRange(X, u, X.length);
        //System.out.println("A = " + Arrays.toString(A));
        //System.out.println("B = " + Arrays.toString(B));

        ByteBuffer P = ByteBuffer.allocate(16);
        P.put(new byte[] {1,2,1,});
        //3 bytes of the radix
        P.put(ByteBuffer.allocate(4).putInt((int)domain.getRadix()).array(), 1, 3);
        P.put((byte)10);
        P.put((byte)(u%256));
        P.putInt(X.length);
        P.putInt(tweak.length);
        //System.out.println("P = " + Arrays.toString(P.array()));

        //This pads the length of the string to a multiple of 16
        int numZeros = (16 - ((b + tweak.length + 1) % 16)) % 16;

        byte[] Q0 = ByteBuffer.allocate(tweak.length + numZeros).put(tweak).array();
        for (int i=9; i >= 0; i--) {
            byte[] Q2 = getBytes(domain.toNumber(A).toByteArray(), 0, b);
            ByteBuffer Q = ByteBuffer.allocate(Q0.length + Q2.length +1);
            Q.put(Q0);
            Q.put((byte)i);
            Q.put(Q2);
            //System.out.println("Q = " + Arrays.toString(Q.array()));

            byte[] R = prf(key, ByteBuffer.allocate(P.capacity() + Q.capacity()).put(P.array()).put(Q.array()).array());
            //System.out.println("R= " + Arrays.toString(R));

            ByteBuffer S = ByteBuffer.allocate(d);
            if (d < R.length) {
                S.put(Arrays.copyOfRange(R, 0, d));
            } else {
                S.put(R);
                for (int j = R.length; j < d; j += 16) {
                    byte[] pad = new byte[R.length];
                    Arrays.fill(pad, (byte) (j/16));
                    byte[] mac = cipher(key, xor(R, pad, 0));
                    if (d - j < 16) {
                        S.put(Arrays.copyOfRange(mac, 0, (d - j)));
                    }
                    else {
                        S.put(mac);
                    }
                }
            }
            BigInteger y = new BigInteger(1, S.array());
            //System.out.println("S = " + DatatypeConverter.printHexBinary(S.array()));
            //System.out.println("y = " + y);

            int m = i % 2 == 0 ? u : v;
            //System.out.println("m = " + m);
            BigInteger c = domain.toNumber(B).subtract(y).mod(BigInteger.valueOf(domain.getRadix()).pow(m));
            char[] C = getChars(domain.fromNumber(c), 0, m, domain.getChar(0));
            //System.out.println("c = " + c);
            //System.out.println("C = " + Arrays.toString(C));
            B = A;
            A = C;
            //System.out.println("A = " + Arrays.toString(A));
            //System.out.println("B = " + Arrays.toString(B));
        }
        //640 5. Return A || B.
        StringBuilder sb = new StringBuilder();
        sb.append(A);
        sb.append(B);
        return sb.toString();
    }

    public static int minlen(Alpha.CharacterDomain domain) {
        return MoreMath.logInt((int)domain.getRadix(), 1000000, RoundingMode.CEILING);
    }
    public static long maxlen(Alpha.CharacterDomain domain) {
        return (long)(Math.pow(2.0, 32.0) - 1.0);
    }
    public static byte[] cipher(SecretKey key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] ciphertext = cipher.doFinal(data);
            return Arrays.copyOfRange(ciphertext, 0, 16);
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
        System.out.println(Arrays.toString(getBytes(new byte[]{10,47,5,10}, 0, 3)));
        BigInteger num = Alpha.NUMBER.toNumber(new char[]{'1', '2', '0', '3', '7'});
        System.out.println(num);
        System.out.println(Arrays.toString(num.toByteArray()));
        System.out.println("Range Number:    " + minlen(Alpha.NUMBER) + " - " + maxlen(Alpha.NUMBER));
        System.out.println("Range Email:     " + minlen(Alpha.ASCII_EMAIL) + " - " + maxlen(Alpha.ASCII_EMAIL));
        System.out.println("Range Printable: " + minlen(Alpha.ASCII_PRINTABLE) + " - " + maxlen(Alpha.ASCII_PRINTABLE));
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

        message = "The quick brown fox jumps over the lazy dog!";
        enc = encrypt(key, new byte[]{5,19, -8, 28, 34,122,4}, message, Alpha.ASCII_PRINTABLE);
        System.out.println(enc);

        dec = decrypt(key, new byte[]{5,19, -8, 28, 34,122,4}, enc, Alpha.ASCII_PRINTABLE);
        System.out.println(dec);
    }
}
