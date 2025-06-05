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

/**
 * Utility class for AES encryption and decryption, and SHA-256 hashing.
 * This class provides methods to encrypt and decrypt private keys using AES,
 * generate SHA-256 hashes from PINs, and handle file I/O for these operations.
 */
public class AESUtil {
    /**
     * Encrypts a private key using AES and saves it to a file.
     * The PIN is used to derive the AES encryption key via SHA-256 hashing.
     *
     * @param privateKey The private key to encrypt.
     * @param pin        The PIN used to generate the AES key.
     * @param outputFile The file where the encrypted private key will be saved.
     * @throws Exception If an error occurs during encryption or file writing.
     */
    public static void encryptAndSavePrivateKey(PrivateKey privateKey, String pin, File outputFile) throws Exception {
        byte[] key = sha256(pin);
        byte[] encrypted = encryptWithAES(privateKey.getEncoded(), key);
        writeFile(outputFile, encrypted);
    }

    /**
     * Decrypts an RSA private key from a file using AES.
     * The PIN is used to derive the AES decryption key via SHA-256 hashing.
     *
     * @param encryptedKeyFile The file containing the encrypted private key.
     * @param pin              The PIN used to generate the AES key.
     * @return The decrypted RSA private key.
     * @throws Exception If an error occurs during decryption, file reading, or if
     *                   the key data is invalid/empty.
     *                   Specifically, an IllegalArgumentException is thrown if the
     *                   encrypted key data is empty,
     *                   and a generic Exception can be thrown if decryption fails
     *                   (e.g., due to an incorrect PIN).
     */
    public static RSAPrivateKey decryptPrivateKey(File encryptedKeyFile, String pin) throws Exception {
        byte[] encryptedKey = readFile(encryptedKeyFile);
        if (encryptedKey.length == 0) {
            throw new IllegalArgumentException("Encrypted key data is empty.");
        }

        byte[] key = sha256(pin);
        byte[] decryptedKey = decryptWithAES(encryptedKey, key);
        if (decryptedKey == null || decryptedKey.length == 0) {
            throw new Exception("Failed to decrypt private key. Invalid PIN or corrupted data.");
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decryptedKey));
    }

    /**
     * Generates a SHA-256 hash of the provided PIN.
     *
     * @param pin The PIN to hash.
     * @return The SHA-256 hash of the PIN as a byte array.
     * @throws Exception If an error occurs during hashing.
     */
    private static byte[] sha256(String pin) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(pin.getBytes("UTF-8"));
    }

    /**
     * Encrypts data using AES encryption.
     *
     * @param data The data to encrypt.
     * @param key  The AES key for encryption.
     * @return The encrypted data as a byte array.
     * @throws Exception If an error occurs during encryption.
     */
    private static byte[] encryptWithAES(byte[] data, byte[] key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts data using AES decryption.
     *
     * @param data The data to decrypt.
     * @param key  The AES key for decryption.
     * @return The decrypted data as a byte array.
     * @throws Exception If an error occurs during decryption.
     */
    private static byte[] decryptWithAES(byte[] data, byte[] key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * Writes byte content to a file.
     *
     * @param file    The file to write to.
     * @param content The byte content to write.
     * @throws Exception If an error occurs during file writing.
     */
    private static void writeFile(File file, byte[] content) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        } catch (Exception e) {
            throw new Exception("Error saving file: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the content of a file into a byte array.
     *
     * @param file The file to read.
     * @return The content of the file as a byte array.
     * @throws Exception If an error occurs during file reading.
     */
    private static byte[] readFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (Exception e) {
            throw new Exception("Error reading file: " + e.getMessage(), e);
        }
    }
}
