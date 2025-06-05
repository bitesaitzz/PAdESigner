package com.padesigner.crypto;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.text.pdf.security.DigestAlgorithms;

import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;

import java.security.KeyFactory;
import com.itextpdf.signatures.BouncyCastleDigest;

/**
 * Utility class for signing PDF documents using the PAdES standard.
 * It handles the creation of self-signed certificates and applying digital
 * signatures
 * to PDF files using iText.
 */
public class MyPdfSigner {

    /**
     * Signs the specified PDF file using the provided RSA private key.
     * 
     * @param pdfFilePath The path to the PDF file to be signed.
     * @param privateKey  The RSA private key used for signing.
     * @param statusLabel A JLabel to display status messages.
     * @throws Exception If an error occurs during the signing process.
     */
    static public void signPDF(String pdfFilePath, RSAPrivateKey privateKey, JLabel statusLabel) throws Exception {
        String outputPath = generateOutputPath(pdfFilePath);
        PdfSigner signer = initializePdfSigner(pdfFilePath, outputPath);

        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, "BC");

        Certificate[] certificateChain = generateSelfSignedCertificateChain(privateKey);

        signer.signDetached(digest, signature, certificateChain, null, null, null, 0, PdfSigner.CryptoStandard.CADES);

        showSuccessMessage(statusLabel, outputPath);
    }

    /**
     * Generates the output path for the signed PDF file by appending "_signed" to
     * the original filename.
     * 
     * @param pdfFilePath The path to the original PDF file.
     * @return A string representing the output file path.
     */
    static private String generateOutputPath(String pdfFilePath) {
        return pdfFilePath.replace(".pdf", "_signed.pdf");
    }

    /**
     * Initializes a PdfSigner object for the given input and output PDF file paths.
     * 
     * @param pdfFilePath The path to the source PDF file.
     * @param outputPath  The path where the signed PDF will be saved.
     * @return An initialized PdfSigner object.
     * @throws Exception If an error occurs while reading the PDF or creating the
     *                   output stream.
     */
    static private PdfSigner initializePdfSigner(String pdfFilePath, String outputPath) throws Exception {
        PdfReader reader = new PdfReader(pdfFilePath);
        return new PdfSigner(reader, new FileOutputStream(outputPath), new StampingProperties());
    }

    /**
     * Displays a success message dialog and updates the status label.
     * 
     * @param statusLabel The JLabel to display status messages on.
     * @param outputPath  The path to the signed PDF file.
     */
    static private void showSuccessMessage(JLabel statusLabel, String outputPath) {
        JOptionPane.showMessageDialog(statusLabel.getParent(), "Document signed successfully. Output: " + outputPath);
        statusLabel.setText("Status: Document signed successfully.");
    }

    /**
     * Generates a self-signed certificate chain based on the provided private key.
     * The public key is derived from the private key.
     * The certificate is valid for one year from the time of creation.
     * This method creates a self-signed X.509 certificate using Bouncy Castle APIs.
     * It uses the SHA-256 with RSA encryption algorithm for signing.
     * The certificate is created with a common name (CN) of "Self-Signed
     * Certificate".
     *
     * @param privateKey The RSA private key used to sign the certificate.
     * @return An array containing a single self-signed X.509 certificate.
     * @throws Exception If an error occurs during key generation or certificate
     *                   creation.
     */
    static private Certificate[] generateSelfSignedCertificateChain(RSAPrivateKey privateKey) throws Exception {
        PublicKey publicKey = generatePublicKeyFromPrivateKey(privateKey);
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        X500Principal subject = new X500Principal("CN=Self-Signed Certificate");
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = calculateExpiryDate();

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC")
                .build(privateKey);
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serialNumber, notBefore, notAfter, subject, keyPair.getPublic());

        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certBuilder.build(contentSigner));

        return new Certificate[] { certificate };
    }

    /**
     * Generates an RSA public key from a given RSA private key.
     * It uses the modulus from the private key and a standard public exponent
     * (65537).
     * 
     * @param privateKey The RSA private key.
     * @return The corresponding RSA public key.
     * @throws Exception If an error occurs during public key spec generation or key
     *                   factory instantiation.
     */
    static private PublicKey generatePublicKeyFromPrivateKey(RSAPrivateKey privateKey) throws Exception {
        BigInteger modulus = privateKey.getModulus();
        BigInteger publicExponent = BigInteger.valueOf(65537);
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicKeySpec);
    }

    /**
     * Calculates an expiry date that is one year from the current date.
     * 
     * @return A Date object representing the expiry date.
     */
    static private Date calculateExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        return calendar.getTime();
    }
}
