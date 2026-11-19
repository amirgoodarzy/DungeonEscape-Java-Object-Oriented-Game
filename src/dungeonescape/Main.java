package dungeonescape;

import dungeonescape.ui.GameWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for Dungeon Escape.
 * University of Messina - Course: Object-Oriented Programming (Prof. Salvatore Distefano)
 * Author: Amirmohammad Goodarzy
 */
public class Main {
    public static void main(String[] args) {
        // Run GUI initialization on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Set native system look and feel if available
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Default Swing look and feel fallback
            }

            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
