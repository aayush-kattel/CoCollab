package main;

import ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use system look but override with custom colors
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}