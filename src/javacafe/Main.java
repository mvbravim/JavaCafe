package javacafe;

import javacafe.gui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread (best practice for Swing)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
