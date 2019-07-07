package com.johnhite.sandbox.fle.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherParameters {
    private final SecretKey key;
    private final IvParameterSpec iv;
    private final String algorithm;

    private CipherParameters(String algorithm, SecretKey key, IvParameterSpec iv) {
        this.key = key;
        this.iv = iv;
        this.algorithm = algorithm;
    }

    public static CipherParameters instance(String algorithm, byte[] keyb, byte[] ivb) {
        SecretKeySpec sk = new SecretKeySpec(keyb, algorithm);
        IvParameterSpec ivs = new IvParameterSpec(ivb);
        return new CipherParameters(algorithm, sk, ivs);
    }

    public static CipherParameters instance(String algorithm, SecretKey key, IvParameterSpec iv) {
        return new CipherParameters(algorithm, key, iv);
    }

    public SecretKey getKey() {
        return key;
    }

    public IvParameterSpec getIv() {
        return iv;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
