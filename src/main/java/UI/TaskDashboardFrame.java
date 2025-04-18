package UI;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;
import java.time.format.DateTimeFormatter;

import model.TaskHandler;
import model.Task;
import COMMON.common;
import net.miginfocom.swing.MigLayout;

public class TaskDashboardFrame extends Frame {

    private TaskHandler taskHandler;
    private JPanel taskListPanel;
    private JComboBox<String> folderComboBox;

    // Constructor accepts TaskHandler
    public TaskDashboardFrame(String title, TaskHandler taskHandler) {
        super(title);
        this.taskHandler = taskHandler;

        initComponents();
        loadTasks();

        // Frame settings
        setResizable(true);
        setSize(700, 700); 
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Shutting down application...");
                taskHandler.saveTasksToJson();
                // Optional: Add logic here to save tasks or confirm exit
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        JPanel topBarPanel = createTopBarPanel();
        add(topBarPanel, BorderLayout.NORTH);
        taskListPanel = new JPanel(new MigLayout("wrap 1, fillx, inset 15", "[grow, fill]", ""));

        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomBarPanel = createBottomBarPanel();
        add(bottomBarPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5 10 5 10, fillx",
                "[][][]push[][]", // Columns: Icon, TaskFlow, Folders | push | Filter, Settings
                "[]"));
        panel.setBackground(common.getPanelColor().darker());

        JLabel logoLabel = new JLabel(common.getAppIcon());
        panel.add(logoLabel, "gapright 10");
        JLabel titleLabel = new JLabel("TaskFlow");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 21f));
        panel.add(titleLabel, "gapright 15, aligny center"); 

        List<String> folderList = taskHandler.getFolderList();
        folderList.add(0, "All Folders"); 
        folderComboBox = new JComboBox<>(folderList.toArray(new String[0]));
        folderComboBox.setSelectedItem("All Folders");
        
        
        panel.add(folderComboBox, "width 150!"); 

        JButton filterButton = new JButton(common.getFilterIcon());
        styleIconButton(filterButton, "Filter Tasks");
        panel.add(filterButton, "gapright 15");

        JButton userButton = new JButton(common.getUserConfigIcon());
        styleIconButton(userButton, "User Profile");

        panel.add(userButton);

        return panel;
    }

    private void styleIconButton(JButton button, String tooltipText) {
        button.setMargin(new Insets(4, 4, 4, 4));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setToolTipText(tooltipText);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel createBottomBarPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5 10 5 10, fillx",
                "[]push[][][]", // Columns: New Task | Last Sync Timestamp | Sync | History
                "[]")); // Single row

        panel.setBackground(common.getPanelColor().darker());

        JButton newTaskButton = new JButton("New Task");
        newTaskButton.setFont(newTaskButton.getFont().deriveFont(Font.BOLD));
        newTaskButton.setBackground(common.getSecondaryColor());
        newTaskButton.setForeground(common.getTextColor());
        panel.add(newTaskButton, "gapleft 5");

        JLabel lastSyncLabel = new JLabel();
        lastSyncLabel.setFont(lastSyncLabel.getFont().deriveFont(Font.PLAIN, 12f));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
        if (taskHandler.getLastSync() != null) {
            lastSyncLabel.setText("Last Sync: " + taskHandler.getLastSync().format(dateFormatter));
        } else {
            lastSyncLabel.setText("No sync yet.");
        }
        panel.add(lastSyncLabel, "gapright 15");
        
        JButton syncButton = new JButton(common.getSyncIcon());
        styleIconButton(syncButton, "Synchronize Tasks");
        panel.add(syncButton, "gapright 15");

        JButton historyButton = new JButton("History");
        historyButton.setFont(historyButton.getFont().deriveFont(Font.BOLD));
        panel.add(historyButton, "gapright 5");
        return panel;
    }

    private void loadTasks() {
        taskListPanel.removeAll();
        if (taskHandler.userTasksList != null && !taskHandler.userTasksList.isEmpty()) {
            for (Task task : taskHandler.userTasksList) {
                JPanel taskCard = createTaskCardPanel(task);
                taskListPanel.add(taskCard, "growx, gapbottom 10");
            }
        } else {
            JLabel noTasksLabel = new JLabel("No tasks found. Create one to get started!");
            noTasksLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noTasksLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            taskListPanel.add(noTasksLabel, "growx");
        }
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel createTaskCardPanel(Task task) {
        // Columns: [checkbox][grow, fill][status][actions]
        JPanel card = new JPanel(new MigLayout("fillx, insets 10 15 10 15", // Padding
                "[][grow, fill][][]", // Columns: Checkbox, Text content, Status, Actions
                "[]5[]5[]")); // Rows: Title, Description Preview, Due Date (5px gap) - Changed row structure
        card.setBackground(common.getTertiaryColor());

        
        Color outlineColor = common.getPanelColor().darker();

     
        card.putClientProperty("FlatLaf.style", "arc: 10");
        card.putClientProperty("Component.outline", outlineColor);
        card.putClientProperty("Component.outlineWidth", 2); 

        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(task.getStatus().equalsIgnoreCase("completed"));
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(checkBox, "spany 3, aligny top, gapright 10"); // Span 3 rows now

        JLabel titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "No Title");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        card.add(titleLabel, "growx");

        JLabel statusLabel = new JLabel(task.getStatus() != null ? task.getStatus() : "Unknown");

        statusLabel.setOpaque(true);
        statusLabel.setBackground(common.getPanelColor().darker());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));


        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        card.add(statusLabel, "aligny top, wrap");

        String descriptionText = task.getDescription() != null ? task.getDescription() : "No Description";
        int previewLength = 60;
        if (descriptionText.length() > previewLength) {
            descriptionText = descriptionText.substring(0, previewLength) + "...";
        }
        JLabel descriptionPreviewLabel = new JLabel(descriptionText);
        descriptionPreviewLabel.setFont(descriptionPreviewLabel.getFont().deriveFont(Font.PLAIN, 11f));
        card.add(descriptionPreviewLabel, "growx, wrap");

        JLabel dateLabel = new JLabel();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        if ("completed".equalsIgnoreCase(task.getStatus()) && task.getUpdated_at() != null) {
             dateLabel.setText("Completed " + task.getUpdated_at().format(dateFormatter));
             card.setBackground(common.getTertiaryColor().darker());
             // remove description preview label and put a line in the title
                card.remove(descriptionPreviewLabel);                
                titleLabel.setFont(titleLabel.getFont().deriveFont(Font.ITALIC));
                titleLabel.setText("<html><s>" + task.getTitle() + "</s></html>");
        } else if (task.getDue_date() != null) {
            dateLabel.setText("Due " + task.getDue_date().format(dateFormatter));
        } else {
            dateLabel.setText("No due date");
        }

        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.PLAIN, 11f));
        card.add(dateLabel, "growx"); // Date label on the third row, below description

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        actionsPanel.setOpaque(false);

        JButton viewButton = new JButton(common.getViewIcon());
        JButton editButton = new JButton(common.getEditIcon());
        JButton deleteButton = new JButton(common.getDeleteIcon());

        styleIconButton(viewButton, "View Task Details");
        styleIconButton(editButton, "Edit Task");
        styleIconButton(deleteButton, "Delete Task");

        actionsPanel.add(viewButton);
        actionsPanel.add(editButton);
        actionsPanel.add(deleteButton);

        card.add(actionsPanel, "spany 3, aligny top"); // Span 3 rows now
        return card;
    }
}
