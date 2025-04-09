package com.padesigner.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
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
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(pin.getBytes("UTF-8"));
    }

    public static void encryptAndSavePrivateKey(PrivateKey privateKey, String pin, File outputFile) throws Exception {
        byte[] key = sha256(pin);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encrypted = cipher.doFinal(privateKey.getEncoded());

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(encrypted);
        }
    }
    public static RSAPrivateKey decryptPrivateKey(File encryptedKeyFile, String pin) throws Exception {

        try (FileInputStream fis = new FileInputStream(encryptedKeyFile)) {

            byte[] encryptedKey = fis.readAllBytes();
            if (encryptedKey.length == 0) {
                throw new IllegalArgumentException("Encrypted key data is empty.");
            }


            byte[] key = sha256(pin);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);


            byte[] decryptedKey = cipher.doFinal(encryptedKey);


            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decryptedKey));
        }
    }
}
