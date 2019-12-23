package com.johnhite.sandbox.fle.crypto;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Alpha {

    public static CharacterDomain ASCII_PRINTABLE = new AsciiPrintable();
    public static CharacterDomain ASCII_DOMAIN = new AsciiDomain();
    public static CharacterDomain ASCII_EMAIL = new AsciiEmail();
    public static CharacterDomain ASCII_NAME = new AsciiName();
    public static CharacterDomain NUMBER = new NumberDomain();

    public static BigInteger toBase10(String alpha, CharacterDomain domain) {
        BigInteger radix = BigInteger.valueOf(domain.getRadix());
        char[] chars = alpha.toCharArray();
        BigInteger val = BigInteger.ZERO;
        BigInteger place = BigInteger.ONE;
        for (int i = chars.length -1; i >=0; i--) {
            int number = domain.getIndex(chars[i]);
            val = val.add(place.multiply(BigInteger.valueOf(number)));
            place = place.multiply(radix);
        }
        return val;
    }

    public static String fromBase10(BigInteger val, CharacterDomain domain) {
        BigInteger radix = BigInteger.valueOf(domain.getRadix());
        StringBuilder alpha = new StringBuilder();
        BigInteger d = val.divide(radix);
        BigInteger r = val.mod(radix);
        alpha.insert(0, domain.getChar(r.intValue()));
        while (d.compareTo(BigInteger.ZERO) > 0) {
            r = d.mod(radix);
            d = d.divide(radix);
            alpha.insert(0, domain.getChar(r.intValue()));
        }
        return alpha.toString();
    }

    public static void main(String... args) {
        StringBuilder sb = new StringBuilder();
        for (int i= 33; i < 128; i++) {
            sb.append((char)i);
        }
        System.out.println(sb.toString());
        System.out.println(Alpha.toBase10("a", ASCII_NAME));
        System.out.println(Alpha.toBase10("b", ASCII_NAME));
        System.out.println(Alpha.toBase10("z", ASCII_NAME));
        System.out.println(Alpha.toBase10("john", ASCII_NAME));

        System.out.println(Alpha.fromBase10(BigInteger.valueOf(0), ASCII_NAME));
        System.out.println(Alpha.fromBase10(BigInteger.valueOf(1), ASCII_NAME));
        System.out.println(Alpha.fromBase10(new BigInteger("12536691"), ASCII_NAME));
    }

    public interface CharacterDomain {
        int getIndex(char c);
        char getChar(int index);
        long getRadix();
    }

    private static class NumberDomain implements CharacterDomain {
        private char[] table = "0123456789".toCharArray();
        private Map<Character, Integer> indices = new HashMap<>();
        public NumberDomain() {
            for (int i =0; i< table.length; i++) {
                indices.put(table[i], i);
            }
        }
        public int getIndex(char c) {
            return indices.get(c);
        }
        public char getChar(int index) {
            return table[index];
        }
        public long getRadix() {
            return table.length;
        }
    }

    private static class AsciiPrintable implements CharacterDomain {
        private char[] table = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ ".toCharArray();
        private Map<Character, Integer> indices = new HashMap<>();
        public AsciiPrintable() {
            for (int i =0; i< table.length; i++) {
                indices.put(table[i], i);
            }
        }
        public int getIndex(char c) {
            return indices.get(c);
        }
        public char getChar(int index) {
            return table[index];
        }
        public long getRadix() {
            return table.length;
        }
    }

    private static class AsciiDomain implements CharacterDomain {
        private char[] table = "-.0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        private Map<Character, Integer> indices = new HashMap<>();
        public AsciiDomain() {
            for (int i =0; i< table.length; i++) {
                indices.put(table[i], i);
            }
        }
        public int getIndex(char c) {
            return indices.get(c);
        }
        public char getChar(int index) {
            return table[index];
        }
        public long getRadix() {
            return table.length;
        }
    }

    private static class AsciiEmail implements CharacterDomain {
        private char[] table = "!#$%&'*+-./0123456789=?ABCDEFGHIJKLMNOPQRSTUVWXYZ^_`abcdefghijklmnopqrstuvwxyz{|}~".toCharArray();
        private Map<Character, Integer> indices = new HashMap<>();
        public AsciiEmail() {
            for (int i =0; i< table.length; i++) {
                indices.put(table[i], i);
            }
        }
        public int getIndex(char c) {
            return indices.get(c);
        }
        public char getChar(int index) {
            return table[index];
        }
        public long getRadix() {
            return table.length;
        }
    }

    private static class AsciiName implements CharacterDomain {
        private char[] table = "'-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        private Map<Character, Integer> indices = new HashMap<>();
        public AsciiName() {
            for (int i =0; i< table.length; i++) {
                indices.put(table[i], i);
            }
        }
        public int getIndex(char c) {
            return indices.get(c);
        }
        public char getChar(int index) {
            return table[index];
        }
        public long getRadix() {
            return table.length;
        }
    }
}
