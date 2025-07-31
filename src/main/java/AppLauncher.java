import java.util.UUID;

import DBH.DBHandler;
import model.TaskHandler;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import UI.TaskDashboardFrame;
import UI.LoginFrame;
import controller.TaskController;
import controller.UserController;
import COMMON.common;
import themes.CoffeYellow;
import themes.NigthBlue;

public class AppLauncher {
    public static void main(String[] args) {
        // Initialize the Look and Feel before creating any UI components
        try {
            // Set up the custom theme based on user preference
            if (common.useNightMode) {
                UIManager.setLookAndFeel(new NigthBlue());
            } else {
                UIManager.setLookAndFeel(new CoffeYellow());
            }
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize custom LaF, falling back to default: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame("Login");
            loginFrame.setController(new UserController());
        });
    }
}