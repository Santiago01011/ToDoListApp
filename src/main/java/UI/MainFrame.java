package UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import COMMON.UserProperties;
import COMMON.common;
import DBH.PSQLtdldbh;
import DBH.TaskDAO;
import model.Task;
import net.miginfocom.swing.MigLayout;

public class MainFrame extends Frame{
    private int userId;
    private JPanel taskListPanel;
    JButton updateButton;
    private List<String> folders;
    
    /**
     * MainFrame is the main window for the To-Do App. It initializes the user interface components
     * and handles user interactions for adding tasks, displaying tasks, and managing folders.
     *
     * @param userId the ID of the user to get tasks for
     */
    public MainFrame(int userId){

        super("To-Do App");
        this.userId = userId;

        // Set frame properties
        setSize(600, 600);
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout());

        // Task Input Panel (North)
        JPanel inputPanel = new JPanel(new MigLayout("fillx", "[][grow]", "[][][]"));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(null, "Add New Task", TitledBorder.CENTER, TitledBorder.TOP)
        ));

        inputPanel.add(new JLabel("Title:"), "align label");
        JTextField titleField = new JTextField();
        inputPanel.add(titleField, "growx, wrap");

        inputPanel.add(new JLabel("Description:"), "align label top");
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        inputPanel.add(new JScrollPane(descriptionArea), "growx, wrap");

        inputPanel.add(new JLabel("Target Date:"), "align label");
        JFormattedTextField dateField = new JFormattedTextField();
        dateField.setBackground(common.getTertiaryColor());
        dateField.setForeground(common.getTextColor());
        inputPanel.add(dateField, "growx, wrap");

        ImageIcon addIcon = common.getViewIcon();
        JButton addFolderButton = new JButton(addIcon);
        addFolderButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addFolderButton.setBorderPainted(false);
        addFolderButton.setContentAreaFilled(false);
        addFolderButton.setToolTipText("New Folder");
        inputPanel.add(addFolderButton, "split 2");
        inputPanel.add(new JLabel("Folder:"), "align label");
        folders = TaskDAO.loadFoldersFromDatabase(userId);
        JComboBox<String> folderCombo = new JComboBox<>(folders.toArray(new String[0]));
        inputPanel.add(folderCombo, "growx, wrap");

        JButton addTaskButton = new JButton("Add Task");
        inputPanel.add(addTaskButton, "span, align center");

        addTaskButton.addActionListener(e -> {
            if(!titleField.getText().isEmpty() /*&& isValidDate(targetDate) */){ //add validation for date
                String targetDate = dateField.getText();
                Task task = new Task.Builder(userId)
                .taskTitle(titleField.getText())
                .description(descriptionArea.getText())
                .dateAdded(LocalDateTime.now())
                .targetDate(targetDate.isEmpty() ? null : LocalDateTime.parse(targetDate))
                .folderName(folderCombo.getSelectedItem().toString())
                .build();

                TaskDAO.saveTaskToDatabase(task);
                addTaskToPanel(task);
                titleField.setText("");
                descriptionArea.setText("");
                dateField.setText("");
                taskListPanel.revalidate();
                taskListPanel.repaint();
            }
        });

        titleField.addActionListener(e -> addTaskButton.doClick());
        descriptionArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                addTaskButton.doClick();
            }
            }
        });

        addFolderButton.addActionListener(e -> {
            String folderName = javax.swing.JOptionPane.showInputDialog("Enter folder name:");
            if (folderName != null && !folderName.isEmpty()) {
                TaskDAO.saveFolderToDatabase(folderName, userId);
                folders = TaskDAO.loadFoldersFromDatabase(userId);
                folderCombo.removeAllItems();
                folderCombo.addItem("All");
                for (String folder : folders) {
                    folderCombo.addItem(folder);
                }
                updateButton.doClick();
            }
        });
        add(inputPanel, BorderLayout.NORTH);

        taskListPanel = new JPanel(new MigLayout("fillx", "[]", "[]"));
            taskListPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 0),
            BorderFactory.createTitledBorder(null, "Uncompleted Tasks", TitledBorder.CENTER, TitledBorder.TOP)
        ));

        JScrollPane taskScrollPane = new JScrollPane(taskListPanel);
        taskScrollPane.getVerticalScrollBar().setUnitIncrement(5);
        add(taskScrollPane, BorderLayout.CENTER);
 
        refreshTaskList(null);

        addUIComponentsSouth();
        // Display the frame
        setVisible(true);

        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                System.out.println("Shutting down application...");
                PSQLtdldbh.closePool();
                UserProperties.setProperty("darkTheme", String.valueOf(common.useNightMode));
            }
        });
    }
    
    /**
     * Creates and adds a task panel to the task list panel to be displayed in the UI.
     *
     * @param task The task to be added to the panel.
     */
    private void addTaskToPanel(Task task){
        JPanel taskPanel = new JPanel(new MigLayout("fill", "[][grow][]", "[]")); 
            
        taskPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 0, 5),
            BorderFactory.createLineBorder(common.getTextColor(), 1)
        ));
        
        ImageIcon editIcon = common.getEditIcon();
        ImageIcon viewIcon = common.getViewIcon();
        ImageIcon deleteIcon = common.getDeleteIcon();
        
        JCheckBox doneCheckBox = new JCheckBox();
        
        JButton editButton = new JButton(editIcon);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setToolTipText("Edit Task");

        JButton viewButton = new JButton(viewIcon);
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.setBorderPainted(false);
        viewButton.setContentAreaFilled(false);
        viewButton.setToolTipText("View Details");
        
        JButton deleteButton = new JButton(deleteIcon);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setToolTipText("Remove Task");

        taskPanel.add(doneCheckBox, "align center");
        taskPanel.add(new JLabel(task.getTaskTitle()), "center");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        taskPanel.add(buttonPanel, "wrap");
        taskListPanel.add(taskPanel, "growx, wrap");

        doneCheckBox.addActionListener(e -> {
            task.setIsDone(!task.getIsDone());
            TaskDAO.updateDoneTaskInDatabase(task);
        });

        editButton.addActionListener(e -> toggleEditPanel(taskPanel, task));

        viewButton.addActionListener(e -> toggleViewPanel(taskPanel, task));

        deleteButton.addActionListener(e -> {
            TaskDAO.deleteTaskFromDatabase(task.getId());
            taskListPanel.remove(taskPanel);
            taskListPanel.revalidate();
            taskListPanel.repaint();
        });
    }

    /**
     * Adds UI components to the south region of the main frame.
     * This method creates a panel with buttons for different actions.
     * The panel is then added to the south region of the frame.
     * 
     * Components created:
     * - Log out button
     * - Update button
     * - Toggle color mode button
     * - History button
     * - User configuration button
     * 
     * @see #addSouthActionListeners(logOutButton, updateButton, toggleColorButton, historyButton, userConfigButton)
     */
    private void addUIComponentsSouth(){
        //Panel to add the buttons to update and view history
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // Make panel transparent
        southPanel.setOpaque(false);
        //create components
        ImageIcon logOutIcon = common.getLogOutIcon();
        JButton logOutButton = new JButton(logOutIcon);
        logOutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
            if(PSQLtdldbh.isCloudAvailable()){
                TaskDAO.syncDatabases(userId);
            }
            refreshTaskList(null);
        });

        historyButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                new NHistoryFrame(userId).setVisible(true);
            });
        });

        toggleColorButton.addActionListener(e -> {
            common.toggleColorMode();
            //dispose frame and recall
            dispose();
            SwingUtilities.invokeLater(() -> {
                new MainFrame(userId).setVisible(true);
            });
        });


        userConfigButton.addActionListener(e -> {

        });
    }

    /**
     * Toggles the visibility of the edit panel for a given task within the task panel.
     * If the edit panel already exists, it will be removed. Otherwise, a new edit panel
     * will be created and added to the task panel.
     *
     * @param taskPanel the JPanel containing the task components
     * @param task the Task object to be edited
     */
    private void toggleEditPanel(JPanel taskPanel, Task task) {
        // Check if the edit panel already exists
        Component existingEditPanel = null;
        for (Component comp : taskPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals("editPanel")) {
                existingEditPanel = comp;
                break;
            }
        }
    
        if (existingEditPanel != null){
            // If the edit panel exists, remove it
            taskPanel.remove(existingEditPanel);
        } else {
            // Create a new edit panel
            JPanel editPanel = new JPanel(new MigLayout("fillx", "[][grow]", "[][]"));
            editPanel.setName("editPanel");
    
            JTextField titleField = new JTextField(task.getTaskTitle());
            JTextArea descriptionArea = new JTextArea(task.getDescription(), 3, 20);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            JFormattedTextField dateField = new JFormattedTextField(task.getTargetDate());
    
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> {
                // Update task with new values
                task.setTaskTitle(titleField.getText());
                task.setDescription(descriptionArea.getText());
                task.setTargetDate(dateField.getText().isEmpty() ? null : LocalDateTime.parse(dateField.getText()));
                TaskDAO.updateTaskInDatabase(task);
    
                // Update the UI
                taskPanel.remove(editPanel);
                taskPanel.revalidate();
                taskPanel.repaint();
    
                // Refresh task list
                refreshTaskList(null);
            });
    
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> {
                taskPanel.remove(editPanel);
                taskPanel.revalidate();
                taskPanel.repaint();
            });
    
            editPanel.add(new JLabel("Title:"), "align label");
            editPanel.add(titleField, "growx, wrap");
            editPanel.add(new JLabel("Description:"), "align label top");
            editPanel.add(new JScrollPane(descriptionArea), "growx, wrap");
            editPanel.add(new JLabel("Target Date:"), "align label");
            editPanel.add(dateField, "growx, wrap");
            editPanel.add(saveButton, "split 2, align center");
            editPanel.add(cancelButton, "align center");
    
            taskPanel.add(editPanel, "newline, span, growx, wrap");
        }
    
        // Revalidate and repaint the task panel to show changes
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    /**
     * Toggles the visibility of the view panel for a given task within the task panel.
     * If the view panel already exists, it will be removed. Otherwise, a new view panel
     * will be created and added to the task panel.
     *
     * @param taskPanel the JPanel containing the task components
     * @param task the Task object to view details for
     */
    private void toggleViewPanel(JPanel taskPanel, Task task){
        Component existingViewPanel = null;
        for (Component comp : taskPanel.getComponents()){
            if (comp.getName() != null && comp.getName().equals("viewPanel")){
                existingViewPanel = comp;
                break;
            }
        }
    
        if (existingViewPanel != null){
            // If the view panel exists, remove it
            taskPanel.remove(existingViewPanel);
        } else{
            // Create a new view panel
            JPanel viewPanel = new JPanel(new MigLayout("fillx", "[][grow]", "[][]"));
            viewPanel.setName("viewPanel");
    

            JTextArea descriptionArea = new JTextArea(task.getDescription(), 3, 20);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setEditable(false);
            //make the text area transparent
            descriptionArea.setBackground(common.getPanelColor());
            //make the text area borders as the same color as the panel
            descriptionArea.setBorder(BorderFactory.createLineBorder(common.getPanelColor()));
            
            

            JLabel folderLabel = new JLabel("Folder: " + task.getFolderName());

            viewPanel.add(new JLabel("Description:"), "align label top");
            viewPanel.add(new JScrollPane(descriptionArea), "growx, wrap");
            viewPanel.add(folderLabel, "align label");

            taskPanel.add(viewPanel, "newline, span, growx, wrap");
        }
    
        // Revalidate and repaint the task panel to show changes
        taskPanel.revalidate();
        taskPanel.repaint();
        }

    /**
     * Refreshes the task list panel by removing all existing components and adding a new JComboBox
     * for folder selection. It then loads tasks from the database based on the selected folder and
     * adds them to the task list panel.
     *
     * @param folderName the name of the folder to filter tasks by; if null, all tasks are loaded
     */
    private void refreshTaskList(String folderName){
        taskListPanel.removeAll();

        JComboBox<String> folderCombo2 = new JComboBox<>(folders.toArray(new String[0]));
        folderCombo2.insertItemAt("All", 0);
        folderCombo2.setSelectedIndex(folderName == null ? 0 : folders.indexOf(folderName) + 1);
        taskListPanel.add(folderCombo2, "growx, wrap");

        folderCombo2.addActionListener(e -> {
            String selectedFolder = folderCombo2.getSelectedItem().toString();
            if(selectedFolder.equals("All")){
                refreshTaskList(null);
            } else{
                refreshTaskList(selectedFolder);
            }
        });

        List<Task> tasks;
        if (folderName == null) {
            tasks = TaskDAO.loadTasksFromDatabase(userId, false, false);
        } else {
            tasks = TaskDAO.loadTasksFromDatabaseByFolder(userId, false, folderName);
        }

        for (Task task : tasks) {
            addTaskToPanel(task);
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
        }

}
