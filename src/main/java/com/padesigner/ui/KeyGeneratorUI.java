package com.padesigner.ui;

import com.padesigner.crypto.AESUtil;
import com.padesigner.crypto.RSAKeyManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.security.KeyPair;

public class KeyGeneratorUI extends JFrame {
    public KeyGeneratorUI() {
        setTitle("PAdESigner: Key Generator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JTextField pinField = new JPasswordField(20);
        JButton generateButton = new JButton("Generate keys");
        JLabel dirLabel = new JLabel("Save public key to:");
        JTextField dirField = new JTextField(20);
        JButton browseButton = new JButton("Browse");
        JButton backButton = new JButton("Back");
        JComboBox<String> drivesComboBox = new JComboBox<>();
        JButton findUSBButton = new JButton("Find USB");

        backButton.addActionListener(e -> handleBackButton());
        generateButton.addActionListener(e -> handleGenerateButton(pinField, dirField, drivesComboBox));
        findUSBButton.addActionListener(e -> handleFindUSBButton(drivesComboBox));
        browseButton.addActionListener(e -> handleBrouseButton(drivesComboBox, dirField));

        add(new JLabel("Enter PIN:"));
        add(pinField);
        add(Box.createHorizontalStrut(800));
        add(dirLabel);
        add(dirField);
        add(browseButton);
        add(Box.createHorizontalStrut(800));
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Select USB drive:"));
        add(drivesComboBox);
        add(findUSBButton);
        add(Box.createHorizontalStrut(800));
        add(generateButton);
        add(backButton);
        setVisible(true);
        handleFindUSBButton(drivesComboBox);
    }

    private void handleBrouseButton(JComboBox<String> drivesComboBox, JTextField dirField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose folder to save public key");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            dirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void handleBackButton() {
        new MainMenu();
        dispose();
    }

    private void handleGenerateButton(JTextField pinField, JTextField dirField, JComboBox<String> drivesComboBox) {
        String pin = pinField.getText();
        String dir = dirField.getText();
        String usbPath = (String) drivesComboBox.getSelectedItem();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "PIN is empty");
            return;
        } else if (pin.length() < 4) {
            JOptionPane.showMessageDialog(this, "PIN is too short");
            return;
        } else if (usbPath == null || usbPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "USB drive not selected");
            return;
        }
        if (dir == null || dir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Directory not selected");
            return;
        }
        if (!new File(dir).exists()) {
            JOptionPane.showMessageDialog(this, "Directory does not exist");
            return;
        }
        if (!new File(usbPath).exists()) {
            JOptionPane.showMessageDialog(this, "USB drive does not exist");
            return;
        }
        if (!new File(usbPath).isDirectory()) {
            JOptionPane.showMessageDialog(this, "USB drive is not a directory");
            return;
        }
        if (!new File(dir).isDirectory()) {
            JOptionPane.showMessageDialog(this, "Directory is not a directory");
            return;
        }
        if (!new File(dir).canWrite()) {
            JOptionPane.showMessageDialog(this, "Directory is not writable");
            return;
        }
        if (!new File(usbPath).canWrite()) {
            JOptionPane.showMessageDialog(this, "USB drive is not writable");
            return;
        }
        if (new File(usbPath + "private_key.enc").exists()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Private key already exists on USB drive. Do you want to overwrite it?", "Warning",
                    JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (new File(dir + "public_key.pem").exists()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Public key already exists in the directory. Do you want to overwrite it?", "Warning",
                    JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        if (!usbPath.endsWith("/")) {
            usbPath += "/";
        }

        try {
            KeyPair keyPair = RSAKeyManager.generateRSAKeyPair();
            File publicKeyFile = new File(dir + "public_key.pem");
            RSAKeyManager.savePublicKey(keyPair.getPublic(), publicKeyFile);
            File privateKeyFile = new File(usbPath + "private_key.enc");
            AESUtil.encryptAndSavePrivateKey(keyPair.getPrivate(), pin, privateKeyFile);
            JOptionPane.showMessageDialog(this, "Keys generated successfully");
            pinField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating keys: " + ex.getMessage());
        }
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
}
