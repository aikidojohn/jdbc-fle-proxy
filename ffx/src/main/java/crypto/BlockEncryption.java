package crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

public class BlockEncryption {

    private Cipher cipher;


    public static byte[] encrypt(CipherParameters params, byte[] data)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, params.getKey(), params.getIv());
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(CipherParameters params, byte[] data)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126PADDING");
        cipher.init(Cipher.DECRYPT_MODE, params.getKey(), params.getIv());
        return cipher.doFinal(data);
    }

    public static void gcm() throws Exception {
        SecureRandom random = SecureRandom.getInstanceStrong();
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128, random);
        SecretKey key = keyGen.generateKey();

        // Encrypt
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        final byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        GCMParameterSpec spec = new GCMParameterSpec(16 * 8, nonce);
        c.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] aad = "Whatever I like".getBytes();;
        c.updateAAD(aad);
        byte[] encrypted = c.doFinal("test".getBytes());
        System.out.println(DatatypeConverter.printBase64Binary(encrypted));

        c.init(Cipher.DECRYPT_MODE, key, spec);
        c.updateAAD(aad);
        byte[] decrypted = c.doFinal(encrypted);
        System.out.println(new String(decrypted));
    }

    public static void main(String... args) throws Exception {
        for (Provider provider: Security.getProviders()) {
            System.out.println(provider.getName());
            for (Provider.Service s : provider.getServices()) {
                if (s.getType().equalsIgnoreCase("cipher")) {
                    System.out.println("\t" + s.getType() + "\t" + s.getAlgorithm() + "\t" + s);
                }
            }
        }

        SecureRandom random = SecureRandom.getInstanceStrong();
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128, random);
        SecretKey key = keyGen.generateKey();

        // Encrypt
        Cipher c = Cipher.getInstance("AES/CBC/ISO10126PADDING");
        final byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        IvParameterSpec iv = new IvParameterSpec(nonce);

        c.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] plain = new byte[16];
        System.arraycopy("test".getBytes(), 0, plain, 0, "test".getBytes().length);
        byte[] encrypted = c.doFinal("test".getBytes());
        System.out.println(DatatypeConverter.printBase64Binary(encrypted));

        c.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrypted = c.doFinal(encrypted);
        System.out.println(new String(decrypted));

    }
}
