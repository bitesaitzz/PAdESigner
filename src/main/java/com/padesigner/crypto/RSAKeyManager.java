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
import java.util.Base64;

public class RSAKeyManager {

    public static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        return keyPairGenerator.generateKeyPair();
    }

    public static void savePublicKey(PublicKey publicKey, File file) throws Exception {
        String publicKeyPEM = convertPublicKeyToPEM(publicKey);
        writeFile(file, publicKeyPEM);
    }

    public static PublicKey loadPublicKey(File publicKeyFile) throws Exception {
        String publicKeyPEM = readFile(publicKeyFile);
        return convertPEMToPublicKey(publicKeyPEM);
    }

    public static RSAPrivateKey loadPrivateKey(String usbPath, String pin) throws Exception {
        validateUSBPath(usbPath);
        File encryptedKeyFile = new File(usbPath + "private_key.enc");
        if (!encryptedKeyFile.exists()) {
            throw new Exception("Private key not found on USB.");
        }
        return AESUtil.decryptPrivateKey(encryptedKeyFile, pin);
    }

    private static String convertPublicKeyToPEM(PublicKey publicKey) {
        return "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
    }

    private static PublicKey convertPEMToPublicKey(String publicKeyPEM) throws Exception {
        publicKeyPEM = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("[\\r\\n]+", "")
                .trim();

        if (!publicKeyPEM.matches("^[A-Za-z0-9+/=]+$")) {
            throw new IllegalArgumentException("Invalid Base64 content in public key file.");
        }

        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private static void writeFile(File file, String content) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        } catch (Exception e) {
            throw new Exception("Error saving file: " + e.getMessage(), e);
        }
    }

    private static String readFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] keyBytes = fis.readAllBytes();
            return new String(keyBytes);
        } catch (Exception e) {
            throw new Exception("Error reading file: " + e.getMessage(), e);
        }
    }

    private static void validateUSBPath(String usbPath) throws Exception {
        if (usbPath == null || usbPath.isEmpty()) {
            throw new Exception("USB drive not selected.");
        }
        if (!new File(usbPath).exists()) {
            throw new Exception("USB drive not found.");
        }
    }
}
