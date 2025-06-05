package com.padesigner.ui;

import javax.swing.*;
import java.awt.*;

/**
 * MainMenu is a Swing-based user interface for the PAdESigner application.
 * It serves as the main menu, allowing users to navigate to different
 * functionalities
 * such as key generation, signing documents, and verifying signatures.
 */
public class MainMenu extends JFrame {

    public MainMenu() {
        setupUI();
    }

    /**
     * Main method to run the MainMenu.
     * It initializes the UI and sets it visible.
     */
    private void setupUI() {
        setTitle("PAdESigner: main menu");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));
        setBackground(Color.LIGHT_GRAY);

        JLabel title = createTitleLabel("Welcome to PAdESigner!");
        JButton keyGenButton = createNavigationButton("Generate Key", KeyGeneratorUI.class);
        JButton signButton = createNavigationButton("Sign Document", SignerUI.class);
        JButton verifyButton = createNavigationButton("Verify Signature", VerifierUI.class);

        add(title);
        add(keyGenButton);
        add(signButton);
        add(verifyButton);

        setVisible(true);
    }

    /**
     * Creates a JLabel with the specified text and centered alignment.
     *
     * @param text The text to display on the label.
     * @return A JLabel configured with the specified text and centered alignment.
     */
    private JLabel createTitleLabel(String text) {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        return title;
    }

    /**
     * Creates a JButton with the specified text and action listener to navigate to
     * the target class.
     *
     * @param text        The text to display on the button.
     * @param targetClass The class to navigate to when the button is clicked.
     * @return A JButton configured with the specified text and action listener.
     */
    private JButton createNavigationButton(String text, Class<? extends JFrame> targetClass) {
        JButton button = new JButton(text);
        button.addActionListener(e -> navigateTo(targetClass));
        return button;
    }

    /**
     * Navigates to the specified target class by creating a new instance of the
     * class,
     * setting it visible, and disposing the current frame.
     *
     * @param targetClass The class to navigate to.
     */
    private void navigateTo(Class<? extends JFrame> targetClass) {
        try {
            JFrame targetFrame = targetClass.getDeclaredConstructor().newInstance();
            targetFrame.setVisible(true);
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error navigating to " + targetClass.getSimpleName() + ": " + ex.getMessage());
        }
    }
}
