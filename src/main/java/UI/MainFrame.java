package UI;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.FlatDarkLaf;

import COMMON.common;
import net.miginfocom.swing.MigLayout;
import java.awt.*;

public class MainFrame extends Frame {
    public MainFrame() {
        // Apply FlatLaf theme
        FlatDarkLaf.setup();

        // Set frame properties
        setTitle("To-Do App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
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

        inputPanel.add(new JLabel("Folder:"), "align label");
        JComboBox<String> folderCombo = new JComboBox<>(new String[]{"Work", "Personal", "Others"});
        inputPanel.add(folderCombo, "growx, wrap");

        JButton addTaskButton = new JButton("Add Task");
        inputPanel.add(addTaskButton, "span, align center");

        add(inputPanel, BorderLayout.NORTH);

        // Task List Panel (Center)
        JPanel taskListPanel = new JPanel(new MigLayout("fillx", "[]", "[]"));
        taskListPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 0),
            BorderFactory.createTitledBorder(null, "Uncompleted Tasks", TitledBorder.CENTER, TitledBorder.TOP)
        ));

        // Dummy task list
        for (int i = 1; i <= 20; i++) {
            // the second column constraints grows to left the label in the center and the remaining buttons to the right
            JPanel taskPanel = new JPanel(new MigLayout("fill", "[][grow][]", "[]")); 
            JButton doneButton = new JButton("Done");
            JButton editButton = new JButton("Edit");
            JButton deleteButton = new JButton("Delete");
            taskPanel.add(doneButton);
            taskPanel.add(new JLabel("Task " + i), "center");
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            taskPanel.add(buttonPanel, "wrap");
            taskListPanel.add(taskPanel, "growx, wrap");
        }

        JScrollPane taskScrollPane = new JScrollPane(taskListPanel);
        taskScrollPane.getVerticalScrollBar().setUnitIncrement(5);
        add(taskScrollPane, BorderLayout.CENTER);

        // Display the frame
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
