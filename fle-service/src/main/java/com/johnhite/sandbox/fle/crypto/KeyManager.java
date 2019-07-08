package com.johnhite.sandbox.fle.crypto;

import com.johnhite.sandbox.fle.hibernate.KeyDao;
import com.johnhite.sandbox.fle.hibernate.KeyEntity;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

public class KeyManager {
    private KeyDao keyDao;
    private static KeyManager instance;
    private ThreadLocal<Long> currentKeyId = new ThreadLocal<>();

    @Inject
    public KeyManager(KeyDao keyDao) {
        this.keyDao = keyDao;
    }

    public static void setInstance(KeyManager manager) {
        instance = manager;
    }

    public static KeyManager getInstance() {
        return instance;
    }

    public void setCurrentKeyId(long keyId) {
        currentKeyId.set(keyId);
    }

    public Long getCurrentKeyId() {
        return currentKeyId.get();
    }

    public SecretKey getCurrentKey() {
        Long keyId = getCurrentKeyId();
        KeyEntity uk = keyId == null ? null : keyDao.findById(keyId);
        if (null == uk) {
            try {
                SecureRandom random = SecureRandom.getInstanceStrong();
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128, random);
                SecretKey key = keyGen.generateKey();
                uk = new KeyEntity(key.getEncoded());
                keyDao.persist(uk);
                this.setCurrentKeyId(uk.getId());
                return key;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return new SecretKeySpec(uk.getKey(), "AES");
    }

    public Optional<SecretKey> getKey(String table, String column) {
        return Optional.empty();
    }

    private IvParameterSpec generateIv(long id) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] nonce = md.digest(ByteBuffer.allocate(8).putLong(id).array());
        return new IvParameterSpec(Arrays.copyOfRange(nonce, 0, 16));
    }
    private CipherParameters generateKey(long id) throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128, random);
        SecretKey key = keyGen.generateKey();
        IvParameterSpec iv = generateIv(id);
        return CipherParameters.instance("AES", key, iv);
    }
}
