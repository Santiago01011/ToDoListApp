package main.UI;

import main.COMMON.common;
import main.model.Task;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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
    private JTextArea taskDetailsArea;
    public TasksFrame(String title, int userId){
        super(title);
        addUIComponentsNorth();
        addUIComponentsCenter();
        addUIComponentsSouth();
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
        //Panel to display the tasks and their actions buttons
        JPanel centerPanel = new JPanel(new GridLayout(0,1));
        JPanel mainPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.getViewport().setBackground(common.PRIMARY_COLOR);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        taskDetailsArea = new JTextArea(10, 20);
        taskDetailsArea.setEditable(false);
        taskDetailsArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);
        taskDetailsArea.setBackground(common.TERTIARY_COLOR);
        taskDetailsArea.setForeground(common.TEXT_COLOR);
        taskDetailsArea.setFont(new java.awt.Font("Dialog", Font.PLAIN, 14));
        scrollPane.setViewportView(taskDetailsArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(taskDetailsArea, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

}
