import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;
import AUTHCLI.AuthService;

public class AppLauncher {
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Running in headless mode. No GUI will be displayed.");
            AuthService authService = new AuthService("https://auth.example.com");
            try {
                authService.initiateRegistration("test.email.com", "password123", "Test User");
            } catch (Exception e) {
                //e.printStackTrace();
            }
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