package main.java;

import javax.swing.SwingUtilities;

import main.java.UI.LoginFrame;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new LoginFrame("Login").setVisible(true);
            }
        });
    }
}