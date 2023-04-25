package crypto.provider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

public class FFXCipherSpi extends CipherSpi {
    private String mode;
    private String padding;
    private AlgorithmParameters parameters;
    private byte[] iv;
    private int cipherMode;

    @Override protected void engineSetMode(String s) throws NoSuchAlgorithmException {
        mode = s;
    }

    @Override protected void engineSetPadding(String s) throws NoSuchPaddingException {
        padding = s;
    }

    @Override protected int engineGetBlockSize() {
        return 128;
    }

    @Override protected int engineGetOutputSize(int i) {
        return 0;
    }

    @Override protected byte[] engineGetIV() {
        return iv;
    }

    @Override protected AlgorithmParameters engineGetParameters() {
        return parameters;
    }

    @Override protected void engineInit(int i, Key key, SecureRandom secureRandom) throws InvalidKeyException {
        this.cipherMode = i;

    }

    @Override protected void engineInit(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {

    }

    @Override protected void engineInit(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {

    }

    @Override protected byte[] engineUpdate(byte[] bytes, int i, int i1) {
        return new byte[0];
    }

    @Override protected int engineUpdate(byte[] bytes, int i, int i1, byte[] bytes1, int i2) throws ShortBufferException {
        return 0;
    }

    @Override protected byte[] engineDoFinal(byte[] bytes, int i, int i1) throws IllegalBlockSizeException, BadPaddingException {
        return new byte[0];
    }

    @Override protected int engineDoFinal(byte[] bytes, int i, int i1, byte[] bytes1, int i2) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return 0;
    }

    public static void main(String... args) throws Exception {
        Security.addProvider(new FFXProvider());
        Cipher c = Cipher.getInstance("AES/FFX/NOPADDING", "FFX");
        c.init();
        /*for (Provider p : Security.getProviders()) {
            System.out.println(p.getName());
            for (Provider.Service s : p.getServices()) {
                if ("Cipher".equalsIgnoreCase(s.getType()))
                    System.out.println("\t" + s.getType() + " :: " + s.getAlgorithm() + " :: " + s.getClassName() + " :: " + s.toString());
            }
        }*/
    }
}
