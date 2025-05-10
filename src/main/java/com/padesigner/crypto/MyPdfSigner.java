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


public class MyPdfSigner {

    static public void signPDF(String pdfFilePath, RSAPrivateKey privateKey, JLabel statusLabel) throws Exception {
        String outputPath = generateOutputPath(pdfFilePath);
        PdfSigner signer = initializePdfSigner(pdfFilePath, outputPath);

        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, "BC");

        Certificate[] certificateChain = generateSelfSignedCertificateChain(privateKey);

        signer.signDetached(digest, signature, certificateChain, null, null, null, 0, PdfSigner.CryptoStandard.CADES);

        showSuccessMessage(statusLabel, outputPath);
    }

    static private String generateOutputPath(String pdfFilePath) {
        return pdfFilePath.replace(".pdf", "_signed.pdf");
    }

    static private PdfSigner initializePdfSigner(String pdfFilePath, String outputPath) throws Exception {
        PdfReader reader = new PdfReader(pdfFilePath);
        return new PdfSigner(reader, new FileOutputStream(outputPath), new StampingProperties());
    }

    static private void showSuccessMessage(JLabel statusLabel, String outputPath) {
        JOptionPane.showMessageDialog(statusLabel.getParent(), "Document signed successfully. Output: " + outputPath);
        statusLabel.setText("Status: Document signed successfully.");
    }

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

    static private PublicKey generatePublicKeyFromPrivateKey(RSAPrivateKey privateKey) throws Exception {
        BigInteger modulus = privateKey.getModulus();
        BigInteger publicExponent = BigInteger.valueOf(65537);
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicKeySpec);
    }

    static private Date calculateExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        return calendar.getTime();
    }
}
