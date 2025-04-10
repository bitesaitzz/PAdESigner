package com.padesigner.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.X509EncodedKeySpec;

public class RSAKeyManager {
    public static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        return keyPairGenerator.generateKeyPair();
    }

    public static void savePublicKey(PublicKey publicKey, File file) throws Exception {
        byte[] encodedKey = publicKey.getEncoded();
        try (
                FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(encodedKey);
        } catch (Exception e) {
            throw new Exception("Error saving public key: " + e.getMessage(), e);
        }
    }

    public static PublicKey loadPublicKey(File publicKeyFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(publicKeyFile)) {
            byte[] keyBytes = fis.readAllBytes();
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new Exception("Error loading public key: " + e.getMessage(), e);
        }
    }

    public static RSAPrivateKey loadPrivateKey(String usbPath, String pin) throws Exception {
        if (usbPath == null || usbPath.isEmpty()) {
            throw new Exception("USB drive not selected.");
        }
        if (!new File(usbPath).exists()) {
            throw new Exception("USB drive not found.");
        }
        File encryptedKeyFile = new File(usbPath + "private_key.enc");
        if (!encryptedKeyFile.exists()) {
            throw new Exception("Private key not found on USB.");
        }
        return AESUtil.decryptPrivateKey(encryptedKeyFile, pin);
    }

}
