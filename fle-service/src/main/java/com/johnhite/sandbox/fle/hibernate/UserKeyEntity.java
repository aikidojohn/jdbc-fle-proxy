package com.johnhite.sandbox.fle.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "user_keys")
public class UserKeyEntity {
    @Column(name = "user_id", nullable=true, insertable = true, updatable = true)
    private Long userId;

    @Id
    @Column(name = "email_id", nullable=false, insertable = true, updatable = false)
    private long emailId;

    @Column(name = "key_id", nullable=false, insertable = true, updatable = false)
    private long keyId;

    public UserKeyEntity() {}

    public UserKeyEntity(long userId, long emailId, long keyId) {
        this.userId = userId;
        this.emailId = emailId;
        this.keyId = keyId;
    }

    public UserKeyEntity(long emailId, long keyId) {
        this.userId = userId;
        this.emailId = emailId;
        this.keyId = keyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getEmailId() {
        return emailId;
    }

    public void setEmailId(long emailId) {
        this.emailId = emailId;
    }

    public long getKey() {
        return keyId;
    }

    public void setKey(long key) {
        this.keyId = key;
    }
}
