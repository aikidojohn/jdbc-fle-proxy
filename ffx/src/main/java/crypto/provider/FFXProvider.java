package crypto.provider;

import java.security.Provider;

public final class FFXProvider extends Provider {
    /**
     * Constructs a provider with the specified name, version number,
     * and information.
     */
    public FFXProvider() {
        super("FFXProvider", 0.1, "Provides the format preserving cipher mode from NIST SP 800-38G REV. 1 (DRAFT)");
        put("Cipher.AES/FFX/NOPADDING", "crypto.provider.FFXCipherSpi");
    }
}
