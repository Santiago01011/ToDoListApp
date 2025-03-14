import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;

public class AppLauncher {
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Running in headless mode. No GUI will be displayed.");
            // Perform headless operations here
        } else {
            SwingUtilities.invokeLater(() -> {
                System.out.println("App is launching...");
                JFrame testFrame = new JFrame("Test Frame");
                testFrame.setSize(400, 300);
                testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                testFrame.setVisible(true);
            });
        }
    }
}