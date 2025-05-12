package UI.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import COMMON.common;
import model.Task;
import model.TaskStatus;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class EditTaskPanel extends JPanel {
    public interface Listener {
        void onSaveEdit(String title, String desc, String folder, LocalDateTime due, TaskStatus status);
        void onCancelEdit();
    }
    private JComboBox<String> folderBox;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private String folderName;

    public EditTaskPanel(Listener listener, Task task) {
        setLayout(new MigLayout("insets 5", "[grow]", "[][][][][][]"));
        setBackground(common.getTertiaryColor());
        Color outlineColor = common.getPanelColor().darker();
        int arc = 10, thickness = 2;
        putClientProperty("FlatLaf.style", "arc: " + arc);
        setBorder(new UI.components.RoundedLineBorder(outlineColor, thickness, arc));
        add(new JLabel("Edit Task"), "wrap");

        JTextField titleField = new JTextField(task.getTitle());
        addFocusListeners(titleField, "Enter task title...");
        
        add(new JLabel("Title:"), "split 2"); add(titleField, "growx, wrap");

        JTextArea descArea = new JTextArea(task.getDescription(),3,10);
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        add(new JLabel("Edit description"), "split 2"); add(new JScrollPane(descArea), "growx, wrap");

        LocalDateTime taskDueDate = task.getDue_date();
        
        datePicker = new DatePicker(); datePicker.setDateFormat("dd/MM/yyyy"); datePicker.setCloseAfterSelected(true); 
        timePicker = new TimePicker(); timePicker.set24HourView(true);
        if (taskDueDate != null) {
            datePicker.setSelectedDate(taskDueDate.toLocalDate());
            timePicker.setSelectedTime(taskDueDate.toLocalTime());
        }
        JFormattedTextField dateEditor = new JFormattedTextField(); datePicker.setEditor(dateEditor);
        JFormattedTextField timeEditor = new JFormattedTextField(); timePicker.setEditor(timeEditor);
        add(new JLabel("Due Date:"), "split 4"); add(dateEditor, "growx"); add(new JLabel("Time:")); add(timeEditor, "growx, wrap");

        folderBox = new JComboBox<>();
        add(new JLabel("Folder:"), "split 2"); add(folderBox, "growx, wrap");
        this.folderName = task.getFolder_name();

        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Pending","In Progress","Completed"});
        add(new JLabel("Status:"), "split 2"); add(statusBox, "growx, wrap");
        statusBox.setSelectedItem(task.getStatus().toString());

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty() || title.equals("Enter task title...")) {
                JOptionPane.showMessageDialog(this, "The title field can't be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate date = datePicker.getSelectedDate();
            LocalDateTime due = null;
            if (date != null) {
                due = timePicker.isTimeSelected() ? date.atTime(timePicker.getSelectedTime()) : date.atStartOfDay();
            }
            String folder = (String) folderBox.getSelectedItem();
            TaskStatus status = TaskStatus.getStringToStatus((String) statusBox.getSelectedItem());
            listener.onSaveEdit(title, descArea.getText(), folder, due, status);
            titleField.setText("");
            descArea.setText("");
            datePicker.clearSelectedDate();
            timePicker.clearSelectedTime();
        });
        JButton cancelBtn = new JButton(common.getBackIcon()); cancelBtn.setToolTipText("Cancel");
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> listener.onCancelEdit());
        add(cancelBtn, "split 2"); add(saveBtn, "wrap");
    }

    public void setFolders(List<String> folders) {
        SwingUtilities.invokeLater(() -> {
            folderBox.removeAllItems();
            boolean taskFolderFound = false;
            if (folders != null) {
                for (String folder : folders) {
                    folderBox.addItem(folder);
                    if (this.folderName != null && this.folderName.equals(folder)) {
                        taskFolderFound = true;
                    }
                }
            }

            if (taskFolderFound) {
                folderBox.setSelectedItem(this.folderName);
            } else if (folderBox.getItemCount() > 0) {
                folderBox.setSelectedIndex(0);
            } else {
                folderBox.setSelectedItem(null);
            }
        });
    }

    public void addFocusListeners(JTextField textField, String defaultText){
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(defaultText)) {
                    textField.setText("");
                }
            }
        });
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(defaultText);
                }
            }
        });
    }
}
