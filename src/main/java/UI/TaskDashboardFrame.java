package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import COMMON.UserProperties;
import COMMON.common;
import controller.TaskController;
import model.FiltersCriteria;
import model.TaskStatus;
import model.Task;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class TaskDashboardFrame extends Frame {
    private static final int ANIMATION_DURATION = 150;
    private static final int TIMER_DELAY = 15;

    private TaskController taskController;
    private JPanel taskListPanel;
    private JLabel lastSyncLabel;
    private JPanel contentContainer;
    private JPanel mainPanel;
    private JPanel newTaskPanel;
    private JPanel topBarPanel;
    private JButton toggleColorButton;
    private JButton newTaskButton;
    private boolean isNewTaskVisible = false;
    private JComboBox<String> newTaskFolderBox;
    private boolean suppressFolderEvents = false;
    private JPopupMenu userPopupMenu;

    private JComboBox<String> folderFilterBox;
    FiltersCriteria filterCriteria = FiltersCriteria.defaultCriteria();
    private JPopupMenu filterPopupMenu;

    private DatePicker datePicker;
    private TimePicker timePicker;

    // Keep track of the filter menu items to easily clear them
    private JCheckBoxMenuItem filterPendingItem;
    private JCheckBoxMenuItem filterCompletedItem;
    private JCheckBoxMenuItem filterInProgressItem;
    private JCheckBoxMenuItem filterCancelledItem; // Add if you have this status

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

    private void rebuildUI() {
        SwingUtilities.invokeLater(() -> {
            Container content = getContentPane();
            content.removeAll();
            initComponents();
            taskController.loadInitialFolderList();
            taskController.loadInitialTasks();
            updateLastSyncLabel();
            toggleColorButton.setIcon(common.getModeIcon());
            revalidate();
            repaint();
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
        contentContainer = new JPanel(null);
        add(contentContainer, BorderLayout.CENTER);

        int width = getWidth();
        int height = getHeight();

        mainPanel = new JPanel(new BorderLayout(0, 0));
        topBarPanel = createTopBarPanel();
        add(topBarPanel, BorderLayout.NORTH);

        taskListPanel = new JPanel(new MigLayout("wrap 1, fillx, inset 15", "[grow, fill]", ""));
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBarPanel = createBottomBarPanel();
        add(bottomBarPanel, BorderLayout.SOUTH);

        mainPanel.setBounds(0, 0, width, height);
        contentContainer.add(mainPanel);

        newTaskPanel = createNewTaskPanel();
        newTaskPanel.setBounds(width, 0, width, height);
        contentContainer.add(newTaskPanel);
        contentContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = contentContainer.getWidth();
                int h = contentContainer.getHeight();
                if (isNewTaskVisible) {
                    mainPanel.setBounds(-w, 0, w, h);
                    newTaskPanel.setBounds(0, 0, w, h);
                } else {
                    mainPanel.setBounds(0, 0, w, h);
                    newTaskPanel.setBounds(w, 0, w, h);
                }
            }
        });


    }

    private JPanel createNewTaskPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 5", "[grow]", "[][][][][][]"));
        panel.setBorder(new EmptyBorder(10, 5, 5, 5));
        panel.add(new JLabel("New Task"), "wrap");
        JTextField titleField = new JTextField();
        titleField.setText("Enter task title...");
        addFocusListeners(titleField, "Enter task title...");
        panel.add(new JLabel("Title:"), "split 2");
        panel.add(titleField, "growx, wrap");

        JTextArea descArea = new JTextArea(3, 10);
        panel.add(new JLabel("Description:"), "split 2");
        panel.add(new JScrollPane(descArea), "growx, wrap");
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        
        // --- DatePicker setup ---
        datePicker = new DatePicker();
        datePicker.setDateFormat("dd/MM/yyyy");
        datePicker.setDateSelectionAble(date -> true);
        JFormattedTextField dateEditor = new JFormattedTextField();
        datePicker.setEditor(dateEditor);
        datePicker.now();
        datePicker.setCloseAfterSelected(true);
        datePicker.addDateSelectionListener(dateEvent -> {
            if (datePicker.isDateSelected()) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                System.out.println("event selected : " + datePicker.getSelectedDate().format(df));
            } else {
                System.out.println("event selected : null");
            }
        });


        timePicker = new TimePicker();
        timePicker.addTimeSelectionListener(timeEvent -> {
            if (timePicker.isTimeSelected()) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("hh:mm a");
                System.out.println("event selected : " + timePicker.getSelectedTime().format(df));
            } else {
                System.out.println("event selected : null");
            }
        });

        JFormattedTextField timeEditor = new JFormattedTextField();
        timePicker.setEditor(timeEditor);

        panel.add(new JLabel("Due Date:"), "split 4");
        panel.add(dateEditor, "growx");
        panel.add(new JLabel("Time:"));
        panel.add(timeEditor, "growx, wrap");

        // --- End DatePicker setup ---

        newTaskFolderBox = new JComboBox<>();
        newTaskFolderBox.setModel(new DefaultComboBoxModel<>(new String[] { "Default Folder" }));
        panel.add(new JLabel("Folder:"), "split 2");
        panel.add(newTaskFolderBox, "growx, wrap");

        JComboBox<String> statusBox = new JComboBox<>(new String[] { "pending", "in_progress", "completed" });
        panel.add(new JLabel("Status:"), "split 2");
        panel.add(statusBox, "growx, wrap");

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            if (titleField.getText().isEmpty() || titleField.getText().equals("Enter task title...")) {
                JOptionPane.showMessageDialog(TaskDashboardFrame.this, "The title field can't be empty.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                LocalDate date = datePicker.getSelectedDate();
                LocalDateTime dueDateTime = null;
                if (date != null) {
                    if (timePicker.isTimeSelected()) {
                        dueDateTime = date.atTime(timePicker.getSelectedTime());
                    } else {
                        dueDateTime = date.atStartOfDay();
                    }
                }
                taskController.handleCreateTask(titleField.getText(), descArea.getText(),
                        (String) newTaskFolderBox.getSelectedItem(), dueDateTime, TaskStatus.valueOf(statusBox.getSelectedItem().toString()));
                newTaskButton.setEnabled(true);
                titleField.setText("Enter task title...");
                descArea.setText("");
                dateEditor.setValue(null);
                datePicker.clearSelectedDate();
                if (newTaskFolderBox.getItemCount() > 0)
                    newTaskFolderBox.setSelectedIndex(0);
                folderFilterBox.setEnabled(true);
                folderFilterBox.requestFocusInWindow();
                slideOutNewTaskPanel();
            }
        });
        JButton goBackBtn = new JButton(common.getBackIcon());
        goBackBtn.setToolTipText("Go back");
        goBackBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        goBackBtn.addActionListener(e -> {
            newTaskButton.setEnabled(true);
            titleField.setText("Enter task title...");
            descArea.setText("");
            dateEditor.setValue(null);
            datePicker.clearSelectedDate();
            if (newTaskFolderBox.getItemCount() > 0)
                newTaskFolderBox.setSelectedIndex(0);
            folderFilterBox.setEnabled(true);
            folderFilterBox.requestFocusInWindow();
            slideOutNewTaskPanel();
        });
        panel.add(goBackBtn, "split 2");
        panel.add(saveBtn, "wrap");
        return panel;
    }

    private void slideInNewTaskPanel() {
        if (isNewTaskVisible) return;
        isNewTaskVisible = true;

        int width = contentContainer.getWidth();
        int totalSteps = Math.max(1, ANIMATION_DURATION / TIMER_DELAY);
        int delta = Math.max(1, width / totalSteps);

        Timer timer = new Timer(TIMER_DELAY, null);
        timer.addActionListener(e -> {
            Point mainLoc = mainPanel.getLocation();
            Point newLoc  = newTaskPanel.getLocation();

            if (newLoc.x <= 0) {
                newTaskPanel.setLocation(0, 0);
                mainPanel.setLocation(-width, 0);
                timer.stop();
            } else {
                mainPanel.setLocation(mainLoc.x - delta, 0);
                newTaskPanel.setLocation(newLoc.x - delta, 0);
            }
        });
        timer.start();
    }

    private void slideOutNewTaskPanel() {
        if (!isNewTaskVisible) return;
        isNewTaskVisible = false;

        int width = contentContainer.getWidth();
        int totalSteps = Math.max(1, ANIMATION_DURATION / TIMER_DELAY);
        int delta = Math.max(1, width / totalSteps);

        Timer timer = new Timer(TIMER_DELAY, null);
        timer.addActionListener(e -> {
            Point mainLoc = mainPanel.getLocation();
            Point newLoc  = newTaskPanel.getLocation();

            if (mainLoc.x >= 0) {
                mainPanel.setLocation(0, 0);
                newTaskPanel.setLocation(width, 0);
                timer.stop();
            } else {
                mainPanel.setLocation(mainLoc.x + delta, 0);
                newTaskPanel.setLocation(newLoc.x + delta, 0);
            }
        });
        timer.start();
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

        folderFilterBox = new JComboBox<>();
        folderFilterBox.addActionListener(e -> {
            if (suppressFolderEvents || taskController == null || folderFilterBox.getItemCount() == 0)
                return;

            String selectedFolder = (String) folderFilterBox.getSelectedItem();
            String folderNameToFilter = "All Folders".equals(selectedFolder) ? null : selectedFolder;

            filterCriteria = new FiltersCriteria(folderNameToFilter, filterCriteria.statuses());
            updateFilterMenuChecks();

            refreshTaskListDisplay();

        });
        panel.add(folderFilterBox, "width 150!");

        JButton filterButton = new JButton(common.getFilterIcon());
        styleIconButton(filterButton, "Filter Tasks");
        createFilterPopupMenu();
        filterButton.addActionListener(e -> {
            filterPopupMenu.show(filterButton, 0, filterButton.getHeight());
        });
        panel.add(filterButton, "gapright 15");

        JButton userButton = new JButton(common.getUserConfigIcon());
        styleIconButton(userButton, "User Profile");
        createUserPopupMenu();
        userButton.addActionListener(e -> {
            userPopupMenu.show(userButton, 0, userButton.getHeight());
        });
        panel.add(userButton);

        toggleColorButton = new JButton(common.getModeIcon());
        styleIconButton(toggleColorButton, "Toggle color mode");
        panel.add(toggleColorButton, "aligny center, gapleft 10");
        // Attach theme toggle action listener
        toggleColorButton.addActionListener(e -> {
            common.toggleColorMode();
            UserProperties.setProperty("darkTheme", String.valueOf(common.useNightMode));
            refreshTheme();
            rebuildUI();
        });

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

        newTaskButton = new JButton("New task");
        newTaskButton
                .setIcon(new ImageIcon(common.getAddIcon().getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        newTaskButton.setIconTextGap(5);
        newTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newTaskButton.addActionListener(e -> {
            newTaskButton.setEnabled(false);
            folderFilterBox.setEnabled(false);
            updateNewTaskFolderBox(taskController.getFolderList());
            slideInNewTaskPanel();
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

    public void refreshTaskListDisplay() {
        List<Task> tasksToDisplay;
        tasksToDisplay = taskController.getTasksByFilters(filterCriteria);
        final List<Task> finalTasksToDisplay = tasksToDisplay;
        SwingUtilities.invokeLater(() -> {
            taskListPanel.removeAll();
            if (finalTasksToDisplay != null && !finalTasksToDisplay.isEmpty()) {
                boolean tasksDisplayed = false;
                for (Task task : finalTasksToDisplay) {
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
            if (folderFilterBox == null)
                return;
            suppressFolderEvents = true;
            String currentSelection = (String) folderFilterBox.getSelectedItem();
            folderFilterBox.removeAllItems();
            folderFilterBox.addItem("All Folders");
            if (folderList != null) {
                for (String folder : folderList) {
                    folderFilterBox.addItem(folder);
                }
            }
            if (currentSelection != null && folderList != null && folderList.contains(currentSelection)) {
                folderFilterBox.setSelectedItem(currentSelection);
            } else {
                folderFilterBox.setSelectedItem("All Folders");
            }
            suppressFolderEvents = false;
        });
    }

    private void updateNewTaskFolderBox(List<String> folderList) {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) newTaskFolderBox.getModel();
        model.removeAllElements();
        if (folderList != null) {
            for (String folder : folderList) {
                model.addElement(folder);
            }
        }
        model.setSelectedItem("Default Folder");
    }

    public void updateLastSyncLabel(LocalDateTime lastSyncTime) {
        SwingUtilities.invokeLater(() -> {
            if (lastSyncLabel != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
                if (lastSyncTime != null)
                    lastSyncLabel.setText("Last Sync: " + lastSyncTime.format(dateFormatter));
                else
                    lastSyncLabel.setText("Never synced.");
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
        checkBox.setSelected(task.getStatus().equals(TaskStatus.completed));
        checkBox.setToolTipText("Mark as " + (task.getStatus().equals(TaskStatus.completed) ? "pending" : "complete"));
        checkBox.setOpaque(false);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkBox.addActionListener(e -> {
            if (taskController != null) {
                taskController.handleTaskCompletionToggle(task);
            }
        });
        card.add(checkBox, "spany 3, aligny top, gapright 10");

        JLabel titleLabel = new JLabel(task.getTitle() != null ? task.getTitle() : "No Title");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        card.add(titleLabel, "growx");

        RoundedLabel statusLabel = new RoundedLabel(
                task.getStatus() != null ? task.getStatus().toString() : "Unknown",
                15);
        Color statusColor = task.getStatus().equals(TaskStatus.in_progress) ? common.getContrastColor()
                : common.getPanelColor().darker();
        statusLabel.setForeground(task.getStatus().equals(TaskStatus.in_progress) ? common.getTextColor().darker()
                : common.getTextColor());
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
        //boolean isCompleted = "completed".equalsIgnoreCase(task.getStatus());

        card.setBackground(common.getTertiaryColor());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setText(task.getTitle() != null ? task.getTitle() : "No Title");
        dateLabel.setForeground(UIManager.getColor("Label.foreground"));

        if (task.getStatus().equals(TaskStatus.completed)) {
            card.setBackground(common.getSecondaryColor().darker());
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.ITALIC | Font.BOLD));
            titleLabel.setText("<html><s>" + (task.getTitle() != null ? task.getTitle() : "No Title") + "</s></html>");
        } else if (task.getDue_date() != null) {
            dateLabel.setText("Due " + task.getDue_date().format(dateFormatter));
            if (task.getDue_date().isBefore(LocalDateTime.now())) {
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

    private void createUserPopupMenu() {
        userPopupMenu = new JPopupMenu();

        JMenuItem logoutItem = new JMenuItem("Log out", common.getLogoutIcon());
        logoutItem.addActionListener(e -> { taskController.handleLogoutRequest(); });
        userPopupMenu.add(logoutItem);

        JMenuItem changeUsernameItem = new JMenuItem("Edit Account", common.getEditUserIcon());
        changeUsernameItem.addActionListener(e -> { taskController.handleChangeUsernameRequest(); });
        userPopupMenu.add(changeUsernameItem);

        JMenuItem deleteAccountItem = new JMenuItem("Delete Account", common.getDeleteUserIcon());
        deleteAccountItem.setForeground(common.CONTRAST_COLOR_NIGHT.brighter());
        deleteAccountItem.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(TaskDashboardFrame.this,
                        "Are you sure you want to permanently delete your account and all associated data?",
                        "Confirm Account Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION)
                    taskController.handleDeleteAccountRequest();
        });
        userPopupMenu.add(deleteAccountItem);
    }

    private void createFilterPopupMenu() {
        filterPopupMenu = new JPopupMenu();

        JMenuItem clearFilterItem = new JMenuItem("Clear Status Filters");
        clearFilterItem.addActionListener(e -> {
            filterCriteria = new FiltersCriteria(filterCriteria.folderName(), Collections.emptySet());
            if (filterPendingItem != null) filterPendingItem.setSelected(false);
            if (filterCompletedItem != null) filterCompletedItem.setSelected(false);
            if (filterInProgressItem != null) filterInProgressItem.setSelected(false);

            refreshTaskListDisplay();
        });
        filterPopupMenu.add(clearFilterItem);
        filterPopupMenu.addSeparator();

        filterPendingItem = new JCheckBoxMenuItem("Pending");
        addStatusFilterListener(filterPendingItem, TaskStatus.pending);
        filterPopupMenu.add(filterPendingItem);

        filterCompletedItem = new JCheckBoxMenuItem("Completed");
        addStatusFilterListener(filterCompletedItem, TaskStatus.completed);
        filterPopupMenu.add(filterCompletedItem);

        filterInProgressItem = new JCheckBoxMenuItem("In Progress");
        addStatusFilterListener(filterInProgressItem, TaskStatus.in_progress);
        filterPopupMenu.add(filterInProgressItem);

        updateFilterMenuChecks();
    }

    private void addStatusFilterListener(JCheckBoxMenuItem item, TaskStatus status) {
        item.addActionListener(e -> {
            Set<TaskStatus> newStatuses = new HashSet<>(filterCriteria.statuses());
            if (item.isSelected())
                newStatuses.add(status);
            else
                newStatuses.remove(status);
            filterCriteria = new FiltersCriteria(filterCriteria.folderName(), newStatuses);
            refreshTaskListDisplay();
        });
    }

    private void updateFilterMenuChecks() {
         if (filterCriteria == null || filterCriteria.statuses() == null) return;
         Set<TaskStatus> currentStatuses = filterCriteria.statuses();
         if (filterPendingItem != null) filterPendingItem.setSelected(currentStatuses.contains(TaskStatus.pending));
         if (filterCompletedItem != null) filterCompletedItem.setSelected(currentStatuses.contains(TaskStatus.completed));
         if (filterInProgressItem != null) filterInProgressItem.setSelected(currentStatuses.contains(TaskStatus.in_progress));
    }
}
