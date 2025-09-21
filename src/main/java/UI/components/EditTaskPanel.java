package UI.components;

import java.awt.Cursor;
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
        void onUpdate(Task task);
        void onCancel();
    }
    private Task currentTask;
    private JTextField titleField;
    private JTextArea descArea;
    private JComboBox<String> folderBox;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private JComboBox<String> statusBox;

    public EditTaskPanel(Listener listener) {
        setLayout(new MigLayout("insets 5", "[grow]", "[][][][][][]"));
        setBorder(new EmptyBorder(10,5,5,5));
        add(new JLabel("Edit Task"), "wrap");

        titleField = new JTextField();
        add(new JLabel("Title:"), "split 2"); add(titleField, "growx, wrap");

        descArea = new JTextArea(3,10);
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        add(new JLabel("Description:"), "split 2"); add(new JScrollPane(descArea), "growx, wrap");

        datePicker = new DatePicker(); datePicker.setDateFormat("dd/MM/yyyy"); datePicker.setCloseAfterSelected(true);
        timePicker = new TimePicker(); timePicker.set24HourView(true);
        JFormattedTextField dateEditor = new JFormattedTextField(); datePicker.setEditor(dateEditor);
        JFormattedTextField timeEditor = new JFormattedTextField(); timePicker.setEditor(timeEditor);
        add(new JLabel("Due Date:"), "split 4"); add(dateEditor, "growx"); add(new JLabel("Time:")); add(timeEditor, "growx, wrap");

        folderBox = new JComboBox<>();
        add(new JLabel("Folder:"), "split 2"); add(folderBox, "growx, wrap");

        statusBox = new JComboBox<>(new String[]{"Pending","In Progress","Completed"});
        add(new JLabel("Status:"), "split 2"); add(statusBox, "growx, wrap");

        JButton saveBtn = new JButton("Update");
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setToolTipText("Update Task");
        saveBtn.addActionListener(e -> {
            if (currentTask == null) return;
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "The title field can't be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate date = datePicker.getSelectedDate();
            LocalDateTime due = null;
            if (date != null) {
                due = timePicker.isTimeSelected() ? date.atTime(timePicker.getSelectedTime()) : date.atStartOfDay();
            }
            Task.Builder builder = new Task.Builder(currentTask.getTask_id())
                .taskTitle(title)
                .description(descArea.getText())
                .folderId((String) folderBox.getSelectedItem())
                .dueDate(due)
                .status(TaskStatus.getStringToStatus((String) statusBox.getSelectedItem()))
                .createdAt(currentTask.getCreated_at())
                .updatedAt(LocalDateTime.now())
                .sync_status(currentTask.getSync_status());
            listener.onUpdate(builder.build());
        });
        JButton cancelBtn = new JButton(common.getBackIcon()); cancelBtn.setToolTipText("Cancel");
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> listener.onCancel());
        add(saveBtn, "split 2, growx, gapright 10");
        add(cancelBtn, "right, gapright 10, wrap");
    }

    public void setFolders(List<String> folders) {
        SwingUtilities.invokeLater(() -> {
            folderBox.removeAllItems();
            if (folders != null) folders.forEach(folderBox::addItem);
        });
    }

    public void setTask(Task task) {
        this.currentTask = task;
        if (task == null) return;
        titleField.setText(task.getTitle());
        descArea.setText(task.getDescription());
        if (task.getDue_date() != null) {
            LocalDate dueDate = task.getDue_date().toLocalDate();
            datePicker.setSelectedDate(dueDate);
            timePicker.setSelectedTime(task.getDue_date().toLocalTime());
        } else {
            datePicker.clearSelectedDate();
            timePicker.clearSelectedTime();
        }
        if (task.getFolder_name() != null) {
            for (int i=0; i<folderBox.getItemCount(); i++) {
                if (folderBox.getItemAt(i).equals(task.getFolder_name())) {
                    folderBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (task.getStatus() != null) {
            statusBox.setSelectedItem(TaskStatus.getStatusToString(task.getStatus()));
        }
    }
}
