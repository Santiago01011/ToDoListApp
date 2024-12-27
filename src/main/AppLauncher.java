package main;

import javax.swing.SwingUtilities;

import main.UI.TasksFrame;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new TasksFrame("ToDoList", 1).setVisible(true);
            }
        });
    }
}