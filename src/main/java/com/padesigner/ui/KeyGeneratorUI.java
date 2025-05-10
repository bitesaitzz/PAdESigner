package com.padesigner.ui;

import com.padesigner.crypto.AESUtil;
import com.padesigner.crypto.RSAKeyManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.KeyPair;

public class KeyGeneratorUI extends JFrame {

    public KeyGeneratorUI() {
        setupUI();
    }

    private void setupUI() {
        setTitle("PAdESigner: Key Generator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JTextField pinField = new JPasswordField(20);
        JTextField dirField = new JTextField(20);
        JComboBox<String> drivesComboBox = new JComboBox<>();

        JButton generateButton = createButton("Generate keys", e -> handleGenerateButton(pinField, dirField, drivesComboBox));
        JButton browseButton = createButton("Browse", e -> handleBrowseButton(dirField));
        JButton backButton = createButton("Back", e -> handleBackButton());
        JButton findUSBButton = createButton("Find USB", e -> handleFindUSBButton(drivesComboBox));

        addComponents(pinField, dirField, drivesComboBox, generateButton, browseButton, backButton, findUSBButton);

        setVisible(true);
        handleFindUSBButton(drivesComboBox);
    }

    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    private void addComponents(JTextField pinField, JTextField dirField, JComboBox<String> drivesComboBox,
                                JButton generateButton, JButton browseButton, JButton backButton, JButton findUSBButton) {
        add(new JLabel("Enter PIN:"));
        add(pinField);
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Save public key to:"));
        add(dirField);
        add(browseButton);
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Select USB drive:"));
        add(drivesComboBox);
        add(findUSBButton);
        add(Box.createHorizontalStrut(800));
        add(generateButton);
        add(backButton);
    }

    private void handleBrowseButton(JTextField dirField) {
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

        if (!validateInputs(pin, dir, usbPath)) {
            return;
        }

        try {
            KeyPair keyPair = RSAKeyManager.generateRSAKeyPair();
            saveKeys(keyPair, pin, dir, usbPath);
            JOptionPane.showMessageDialog(this, "Keys generated successfully");
            pinField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating keys: " + ex.getMessage());
        }
    }

    private boolean validateInputs(String pin, String dir, String usbPath) {
        if (pin.isEmpty()) {
            showMessage("PIN is empty");
            return false;
        } else if (pin.length() < 4) {
            showMessage("PIN is too short");
            return false;
        } else if (usbPath == null || usbPath.isEmpty()) {
            showMessage("USB drive not selected");
            return false;
        } else if (dir == null || dir.isEmpty()) {
            showMessage("Directory not selected");
            return false;
        } else if (!new File(dir).exists() || !new File(dir).isDirectory() || !new File(dir).canWrite()) {
            showMessage("Invalid or unwritable directory");
            return false;
        } else if (!new File(usbPath).exists() || !new File(usbPath).isDirectory() || !new File(usbPath).canWrite()) {
            showMessage("Invalid or unwritable USB drive");
            return false;
        }

        if (confirmOverwrite(new File(usbPath + "private_key.enc"), "Private key already exists on USB drive. Do you want to overwrite it?") &&
            confirmOverwrite(new File(dir + "public_key.pem"), "Public key already exists in the directory. Do you want to overwrite it?")) {
            return true;
        }

        return false;
    }

    private boolean confirmOverwrite(File file, String message) {
        if (file.exists()) {
            int result = JOptionPane.showConfirmDialog(this, message, "Warning", JOptionPane.YES_NO_OPTION);
            return result == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private void saveKeys(KeyPair keyPair, String pin, String dir, String usbPath) throws Exception {
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        if (!usbPath.endsWith("/")) {
            usbPath += "/";
        }

        File publicKeyFile = new File(dir + "public_key.pem");
        RSAKeyManager.savePublicKey(keyPair.getPublic(), publicKeyFile);

        File privateKeyFile = new File(usbPath + "private_key.enc");
        AESUtil.encryptAndSavePrivateKey(keyPair.getPrivate(), pin, privateKeyFile);
    }

    private void handleFindUSBButton(JComboBox<String> drivesComboBox) {
        drivesComboBox.removeAllItems();
        try {
            String[] usbDrives = HardwareDetector.getUsbDrivePaths().toArray(new String[0]);
            if (usbDrives.length > 0) {
                drivesComboBox.addItem(usbDrives[0]);
            } else {
                showMessage("No USB drives found");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error getting USB drives: " + ex.getMessage());
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
