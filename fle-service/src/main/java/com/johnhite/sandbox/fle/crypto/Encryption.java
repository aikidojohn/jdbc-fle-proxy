package com.johnhite.sandbox.fle.crypto;

import javax.crypto.SecretKey;
import java.math.BigInteger;

public interface Encryption<T> {
    T encrypt(SecretKey key, byte[] tweak, T value);
    T decrypt(SecretKey key, byte[] tweak, T value);
}
