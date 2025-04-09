package com.padesigner.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;


import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import com.padesigner.crypto.AESUtil;

public class SignerUI extends JFrame {
    private JTextField pdfFileField;
    private JTextField pinField;
    private JLabel statusLabel;

    public SignerUI() {
        setTitle("PAdESigner: Sign Document");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        pdfFileField = new JTextField(20);
        JButton browseButton = new JButton("Choose PDF File");
        pinField = new JPasswordField(20);
        JButton signButton = new JButton("Sign Document");
        JButton backButton = new JButton("Back");
        statusLabel = new JLabel("Status: Waiting for input");

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select PDF file to sign");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                pdfFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        signButton.addActionListener(e -> {
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

                RSAPrivateKey privateKey = loadPrivateKey(pin);
                if (privateKey == null) {
                    JOptionPane.showMessageDialog(this, "Invalid PIN or failed to load private key.");
                    return;
                }

//                signPDF(pdfFilePath, privateKey); TODO: Implement PDF signing logic
                statusLabel.setText("Status: Document signed successfully.");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error signing document: " + ex.getMessage());
                statusLabel.setText("Status: Error signing document.");
            }
        });


        backButton.addActionListener(e -> {
            new MainMenu();
            dispose();
        });
        add(new JLabel("Enter PIN:"));
        add(pinField);
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Select PDF file to sign:"));
        add(pdfFileField);
        add(browseButton);
        add(Box.createHorizontalStrut(800));
        add(signButton);
        add(Box.createHorizontalStrut(800));
        add(statusLabel);
        add(Box.createHorizontalStrut(800));
        add(backButton);

        setVisible(true);

    }
    private RSAPrivateKey loadPrivateKey(String pin) throws Exception {

        File encryptedKeyFile = new File("usb/private_key.enc"); //TODO : Change to USB path
        if (!encryptedKeyFile.exists()) {
            throw new Exception("Private key not found on USB.");
        }
        return AESUtil.decryptPrivateKey(encryptedKeyFile, pin);
    }

    private void signPDF(String pdfFilePath, RSAPrivateKey privateKey){
        // TODO: Implement PDF signing logic
    }

}
