package UI.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import COMMON.common;
import model.Task;
import model.TaskStatus;
import net.miginfocom.swing.MigLayout;

public class ViewTaskPanel extends CardPanel {
    public interface Listener {
        void onClose();
    }
    
    public ViewTaskPanel(Listener listener, Task task) {
        super("insets 15", "[grow]", "[][][][][][][]");
        setThemeColors(common.getTertiaryColor(), common.getPanelColor().darker());

        JLabel titleLabel = new JLabel("Task Details");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18));
        add(titleLabel, "wrap");
        
        JLabel taskTitleLabel = new JLabel(task.getTitle());
        taskTitleLabel.setFont(taskTitleLabel.getFont().deriveFont(Font.BOLD, 16));
        add(taskTitleLabel, "growx, wrap");
        
        JLabel statusLabel = new JLabel("Status: " + TaskStatus.getStatusToString(task.getStatus()));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12));
        add(statusLabel, "wrap");
        
        JLabel folderLabel = new JLabel("Folder: " + (task.getFolder_name() != null ? task.getFolder_name() : "None"));
        folderLabel.setFont(folderLabel.getFont().deriveFont(Font.PLAIN, 12));
        add(folderLabel, "wrap");
        DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dueDate = task.getDue_date() != null ? task.getDue_date().format(DATE_FORMATTER) : "No due date";
        JLabel dueDateLabel = new JLabel("Due date: " + dueDate);
        dueDateLabel.setFont(dueDateLabel.getFont().deriveFont(Font.PLAIN, 12));
        add(dueDateLabel, "wrap");
        
        JLabel descriptionTitle = new JLabel("Description:");
        descriptionTitle.setFont(descriptionTitle.getFont().deriveFont(Font.BOLD, 12));
        add(descriptionTitle, "wrap");
        
        JTextArea descriptionArea = new JTextArea(task.getDescription());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.PLAIN, 12));

        add(descriptionArea, "grow, wrap");
        
        JPanel metadataPanel = new JPanel(new MigLayout("insets 0", "[grow]"));
        metadataPanel.setBackground(common.getTertiaryColor());
        
        String createdDate = task.getCreated_at() != null ? task.getCreated_at().format(DATE_FORMATTER) : "Unknown";
        JLabel createdAtLabel = new JLabel("Created: " + createdDate);
        createdAtLabel.setFont(createdAtLabel.getFont().deriveFont(Font.ITALIC, 11));
        metadataPanel.add(createdAtLabel, "wrap");
        
        String updatedDate = task.getUpdated_at() != null ? task.getUpdated_at().format(DATE_FORMATTER) : "Never";
        JLabel updatedAtLabel = new JLabel("Last updated: " + updatedDate);
        updatedAtLabel.setFont(updatedAtLabel.getFont().deriveFont(Font.ITALIC, 11));
        metadataPanel.add(updatedAtLabel, "wrap");
        
        // JLabel taskIdLabel = new JLabel("Task ID: " + task.getTask_id());
        // taskIdLabel.setFont(taskIdLabel.getFont().deriveFont(Font.ITALIC, 11));
        // metadataPanel.add(taskIdLabel);
        
        JButton closeBtn = new JButton(common.getBackIcon());
        closeBtn.setToolTipText("Close");
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.addActionListener(e -> listener.onClose());
        add(metadataPanel, "split 2, growx");
        add(closeBtn, "right, gapright 10, gaptop 10, wrap");
    }
}
