package admin;

import admin.ui.AdminFrame;
import javax.swing.*;

/**
 * Entry point for the Admin Client application
 * Run this separately from the game
 */
public class AdminMain {
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch on EDT
        SwingUtilities.invokeLater(() -> {
            AdminFrame frame = new AdminFrame();
            frame.setVisible(true);
        });
    }
}