package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.ArrayList;

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
import UI.components.EditTaskPanel;
import UI.components.TaskCardPanel;
import controller.TaskController;
import model.FiltersCriteria;
import model.TaskStatus;
import model.Task;
import net.miginfocom.swing.MigLayout;

public class TaskDashboardFrame extends Frame {
    private static final int ANIMATION_DURATION = 150;
    private static final int TIMER_DELAY = 15;
    private static final int CARD_ANIMATION_DURATION = 100;
    private static final int CARD_TIMER_DELAY = 15;

    private TaskController taskController;
    private JPanel taskListPanel;
    private JPanel contentContainer;
    private JPanel mainPanel;
    private NewTaskPanel newTaskPanel;
    private EditTaskPanel editTaskPanel;
    private EditTaskPanel editTaskCardPanel;
    private TaskCardPanel activeEditCardPanel;
    private TopBarPanel topBarPanel;
    private BottomBarPanel bottomBarPanel;
    private boolean isNewTaskVisible = false;
    private boolean isEditTaskVisible = false;
    private List<String> currentFolderList = new ArrayList<>();
    private Timer addCardTimer;

    FiltersCriteria filterCriteria = FiltersCriteria.defaultCriteria();

    public TaskDashboardFrame(String title) {
        super(title);

        setResizable(true);
        setSize(700, 700);
        setMinimumSize(new Dimension(600, 400));
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
                    if (editTaskPanel != null) {
                        editTaskPanel.setBounds(-w, 0, w, h);
                    }
                } else if (isEditTaskVisible) {
                    mainPanel.setBounds(w, 0, w, h);
                    if (editTaskPanel != null) {
                        editTaskPanel.setBounds(0, 0, w, h);
                    }
                    newTaskPanel.setBounds(w, 0, w, h);
                } else {
                    mainPanel.setBounds(0, 0, w, h);
                    newTaskPanel.setBounds(w, 0, w, h);
                    if (editTaskPanel != null) {
                        editTaskPanel.setBounds(-w, 0, w, h);
                    }
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

    private void toggleEditTaskCard(Task taskToEdit, TaskCardPanel cardPanel) {
        if (editTaskCardPanel != null && activeEditCardPanel == cardPanel) {
            taskListPanel.remove(editTaskCardPanel);
            editTaskCardPanel = null;
            activeEditCardPanel = null;
            taskListPanel.revalidate();
            taskListPanel.repaint();
            return;
        }
        if (editTaskCardPanel != null) {
            taskListPanel.remove(editTaskCardPanel);
        }
        activeEditCardPanel = cardPanel;
        editTaskCardPanel = new EditTaskPanel(new EditTaskPanel.Listener() {
            public void onSaveEdit(String title, String desc, String folder, LocalDateTime due, TaskStatus status) {
                taskController.handleEditTaskRequest(taskToEdit.getTask_id(), title, desc, folder, due, status);
                taskListPanel.remove(editTaskCardPanel);
                editTaskCardPanel = null;
                activeEditCardPanel = null;
                refreshTaskListDisplay();
            }
            public void onCancelEdit() {
                taskListPanel.remove(editTaskCardPanel);
                editTaskCardPanel = null;
                activeEditCardPanel = null;
                taskListPanel.revalidate();
                taskListPanel.repaint();
            }
        }, taskToEdit);
        editTaskCardPanel.setFolders(this.currentFolderList);
        Component[] comps = taskListPanel.getComponents();
        int idx = -1;
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == cardPanel) {
                idx = i;
                break;
            }
        }
        int pos = (idx >= 0) ? idx + 1 : comps.length;
        taskListPanel.add(editTaskCardPanel, "growx, gapbottom 10", pos);
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private void animateCardSlideDown(JPanel card) {
        Dimension full = card.getPreferredSize();
        int steps = Math.max(1, CARD_ANIMATION_DURATION / CARD_TIMER_DELAY);
        int delta = Math.max(1, full.height / steps);
        card.setPreferredSize(new Dimension(full.width, 0));
        Timer t = new Timer(CARD_TIMER_DELAY, null);
        t.addActionListener(e -> {
            Dimension curr = card.getPreferredSize();
            int h = Math.min(full.height, curr.height + delta);
            card.setPreferredSize(new Dimension(full.width, h));
            card.revalidate();
            Container parent = card.getParent();
            if (parent != null) { parent.revalidate(); parent.repaint(); }
            if (h >= full.height) {
                card.setPreferredSize(null);
                ((Timer)e.getSource()).stop();
            }
        });
        t.start();
    }

    public void refreshTaskListDisplay() {
        List<Task> tasksToDisplay = taskController.getTasksByFilters(filterCriteria);
        final List<Task> finalTasksToDisplay = tasksToDisplay;
        SwingUtilities.invokeLater(() -> {
            taskListPanel.removeAll();
            if (addCardTimer != null) {
                addCardTimer.stop();
                addCardTimer = null;
            }
            if (finalTasksToDisplay != null && !finalTasksToDisplay.isEmpty()) {
                addCardTimer = new Timer(CARD_TIMER_DELAY, null);
                addCardTimer.addActionListener(new java.awt.event.ActionListener() {
                    private int idx = 0;
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if (idx >= finalTasksToDisplay.size()) {
                            addCardTimer.stop();
                            addCardTimer = null;
                            return;
                        }
                        Task task = finalTasksToDisplay.get(idx++);
                        final TaskCardPanel[] holder = new TaskCardPanel[1];
                        final TaskCardPanel card = new TaskCardPanel(task, new TaskCardPanel.Listener() {
                            public void onToggleComplete(Task t) { taskController.handleTaskCompletionToggle(t); }
                            public void onView(Task t) { taskController.handleViewTaskRequest(t.getTask_id()); }
                            public void onEdit(Task t) { toggleEditTaskCard(t, holder[0]); }
                            public void onDelete(Task t) { taskController.handleDeleteTaskRequest(t.getTask_id()); }
                        });
                        holder[0] = card;
                        taskListPanel.add(card, "growx, gapbottom 10");
                        animateCardSlideDown(card);
                        taskListPanel.revalidate();
                        taskListPanel.repaint();
                    }
                });
                addCardTimer.setInitialDelay(0);
                addCardTimer.start();
            } else {
                JLabel noTasksLabel = new JLabel("No tasks found matching criteria.");
                noTasksLabel.setHorizontalAlignment(SwingUtilities.CENTER);
                taskListPanel.add(noTasksLabel, "growx");
            }
        });
    }

    public void updateFolderList(List<String> folderList) {
        this.currentFolderList = new ArrayList<>(folderList);
        SwingUtilities.invokeLater(() -> {
            topBarPanel.updateFolders(folderList);
            newTaskPanel.setFolders(folderList);
        });
    }

    private void updateLastSyncLabel() {
        if (taskController != null) {
            bottomBarPanel.setLastSync(taskController.getLastSyncTime());
        }
    }

    public void updateLastSyncLabel(LocalDateTime lastSyncTime) {
        SwingUtilities.invokeLater(() -> {
            bottomBarPanel.setLastSync(lastSyncTime);
        });
    }
}
