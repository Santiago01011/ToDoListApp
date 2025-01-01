import javax.swing.SwingUtilities;

import DBH.PSQLtdldbh;
import UI.LoginFrame;

public class AppLauncher {
    public static void main(String[] args) {
        // Add a shutdown hook to close the connection pool
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down application...");
            PSQLtdldbh.closePool();
        }));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame("Login").setVisible(true);
            }
        });
    }
}