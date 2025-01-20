package UI;

import COMMON.UserProperties;
import COMMON.common;
import DBH.*;
import model.Task;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class TasksFrame extends Frame{
    private int userId;
    public List<Task> tasks;
    public List<Task> tasksToDelete;
    public List<Task> tasksToEdit;
    
    private JTextArea taskDetailsArea;
    private JPanel centerPanel;
    public JPanel taskPanel;
    private JLabel todoLabel;
    public JButton updateButton;
    public TasksFrame(String title, int userId){
        super(title);
        this.userId = userId;
        setTitle(title);

        //Initialize the lists
        tasksToDelete = new ArrayList<>();
        tasksToEdit = new ArrayList<>();

        addUIComponentsNorth();
        addUIComponentsCenter();
        addUIComponentsSouth();

        updateTaskList();
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                System.out.println("Shutting down application...");
                PSQLtdldbh.closePool();
                UserProperties.setProperty("darkTheme", String.valueOf(common.useNightMode));
            }
        });
    }
    
    //Method to initialize the North Panel
    private void addUIComponentsNorth(){
        //Panel to add new tasks with title and description textfields
        JPanel northPanel = new JPanel(new GridLayout(3,1));
        //subpanels
        JPanel todoTitlePanel = new JPanel(new FlowLayout());
        JPanel taskTitlePanel = new JPanel(new FlowLayout()); //taskTitlePanel
        JPanel descriptionPanel = new JPanel(new FlowLayout());
        // Make panel transparent
        northPanel.setOpaque(false);
        todoTitlePanel.setOpaque(false);
        taskTitlePanel.setOpaque(false);
        descriptionPanel.setOpaque(false);
        //create components
        todoLabel = new JLabel("To Do List");
        todoLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        
        JButton addButton = new JButton("Add Task");
        addButton.setPreferredSize(new Dimension(100, 20));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JTextField taskField = new JTextField("Task title", 20);
        
        JTextField taskDescriptionField = new JTextField("Task description",32);
        
        //add components to subpanels
        todoTitlePanel.add(todoLabel);
        taskTitlePanel.add(taskField);
        taskTitlePanel.add(addButton);
        descriptionPanel.add(taskDescriptionField);
        //add subpanels to northPanel
        northPanel.add(todoTitlePanel);
        northPanel.add(taskTitlePanel);
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
        addButton.addActionListener(e -> {
            if (!taskField.getText().equals("Task title")){
                addTask(taskField.getText(), taskDescriptionField.getText());
                taskField.setText("Task title");
                taskDescriptionField.setText("Task description");
            }
        });

        taskField.addActionListener(e -> {
            if (!taskField.getText().equals("Task title")){
                addTask(taskField.getText(), taskDescriptionField.getText());
                taskField.setText("Task title");
                taskDescriptionField.setText("Task description");
            }
        });

        taskDescriptionField.addActionListener(e -> {
            addTask(taskField.getText(), taskDescriptionField.getText());
            taskField.setText("Task title");
            taskDescriptionField.setText("Task description");
        });
    }
    
    //Method to initialize the South Panel
    /**
     * Adds UI components to the south panel.
     * The south panel contains buttons for various actions such as updating the task list, toggling color mode, viewing history, logging out, and user configuration.
     * 
     * Components:
     * - Log Out Button: Logs out the user.
     * - Update Button: Updates the task list.
     * - Toggle Color Button: Toggles between light and dark modes.
     * - History Button: Opens the history view.
     * - User Config Button: Opens user configuration settings.
     * 
     * Layout:
     * - Uses FlowLayout to center components with 10px horizontal and vertical gaps.
     * - Makes the panel transparent and sets tooltips for better user experience.
     */
    private void addUIComponentsSouth(){
        //Panel to add the buttons to update and view history
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // Make panel transparent
        southPanel.setOpaque(false);
        //create components
        ImageIcon logOutIcon = common.getLogOutIcon();
        JButton logOutButton = new JButton(logOutIcon);

        updateButton = new JButton("Update");
        updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        ImageIcon toggleColorIcon = common.getModeIcon();
        JButton toggleColorButton = new JButton(toggleColorIcon);
        toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton historyButton = new JButton("History");
        historyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon userConfigIcon = common.getUserConfigIcon();
        JButton userConfigButton = new JButton(userConfigIcon);
        //add components to southPanel
        southPanel.add(logOutButton);
        southPanel.add(updateButton);
        southPanel.add(toggleColorButton);
        southPanel.add(historyButton);
        southPanel.add(userConfigButton);
        //add southPanel to the frame
        add(southPanel, BorderLayout.SOUTH);
        addSouthActionListeners(logOutButton, updateButton, toggleColorButton, historyButton, userConfigButton);
    }

    //Method to add South Panel action listeners
    private void addSouthActionListeners(JButton logOutButton, JButton updateButton, JButton toggleColorButton, JButton historyButton, JButton userConfigButton){
        //add action listeners to the buttons
        logOutButton.addActionListener(e -> {
            UserProperties.logOut();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame("Login").setVisible(true);
            });
            dispose();
        });
        
        updateButton.addActionListener(e -> {
            tasks = TaskDAO.loadTasksFromDatabase(getUserId(), false, false);
            updateTaskList();
        });

        historyButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                new HistoryFrame("History", TasksFrame.this, getUserId()).setVisible(true);
            });
        });

        toggleColorButton.addActionListener(e -> {  //it dosent update the tasks in the database, solve later
            common.toggleColorMode();
            //dispose frame and recall
            dispose();
            SwingUtilities.invokeLater(() -> {
                new TasksFrame("ToDoList", getUserId()).setVisible(true);
            });
        });


        userConfigButton.addActionListener(e -> {

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

        //add gap between the text area and the boarders
        taskDetailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 5));
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(taskDetailsArea, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
  
    //this panels manages the task components and buttons
    //Method to create the task panel
    public JPanel createTaskPanel(Task task){
        taskPanel = new JPanel(new BorderLayout());
        taskPanel.setOpaque(false);
        taskPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 2, 5),
            BorderFactory.createLineBorder(common.getTextColor(), 1)
        ));
        taskPanel.setPreferredSize(new Dimension(getWidth() - 50, 40));
        taskPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        taskPanel.add(createCheckboxPanel(task), BorderLayout.WEST);
        taskPanel.add(createTitlePanel(task), BorderLayout.CENTER);
        taskPanel.add(createActionPanel(task), BorderLayout.EAST);
        return taskPanel;
    }
    
    //Method to create the checkbox panel
    private JPanel createCheckboxPanel(Task task){
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.setOpaque(false);
        
        JCheckBox updateCheckBox = new JCheckBox("", task.getIsDone());
        updateCheckBox.setToolTipText("Mark as Done");
        updateCheckBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateCheckBox.setOpaque(false);

        updateCheckBox.addActionListener(e -> {
            task.setIsDone(!task.getIsDone());
            TaskDAO.updateDoneTaskInDatabase(task);
        });

        checkboxPanel.add(updateCheckBox);
        return checkboxPanel;
    }
    
    public static JPanel createTitlePanel(Task task){
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel taskLabel = new JLabel(task.getTaskTitle());
        titlePanel.add(taskLabel);
        return titlePanel;
    }
    
    private JPanel createActionPanel(Task task){
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
    
        ImageIcon editIcon = common.getEditIcon();
        ImageIcon viewIcon = common.getViewIcon();
        ImageIcon deleteIcon = common.getDeleteIcon();
        
        JButton viewButton = new JButton(viewIcon);
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.setPreferredSize(new Dimension(20, 20));
        viewButton.setBorderPainted(false);
        viewButton.setContentAreaFilled(false);
        viewButton.setToolTipText("View Task Details");
        
        viewButton.addActionListener(e -> {
                viewTaskDetails(task);
        });
        
        JButton deleteButton = new JButton(deleteIcon);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setPreferredSize(new Dimension(20, 20));
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setToolTipText("Delete Task");
        
        deleteButton.addActionListener(e -> {
                TaskDAO.deleteTaskFromDatabase(task);
                updateTaskList();
        });

        JButton editButton = new JButton(editIcon);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.setPreferredSize(new Dimension(20, 20));
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setToolTipText("Edit Task");

        editButton.addActionListener(e -> {
            centerPanel.remove(taskPanel);
            centerPanel.revalidate();
            centerPanel.repaint();
            SwingUtilities.invokeLater(() -> {
                EditTaskFrame editTaskFrame = new EditTaskFrame("Edit Task", task);
                editTaskFrame.setVisible(true);
                editTaskFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        refreshTaskList(task);
                    }
                });
            });
        });

        actionPanel.add(editButton);
        actionPanel.add(viewButton);
        actionPanel.add(deleteButton);
        return actionPanel;
    }

    //Method to show the changes in the center Panel
    public void updateTaskList(){
        centerPanel.removeAll();
        //add the tasks from the database
        tasks = TaskDAO.loadTasksFromDatabase(getUserId(), false, false);
        for (Task task : tasks){
            centerPanel.add(createTaskPanel(task));
        }
        centerPanel.revalidate();
        centerPanel.repaint();
        // Set initial focus to the title each update
        SwingUtilities.invokeLater(() -> {
            todoLabel.requestFocusInWindow();
        });
    }

    public void refreshTaskList(Task task){
        centerPanel.add(createTaskPanel(task));
        centerPanel.revalidate();
        centerPanel.repaint();
    }
    
    // Method to get the user ID
    public int getUserId(){
        return userId;
    }

    // Method to view the task details
    private void viewTaskDetails(Task task){
        taskDetailsArea.setText(task.viewTaskDesc());
    }

    // Method to add a new task
    public void addTask(String taskTitle, String description){
        Task task = new Task.Builder(getUserId())
            .taskTitle(taskTitle)
            .description(description)
            .folderId(1)
            .build();
        tasks.add(task);
        TaskDAO.saveTaskToDatabase(task);
        updateTaskList();
    }
}
