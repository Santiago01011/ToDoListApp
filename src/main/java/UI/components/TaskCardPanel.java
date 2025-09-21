package UI.components;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import COMMON.common;
import model.Task;
import model.TaskStatus;
import net.miginfocom.swing.MigLayout;

public class TaskCardPanel extends CardPanel {
    public interface Listener {
        void onToggleComplete(Task task);
        void onView(Task task);
        void onEdit(Task task);
        void onDelete(Task task);
    }

    private final JLabel titleLabel;
    private final JLabel descLabel;
    private final JLabel statusBadge;
    private final JLabel dueDateLabel;
    private final JCheckBox completeCheckBox;

    private final JPanel actionsPanel;
    
    public TaskCardPanel(Task task, Listener listener) {

        // DateTimeFormatter for reuse
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        setLayout(new MigLayout("fillx, insets 12 16 12 16, wrap", "[grow,fill][right]"));

        // Base card styling with lighter background and hover effect
        putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:16;" +
                "background: lighten($Panel.background,3%);" +
                "border: 1,1,1,1,$Component.borderColor");

        // Add hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                putClientProperty(FlatClientProperties.STYLE, "" +
                        "arc:16;" +
                        "background: lighten($Panel.background,8%);" +
                        "border: 1,1,1,1,$Component.borderColor");
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                putClientProperty(FlatClientProperties.STYLE, "" +
                        "arc:16;" +
                        "background: lighten($Panel.background,3%);" +
                        "border: 1,1,1,1,$Component.borderColor");
                repaint();
            }
        });

        // Title
        titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "No Title");
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");

        // Status badge with dynamic colors
        statusBadge = new JLabel(task.getStatus() != null ? TaskStatus.getStatusToString(task.getStatus()) : "Unknown");
        statusBadge.setOpaque(true);
        statusBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusBadge.setBorder(new javax.swing.border.EmptyBorder(2, 8, 2, 8));
        
        // Set dynamic colors based on status
        String bgColor = getStatusBackgroundColor(task.getStatus());
        String fgColor = getStatusForegroundColor(task.getStatus());
        statusBadge.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:12;" +
                "background: " + bgColor + ";" +
                "foreground: " + fgColor);

        // Description with truncation for preview
        String fullDescText = task.getDescription() != null ? task.getDescription() : "No Description";
        String descText = truncateDescription(fullDescText, 15); // Show first 15 words
        descLabel = new JLabel("<html>" + descText.replace("\n", "<br>") + "</html>");
        descLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground; font: medium");

        // Completion checkbox
        completeCheckBox = new JCheckBox();
        completeCheckBox.setSelected(task.getStatus() == TaskStatus.completed);
        completeCheckBox.setOpaque(false);
        completeCheckBox.putClientProperty(FlatClientProperties.STYLE, "border: 0,0,0,0");
        completeCheckBox.addActionListener(e -> {
            if (listener != null) {
                listener.onToggleComplete(task);
            }
        });

        // Due date
        String dueText = task.getDue_date() != null ? "Due " + task.getDue_date().format(fmt) : "No due date";
        dueDateLabel = new JLabel(dueText);
        dueDateLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Component.errorColor; font: italic");

        // Actions (Compact only)
        actionsPanel = new JPanel(new MigLayout("insets 0, gap 4", "[]"));
        actionsPanel.setOpaque(false);
        addActionButton(common.getEditIcon(), "Edit task", () -> { if (listener != null) listener.onEdit(task); });
        addActionButton(common.getDeleteIcon(), "Delete task", () -> { if (listener != null) listener.onDelete(task); });
        addActionButton(common.getViewIcon(), "View task", () -> { if (listener != null) listener.onView(task); });
        buildCompactLayout();
    }

    private void buildCompactLayout() {
        setLayout(new MigLayout("fillx, insets 16 20 16 20", "[][][grow][]", "[][][]"));

        // Header row: Checkbox, Title, Status badge
        add(completeCheckBox, "cell 0 0");
        add(titleLabel, "cell 1 0, growx");
        add(statusBadge, "cell 2 0, gapright 0");

        // Description row
        add(descLabel, "cell 0 1, span 3, gaptop 8, wrap");

        // Footer row: Due date and Actions
        JPanel footerPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        footerPanel.setOpaque(false);

        footerPanel.add(dueDateLabel, "growx");
        footerPanel.add(actionsPanel, "");

        add(footerPanel, "cell 0 2, span 3, gaptop 12, growx");
    }

    private void addActionButton(ImageIcon icon, String tooltip, Runnable action) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        
        // Default state: circular button
        btn.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc: 32;" +
                "background: $Button.background;" +
                "foreground: $Label.foreground;" +
                "borderWidth: 0;" +
                "focusWidth: 0;" +
                "innerFocusWidth: 0");
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setBorderPainted(false);

        // Hover effect: square with accent color
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.putClientProperty(FlatClientProperties.STYLE, "" +
                        "arc: 8;" +
                        "background: $Component.accentColor;" +
                        "foreground: $Label.foreground;" +
                        "borderWidth: 0;" +
                        "focusWidth: 0;" +
                        "innerFocusWidth: 0");
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.putClientProperty(FlatClientProperties.STYLE, "" +
                        "arc: 32;" +
                        "background: $Button.background;" +
                        "foreground: $Label.foreground;" +
                        "borderWidth: 0;" +
                        "focusWidth: 0;" +
                        "innerFocusWidth: 0");
                btn.repaint();
            }
        });

        btn.addActionListener(e -> action.run());
        actionsPanel.add(btn);
    }

    /**
     * Truncates the description to show only the first 'maxWords' words.
     * If the description is longer, adds "..." to indicate truncation.
     */
    private String truncateDescription(String description, int maxWords) {
        if (description == null || description.trim().isEmpty()) {
            return "No Description";
        }

        String[] words = description.trim().split("\\s+");
        if (words.length <= maxWords) {
            return description;
        }

        // Take first maxWords words and join them back
        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (i > 0) truncated.append(" ");
            truncated.append(words[i]);
        }
        truncated.append("...");

        return truncated.toString();
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