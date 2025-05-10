package com.padesigner.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class AESUtil {

    public static byte[] sha256(String pin) throws Exception {
        return hashWithSHA256(pin.getBytes("UTF-8"));
    }

    public static void encryptAndSavePrivateKey(PrivateKey privateKey, String pin, File outputFile) throws Exception {
        byte[] key = sha256(pin);
        byte[] encrypted = encryptWithAES(privateKey.getEncoded(), key);
        writeFile(outputFile, encrypted);
    }

    public static RSAPrivateKey decryptPrivateKey(File encryptedKeyFile, String pin) throws Exception {
        byte[] encryptedKey = readFile(encryptedKeyFile);
        if (encryptedKey.length == 0) {
            throw new IllegalArgumentException("Encrypted key data is empty.");
        }

        byte[] key = sha256(pin);
        byte[] decryptedKey = decryptWithAES(encryptedKey, key);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decryptedKey));
    }

    private static byte[] hashWithSHA256(byte[] input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input);
    }

    private static byte[] encryptWithAES(byte[] data, byte[] key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private static byte[] decryptWithAES(byte[] data, byte[] key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    private static void writeFile(File file, byte[] content) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        } catch (Exception e) {
            throw new Exception("Error saving file: " + e.getMessage(), e);
        }
    }

    private static byte[] readFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (Exception e) {
            throw new Exception("Error reading file: " + e.getMessage(), e);
        }
    }
}
