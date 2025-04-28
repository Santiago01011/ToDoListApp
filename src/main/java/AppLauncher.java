import java.util.UUID;

import DBH.NewDBHandler;
import model.TaskHandler;
import javax.swing.SwingUtilities;
import UI.TaskDashboardFrame;
import UI.LoginFrame;
import controller.TaskController;
import controller.UserController;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame("Login");
            loginFrame.setController(new UserController());
        });
    }
}