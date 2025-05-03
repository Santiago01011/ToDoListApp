package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import COMMON.UserProperties;
import COMMON.common;
import UI.components.TopBarPanel;
import UI.components.BottomBarPanel;
import UI.components.NewTaskPanel;
import UI.components.TaskCardPanel;
import controller.TaskController;
import model.FiltersCriteria;
import model.TaskStatus;
import model.Task;
import net.miginfocom.swing.MigLayout;

public class TaskDashboardFrame extends Frame {
    private static final int ANIMATION_DURATION = 150;
    private static final int TIMER_DELAY = 15;

    private TaskController taskController;
    private JPanel taskListPanel;
    private JPanel contentContainer;
    private JPanel mainPanel;
    private NewTaskPanel newTaskPanel;
    private TopBarPanel topBarPanel;
    private BottomBarPanel bottomBarPanel;
    private boolean isNewTaskVisible = false;

    FiltersCriteria filterCriteria = FiltersCriteria.defaultCriteria();

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
        topBarPanel = new TopBarPanel(new TopBarPanel.Listener() {
            public void onFolderFilterChanged(String folder) {
                filterCriteria = new FiltersCriteria(folder, filterCriteria.statuses());
                refreshTaskListDisplay();
            }
            public void onStatusFilterChanged(Set<TaskStatus> statuses) {
                filterCriteria = new FiltersCriteria(filterCriteria.folderName(), statuses);
                refreshTaskListDisplay();
            }
            public void onClearFilters() {
                filterCriteria = new FiltersCriteria(filterCriteria.folderName(), Collections.emptySet());
                refreshTaskListDisplay();
            }
            public void onSyncRequested() {
                taskController.handleSyncRequest();
            }
            public void onHistoryRequested() {
                taskController.handleHistoryRequest();
            }
            public void onLogout() {
                taskController.handleLogoutRequest();
            }
            public void onEditAccount() {
                taskController.handleChangeUsernameRequest();
            }
            public void onDeleteAccount() {
                taskController.handleDeleteAccountRequest();
            }
            public void onToggleTheme() {
                common.toggleColorMode();
                UserProperties.setProperty("darkTheme", String.valueOf(common.useNightMode));
                refreshTheme();
                rebuildUI();
            }
        });
        add(topBarPanel, BorderLayout.NORTH);

        taskListPanel = new JPanel(new MigLayout("wrap 1, fillx, inset 15", "[grow, fill]", ""));
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        bottomBarPanel = new BottomBarPanel(new BottomBarPanel.Listener() {
            public void onNewTask() {
                slideInNewTaskPanel();
            }
            public void onSync() {
                taskController.handleSyncRequest();
            }
            public void onHistory() {
                taskController.handleHistoryRequest();
            }
        });
        add(bottomBarPanel, BorderLayout.SOUTH);

        mainPanel.setBounds(0, 0, width, height);
        contentContainer.add(mainPanel);

        newTaskPanel = new NewTaskPanel(new NewTaskPanel.Listener() {
            public void onSave(String title, String desc, String folder, LocalDateTime due, TaskStatus status) {
                taskController.handleCreateTask(title, desc, folder, due, status);
                slideOutNewTaskPanel();
            }
            public void onCancel() {
                slideOutNewTaskPanel();
            }
        });
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

    public void refreshTaskListDisplay() {
        List<Task> tasksToDisplay;
        tasksToDisplay = taskController.getTasksByFilters(filterCriteria);
        final List<Task> finalTasksToDisplay = tasksToDisplay;
        SwingUtilities.invokeLater(() -> {
            taskListPanel.removeAll();
            if (finalTasksToDisplay != null && !finalTasksToDisplay.isEmpty()) {
                boolean tasksDisplayed = false;
                for (Task task : finalTasksToDisplay) {
                    TaskCardPanel card = new TaskCardPanel(task, new TaskCardPanel.Listener() {
                        public void onToggleComplete(Task t) { taskController.handleTaskCompletionToggle(t); }
                        public void onView(Task t) { taskController.handleViewTaskRequest(t.getTask_id()); }
                        public void onEdit(Task t) { taskController.handleEditTaskRequest(t.getTask_id()); }
                        public void onDelete(Task t) { taskController.handleDeleteTaskRequest(t.getTask_id()); }
                    });
                    taskListPanel.add(card, "growx, gapbottom 10");
                    tasksDisplayed = true;
                }
                if (!tasksDisplayed) {
                    JLabel noTasksLabel = new JLabel("No tasks found matching criteria.");
                    noTasksLabel.setHorizontalAlignment(SwingUtilities.CENTER);
                    taskListPanel.add(noTasksLabel, "growx");
                }
            } else {
                JLabel noTasksLabel = new JLabel("No tasks found matching criteria.");
                noTasksLabel.setHorizontalAlignment(SwingUtilities.CENTER);
                taskListPanel.add(noTasksLabel, "growx");
            }
            taskListPanel.revalidate();
            taskListPanel.repaint();
        });
    }

    public void updateFolderList(List<String> folderList) {
        SwingUtilities.invokeLater(() -> {
            topBarPanel.updateFolders(folderList);
            newTaskPanel.setFolders(folderList);
        });
    }

    // Update the bottom bar's sync label using controller's last sync time
    private void updateLastSyncLabel() {
        if (taskController != null) {
            bottomBarPanel.setLastSync(taskController.getLastSyncTime());
        }
    }

    // Update sync label with provided timestamp
    public void updateLastSyncLabel(LocalDateTime lastSyncTime) {
        SwingUtilities.invokeLater(() -> {
            bottomBarPanel.setLastSync(lastSyncTime);
        });
    }
}
