package com.padesigner.ui;

import javax.crypto.KeyGenerator;
import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {
    public MainMenu() {
        setTitle("PAdESigner: main menu");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));
        setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("Welcome to PAdESigner!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton keyGenButton = new JButton("Generate Key");
        JButton signButton = new JButton("Sign Document");
        JButton verifyButton = new JButton("Verify Signature");

        keyGenButton.addActionListener(e -> {
            new KeyGeneratorUI();
            dispose();
        });

        signButton.addActionListener(e -> {
          new SignerUI();
            dispose();
        });

        verifyButton.addActionListener(e -> {
//            new VerifierUI();
            dispose();
        });

        add(title);
        add(keyGenButton);
        add(signButton);
        add(verifyButton);

        setVisible(true);
    }
}
