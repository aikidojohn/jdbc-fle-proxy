package crypto;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alpha {

    public static CharacterDomain ASCII_PRINTABLE = new AsciiPrintable();
    public static CharacterDomain ASCII_DOMAIN = new AsciiDomain();
    public static CharacterDomain ASCII_EMAIL = new AsciiEmail();
    public static CharacterDomain ASCII_NAME = new AsciiName();
    public static CharacterDomain NUMBER = new NumberDomain();
    public static CharacterDomain BASE36 = new Base36Domain();

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

    public interface CharacterDomain {
        int getIndex(char c);
        char getChar(int index);
        long getRadix();
        default BigInteger toNumber(char[] symbols) {
            BigInteger rad = BigInteger.valueOf(getRadix());
            BigInteger x = BigInteger.valueOf(0);
            BigInteger radpow = BigInteger.ONE;
            for (int i=0; i < symbols.length; i++) {
                x = x.add(radpow.multiply(BigInteger.valueOf(getIndex(symbols[symbols.length - i - 1]))));
                radpow = radpow.multiply(rad);
            }
            return x;
        }

        default char[] fromNumber(BigInteger n) {
            List<Character> chars = new ArrayList<>();
            BigInteger rad = BigInteger.valueOf(getRadix());
            BigInteger val = n;
            while (val.compareTo(BigInteger.ZERO) > 0) {
                BigInteger[] divAndRem = val.divideAndRemainder(rad);
                int ind = divAndRem[1].intValue();
                chars.add(getChar(ind));
                val = divAndRem[0];
            }
            char[] result = new char[chars.size()];
            for (int i=0; i < chars.size(); i++) {
                result[result.length - 1 - i] = chars.get(i);
            }
            return result;
        }
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
    private static class Base36Domain implements CharacterDomain {
        private char[] table = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        private Map<Character, Integer> indices = new HashMap<>();
        public Base36Domain() {
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


    public static BigInteger numRadix(char[] symbols, Alpha.CharacterDomain domain) {
        return domain.toNumber(symbols);
    }

    public static char[] strRadix(BigInteger n, Alpha.CharacterDomain domain) {
        return domain.fromNumber(n);
    }

    public static class BitBuffer {
        private byte[] buffer;
        private int mostSignificantByte = 0;
        private BitBuffer(byte[] value) {
            buffer = value;
            for (int i=0; i < value.length; i++) {
                if (value[i] != 0) {
                    mostSignificantByte = i;
                    break;
                }
            }
        }

        public static BitBuffer wrap(byte[] value ) {
            return new BitBuffer(value);
        }

        public static BitBuffer allocate(int length) {
            return new BitBuffer(new byte[length]);
        }

        public byte[] asByteArray() {
            return this.buffer;
        }

        public void leftShift(boolean increment) {
            int index = buffer.length - 1;
            int msb = (buffer[index] >> 7 & 0x01);
            buffer[index] = (byte)(buffer[index] << 1);
            if (increment) {
                buffer[index] += 1;
            }

            while (index > 0) {
                index--;
                int nextMsb = (buffer[index] >> 7 & 0x01);
                buffer[index] = (byte)(buffer[index] << 1);
                buffer[index] += msb;
                msb = nextMsb;
                //detect carry
                if (index < mostSignificantByte && msb > 0) {
                    mostSignificantByte = index;
                }
            }
            if (msb != 0) {
                throw new BufferOverflowException();
            }
        }

        public void sub(int startBit, int bits, byte value) {
            int byteIndex = buffer.length -1 - (startBit / 8);
            int bitIndex = startBit % 8;
            if (bitIndex + bits > 8) {
                //split across multiple bytes

            } else {
                byte toSub = (byte)(value << bitIndex);
                byte newVal = (byte)(buffer[byteIndex] - toSub);
                buffer[byteIndex] = newVal;
            }

        }

        public void add(int startBit, int bits, byte value) {

        }
    }

    public static void main(String... args) {
        System.out.println(DatatypeConverter.printHexBinary(new BigInteger("255").toByteArray()));
        System.out.println(DatatypeConverter.printHexBinary(new BigInteger("256").toByteArray()));
        System.out.println(DatatypeConverter.printHexBinary(new BigInteger("257").toByteArray()));
        System.out.println(Arrays.toString(new BigInteger("255").toByteArray()));
        System.out.println(Arrays.toString(new BigInteger("256").toByteArray()));
        System.out.println(Arrays.toString(new BigInteger("257").toByteArray()));

        BitBuffer buffer = BitBuffer.wrap(new byte[]{(byte)0x00,(byte)0xFF});
        System.out.println(DatatypeConverter.printHexBinary(buffer.asByteArray()));
        buffer.sub(3,5, (byte)0xF);
        System.out.println(DatatypeConverter.printHexBinary(buffer.asByteArray()));
    }
}
