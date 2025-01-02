import javax.swing.SwingUtilities;

import UI.LoginFrame;

public class AppLauncher{
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            new LoginFrame("Login").setVisible(true);
        });
    }
}