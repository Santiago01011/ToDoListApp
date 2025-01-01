package UI;


import COMMON.common;
import DBH.TaskDAO;
import model.Task;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HistoryFrame extends Frame{
    private java.util.List<Task> historyTasks;
    private JPanel centerPanel;
    public HistoryFrame(String title, TasksFrame tasksFrame, int userId){
        super(title);
        setLocationRelativeTo(null);
        addUIComponents();
        historyTasks = TaskDAO.loadTasksFromDatabase(userId, true);
        //display history tasks
        for(Task task : historyTasks){
            JPanel historyPanel = createHistoryPanel(task);
            centerPanel.add(historyPanel);
        }
        //add window listener to save changes and update task list
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                tasksFrame.updateButton.doClick();
            }
        });
    }

    private JPanel createHistoryPanel(Task task){
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setOpaque(false);
        //create components
        javax.swing.JLabel titleLabel = new javax.swing.JLabel(task.getTaskTitle());
        titleLabel.setOpaque(false);
        titleLabel.setForeground(common.getTextColor());
        
        javax.swing.JCheckBox checkBox = new javax.swing.JCheckBox("", task.getIsDone());
        checkBox.setForeground(common.getTextColor());
        checkBox.setToolTipText("Restore task");
        checkBox.setOpaque(false);
        checkBox.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        checkBox.addActionListener(e -> {
            task.setIsDone(checkBox.isSelected());
            TaskDAO.updateTaskInDatabase(task);
            historyPanel.remove(checkBox);
            historyPanel.remove(titleLabel);
            historyPanel.revalidate();
            historyPanel.repaint();
        });


        historyPanel.add(checkBox, BorderLayout.WEST);
        historyPanel.add(titleLabel, BorderLayout.CENTER);

        return historyPanel;
    }

    private void addUIComponents(){
        //Panel to display history of tasks
        centerPanel = new JPanel();
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