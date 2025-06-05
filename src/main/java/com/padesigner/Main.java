package com.padesigner;

import com.padesigner.ui.MainMenu;

import javax.swing.*;

/**
 * Main is the entry point for the PAdESigner application.
 * It initializes the main menu UI and starts the application.
 */
public class Main {
    /**
     * Main method to run the PAdESigner application.
     * It initializes the main menu UI and sets it visible.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}