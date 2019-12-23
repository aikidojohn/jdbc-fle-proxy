package com.johnhite.sandbox.fle.crypto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoreMath {

    public static double log(int base, double val) {
        return Math.log(val) / Math.log(base);
    }

    public static int logInt(int base, int val, RoundingMode rounding) {
        double l = log(base, (double)val);
        switch(rounding) {
            case UP:
            case CEILING:
                return (int)Math.ceil(l);
            case HALF_UP:
            case HALF_EVEN:
            case HALF_DOWN:
                return (int)Math.round(l);
            default:
                return (int)Math.floor(l);
        }
    }

    public static int celining(double val) {
        return (int)Math.ceil(val);
    }
}
