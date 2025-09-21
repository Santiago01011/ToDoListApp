package UI.components;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Cursor;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import COMMON.common;
import model.Task;
import model.TaskStatus;
import net.miginfocom.swing.MigLayout;

public class ViewTaskPanel extends CardPanel {
    public interface Listener {
        void onClose();
    }
    
    public ViewTaskPanel(Listener listener, Task task) {
        super("insets 20", "[grow]", "[][][][][][]");
        
        // Modern card styling with FlatLaf
        putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:16;" +
                "background: $Panel.background;" +
                "border: 1,1,1,1,$Component.borderColor");

        // Header section with title and close button
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Task Details");
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +2; foreground: $Label.foreground");
        headerPanel.add(titleLabel, "growx");
        
        JButton closeBtn = new JButton(common.getBackIcon());
        closeBtn.setToolTipText("Close");
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:50;" +
                "background: $Button.background;" +
                "border: 0,0,0,0");
        closeBtn.addActionListener(e -> listener.onClose());
        headerPanel.add(closeBtn, "");
        add(headerPanel, "growx, wrap");
        
        // Task title and status section
        JPanel titleStatusPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        titleStatusPanel.setOpaque(false);
        
        JLabel taskTitleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "No Title");
        taskTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +4");
        titleStatusPanel.add(taskTitleLabel, "growx");
        
        JLabel statusLabel = new JLabel(TaskStatus.getStatusToString(task.getStatus()));
        statusLabel.setOpaque(true);
        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusLabel.setBorder(new javax.swing.border.EmptyBorder(6, 16, 6, 16));
        
        // Set dynamic colors based on status
        String bgColor = getStatusBackgroundColor(task.getStatus());
        String fgColor = getStatusForegroundColor(task.getStatus());
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:20;" +
                "background: " + bgColor + ";" +
                "foreground: " + fgColor + ";" +
                "font: medium");
        titleStatusPanel.add(statusLabel, "");
        
        add(titleStatusPanel, "growx, gaptop 16, wrap");
        
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            // Description panel with subtle background
            JPanel descriptionPanel = new JPanel(new MigLayout("insets 12", "[grow]", "[]"));
            descriptionPanel.setOpaque(true);
            descriptionPanel.putClientProperty(FlatClientProperties.STYLE, "" +
                    "arc:8;" +
                    "background: lighten($Panel.background,5%);" +
                    "border: 1,1,1,1,$Component.borderColor");
            
            // Use JTextArea for proper text wrapping
            javax.swing.JTextArea taskDescArea = new javax.swing.JTextArea(task.getDescription());
            taskDescArea.setEditable(false);
            taskDescArea.setOpaque(false);
            taskDescArea.setWrapStyleWord(true);
            taskDescArea.setLineWrap(true);
            taskDescArea.setBorder(null);
            taskDescArea.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.foreground; font: medium;");
            taskDescArea.setFont(javax.swing.UIManager.getFont("Label.font"));
            
            descriptionPanel.add(taskDescArea, "growx");
            
            add(descriptionPanel, "growx, gaptop 12, wrap");
        }
        
        // Details section
        JPanel detailsPanel = new JPanel(new MigLayout("insets 0", "[][grow]", "[][][][]"));
        detailsPanel.setOpaque(false);
        
        // Status row
        JLabel statusTitleLabel = new JLabel("Status:");
        statusTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: medium");
        detailsPanel.add(statusTitleLabel, "");
        
        JLabel statusValueLabel = new JLabel(TaskStatus.getStatusToString(task.getStatus()));
        statusValueLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.foreground; font: medium");
        detailsPanel.add(statusValueLabel, "wrap");
        
        // Folder row
        JLabel folderTitleLabel = new JLabel("Folder:");
        folderTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: medium");
        detailsPanel.add(folderTitleLabel, "");
        
        JLabel folderValueLabel = new JLabel(task.getFolder_name() != null ? task.getFolder_name() : "Default Folder");
        folderValueLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.foreground; font: medium");
        detailsPanel.add(folderValueLabel, "wrap");
        
        // Due date row
        JLabel dueDateTitleLabel = new JLabel("Due date:");
        dueDateTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: medium");
        detailsPanel.add(dueDateTitleLabel, "");
        
        DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dueDate = task.getDue_date() != null ? task.getDue_date().format(DATE_FORMATTER) : "No due date";
        JLabel dueDateValueLabel = new JLabel(dueDate);
        dueDateValueLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.foreground; font: medium");
        detailsPanel.add(dueDateValueLabel, "wrap");
        
        // Created date row
        JLabel createdTitleLabel = new JLabel("Created:");
        createdTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: medium");
        detailsPanel.add(createdTitleLabel, "");
        
        String createdDate = task.getCreated_at() != null ? task.getCreated_at().format(DATE_FORMATTER) : "Unknown";
        JLabel createdValueLabel = new JLabel(createdDate);
        createdValueLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: medium");
        detailsPanel.add(createdValueLabel, "wrap");
        
        // Last updated date row
        JLabel updatedTitleLabel = new JLabel("Last updated:");
        updatedTitleLabel.putClientProperty(FlatClientProperties.STYLE, "font: medium");
        detailsPanel.add(updatedTitleLabel, "");
        
        String updatedDate = task.getUpdated_at() != null ? task.getUpdated_at().format(DATE_FORMATTER) : "Never";
        JLabel updatedValueLabel = new JLabel(updatedDate);
        updatedValueLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: medium");
        detailsPanel.add(updatedValueLabel, "");
        
        add(detailsPanel, "growx, gaptop 20, wrap");
    }

    /**
     * Get the background color theme variable for a task status.
     */
    private String getStatusBackgroundColor(TaskStatus status) {
        if (status == null) return "$Component.borderColor";

        switch (status) {
            case completed: return "@StatusCompleted";
            case in_progress: return "@StatusInProgress";
            case pending: return "@StatusPending";
            case incoming_due: return "@StatusIncomingDue";
            case overdue: return "@StatusOverdue";
            case newest: return "@StatusNewest";
            default: return "$Component.borderColor";
        }
    }

    /**
     * Get the foreground color theme variable for a task status.
     */
    private String getStatusForegroundColor(TaskStatus status) {
        // Most statuses use default label color, but yellow status needs dark text
        return status == TaskStatus.incoming_due ? "#000000" : "$Label.foreground";
    }
}
