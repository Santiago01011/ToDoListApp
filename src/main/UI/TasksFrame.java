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
import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TasksFrame extends Frame{
    private int userId;
    public List<Task> tasks;
    public List<Task> tasksToAdd;
    public List<Task> tasksToDelete;
    public List<Task> tasksToUpdate;
    
    private JTextArea taskDetailsArea;
    private JPanel centerPanel;
    private JLabel todoLabel;
    private JButton toggleColorButton;
    public TasksFrame(String title, int userId){
        super(title);
        this.userId = userId;
        setTitle(title);
        //Initialize the lists
        tasksToAdd = new ArrayList<>();
        tasksToDelete = new ArrayList<>();
        tasksToUpdate = new ArrayList<>();
        addUIComponentsNorth();
        addUIComponentsCenter();
        addUIComponentsSouth();
        tasks = TaskDAO.loadTasksFromDatabase(userId, false);
        updateTaskList();
    }
    
    //Method to initialize the North Panel
    private void addUIComponentsNorth(){
        //Panel to add new tasks with title and description textfields
        JPanel northPanel = new JPanel(new GridLayout(3,1));
        //subpanels
        JPanel todoTitlePanel = new JPanel(new FlowLayout());
        JPanel titlePanel = new JPanel(new FlowLayout());
        JPanel descriptionPanel = new JPanel(new FlowLayout());
        // Make panel transparent
        northPanel.setOpaque(false);
        todoTitlePanel.setOpaque(false);
        titlePanel.setOpaque(false);
        descriptionPanel.setOpaque(false);
        //create components
        todoLabel = new JLabel("To Do List");
        todoLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        todoLabel.setForeground(common.getTextColor());
        
        JButton addButton = new JButton("Add Task");
        addButton.setBackground(common.getSecondaryColor());
        addButton.setPreferredSize(new Dimension(100, 20));
        addButton.setForeground(common.getTextColor());
        addButton.setFont(new Font("Dialog", Font.BOLD, 12));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JTextField taskField = new JTextField("Task title", 20);   
        taskField.setBackground(common.getTertiaryColor());
        taskField.setForeground(common.getTextColor());
        taskField.setFont(new Font("Dialog", Font.BOLD, 14));
        
        JTextField taskDescriptionField = new JTextField("Task description",32);
        taskDescriptionField.setBackground(common.getTertiaryColor());
        taskDescriptionField.setForeground(common.getTextColor());
        taskDescriptionField.setFont(new Font("Dialog", Font.PLAIN, 14));
        
        //add components to subpanels
        todoTitlePanel.add(todoLabel);
        titlePanel.add(taskField);
        titlePanel.add(addButton);
        descriptionPanel.add(taskDescriptionField);
        //add subpanels to northPanel
        northPanel.add(todoTitlePanel);
        northPanel.add(titlePanel);
        northPanel.add(descriptionPanel);
        //add northPanel to the frame
        add(northPanel, BorderLayout.NORTH);
  
        //add focus listeners to the textfield and descriptionfield to set the text to empty when focused
        addFocusListeners(taskField, "Task title");
        addFocusListeners(taskDescriptionField, "Task description");
        addNorthActionListeners(addButton, taskField, taskDescriptionField);
    }

    //Method to add focus listeners to the textfield and a default text
    //addFocusListeners method is defined in the Frame class

    //Method to add action listeners to the north panel buttons
    private void addNorthActionListeners(JButton addButton, JTextField taskField, JTextField taskDescriptionField){
        addButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (!taskField.getText().equals("Task title")) {
                    addTask(taskField.getText(), taskDescriptionField.getText());
                    taskField.setText("Task title");
                    taskDescriptionField.setText("Task description");
                }
            }
        });
        taskField.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (!taskField.getText().equals("Task title")) {
                    addTask(taskField.getText(), taskDescriptionField.getText());
                    taskField.setText("Task title");
                    taskDescriptionField.setText("Task description");
                }
            }            
        });

        taskDescriptionField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                addTask(taskField.getText(), taskDescriptionField.getText());
                taskField.setText("Task title");
                taskDescriptionField.setText("Task description");
            }
        });
    }
    
    //Method to initialize the South Panel
    private void addUIComponentsSouth(){
        //Panel to add the buttons to update and view history
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // Make panel transparent
        southPanel.setOpaque(false);
        //create components
        JButton updateButton = new JButton("Update");
        updateButton.setBackground(common.getSecondaryColor());
        updateButton.setForeground(common.getTextColor());
        updateButton.setFont(new Font("Dialog", Font.BOLD, 12));
        updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        ImageIcon toggleColorIcon = new ImageIcon(common.getModePath());
        toggleColorIcon = new ImageIcon(toggleColorIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        toggleColorButton = new JButton(toggleColorIcon);
        toggleColorButton.setBackground(common.getSecondaryColor());
        toggleColorButton.setForeground(common.getTextColor());
        toggleColorButton.setFont(new Font("Dialog", Font.BOLD, 12));
        toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton historyButton = new JButton("History");
        historyButton.setBackground(common.getSecondaryColor());
        historyButton.setForeground(common.getTextColor());
        historyButton.setFont(new Font("Dialog", Font.BOLD, 12));
        historyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //add components to southPanel
        southPanel.add(updateButton);
        southPanel.add(toggleColorButton);
        southPanel.add(historyButton);
        //add southPanel to the frame
        add(southPanel, BorderLayout.SOUTH);
        addSouthActionListeners(updateButton, toggleColorButton, historyButton);
    }

    //Method to add South Panel action listeners
    private void addSouthActionListeners(JButton updateButton, JButton toggleColorButton, JButton historyButton){
        //add action listeners to the buttons
        updateButton.addActionListener(e -> {
            updateTaskList();
        });
        historyButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                new HistoryFrame("History", TasksFrame.this, 1).setVisible(true);
            }
        });

        toggleColorButton.addActionListener(e -> {
            common.toggleColorMode();
            //dispose frame and recall
            dispose();
            new TasksFrame("To Do List", 1).setVisible(true);
        });
    }

    //Method to initialize the Center Panel
    //this panel is gonna be updated by other methods   
    private void addUIComponentsCenter(){
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(common.getPrimaryColor());

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.getViewport().setBackground(common.getPrimaryColor()); // Set the background color of the scroll pane viewport
        scrollPane.setOpaque(false); // Make the scroll pane transparent
        scrollPane.getViewport().setOpaque(true);        
        taskDetailsArea = new JTextArea(10, 20);
        
        taskDetailsArea.setEditable(false);
        taskDetailsArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);
        taskDetailsArea.setBackground(common.getTertiaryColor());
        taskDetailsArea.setForeground(common.getTextColor());
        taskDetailsArea.setFont(new java.awt.Font("Dialog", Font.PLAIN, 14));
        //add gap between the text area and the boarders
        taskDetailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 5));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(taskDetailsArea, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
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
            }
        });
        checkboxPanel.add(updateCheckBox);
        return checkboxPanel;
    }
    
    private JPanel createTitlePanel(Task task) {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel taskLabel = new JLabel(task.getTaskTitle());
        taskLabel.setFont(new java.awt.Font("Dialog", Font.BOLD, 13));
        taskLabel.setForeground(common.getTextColor());
        titlePanel.add(taskLabel);
        return titlePanel;
    }
    
    private JPanel createActionPanel(Task task){
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
    
        ImageIcon viewIcon = new ImageIcon(common.getViewPath());
        ImageIcon deleteIcon = new ImageIcon(common.getDeletePath());
        viewIcon = new ImageIcon(viewIcon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
        deleteIcon = new ImageIcon(deleteIcon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
        
        JButton viewButton = new JButton(viewIcon);
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
    
    //Method to show the changes in the center Panel
    public void updateTaskList(){
        centerPanel.removeAll();
        //load all the tasks from the database
        tasks = TaskDAO.loadTasksFromDatabase(getUserId(), false);
        //display the tasks in the centerPanel
        for(Task task : tasks){
            JPanel taskPanel = createTaskPanel(task);
            centerPanel.add(taskPanel);
        }
        centerPanel.revalidate();
        centerPanel.repaint();
        // Set initial focus to the title each update
        SwingUtilities.invokeLater(() -> {
            todoLabel.requestFocusInWindow();
        });
    }
    // Method to get the user ID
    public int getUserId() {
        return userId;
    }

    // Method to view the task details

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
        saveChangesToDatabase();
        updateTaskList();
    }

}
