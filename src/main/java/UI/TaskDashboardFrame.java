package UI;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import model.Task;
import COMMON.common;
import controller.TaskController;
import net.miginfocom.swing.MigLayout;

public class TaskDashboardFrame extends Frame {

    private TaskController taskController;
    private JPanel taskListPanel;
    private JComboBox<String> folderComboBox;
    private JLabel lastSyncLabel;

    public TaskDashboardFrame(String title) {
        super(title);

        setResizable(true);
        setSize(700, 700);
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Shutting down application...");
                taskController.handleWindowClosing();
                System.exit(0);
            }
        });
    }

    public void setController(TaskController taskController) {
        this.taskController = taskController;
    }

    public void initialize() {
        if (taskController == null) {
            System.err.println("Error: TaskController not set before initializing TaskDashboardFrame.");
            JOptionPane.showMessageDialog(this,
                "Application initialization failed: Controller not set.",
                "Initialization Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        initComponents();
        taskController.loadInitialFolderList();
        taskController.loadInitialTasks();
        updateLastSyncLabel();
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
                "[][][]push[][]",
                "[]"));
        panel.setBackground(common.getPanelColor().darker());

        JLabel logoLabel = new JLabel(common.getAppIcon());
        panel.add(logoLabel, "gapright 10");
        JLabel titleLabel = new JLabel("TaskFlow");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 21f));
        panel.add(titleLabel, "gapright 15, aligny center");

        folderComboBox = new JComboBox<>();
        folderComboBox.addActionListener(e -> {
            if (taskController != null && folderComboBox.getItemCount() > 0) {
                if (e.getActionCommand().equals("comboBoxChanged")) {
                    String selectedFolder = (String) folderComboBox.getSelectedItem();
                    System.out.println("Folder selected: " + selectedFolder);
                    taskController.handleFilterByFolderRequest(selectedFolder);
                }
            }
        });
        panel.add(folderComboBox, "width 150!");

        JButton filterButton = new JButton(common.getFilterIcon());
        styleIconButton(filterButton, "Filter Tasks");
        filterButton.addActionListener(e -> {
            if (taskController != null) {
                System.out.println("Filter button clicked");
                taskController.handleFilterButtonClicked();
            }
        });
        panel.add(filterButton, "gapright 15");

        JButton userButton = new JButton(common.getUserConfigIcon());
        styleIconButton(userButton, "User Profile");
        userButton.addActionListener(e -> {
            if (taskController != null) {
                System.out.println("User button clicked");
                taskController.handleUserButtonClicked();
            }
        });
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
                "[]push[][][]",
                "[]"));
        panel.setBackground(common.getPanelColor().darker());

        JButton newTaskButton = new JButton("New task");
        newTaskButton.setIcon(new ImageIcon(common.getAddIcon().getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        newTaskButton.setIconTextGap(5);
        newTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newTaskButton.addActionListener(e -> {
            if (taskController != null) {
                System.out.println("New Task button clicked");
                taskController.handleNewTaskRequest();
            }
        });
        panel.add(newTaskButton, "gapleft 5");

        lastSyncLabel = new JLabel("Sync status unknown");
        lastSyncLabel.setFont(lastSyncLabel.getFont().deriveFont(Font.PLAIN, 12f));
        panel.add(lastSyncLabel, "gapright 15");

        JButton syncButton = new JButton(common.getSyncIcon());
        styleIconButton(syncButton, "Synchronize Tasks");
        syncButton.addActionListener(e -> {
            if (taskController != null) {
                System.out.println("Sync button clicked");
                taskController.handleSyncRequest();
            }
        });
        panel.add(syncButton, "gapright 15");

        JButton historyButton = new JButton("History");
        historyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        historyButton.addActionListener(e -> {
            if (taskController != null) {
                System.out.println("History button clicked");
                taskController.handleHistoryRequest();
            }
        });
        panel.add(historyButton, "gapright 5");
        return panel;
    }

    public void refreshTaskListDisplay(List<Task> tasks) {
        SwingUtilities.invokeLater(() -> {
            taskListPanel.removeAll();
            if (tasks != null && !tasks.isEmpty()) {
                boolean tasksDisplayed = false;
                for (Task task : tasks) {
                    if (task == null) continue;

                    JPanel taskCard = createTaskCardPanel(task);
                    taskListPanel.add(taskCard, "growx, gapbottom 10");
                    tasksDisplayed = true;
                }
                if (!tasksDisplayed) {
                    JLabel noTasksLabel = new JLabel("No tasks found matching criteria.");
                    noTasksLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    noTasksLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
                    taskListPanel.add(noTasksLabel, "growx");
                }
            } else {
                JLabel noTasksLabel = new JLabel("No tasks found matching criteria.");
                noTasksLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noTasksLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
                taskListPanel.add(noTasksLabel, "growx");
            }
            taskListPanel.revalidate();
            taskListPanel.repaint();
        });
    }

    public void updateFolderList(List<String> folderList) {
        SwingUtilities.invokeLater(() -> {
            if (folderComboBox != null) {
                String currentSelection = (String) folderComboBox.getSelectedItem();

                ActionListener[] listeners = folderComboBox.getActionListeners();
                for (ActionListener l : listeners) {
                    folderComboBox.removeActionListener(l);
                }

                folderComboBox.removeAllItems();
                folderComboBox.addItem("All Folders");
                if (folderList != null) {
                    for (String folder : folderList) {
                        folderComboBox.addItem(folder);
                    }
                }

                boolean selectionRestored = false;
                if (currentSelection != null && folderList != null) {
                    for (String item : folderList) {
                        if (item.equals(currentSelection)) {
                            folderComboBox.setSelectedItem(currentSelection);
                            selectionRestored = true;
                            break;
                        }
                    }
                }
                if (!selectionRestored) {
                    folderComboBox.setSelectedItem("All Folders");
                }

                for (ActionListener l : listeners) {
                    folderComboBox.addActionListener(l);
                }
            }
        });
    }

    public void updateLastSyncLabel(LocalDateTime lastSyncTime) {
        SwingUtilities.invokeLater(() -> {
            if (lastSyncLabel != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
                if (lastSyncTime != null) lastSyncLabel.setText("Last Sync: " + lastSyncTime.format(dateFormatter));
                else lastSyncLabel.setText("Never synced.");
            }
        });
    }

    private JPanel createTaskCardPanel(Task task) {
        JPanel card = new JPanel(new MigLayout("fillx, insets 10 15 10 15",
                "[][grow, fill][][]",
                "[]5[]5[]"));
        card.setBackground(common.getTertiaryColor());
        Color outlineColor = common.getPanelColor().darker();
        int arc = 10;
        int thickness = 2;
        card.putClientProperty("FlatLaf.style", "arc: " + arc);
        card.setBorder(new RoundedLineBorder(outlineColor, thickness, arc));

        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(task.getStatus().equals("completed"));
        checkBox.setOpaque(false);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkBox.addActionListener(e -> {
            if (taskController != null) {
                boolean isSelected = checkBox.isSelected();
                System.out.println("Checkbox for task '" + task.getTitle() + "' changed to: " + isSelected);
                taskController.handleTaskCompletionToggle(task.getTask_id(), isSelected);
            }
        });
        card.add(checkBox, "spany 3, aligny top, gapright 10");

        JLabel titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "No Title");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        card.add(titleLabel, "growx");

        RoundedLabel statusLabel = new RoundedLabel(
            task.getStatus() != null ? task.getStatus() : "Unknown",
            15
        );
        Color statusColor = task.getStatus().equalsIgnoreCase("in_progress") ? common.getContrastColor() : common.getPanelColor().darker();
        statusLabel.setForeground(task.getStatus().equalsIgnoreCase("in_progress") ?  common.getTextColor().darker() : common.getTextColor());
        statusLabel.setBackground(statusColor);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 9, 3, 9));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        card.add(statusLabel, "aligny top, wrap");

        String descriptionText = task.getDescription() != null ? task.getDescription() : "No Description";
        int previewLength = 60;
        if (descriptionText.length() > previewLength) {
            descriptionText = descriptionText.substring(0, previewLength) + "...";
        }
        JLabel descriptionPreviewLabel = new JLabel("<html>" + descriptionText.replace("\n", "<br>") + "</html>");
        descriptionPreviewLabel.setFont(descriptionPreviewLabel.getFont().deriveFont(Font.PLAIN, 11f));
        card.add(descriptionPreviewLabel, "growx, wrap");

        JLabel dateLabel = new JLabel();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        boolean isCompleted = "completed".equalsIgnoreCase(task.getStatus());

        if (isCompleted && task.getUpdated_at() != null) {
            dateLabel.setText("Completed " + task.getUpdated_at().format(dateFormatter));
            card.setBackground(common.getSecondaryColor().darker());
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.ITALIC));
            titleLabel.setText("<html><s>" + (task.getTitle() != null ? task.getTitle() : "No Title") + "</s></html>");
        } else if (task.getDue_date() != null) {
            dateLabel.setText("Due " + task.getDue_date().format(dateFormatter));
            if (task.getDue_date().isBefore(LocalDateTime.now()) && !isCompleted) {
                dateLabel.setForeground(Color.RED);
            }
        } else {
            dateLabel.setText("No due date");
        }
        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.PLAIN, 11f));
        card.add(dateLabel, "growx");

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionsPanel.setOpaque(false);

        JButton viewButton = new JButton(common.getViewIcon());
        styleIconButton(viewButton, "View Task Details");
        viewButton.addActionListener(e -> {
            if (taskController != null) {
                System.out.println("View button clicked for task: " + task.getTitle());
                taskController.handleViewTaskRequest(task.getTask_id());
            }
        });
        actionsPanel.add(viewButton);

        JButton editButton = new JButton(common.getEditIcon());
        styleIconButton(editButton, "Edit Task");
        editButton.addActionListener(e -> {
            if (taskController != null) {
                System.out.println("Edit button clicked for task: " + task.getTitle());
                taskController.handleEditTaskRequest(task.getTask_id());
            }
        });
        actionsPanel.add(editButton);

        JButton deleteButton = new JButton(common.getDeleteIcon());
        styleIconButton(deleteButton, "Delete Task");
        deleteButton.addActionListener(e -> {
            if (taskController != null) {
                System.out.println("Delete button clicked for task: " + task.getTitle());
                int result = JOptionPane.showConfirmDialog(TaskDashboardFrame.this,
                        "Are you sure you want to delete task '" + task.getTitle() + "'?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    taskController.handleDeleteTaskRequest(task.getTask_id());
                }
            }
        });
        actionsPanel.add(deleteButton);

        card.add(actionsPanel, "spany 3, aligny top");
        return card;
    }

    private void updateLastSyncLabel() {
        if (taskController != null) {
            taskController.loadInitialSyncTime();
        }
    }
}
