package com.padesigner;

import com.padesigner.ui.MainMenu;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}