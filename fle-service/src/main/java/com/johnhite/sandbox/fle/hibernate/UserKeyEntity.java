package com.johnhite.sandbox.fle.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;

@Entity
@Table( name = "key_mappings")
public class UserKeyEntity {
    @Id
    @Column(name = "id")
    private byte[] id;

    @Column(name = "key_id")
    private long keyId;

    public UserKeyEntity() {}

    public UserKeyEntity(byte[] id, long keyId) {
        this.id = id;
        this.keyId = keyId;
    }

    public static byte[] generateId(String id, String tweak) {
        try {
            return generateId(
                    id.getBytes(Charset.forName("UTF-8")),
                    tweak.getBytes(Charset.forName("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateId(long id, String tweak) {
        try {
            return generateId(
                    ByteBuffer.allocate(8).putLong(id).array(),
                    tweak.getBytes(Charset.forName("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateId(byte[] id, byte[] tweak) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(tweak);
            byte[] digest = md.digest(id);
            return Arrays.copyOfRange(digest, 0, 16);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public static void main(String... args) {
        System.out.println(Arrays.toString(generateId(10, "user.id")));
        System.out.println(Arrays.toString(generateId(11, "user.id")));
        System.out.println(Arrays.toString(generateId(10, "address.id")));
        System.out.println(Arrays.toString(generateId("jhite@example.com", "user.email")));
    }
}
