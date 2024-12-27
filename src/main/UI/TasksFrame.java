package main.UI;

import main.COMMON.common;
import main.DBH.TaskDAO;
import main.model.Task;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TasksFrame extends Frame{
    public List<Task> tasks;
    public List<Task> tasksToAdd;
    public List<Task> tasksToDelete;
    public List<Task> tasksToUpdate;
    
    private JTextArea taskDetailsArea;
    private JPanel centerPanel;
    public TasksFrame(String title, int userId){
        super(title);
        addUIComponentsNorth();
        addUIComponentsCenter();
        addUIComponentsSouth();
        tasks = TaskDAO.loadTasksFromDatabase(userId, false);
        updateTaskList();
    }

    private void addUIComponentsNorth(){
        //Panel to add new tasks with title and description textfields
        JPanel northPanel = new JPanel(new GridLayout(2,1));
        //subpanels
        JPanel titlePanel = new JPanel(new FlowLayout());
        JPanel descriptionPanel = new JPanel(new FlowLayout());
        // Make panel transparent
        northPanel.setOpaque(false); 
        titlePanel.setOpaque(false);
        descriptionPanel.setOpaque(false);
        //create components
        JButton addButton = new JButton("Add Task");
        addButton.setBackground(common.SECONDARY_COLOR);
        addButton.setPreferredSize(new Dimension(100, 20));
        addButton.setForeground(common.TEXT_COLOR);
        addButton.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
        addButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        JTextField taskField = new JTextField(20);
        taskField.setBackground(common.TERTIARY_COLOR);
        taskField.setForeground(common.TEXT_COLOR);
        taskField.setFont(new java.awt.Font("Dialog", Font.BOLD, 14));
        JTextField taskDescriptionField = new JTextField(32);
        taskDescriptionField.setBackground(common.TERTIARY_COLOR);
        taskDescriptionField.setForeground(common.TEXT_COLOR);
        taskDescriptionField.setFont(new java.awt.Font("Dialog", Font.PLAIN, 14));
        //add components to subpanels
        titlePanel.add(taskField);
        titlePanel.add(addButton);
        descriptionPanel.add(taskDescriptionField);
        //add subpanels to northPanel
        northPanel.add(titlePanel);
        northPanel.add(descriptionPanel);
        //add northPanel to the frame
        add(northPanel, BorderLayout.NORTH);
    }

    private void addUIComponentsSouth(){
        //Panel to add the buttons to update and view history
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // Make panel transparent
        southPanel.setOpaque(false);
        //create components
        JButton updateButton = new JButton("Update");
        updateButton.setBackground(common.SECONDARY_COLOR);
        updateButton.setForeground(common.TEXT_COLOR);
        updateButton.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
        updateButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        JButton historyButton = new JButton("History");
        historyButton.setBackground(common.SECONDARY_COLOR);
        historyButton.setForeground(common.TEXT_COLOR);
        historyButton.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
        historyButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        //add components to southPanel
        southPanel.add(updateButton);
        southPanel.add(historyButton);
        //add southPanel to the frame
        add(southPanel, BorderLayout.SOUTH);


        //add action listeners to the buttons
        updateButton.addActionListener(e -> {
            //update the task in the database
            //update the task list
        });

        historyButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                new HistoryFrame("History", TasksFrame.this, 1).setVisible(true);
            }
        });

        
    }

    //this panel is gonna be updated by other methods   
    private void addUIComponentsCenter(){
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(common.PRIMARY_COLOR);

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.getViewport().setBackground(common.PRIMARY_COLOR); // Set the background color of the scroll pane viewport
        scrollPane.setOpaque(false); // Make the scroll pane transparent
        scrollPane.getViewport().setOpaque(true);        taskDetailsArea = new JTextArea(10, 20);
        
        taskDetailsArea.setEditable(false);
        taskDetailsArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);
        taskDetailsArea.setBackground(common.TERTIARY_COLOR);
        taskDetailsArea.setForeground(common.TEXT_COLOR);
        taskDetailsArea.setFont(new java.awt.Font("Dialog", Font.PLAIN, 14));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(taskDetailsArea, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    public void updateTaskList(){
        centerPanel.removeAll();
        //load all the tasks from the database
        tasks = TaskDAO.loadTasksFromDatabase(1, false);
        //display the tasks in the centerPanel
        for(Task task : tasks){
            JPanel taskPanel = createTaskPanel(task);
            centerPanel.add(taskPanel);
        }
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private JPanel createTaskPanel(Task task) {
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setOpaque(false);
        taskPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        taskPanel.add(createCheckboxPanel(task), BorderLayout.WEST);
        taskPanel.add(createTitlePanel(task), BorderLayout.CENTER);
        taskPanel.add(createActionPanel(task), BorderLayout.EAST);
        return taskPanel;
    }

    private JPanel createCheckboxPanel(Task task) {
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.setOpaque(false);
        JCheckBox updateCheckBox = new JCheckBox("", task.getIsDone());
        updateCheckBox.setToolTipText("Mark as Done");
        updateCheckBox.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updateCheckBox.setOpaque(false);
        updateCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                task.setIsDone(!task.getIsDone());
                if (!tasksToUpdate.contains(task)) {
                    tasksToUpdate.add(task);
                }
                saveChangesToDatabase();
                updateTaskList();
            }
        });
        checkboxPanel.add(updateCheckBox);
        return checkboxPanel;
    }
    
    private JPanel createTitlePanel(Task task) {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel taskLabel = new JLabel(task.getTaskTitle());
        titlePanel.add(taskLabel);
        return titlePanel;
    }
    
    private JPanel createActionPanel(Task task) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
    
        ImageIcon viewIcon = new ImageIcon("src/main/assets/view.png");
        ImageIcon deleteIcon = new ImageIcon("src/main/assets/delete.png");
        viewIcon = new ImageIcon(viewIcon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
        deleteIcon = new ImageIcon(deleteIcon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
    
        JButton viewButton = new JButton(viewIcon);
        viewButton.setPreferredSize(new Dimension(20, 20));
        viewButton.setBorderPainted(false);
        viewButton.setContentAreaFilled(false);
        viewButton.setToolTipText("View Task Details");
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewTaskDetails(task);
            }
        });
    
        JButton deleteButton = new JButton(deleteIcon);
        deleteButton.setPreferredSize(new Dimension(20, 20));
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setToolTipText("Delete Task");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tasksToAdd.contains(task)) {
                    tasksToAdd.remove(task);
                } else {
                    tasksToDelete.add(task);
                }
                tasks.remove(task);
                saveChangesToDatabase();
                updateTaskList();
            }
        });
    
        actionPanel.add(viewButton);
        actionPanel.add(deleteButton);
        return actionPanel;
    }

    // Method to view the task details
    private void viewTaskDetails(Task task) {
        taskDetailsArea.setText(task.viewTaskDesc());
    }
    
    // Method to save changes to the database
    private void saveChangesToDatabase(){
        for (Task task : tasksToAdd){
            TaskDAO.saveTaskToDatabase(task);
        }
        for (Task task : tasksToUpdate){
            TaskDAO.updateTaskInDatabase(task);
        }
        for (Task task : tasksToDelete){
            TaskDAO.deleteTaskFromDatabase(task);
        }
        tasksToAdd.clear();
        tasksToUpdate.clear();
        tasksToDelete.clear();
    }

    // Method to add a new task
    public void addTask(String taskTitle, String description) {
        Task task = new Task(tasks.size() + 1, taskTitle, description, 1);
        tasks.add(task);
        tasksToAdd.add(task);
        updateTaskList();
    }
}
