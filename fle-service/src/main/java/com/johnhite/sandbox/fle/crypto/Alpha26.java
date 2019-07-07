package com.johnhite.sandbox.fle.crypto;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Alpha26 {

    private static BigInteger toBase10(String alpha) {
        char[] chars = alpha.toCharArray();
        BigInteger val = BigInteger.ZERO;
        BigInteger place = BigInteger.ONE;
        int p = 1;
        for (int i = chars.length -1; i >=0; i--) {
            int number = (byte)chars[i] - (byte)'a';
            val = val.add(place.multiply(BigInteger.valueOf(number)));
            place = place.multiply(BigInteger.valueOf(26));
        }
        return val;
    }

    private static String toBase26(BigInteger val) {
        BigInteger radix = BigInteger.valueOf(26);
        StringBuilder alpha = new StringBuilder();
        BigInteger d = val.divide(radix);
        BigInteger r = val.mod(radix);
        alpha.insert(0, (char)(r.intValue() + (int)'a'));
        while (d.compareTo(BigInteger.ZERO) > 0) {
            r = d.mod(radix);
            d = d.divide(radix);
            alpha.insert(0, (char)(r.intValue() + (int)'a'));
        }
        return alpha.toString();
    }

    public static void main(String... args) {
        StringBuilder sb = new StringBuilder();
        for (int i= 33; i < 128; i++) {
            sb.append((char)i);
        }
        System.out.println(sb.toString());
        System.out.println(Alpha26.toBase10("a"));
        System.out.println(Alpha26.toBase10("b"));
        System.out.println(Alpha26.toBase10("z"));
        System.out.println(Alpha26.toBase10("john"));

        System.out.println(Alpha26.toBase26(BigInteger.valueOf(0)));
        System.out.println(Alpha26.toBase26(BigInteger.valueOf(1)));
        System.out.println(Alpha26.toBase26(new BigInteger("167843")));
    }
}
