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
import java.io.IOException;

/**
 * Manages RSA key pairs, including generation, saving, and loading.
 * It handles saving public keys in PEM format and loading encrypted private
 * keys
 * from a specified location (simulating a USB drive) using AES decryption.
 */
public class RSAKeyManager {

    /**
     * Generates a new RSA key pair with a key size of 4096 bits.
     *
     * @return A newly generated RSA KeyPair.
     * @throws Exception If an error occurs during key pair generation.
     */
    public static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Saves the provided public key to a file in PEM format.
     * 
     * @param publicKey The public key to save.
     * @param file      The file where the public key will be saved.
     * @throws Exception If an error occurs during conversion or file writing.
     */
    public static void savePublicKey(PublicKey publicKey, File file) throws Exception {
        String publicKeyPEM = convertPublicKeyToPEM(publicKey);
        writeFile(file, publicKeyPEM);
    }

    /**
     * Loads a public key from a PEM formatted file.
     *
     * @param publicKeyFile The file containing the public key in PEM format.
     * @return The loaded PublicKey object.
     * @throws Exception If the file is empty, contains invalid data, or an error
     *                   occurs during loading.
     */
    public static PublicKey loadPublicKey(File publicKeyFile) throws Exception {
        String publicKeyPEM = readFile(publicKeyFile);
        if (publicKeyPEM == null || publicKeyPEM.trim().isEmpty()) {
            throw new IllegalArgumentException("Public key file is empty or contains no data.");
        }
        return convertPEMToPublicKey(publicKeyPEM);
    }

    /**
     * Loads and decrypts an RSA private key from a file located at the specified
     * USB path.
     * The private key file is expected to be named "private_key.enc" and encrypted
     * using AES.
     *
     * @param usbPath The path to the USB drive directory.
     * @param pin     The PIN used to decrypt the private key.
     * @return The decrypted RSA private key.
     * @throws Exception If the USB path is invalid, the encrypted key file is not
     *                   found,
     *                   or an error occurs during decryption (e.g., incorrect PIN).
     */
    public static RSAPrivateKey loadPrivateKey(String usbPath, String pin) throws Exception {
        validateUSBPath(usbPath);
        File encryptedKeyFile = new File(usbPath + "private_key.enc");
        if (!encryptedKeyFile.exists()) {
            throw new Exception("Private key not found on USB.");
        }
        return AESUtil.decryptPrivateKey(encryptedKeyFile, pin);
    }

    /**
     * Converts a PublicKey object into a PEM formatted string.
     *
     * @param publicKey The PublicKey to convert.
     * @return A string representing the public key in PEM format.
     */
    private static String convertPublicKeyToPEM(PublicKey publicKey) {
        return "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
    }

    private static PublicKey convertPEMToPublicKey(String publicKeyPEM) throws Exception {
        if (publicKeyPEM == null || publicKeyPEM.trim().isEmpty()) {
            throw new IllegalArgumentException("PEM string is empty.");
        }

        // Clean up the PEM string by removing headers, footers, and whitespace
        // This regex handles various line endings and multiple whitespace characters
        // It also ensures only valid Base64 characters remain after cleaning
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

    /**
     * Writes a string content to a file.
     *
     * @param file    The file to write to.
     * @param content The string content to write.
     * @throws Exception If an error occurs during file writing.
     */
    private static void writeFile(File file, String content) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        } catch (IOException e) {
            throw new Exception("Error saving file: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the entire content of a file into a string.
     *
     * @param file The file to read.
     * @return The content of the file as a string.
     * @throws Exception If an error occurs during file reading.
     */
    private static String readFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] keyBytes = fis.readAllBytes();
            return new String(keyBytes);
        } catch (IOException e) {
            throw new Exception("Error reading file: " + e.getMessage(), e);
        }
    }

    /**
     * Validates if the provided path is a valid, existing directory.
     *
     * @param path The path string to validate.
     * @param name A descriptive name for the path (e.g., "USB drive").
     * @throws Exception If the path is null, empty, or does not exist as a
     *                   directory.
     */
    private static void validateUSBPath(String usbPath) throws Exception {
        if (usbPath == null || usbPath.isEmpty()) {
            throw new Exception("USB drive not selected.");
        }
        if (!new File(usbPath).exists()) {
            throw new Exception("USB drive not found.");
        }
    }
}
