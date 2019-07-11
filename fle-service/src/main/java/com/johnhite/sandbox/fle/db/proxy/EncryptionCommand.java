package com.johnhite.sandbox.fle.db.proxy;

import javax.crypto.SecretKey;

public interface EncryptionCommand {
    void encrypt(SecretKey key) throws Exception;
}
