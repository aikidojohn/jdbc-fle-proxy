package com.johnhite.sandbox.fle.crypto;

import com.johnhite.sandbox.fle.hibernate.KeyDao;
import com.johnhite.sandbox.fle.hibernate.KeyEntity;
import com.johnhite.sandbox.fle.hibernate.UserKeyDao;
import com.johnhite.sandbox.fle.hibernate.UserKeyEntity;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

public class KeyManager {
    private KeyDao keyDao;
    private UserKeyDao userKeyDao;
    private static KeyManager instance;

    @Inject
    public KeyManager(KeyDao keyDao, UserKeyDao userKeyDao) {
        this.keyDao = keyDao;
        this.userKeyDao = userKeyDao;
    }

    public static void setInstance(KeyManager manager) {
        instance = manager;
    }

    public static KeyManager getInstance() {
        return instance;
    }

    public Optional<SecretKey> getKeyByUserAttribute(long userAttribute, String userAttributeName) {
        byte[] id = UserKeyEntity.generateId(userAttribute, userAttributeName);
        System.out.println("Get Key : {" + userAttribute + ", " + userAttributeName + ", " + DatatypeConverter.printHexBinary(id) + "}");
        return getKeyByUserAttributeId(id);
    }

    public Optional<SecretKey> getKeyByUserAttribute(String userAttribute, String userAttributeName) {
        byte[] id = UserKeyEntity.generateId(userAttribute, userAttributeName);
        System.out.println("Get Key : {" + userAttribute + ", " + userAttributeName + ", " + DatatypeConverter.printHexBinary(id) + "}");
        return getKeyByUserAttributeId(id);
    }

    public Optional<SecretKey> getKeyByUserAttributeId( byte[] id ) {
        Optional<UserKeyEntity> entity = userKeyDao.findById(id);
        if(!entity.isPresent()) {
            return Optional.empty();
        }

        KeyEntity rawKey = keyDao.findById(entity.get().getKeyId());
        if (rawKey == null) {
            //oops - this is a problem. should have had a foreign key!
            return Optional.empty();
        }

        return Optional.of(new SecretKeySpec(rawKey.getKey(), "AES"));
    }

    public SecretKey generateTransientKey() {
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128, random);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long persistKey(SecretKey key) {
        KeyEntity ke = new KeyEntity(key.getEncoded());
        return keyDao.persist(ke).getId();
    }

    public void persistKeyMapping(long keyId, String userAttribute, String userAttributeName) {
        byte[] id = UserKeyEntity.generateId(userAttribute, userAttributeName);
        System.out.println("Persist Key : {" + userAttribute + ", " + userAttributeName + ", " + DatatypeConverter.printHexBinary(id) + "}");
        persistKeyMapping(keyId, id);
    }

    public void persistKeyMapping(long keyId, long userAttribute, String userAttributeName) {
        byte[] id = UserKeyEntity.generateId(userAttribute, userAttributeName);
        System.out.println("Persist Key : {" + userAttribute + ", " + userAttributeName + ", " + DatatypeConverter.printHexBinary(id) + "}");
        persistKeyMapping(keyId, id);
    }

    public void persistKeyMapping(long keyId, byte[] userAttributeId) {
        UserKeyEntity uke = new UserKeyEntity(userAttributeId, keyId);
        userKeyDao.persist(uke);
    }

    private IvParameterSpec generateIv(long id) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] nonce = md.digest(ByteBuffer.allocate(8).putLong(id).array());
        return new IvParameterSpec(Arrays.copyOfRange(nonce, 0, 16));
    }
}
