package com.padesigner.ui;

import com.padesigner.crypto.RSAKeyManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.awt.event.ActionListener;

import com.padesigner.crypto.MyPdfSigner;

/**
 * SignerUI is a Swing-based user interface for signing PDF documents using a
 * private key stored on a USB drive.
 * It allows users to select a PDF file, enter their PIN, and choose the USB
 * drive containing their private key.
 * The signed document is saved with a new name indicating it has been signed.
 */
public class SignerUI extends JFrame {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private JTextField pdfFileField;
    private JTextField pinField;
    private JLabel statusLabel;
    private JComboBox<String> drivesComboBox;

    /**
     * Constructor for SignerUI.
     * Initializes the UI components and sets up the event handlers.
     */
    public SignerUI() {
        setupUI();
        handleFindUSBButton(drivesComboBox);
    }

    /**
     * Main method to run the SignerUI.
     * It initializes the UI and sets it visible.
     */
    private void setupUI() {
        setTitle("PAdESigner: Sign Document");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        pdfFileField = new JTextField(20);
        pinField = new JPasswordField(20);
        statusLabel = new JLabel("Status: Waiting for input");
        drivesComboBox = new JComboBox<>();

        JButton browseButton = createButton("Choose PDF File", e -> handleBrowsePdfButton());
        JButton signButton = createButton("Sign Document", e -> handleSignButton());
        JButton backButton = createButton("Back", e -> handleBackButton());
        JButton findUSBButton = createButton("Find USB", e -> handleFindUSBButton(drivesComboBox));

        addComponents(browseButton, signButton, backButton, findUSBButton);

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
     * @param browseButton  The JButton to browse for the PDF file.
     * @param signButton    The JButton to sign the document.
     * @param backButton    The JButton to go back to the main menu.
     * @param findUSBButton The JButton to find USB drives.
     */
    private void addComponents(JButton browseButton, JButton signButton, JButton backButton, JButton findUSBButton) {
        add(new JLabel("Enter PIN:"));
        add(pinField);
        add(Box.createHorizontalStrut(800));
        add(new JLabel("Select PDF file to sign:"));
        add(pdfFileField);
        add(browseButton);
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
    }

    /**
     * Handles the action of finding USB drives and populating the combo box.
     *
     * @param drivesComboBox The JComboBox to populate with USB drive paths.
     */
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

    /**
     * Handles the action of browsing for a PDF file to sign.
     */
    private void handleBrowsePdfButton() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select PDF file to sign");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            pdfFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Handles the action of signing the document.
     */
    private void handleSignButton() {
        String pdfFilePath = pdfFileField.getText();
        String pin = pinField.getText();

        if (pdfFilePath.isEmpty()) {
            showMessage("Please select a PDF file.");
            return;
        }

        if (pin.isEmpty()) {
            showMessage("Please enter your PIN.");
            return;
        }

        try {
            RSAPrivateKey privateKey = RSAKeyManager.loadPrivateKey((String) drivesComboBox.getSelectedItem(), pin);
            if (privateKey == null) {
                showMessage("Invalid PIN or failed to load private key.");
                return;
            }
            File pdfFile = new File(pdfFilePath);
            if (!pdfFile.exists()) {
                showMessage("PDF file does not exist.");
                return;
            }
            if (!pdfFilePath.endsWith(".pdf")) {
                showMessage("Invalid file type. Please select a PDF file.");
                return;
            }
            MyPdfSigner.signPDF(pdfFilePath, privateKey, statusLabel);
            statusLabel.setText("Status: Document signed successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error signing document: " + ex.getMessage());
            statusLabel.setText("Status: Error signing document.");
        }
    }

    /**
     * Handles the action of going back to the main menu.
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
