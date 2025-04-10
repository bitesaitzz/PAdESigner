package com.padesigner.ui;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.padesigner.crypto.RSAKeyManager;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.security.auth.x500.X500Principal;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Date;

public class SignerUI extends JFrame {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private JTextField pdfFileField;
    private JTextField publicKeyField;
    private JTextField pinField;
    private JLabel statusLabel;
    private JComboBox<String> drivesComboBox;

    public SignerUI() {
        setTitle("PAdESigner: Sign Document");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        pdfFileField = new JTextField(20);
        JButton browseButton = new JButton("Choose PDF File");
        publicKeyField = new JTextField(20);
        JButton publicKeyButton = new JButton("Choose Public Key");
        pinField = new JPasswordField(20);
        JButton signButton = new JButton("Sign Document");
        JButton backButton = new JButton("Back");
        statusLabel = new JLabel("Status: Waiting for input");
        drivesComboBox = new JComboBox<>();
        JButton findUSBButton = new JButton("Find USB");

        browseButton.addActionListener(e -> handleBrowsePdfButton());
        publicKeyButton.addActionListener(e -> handlePublicKeyButton());
        signButton.addActionListener(e -> handleSignButton());
        backButton.addActionListener(e -> handleBackButton());
        findUSBButton.addActionListener(e -> handleFindUSBButton(drivesComboBox));

        add(new JLabel("Enter PIN:"));
        add(pinField);
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Select PDF file to sign:"));
        add(pdfFileField);
        add(browseButton);
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Select Public Key:"));
        add(publicKeyField);
        add(publicKeyButton);
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Select USB drive with your private key:"));
        add(drivesComboBox);
        add(findUSBButton);
        add(Box.createHorizontalStrut(800));
        add(signButton);
        add(Box.createHorizontalStrut(800));
        add(statusLabel);
        add(Box.createHorizontalStrut(800));
        add(backButton);

        setVisible(true);
        handleFindUSBButton(drivesComboBox);
    }

    private void handleFindUSBButton(JComboBox<String> drivesComboBox) {
        drivesComboBox.removeAllItems();
        try {
            String[] usbDrives = HardwareDetector.getUsbDrivePaths().toArray(new String[0]);
            if (usbDrives.length > 0) {
                drivesComboBox.addItem(usbDrives[0]);
            } else {
                JOptionPane.showMessageDialog(this, "No USB drives found");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error getting USB drives: " + ex.getMessage());
        }
    }

    private void handleBrowsePdfButton() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select PDF file to sign");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            pdfFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void handlePublicKeyButton() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Public Key");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Public Key Files", "pem"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            publicKeyField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void handleSignButton() {
        String pdfFilePath = pdfFileField.getText();
        String pin = pinField.getText();

        if (pdfFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a PDF file.");
            return;
        }

        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your PIN.");
            return;
        }

        try {
            RSAPrivateKey privateKey = RSAKeyManager.loadPrivateKey((String) drivesComboBox.getSelectedItem(), pin);
            if (privateKey == null) {
                JOptionPane.showMessageDialog(this, "Invalid PIN or failed to load private key.");
                return;
            }
            PublicKey publicKey = RSAKeyManager.loadPublicKey(new File(publicKeyField.getText()));

            if (publicKey == null) {
                JOptionPane.showMessageDialog(this, "Failed to load public key.");
                return;
            }
            File pdfFile = new File(pdfFilePath);
            if (!pdfFile.exists()) {
                JOptionPane.showMessageDialog(this, "PDF file does not exist.");
                return;
            }
            if (!pdfFilePath.endsWith(".pdf")) {
                JOptionPane.showMessageDialog(this, "Invalid file type. Please select a PDF file.");
                return;
            }
            signPDF(pdfFilePath, privateKey, publicKey);
            statusLabel.setText("Status: Document signed successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error signing document: " + ex.getMessage());
            statusLabel.setText("Status: Error signing document.");
        }
    }

    private void handleBackButton() {
        new MainMenu();
        dispose();
    }

    private void signPDF(String pdfFilePath, RSAPrivateKey privateKey, PublicKey publicKey) throws Exception {
        String outputPath = pdfFilePath.replace(".pdf", "_signed.pdf");
        Certificate[] chain = generateCertificateChain(privateKey, publicKey);
        PdfReader reader = new PdfReader(pdfFilePath);
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(outputPath), new StampingProperties());

        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, "BC");

        signer.signDetached(digest, signature, chain, null, null, null, 0, null);

        JOptionPane.showMessageDialog(this, "Document signed successfully. Output: " + outputPath);
        statusLabel.setText("Status: Document signed successfully.");
    }

    private Certificate[] generateCertificateChain(RSAPrivateKey privateKey, PublicKey publicKey) throws Exception {
        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        X509Certificate cert = generateSelfSignedCertificate(keyPair);
        return new Certificate[] { cert };
    }

    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);

        X500Principal subject = new X500Principal("CN=Self-Signed Certificate");
        X500Principal issuer = new X500Principal("CN=Self-Signed Certificate");

        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = calendar.getTime();

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC")
                .build(keyPair.getPrivate());
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serialNumber,
                notBefore,
                notAfter,
                subject,
                keyPair.getPublic());

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certBuilder.build(contentSigner));
    }

}
