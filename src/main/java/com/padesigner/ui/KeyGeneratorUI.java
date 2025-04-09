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
        JLabel statusUSB = new JLabel("USB not found(for testing uses directory:"+ System.getProperty("user.dir") + "/usb/)");
        JButton findUSBButton = new JButton("Find USB");
        backButton.addActionListener(e -> {
            new MainMenu();
            dispose();
        });

        generateButton.addActionListener(e -> {
            String pin = pinField.getText();
            String dir = dirField.getText();
            if(pin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "PIN is empty");
                return;
            }
            else if(pin.length() < 4) {
                JOptionPane.showMessageDialog(this, "PIN is too short");
                return;
            }
            else if(dir.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Directory is empty");
                return;
            }
            else if(!dir.endsWith("/")) {
                dir += "/";
            }

            try{
                KeyPair keyPair = RSAKeyManager.generateRSAKeyPair();
                File publicKeyFile = new File(dir+"public_key.pem");
                RSAKeyManager.savePublicKey(keyPair.getPublic(), publicKeyFile);
                //create usb directory if not exists
                File usbDir = new File("usb");
                if (!usbDir.exists()) {
                    usbDir.mkdir();
                }
                File usbPath = new File("usb/" +"private_key.enc"); // CHANGE TO USB
                AESUtil.encryptAndSavePrivateKey(keyPair.getPrivate(), pin, usbPath);
                JOptionPane.showMessageDialog(this, "Keys generated successfully");
                pinField.setText("");
            }
            catch (Exception ex){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error generating keys: " + ex.getMessage());
            }}
            );



        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose folder to save public key");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                dirField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        findUSBButton.addActionListener(e -> {
            ;//FIND USB
        });
        add(new JLabel("Enter PIN:"));
        add(pinField);
       add(Box.createHorizontalStrut(800));
        add(dirLabel);
        add(dirField);
        add(browseButton);
       add(Box.createHorizontalStrut(800));
        add(generateButton);
        add(Box.createHorizontalStrut(800));
        add(statusUSB);
        add(findUSBButton);
        add(Box.createHorizontalStrut(800));
        add(backButton);
        setVisible(true);
    }
}
