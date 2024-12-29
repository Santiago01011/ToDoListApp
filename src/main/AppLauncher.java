package main;

import javax.swing.SwingUtilities;

// import main.UI.TasksFrame;
import main.UI.LoginFrame;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new LoginFrame("Login").setVisible(true);
                //new TasksFrame("ToDoList", 1).setVisible(true);
            }
        });
    }
}