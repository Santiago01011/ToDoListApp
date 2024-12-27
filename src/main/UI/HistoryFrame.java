package main.UI;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HistoryFrame extends Frame{
    public HistoryFrame(String title, TasksFrame tasksFrame, int userId){
        super(title);
        setLocationRelativeTo(null);
        addUIComponents();
        //add window listener to save changes and update task list
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                //appFrame.saveChangesDB();
                //appFrame.updateTaskList();
                tasksFrame.dispose();
                new TasksFrame("ToDoList", userId).setVisible(true);
            }
        });
    }

    private void addUIComponents(){
        //Panel to display history of tasks
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        //create components
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        //add components to centerPanel
        centerPanel.add(scrollPane);
        //add centerPanel to the frame
        add(centerPanel);
    }
}