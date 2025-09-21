
import javax.swing.SwingUtilities;

import UI.LoginFrame;
import controller.UserController;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame("Login");
            loginFrame.setController(new UserController());
        });
    }
}