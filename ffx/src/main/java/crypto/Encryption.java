package crypto;

import javax.crypto.SecretKey;

public interface Encryption<T> {
    T encrypt(SecretKey key, byte[] tweak, T value);
    T decrypt(SecretKey key, byte[] tweak, T value);
}
