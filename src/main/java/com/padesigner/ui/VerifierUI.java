package com.padesigner.ui;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;
import com.padesigner.crypto.RSAKeyManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.PublicKey;
import java.util.List;

/**
 * VerifierUI is a Swing-based user interface for verifying digital signatures
 * on PDF documents. It allows users to select a signed PDF file and a public
 * key file, and then verifies the signature using the selected public key.
 */
public class VerifierUI extends JFrame {

    private JTextField pdfFileField;
    private JTextField publicKeyFileField;
    private JLabel statusLabel;

    public VerifierUI() {
        setupUI();
    }

    /**
     * Main method to run the VerifierUI.
     * It initializes the UI and sets it visible.
     */
    private void setupUI() {
        setTitle("PAdESigner: Verify Signature");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        pdfFileField = new JTextField(20);
        publicKeyFileField = new JTextField(20);
        statusLabel = new JLabel("Status: Waiting for input");

        JButton browsePdfButton = createButton("Choose Signed PDF", e -> handleBrowsePdfButton());
        JButton browseKeyButton = createButton("Choose Public Key", e -> handleBrowseKeyButton());
        JButton verifyButton = createButton("Verify Signature", e -> handleVerifyButton());
        JButton backButton = createButton("Back", e -> handleBackButton());

        addComponents(browsePdfButton, browseKeyButton, verifyButton, backButton);

        setVisible(true);
    }

    /**
     * Creates a JButton with the specified text and action listener.
     *
     * @param text           The text to display on the button.
     * @param actionListener The ActionListener to handle button clicks.
     * @return A JButton configured with the specified text and action listener.
     */
    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    /**
     * Adds components to the UI.
     *
     * @param browsePdfButton Button to browse for signed PDF files.
     * @param browseKeyButton Button to browse for public key files.
     * @param verifyButton    Button to verify the signature.
     * @param backButton      Button to go back to the main menu.
     */
    private void addComponents(JButton browsePdfButton, JButton browseKeyButton, JButton verifyButton,
            JButton backButton) {
        add(new JLabel("Select signed PDF file:"));
        add(pdfFileField);
        add(browsePdfButton);
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Select public key file:"));
        add(publicKeyFileField);
        add(browseKeyButton);
        add(Box.createHorizontalStrut(800));
        add(verifyButton);
        add(Box.createHorizontalStrut(800));
        add(statusLabel);
        add(Box.createHorizontalStrut(800));
        add(backButton);
    }

    /**
     * Handles the action of browsing for a signed PDF file.
     * Opens a file chooser dialog to select a PDF file and updates the text field.
     */
    private void handleBrowsePdfButton() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Signed PDF File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            pdfFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Handles the action of browsing for a public key file.
     * Opens a file chooser dialog to select a public key file and updates the text
     * field.
     */
    private void handleBrowseKeyButton() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Public Key File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Public Key Files", "pem"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            publicKeyFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Handles the action of verifying the signature of the selected PDF file
     * using the selected public key file.
     * Displays the verification result in a status label and a message dialog.
     */
    private void handleVerifyButton() {
        String pdfFilePath = pdfFileField.getText();
        String publicKeyFilePath = publicKeyFileField.getText();

        if (pdfFilePath.isEmpty()) {
            showMessage("Please select a signed PDF file.");
            return;
        }

        if (publicKeyFilePath.isEmpty()) {
            showMessage("Please select a public key file.");
            return;
        }

        try {
            File pdfFile = new File(pdfFilePath);
            File publicKeyFile = new File(publicKeyFilePath);

            if (!pdfFile.exists() || !pdfFilePath.endsWith(".pdf")) {
                showMessage("Invalid PDF file.");
                return;
            }

            if (!publicKeyFile.exists()) {
                showMessage("Public key file does not exist.");
                return;
            }

            PublicKey publicKey = RSAKeyManager.loadPublicKey(publicKeyFile);
            boolean isValid = verifySignature(pdfFilePath, publicKey);

            if (isValid) {
                statusLabel.setText("Status: Signature is valid.");
                showMessage("Signature is valid.");
            } else {
                statusLabel.setText("Status: Signature is invalid.");
                showMessage("Signature is invalid.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error verifying signature: " + ex.getMessage());
            statusLabel.setText("Status: Error verifying signature.");
        }
    }

    /**
     * Verifies the signature of the PDF file using the provided public key.
     *
     * @param pdfFilePath The path to the signed PDF file.
     * @param publicKey   The public key used for verification.
     * @return true if the signature is valid, false otherwise.
     * @throws Exception If an error occurs during verification.
     */
    private boolean verifySignature(String pdfFilePath, PublicKey publicKey) throws Exception {
        PdfReader reader = new PdfReader(pdfFilePath);
        PdfDocument pdfDoc = new PdfDocument(reader);
        SignatureUtil signUtil = new SignatureUtil(pdfDoc);

        List<String> signatureNames = signUtil.getSignatureNames();
        if (signatureNames.isEmpty()) {
            throw new Exception("No signatures found in the PDF.");
        }

        String signatureName = signatureNames.get(0); // Assuming the first signature
        PdfPKCS7 pkcs7 = signUtil.readSignatureData(signatureName);

        return pkcs7.verifySignatureIntegrityAndAuthenticity()
                && pkcs7.getSigningCertificate().getPublicKey().equals(publicKey);
    }

    /**
     * Handles the action of going back to the main menu.
     * Creates a new instance of MainMenu and disposes the current frame.
     */
    private void handleBackButton() {
        new MainMenu();
        dispose();
    }

    /**
     * Displays a message dialog with the specified message.
     *
     * @param message The message to display in the dialog.
     */
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
