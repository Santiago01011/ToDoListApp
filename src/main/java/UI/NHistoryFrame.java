package UI;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import COMMON.common;
import DBH.TaskDAO;
import model.Task;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;

public class NHistoryFrame extends Frame{
    private int userId;
    private JButton toggleColorButton;
    private JPanel topPanel;
    private JPanel bottomPanel;

    private java.util.List<Task> completedTasks;
    private java.util.List<Task> deletedTasks;
    private java.util.List<Task> deletedTasksTemp;  
    public NHistoryFrame(int userId){
        super("Task History");
        setLayout(new BorderLayout());
        setSize(500, 700);
        setLocationRelativeTo(null);
        
        this.userId = userId;
        JLabel title = new JLabel("Task History");
        title.setFont(new Font("Consolas", Font.BOLD, 24));
        title.setForeground(common.getTextColor());
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(title, BorderLayout.NORTH);

        // Create a display panel with GridLayout to hold the top and bottom panels
        JPanel displayPanel = new JPanel(new GridLayout(2, 1));

        // Add the topPanel, used to display the done tasks
        topPanel = new JPanel(new MigLayout("fillx", "[]", "[]"));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 0),
            BorderFactory.createTitledBorder(null, "Completed Tasks", TitledBorder.CENTER, TitledBorder.TOP)
        ));

        JScrollPane topScrollPane = new JScrollPane(topPanel);
        topScrollPane.getVerticalScrollBar().setUnitIncrement(5);
        displayPanel.add(topScrollPane);

        // Add the bottomPanel, used to display the deleted tasks
        bottomPanel = new JPanel(new MigLayout("fillx", "[]", "[]"));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(null, "Deleted Tasks", TitledBorder.CENTER, TitledBorder.TOP)
        ));

        JScrollPane bottomScrollPane = new JScrollPane(bottomPanel);
        bottomScrollPane.getVerticalScrollBar().setUnitIncrement(5);
        displayPanel.add(bottomScrollPane);

        // Add the display panel to the center of the frame
        add(displayPanel, BorderLayout.CENTER);
            
        addUIComponentsSouth();
        initTasks();

    }

    private void initTasks(){
        //load tasks from database
        completedTasks = TaskDAO.loadTasksFromDatabase(userId, true, false);
        
        deletedTasks = TaskDAO.loadTasksFromDatabase(userId, false, true);
        deletedTasksTemp = TaskDAO.loadTasksFromDatabase(userId, true, true);

        deletedTasks.addAll(deletedTasksTemp);

        //display history tasks
        for(Task task : completedTasks){
            addTaskToPanel(task, false);
        }
        
        for(Task task : deletedTasks){
            addTaskToPanel(task, true);
        }
    }



    private void addTaskToPanel(Task task, boolean isDeleted){
        JPanel taskPanel = new JPanel(new MigLayout("fill", "[][grow][]", "[]")); 
            
        taskPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 0, 5),
            BorderFactory.createLineBorder(common.getTextColor(), 1)
        ));
        ImageIcon editIcon = common.getEditIcon();
        ImageIcon viewIcon = common.getViewIcon();
        ImageIcon deleteIcon = common.getDeleteIcon();
        
        if(!isDeleted){
            
            JCheckBox doneCheckBox = new JCheckBox();
            doneCheckBox.setSelected(task.getIsDone());
            
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
            topPanel.add(taskPanel, "growx, wrap");

            doneCheckBox.addActionListener(e -> {
                task.setIsDone(!task.getIsDone());
                TaskDAO.updateDoneTaskInDatabase(task);
            });

            editButton.addActionListener(e -> toggleEditPanel(taskPanel, task));

            viewButton.addActionListener(e -> toggleViewPanel(taskPanel, task));

            deleteButton.addActionListener(e -> {
                TaskDAO.deleteTaskFromDatabase(task.getId());
                topPanel.remove(taskPanel);
                topPanel.revalidate();
                topPanel.repaint();
                addTaskToPanel(task, true);
                bottomPanel.revalidate();
                bottomPanel.repaint();
            });
            } else{
                taskPanel.add(new JLabel(task.getTaskTitle()), "center, growx");

                JButton deleteButton = new JButton(deleteIcon);
                deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                deleteButton.setBorderPainted(false);
                deleteButton.setContentAreaFilled(false);
                deleteButton.setToolTipText("Hard Delete Task");

                ImageIcon restoreIcon = common.getRestoreIcon();
                JButton restoreButton = new JButton(restoreIcon);
                restoreButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                restoreButton.setPreferredSize(new Dimension(20, 20));
                restoreButton.setBorderPainted(false);
                restoreButton.setContentAreaFilled(false);
                restoreButton.setToolTipText("Restore Task");

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                buttonPanel.add(deleteButton);
                buttonPanel.add(restoreButton);
                taskPanel.add(buttonPanel, "align right, wrap");
                bottomPanel.add(taskPanel, "growx, wrap");

                deleteButton.addActionListener(e -> {
                    TaskDAO.hardDeleteTaskFromDatabase(task.getId());
                    bottomPanel.remove(taskPanel);
                    bottomPanel.revalidate();
                    bottomPanel.repaint();
                });

                restoreButton.addActionListener(e -> {
                    TaskDAO.restoreTaskFromDatabase(task);
                    bottomPanel.remove(taskPanel);
                    bottomPanel.revalidate();
                    bottomPanel.repaint();
                    if(task.getIsDone()){
                        addTaskToPanel(task, false);
                        topPanel.revalidate();
                        topPanel.repaint();
                    }
                });
        }
    }


    private void addUIComponentsSouth(){
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        southPanel.setOpaque(false);

        ImageIcon backIcon = common.getLogOutIcon();
        JButton backButton = new JButton(backIcon);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        ImageIcon toggleColorIcon = common.getModeIcon();
        toggleColorButton = new JButton(toggleColorIcon);
        toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        southPanel.add(backButton);
        southPanel.add(toggleColorButton);
        add(southPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

    }

    private void toggleEditPanel(JPanel taskPanel, Task task){
        // Check if the edit panel already exists
        Component existingEditPanel = null;
        for (Component comp : taskPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals("editPanel")) {
                existingEditPanel = comp;
                break;
            }
        }
    
        if (existingEditPanel != null) {
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
                task.setTaskTitle(titleField.getText());
                task.setDescription(descriptionArea.getText());
                task.setTargetDate(dateField.getText().isEmpty() ? null : LocalDateTime.parse(dateField.getText()));
                TaskDAO.updateTaskInDatabase(task);
   
                taskPanel.remove(editPanel);
                topPanel.remove(taskPanel);
                addTaskToPanel(task, false);
                topPanel.revalidate();
                topPanel.repaint();
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

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            new NHistoryFrame(1).setVisible(true);
        });
    }
}
