package UI;

import net.miginfocom.swing.*;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

import COMMON.common;
import DBH.TaskDAO;
import model.Task;

public class EditTaskFrame extends Frame {
    public EditTaskFrame(String title, Task task) {
        super(title);
        setSize(350, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[][grow][]"));

        setupComponents(task);
    }
    
    private void setupComponents(Task task) {
        // Title
        JLabel titleLabel = new JLabel("Edit Task", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 25));
        add(titleLabel, "cell 0 0, growx, gaptop 10");

        // Center panel
        JPanel centerPanel = new JPanel(new MigLayout("fillx, insets 20 5", "[grow]", "[]10[]"));
        centerPanel.setBackground(common.getPrimaryColor());

        // Edit controls
        String[] options = {"Title", "Description", "Target date", "Folder"};
        JComboBox<String> optionList = new JComboBox<>(options);
        optionList.setPreferredSize(new Dimension(120, 25));
        
        JButton saveButton = new JButton(common.getSaveIcon());
        saveButton.setPreferredSize(new Dimension(30, 25));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel editPanel = new JPanel(new MigLayout("insets 0", "[]10[]", "[]"));
        editPanel.setBackground(common.getPrimaryColor());
        editPanel.add(optionList);
        editPanel.add(saveButton);
        
        // Text areas
        JTextArea editField = new JTextArea("Edit " + optionList.getSelectedItem());
        editField.setRows(10);
        editField.setLineWrap(true);
        editField.setWrapStyleWord(true);
        editField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(common.getTextColor(), 2),
            BorderFactory.createEmptyBorder(10, 10, 5, 5)
        ));
        
        JTextArea taskDetailsArea = new JTextArea();
        taskDetailsArea.setRows(10);
        taskDetailsArea.setEditable(false);
        taskDetailsArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);
        taskDetailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 5));
        taskDetailsArea.setText(task.viewTaskDesc());

        // Add components
        centerPanel.add(editPanel, "growx, wrap");
        centerPanel.add(new JScrollPane(editField), "grow, wrap");
        add(centerPanel, "cell 0 1, grow");
        add(new JScrollPane(taskDetailsArea), "cell 0 2, grow, gapbottom 10");

        // Event handlers
        optionList.addActionListener(e -> editField.setText("Edit " + optionList.getSelectedItem()));

        editField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (editField.getText().equals("Edit " + optionList.getSelectedItem())) {
                    editField.setText(switch(optionList.getSelectedIndex()) {
                        case 0 -> task.getTaskTitle().toString();
                        case 1 -> task.getDescription().toString();
                        case 2 -> {
                            if(task.getTargetDate() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                yield task.getTargetDate().format(formatter);
                            } else {
                                yield "";
                            }
                        }
                        case 3 -> String.valueOf(task.getFolderId());
                        default -> "";
                    });
                }
            }
        });

        saveButton.addActionListener(e -> {
            if (!editField.getText().isEmpty() && !editField.getText().equals("Edit " + optionList.getSelectedItem())) {
                switch(optionList.getSelectedIndex()) {
                    case 0 -> task.setTaskTitle(editField.getText());
                    case 1 -> task.setDescription(editField.getText());
                    // case 2 -> {
                    //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    //     task.setTargetDate(LocalDateTime.parse(editField.getText(), formatter));
                    // }
                    case 3 -> task.setFolderId(Integer.parseInt(editField.getText()));
                    default -> {}
                }
                taskDetailsArea.setText(task.viewTaskDesc());
                editField.setText("Edit " + optionList.getSelectedItem());
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                TaskDAO.updateTaskInDatabase(task);
            }
        });
    }
}