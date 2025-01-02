package UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.BorderUIResource;

import COMMON.common;
import model.Task;


public class EditTaskFrame extends Frame {
    
    public EditTaskFrame(String title, Task task, TasksFrame tasksFrame){
        super(title);
        setTitle(title);
        setSize(350, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 20, 5));
        
        JLabel titleLabel = new JLabel("Edit Task");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 25));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        
        String[] options = {"Title", "Description", "Target date", "Folder"};
        JComboBox<String> optionList = new JComboBox<>(options);
        optionList.setSelectedIndex(0);
        optionList.setPrototypeDisplayValue("Select an option");
        optionList.setPreferredSize(new Dimension(90, 20));
        optionList.setForeground(common.getTextColor());
        optionList.setBackground(common.getTertiaryColor());
        optionList.setBorder(BorderUIResource.getBlackLineBorderUIResource());
        
        JTextField editField = new JTextField("  Edit " + optionList.getSelectedItem());
        editField.setPreferredSize(new Dimension(200, 20));
        editField.setBorder(BorderUIResource.getBlackLineBorderUIResource());

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.X_AXIS));
        editPanel.add(optionList);
        editPanel.add(editField);

        JTextArea taskDetailsArea = new JTextArea(10, 20);        
        taskDetailsArea.setEditable(false);
        taskDetailsArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);
        taskDetailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 5));
         
        add(titleLabel, BorderLayout.NORTH);
        centerPanel.add(editPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        add(taskDetailsArea, BorderLayout.SOUTH);

        taskDetailsArea.setText(task.viewTaskDesc());

        optionList.addActionListener(e -> {
            editField.setText("  Edit " + optionList.getSelectedItem());
        });

        editField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (editField.getText().equals("  Edit " + optionList.getSelectedItem())) {
                    editField.setText("");
                }
            }
        });

        editField.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (!editField.getText().equals("")){
                    switch(optionList.getSelectedIndex()){
                        case 0:
                            task.setTaskTitle(editField.getText());
                            break;
                        case 1:
                            task.setDescription(editField.getText());
                            break;
                        case 2:
                            //task.setTargetDate(editField.getText());
                            break;
                        case 3:
                            //task.setFolder(editField.getText());
                            break;
                    }
                    taskDetailsArea.setText(task.viewTaskDesc());
                    editField.setText("");
                }
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                tasksFrame.updateTaskList();
            }
        });
    }
}
