package com.padesigner.ui;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setupUI();
    }

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

    private JLabel createTitleLabel(String text) {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        return title;
    }

    private JButton createNavigationButton(String text, Class<? extends JFrame> targetClass) {
        JButton button = new JButton(text);
        button.addActionListener(e -> navigateTo(targetClass));
        return button;
    }

    private void navigateTo(Class<? extends JFrame> targetClass) {
        try {
            JFrame targetFrame = targetClass.getDeclaredConstructor().newInstance();
            targetFrame.setVisible(true);
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error navigating to " + targetClass.getSimpleName() + ": " + ex.getMessage());
        }
    }
}
