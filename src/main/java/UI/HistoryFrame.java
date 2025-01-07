package UI;


import COMMON.UserProperties;
import COMMON.common;
import DBH.TaskDAO;
import model.Task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class HistoryFrame extends Frame{
    private final java.util.List<Task> completedTasks;
    private final java.util.List<Task> deletedTasks;
    //private JPanel centerPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JButton toggleColorButton;
    private JButton updateButton;
    public HistoryFrame(String title, TasksFrame tasksFrame, int userId){
        super(title);
        setLocationRelativeTo(null);
        addUIComponents();

        addUIComponentsSouth();
        //load tasks from database
        completedTasks = TaskDAO.loadTasksFromDatabase(userId, true, false);
        deletedTasks = TaskDAO.loadTasksFromDatabase(userId, true, true);
        
        //display history tasks
        for(Task task : completedTasks){
            JPanel taskPanel = createTaskPanel(task, false);
            topPanel.add(taskPanel);
        }
        
        for(Task task : deletedTasks){
            JPanel taskPanel = createTaskPanel(task, true);
            bottomPanel.add(taskPanel);
        }
        //add window listener to save changes and update task list
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                tasksFrame.updateButton.doClick();
            }
        });
        
        //add action listeners to the buttons of the south panel
        toggleColorButton.addActionListener(e -> {
            common.toggleColorMode();
            SwingUtilities.invokeLater(() -> {
                new HistoryFrame("History", tasksFrame, userId).setVisible(true);
                dispose();
            });
        });

        updateButton.addActionListener(e -> {
            dispose();
            tasksFrame.updateButton.doClick();
        });

        

        
    }

    /**
     * Create a panel to display a task
     * @param task - the task to display
     * @param isDeleted - to manage the display of the task
     * @return - panel with the task to be displayed
     */
    private JPanel createTaskPanel(Task task, boolean isDeleted){
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setOpaque(false);
        taskPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 2, 5),
            BorderFactory.createLineBorder(common.getTextColor(), 1)
        ));
        taskPanel.setPreferredSize(new Dimension(getWidth() - 50, 40));
        taskPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        if(!isDeleted){
            JCheckBox checkBox = new JCheckBox("", task.getIsDone());
            checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
            checkBox.setOpaque(false);
            checkBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            taskPanel.add(checkBox, BorderLayout.WEST);

            checkBox.addActionListener(e -> {
                task.setIsDone(!task.getIsDone());
                TaskDAO.updateTaskInDatabase(task);
            });
        }
        
        taskPanel.add(TasksFrame.createTitlePanel(task), BorderLayout.CENTER);
        
        if(isDeleted){
            taskPanel.add(createActionPanel(task, isDeleted), BorderLayout.EAST);
        }else{
            taskPanel.add(createActionPanel(task, isDeleted), BorderLayout.EAST);
        }



        return taskPanel;
    }


 
    private JPanel createActionPanel(Task task, boolean isDeleted) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        
        ImageIcon deleteIcon = common.getDeleteIcon();
        JButton deleteButton = new JButton(deleteIcon);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setPreferredSize(new Dimension(20, 20));
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setToolTipText("Delete Task");
        
        if(isDeleted){
            ImageIcon restoreIcon = common.getRestoreIcon();
            JButton restoreButton = new JButton(restoreIcon);
            restoreButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            restoreButton.setPreferredSize(new Dimension(20, 20));
            restoreButton.setBorderPainted(false);
            restoreButton.setContentAreaFilled(false);
            restoreButton.setToolTipText("Restore Task");
            actionPanel.add(restoreButton);

            deleteButton.addActionListener(e -> {
                TaskDAO.deleteTaskFromDatabase(task);
                bottomPanel.remove((Component) actionPanel.getParent());
                bottomPanel.revalidate();
                bottomPanel.repaint();
            });
        }else{
            ImageIcon editIcon = common.getEditIcon();
            JButton editButton = new JButton(editIcon);
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editButton.setPreferredSize(new Dimension(20, 20));
            editButton.setBorderPainted(false);
            editButton.setContentAreaFilled(false);
            editButton.setToolTipText("Edit Task");
            actionPanel.add(editButton);

            deleteButton.addActionListener(e -> {
                TaskDAO.deleteTaskFromDatabase(task);
                topPanel.remove((Component) actionPanel.getParent());
                topPanel.revalidate();
                topPanel.repaint();
            });
        }

        actionPanel.add(deleteButton);

        return actionPanel;
    }

    /**
     * Initialize and add the UI components for both panels, and set the layout of the frame
     * 
     * The top panel will display the completed tasks
     * The bottom panel will display the deleted tasks
     * Each panel will have a scroll pane to allow scrolling if the tasks exceed the panel size
     * and their own action buttons
     */
    private void addUIComponents(){
        
        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        mainPanel.setOpaque(false);

        JLabel topLabel = new JLabel("Completed Tasks");
        topLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        topLabel.setAlignmentX(CENTER_ALIGNMENT);
        topLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(common.getPrimaryColor());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel bottomLabel = new JLabel("Deleted Tasks");
        bottomLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        bottomLabel.setAlignmentX(CENTER_ALIGNMENT);
        bottomLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(common.getPrimaryColor());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane topScrollPane = new JScrollPane(topPanel);
        topScrollPane.setOpaque(false);
        topScrollPane.getViewport().setOpaque(true);        
        
        JScrollPane bottomScrollPane = new JScrollPane(bottomPanel);
        bottomScrollPane.setOpaque(false);
        bottomScrollPane.getViewport().setOpaque(true);

        topPanel.add(topLabel);
        mainPanel.add(topScrollPane);
        bottomPanel.add(bottomLabel);
        mainPanel.add(bottomScrollPane);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Adds UI components to the south panel.
     * The south panel contains buttons for various actions such as updating the task list, toggling color mode, viewing history, logging out, and user configuration.
     * 
     * Components:
     * - Go back Button: Go back to the previous frame.
     * - Update Button: Updates the task list.
     * - Toggle Color Button: Toggles between light and dark modes.
     */
    private void addUIComponentsSouth(){
        //Panel to add the buttons to update and view history
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // Make panel transparent
        southPanel.setOpaque(false);
        //create components
        ImageIcon backIcon = common.getLogOutIcon();
        JButton backButton = new JButton(backIcon);

        updateButton = new JButton("Update");
        updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        ImageIcon toggleColorIcon = common.getModeIcon();
        toggleColorButton = new JButton(toggleColorIcon);
        toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        //add components to southPanel
        southPanel.add(backButton);
        southPanel.add(updateButton);
        southPanel.add(toggleColorButton);
        //add southPanel to the frame
        add(southPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

    }


}