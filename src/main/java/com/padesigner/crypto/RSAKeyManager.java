package com.padesigner.crypto;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class RSAKeyManager {
    public static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        return keyPairGenerator.generateKeyPair();
    }

    public static void savePublicKey(PublicKey publicKey, File file) throws Exception {
        byte[] encodedKey = publicKey.getEncoded();
        try (
                FileOutputStream fos = new FileOutputStream(file)
        ) {
            fos.write(encodedKey);
        } catch (Exception e) {
            throw new Exception("Error saving public key: " + e.getMessage(), e);
        }
    }
}
