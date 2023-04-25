package crypto;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class AlphaTest {

    @Test
    public void testNumRadix10() {
        long start = System.currentTimeMillis();
        BigInteger big = Alpha.numRadix("1234567890987654321234567890987654321".toCharArray(), Alpha.NUMBER);
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end-start));
        Assert.assertEquals(new BigInteger("1234567890987654321234567890987654321"), big);
    }

    @Test
    public void testStr10() {
        long start = System.currentTimeMillis();
        char[] big = Alpha.strRadix(new BigInteger("1234567890987654321234567890987654321"), Alpha.NUMBER);
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end-start));
        Assert.assertEquals("1234567890987654321234567890987654321", new String(big));
    }
}
