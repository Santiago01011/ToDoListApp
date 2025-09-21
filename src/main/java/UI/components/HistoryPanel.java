package UI.components;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import COMMON.common;
import model.Task;
import net.miginfocom.swing.MigLayout;

public class HistoryPanel extends JPanel {
    public interface Listener {
        void onClose();
        void onTaskSelected(Task task);
        void onTaskRestore(Task task);
        void onTaskDelete(Task task);
    }
    
    private JPanel contentPanel;
    private Listener listener;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    
    public HistoryPanel(Listener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 0", "[][grow][]"));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Task History");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        
        JButton closeBtn = new JButton(common.getBackIcon());
        closeBtn.setToolTipText("Close History");
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> listener.onClose());
        
        headerPanel.add(titleLabel, "cell 0 0");
        headerPanel.add(closeBtn, "cell 2 0, gapbottom 10");
        
        add(headerPanel, BorderLayout.NORTH);
        
        contentPanel = new JPanel(new MigLayout("fillx, wrap 1", "[grow]", "[]5[]"));
        contentPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void displayTaskHistory(List<Task> taskHistory) {
        SwingUtilities.invokeLater(() -> {
            contentPanel.removeAll();
            
            if (taskHistory == null || taskHistory.isEmpty()) {
                JLabel emptyLabel = new JLabel("No task history available");
                emptyLabel.setForeground(UIManager.getColor("Label.foreground"));
                emptyLabel.setHorizontalAlignment(SwingUtilities.CENTER);
                contentPanel.add(emptyLabel, "growx");
            } else {
                for (Task task : taskHistory) {
                    HistoryItemPanel itemPanel = new HistoryItemPanel(task, t -> listener.onTaskSelected(t));
                    contentPanel.add(itemPanel, "growx");
                }
            }
            
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }
    
    private class HistoryItemPanel extends CardPanel {
        public HistoryItemPanel(Task task, java.util.function.Consumer<Task> onSelect) {
            super(new MigLayout("fillx, insets 10", "[grow][]", "[]5[]"));
            setThemeColors(UIManager.getColor("Card.background"), UIManager.getColor("Panel.background").darker());
            
            JLabel titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "No Title");
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            titleLabel.setForeground(UIManager.getColor("Label.foreground"));
            add(titleLabel, "cell 0 0, growx");
            
            LocalDateTime actionDate = task.getUpdated_at() != null ? task.getUpdated_at() : task.getCreated_at();
            String dateText = actionDate != null ? dateFormatter.format(actionDate) : "Unknown date";
            JLabel dateLabel = new JLabel(dateText);
            dateLabel.setFont(dateLabel.getFont().deriveFont(Font.PLAIN, 11f));
            dateLabel.setForeground(UIManager.getColor("Label.foreground").darker());
            add(dateLabel, "cell 0 1");
            

            JButton restoreBtn = new JButton(common.getRestoreIcon());
            restoreBtn.setToolTipText("Restore Task");
            restoreBtn.setBorderPainted(false);
            restoreBtn.setContentAreaFilled(false);
            restoreBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));            
            restoreBtn.addActionListener(e -> listener.onTaskRestore(task));
            add(restoreBtn, "cell 1 0, aligny center");
            
            JButton deleteBtn = new JButton(common.getDeleteIcon());
            deleteBtn.setToolTipText("Delete Task");
            deleteBtn.setBorderPainted(false);
            deleteBtn.setContentAreaFilled(false);
            deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            deleteBtn.addActionListener(e -> listener.onTaskDelete(task));
            add(deleteBtn, "cell 2 0, aligny center");

            JButton viewBtn = new JButton(common.getViewIcon());
            viewBtn.setToolTipText("View Task Details");
            viewBtn.setBorderPainted(false);
            viewBtn.setContentAreaFilled(false);
            viewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            viewBtn.addActionListener(e -> onSelect.accept(task));
            add(viewBtn, "cell 1 0 1 3, aligny center");
        }
    }
}
