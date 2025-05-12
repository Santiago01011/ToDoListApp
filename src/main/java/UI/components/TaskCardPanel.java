package UI.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import COMMON.common;
import model.Task;
import model.TaskStatus;
import net.miginfocom.swing.MigLayout;

public class TaskCardPanel extends JPanel {
    public interface Listener {
        void onToggleComplete(Task task);
        void onView(Task task);
        void onEdit(Task task);
        void onDelete(Task task);
    }

    public TaskCardPanel(Task task, Listener listener) {
        setLayout(new MigLayout("fillx, insets 10 15 10 15", "[][grow, fill][][]", "[]5[]5[]"));
        setBackground(common.getTertiaryColor());
        Color outlineColor = common.getPanelColor().darker();
        int arc = 10, thickness = 2;
        putClientProperty("FlatLaf.style", "arc: " + arc);
        setBorder(new UI.components.RoundedLineBorder(outlineColor, thickness, arc));

        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(task.getStatus() == TaskStatus.completed);
        checkBox.setToolTipText("Mark as " + (task.getStatus() == TaskStatus.completed ? "pending" : "complete"));
        checkBox.setOpaque(false);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkBox.addActionListener(e -> listener.onToggleComplete(task));
        add(checkBox, "spany 3, aligny top, gapright 10");

        JLabel titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "No Title");
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD));
        add(titleLabel, "growx");

        RoundedLabel statusLabel = new RoundedLabel(
                task.getStatus() != null ? TaskStatus.getStatusToString(task.getStatus()) : "Unknown", 15);
        Color statusColor = task.getStatus() == TaskStatus.in_progress ? common.getContrastColor()
                : common.getPanelColor().darker();
        statusLabel.setBackground(statusColor);
        statusLabel.setForeground(task.getStatus() == TaskStatus.in_progress ? common.getTextColor().darker()
                : common.getTextColor());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 9, 3, 9));
        statusLabel.setFont(statusLabel.getFont().deriveFont(java.awt.Font.PLAIN, 11f));
        add(statusLabel, "aligny top, wrap");

        String descText = task.getDescription() != null ? task.getDescription() : "No Description";
        int previewLength = 60;
        if (descText.length() > previewLength) {
            descText = descText.substring(0, previewLength) + "...";
        }
        JLabel descLabel = new JLabel("<html>" + descText.replace("\n", "<br>") + "</html>");
        descLabel.setFont(descLabel.getFont().deriveFont(java.awt.Font.PLAIN, 11f));
        add(descLabel, "growx, wrap");

        JLabel dateLabel = new JLabel();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        if (task.getStatus() == TaskStatus.completed) {
            setBackground(getBackground().darker());
            titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.ITALIC | java.awt.Font.BOLD));
            titleLabel.setText("<html><s>" + (task.getTitle() != null ? task.getTitle() : "No Title") + "</s></html>");
        } else if (task.getDue_date() != null) {
            dateLabel.setText("Due " + task.getDue_date().format(df));
            if (task.getDue_date().isBefore(LocalDateTime.now())) {
                dateLabel.setForeground(Color.RED);
            }
        } else {
            dateLabel.setText("No due date");
        }
        dateLabel.setFont(dateLabel.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
        add(dateLabel, "growx");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actions.setOpaque(false);

        JButton viewBtn = new JButton(common.getViewIcon());
        styleIcon(viewBtn, "View Task Details");
        viewBtn.addActionListener(e -> listener.onView(task));
        actions.add(viewBtn);

        JButton editBtn = new JButton(common.getEditIcon());
        styleIcon(editBtn, "Edit Task");
        editBtn.addActionListener(e -> listener.onEdit(task));
        actions.add(editBtn);

        JButton deleteBtn = new JButton(common.getDeleteIcon());
        styleIcon(deleteBtn, "Delete Task");
        deleteBtn.addActionListener(e -> listener.onDelete(task));
        actions.add(deleteBtn);

        add(actions, "spany 3, aligny top");
    }

    private void styleIcon(JButton btn, String tip) {
        btn.setToolTipText(tip);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}