package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.LayoutManager;

import COMMON.UserProperties;
import COMMON.common;
import UI.components.TopBarPanel;
import UI.components.BottomBarPanel;
import UI.components.NewTaskPanel;
import UI.components.EditTaskPanel;
import UI.components.ViewTaskPanel;
import UI.components.TaskCardPanel;
import UI.components.HistoryPanel;
import controller.TaskController;
import model.FiltersCriteria;
import model.TaskStatus;
import model.Task;
import net.miginfocom.swing.MigLayout;

public class TaskDashboardFrame extends Frame {
    private static final int ANIMATION_DURATION = 150;
    private static final int TIMER_DELAY = 15;
    private static final int CARD_ANIMATION_DURATION = 100;
    private static final int CARD_TIMER_DELAY = 10;
    private static final int STAGGER_DELAY = 50;
    private static final int FIRST_RUN_DELAY = 200;

    private TaskController taskController;
    private boolean firstRun = true;
    private JPanel taskListPanel;
    private MigLayout taskListLayout;
    private JPanel contentContainer;
    private JPanel mainPanel;
    private NewTaskPanel newTaskPanel;
    private EditTaskPanel editTaskCardPanel;
    private TaskCardPanel activeEditCardPanel;
    private ViewTaskPanel viewTaskCardPanel;
    private TaskCardPanel activeViewCardPanel;
    private TopBarPanel topBarPanel;
    private BottomBarPanel bottomBarPanel;
    private boolean isNewTaskVisible = false;
    private List<String> currentFolderList = new ArrayList<>();

    private HistoryPanel historyPanel;
    private boolean isHistoryVisible = false;

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
            }            public void onHistoryRequested() {
                taskController.handleHistoryRequest();
                displayTaskHistory();
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

        taskListLayout = new MigLayout("wrap 1, fillx, inset 15, hidemode 3", "[grow, fill]", "");
        taskListPanel = new JPanel(taskListLayout);
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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
                displayTaskHistory();
            }
        });
        add(bottomBarPanel, BorderLayout.SOUTH);

        mainPanel.setBounds(0, 0, width, height);
        contentContainer.add(mainPanel);

        newTaskPanel = new NewTaskPanel(new NewTaskPanel.Listener() {
            public void onSave(String title, String desc, String folder, LocalDateTime due, TaskStatus status) {
                taskController.handleCreateTask(title, desc, folder, due, status);
                slideOutNewTaskPanel();
                bottomBarPanel.enableBottomBarButtons();
            }
            public void onCancel() {
                slideOutNewTaskPanel();
                bottomBarPanel.enableBottomBarButtons();
            }
        });        
        newTaskPanel.setBounds(width, 0, width, height);
        contentContainer.add(newTaskPanel);
          historyPanel = new HistoryPanel(new HistoryPanel.Listener() {
            @Override
            public void onClose() {
                slideOutHistoryPanel();
                bottomBarPanel.enableBottomBarButtons();
            }            
            @Override
            public void onTaskSelected(Task task) {
                SwingUtilities.invokeLater(() -> {
                    javax.swing.JDialog dialog = new javax.swing.JDialog(TaskDashboardFrame.this, "Task Details", true);
                    dialog.setLayout(new BorderLayout());
                    ViewTaskPanel viewPanel = new ViewTaskPanel(new ViewTaskPanel.Listener() {
                        @Override
                        public void onClose() {
                            dialog.dispose();
                        }
                    }, task);
                    
                    dialog.add(viewPanel, BorderLayout.CENTER);
                    dialog.pack();
                    dialog.setLocationRelativeTo(TaskDashboardFrame.this);
                    dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
                    dialog.setResizable(false);
                    dialog.setVisible(true);
                });
            }
            
            @Override
            public void onTaskRestore(Task task) {
                taskController.handleTaskCompletionToggle(task);
                List<Task> taskHistory = taskController.getTaskHistory();
                historyPanel.displayTaskHistory(taskHistory);
            }
            
            @Override
            public void onTaskDelete(Task task) {
                taskController.handleDeleteTaskRequest(task.getTask_id());
                List<Task> taskHistory = taskController.getTaskHistory();
                historyPanel.displayTaskHistory(taskHistory);
            }
        });        
        historyPanel.setBounds(-width, 0, width, height);
        contentContainer.add(historyPanel);

        contentContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = contentContainer.getWidth();
                int h = contentContainer.getHeight();
                if (isNewTaskVisible) {
                    mainPanel.setBounds(-w, 0, w, h);
                    newTaskPanel.setBounds(0, 0, w, h);
                    historyPanel.setBounds(-w, 0, w, h);
                } else if (isHistoryVisible) {
                    mainPanel.setBounds(w, 0, w, h);
                    historyPanel.setBounds(0, 0, w, h);
                    newTaskPanel.setBounds(w, 0, w, h);
                } else {
                    mainPanel.setBounds(0, 0, w, h);
                    newTaskPanel.setBounds(w, 0, w, h);
                    historyPanel.setBounds(-w, 0, w, h);
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

    private void slideInHistoryPanel() {
        if (isHistoryVisible) return;
        isHistoryVisible = true;

        int width = contentContainer.getWidth();
        int totalSteps = Math.max(1, ANIMATION_DURATION / TIMER_DELAY);
        int delta = Math.max(1, width / totalSteps);

        Timer timer = new Timer(TIMER_DELAY, null);
        timer.addActionListener(e -> {
            Point mainLoc = mainPanel.getLocation();
            Point historyLoc = historyPanel.getLocation();

            if (historyLoc.x >= 0) {
                historyPanel.setLocation(0, 0);
                mainPanel.setLocation(width, 0);
                timer.stop();
            } else {
                mainPanel.setLocation(mainLoc.x + delta, 0);
                historyPanel.setLocation(historyLoc.x + delta, 0);
            }
        });
        timer.start();
    }    
    
    private void slideOutHistoryPanel() {
        if (!isHistoryVisible) return;
        isHistoryVisible = false;

        int width = contentContainer.getWidth();
        int totalSteps = Math.max(1, ANIMATION_DURATION / TIMER_DELAY);
        int delta = Math.max(1, width / totalSteps);

        Timer timer = new Timer(TIMER_DELAY, null);
        timer.addActionListener(e -> {
            Point mainLoc = mainPanel.getLocation();
            Point historyLoc = historyPanel.getLocation();

            if (mainLoc.x <= 0) {
                mainPanel.setLocation(0, 0);
                historyPanel.setLocation(-width, 0);
                timer.stop();
            } else {
                mainPanel.setLocation(mainLoc.x - delta, 0);
                historyPanel.setLocation(historyLoc.x - delta, 0);
            }
        });
        timer.start();
    }

    /**
     * Displays the task history by sliding in the history panel
     * and loading task history data from the controller.
     */
    private void displayTaskHistory() {
        slideInHistoryPanel();
        List<Task> taskHistory = taskController.getTaskHistory();
        historyPanel.displayTaskHistory(taskHistory);
    }

    private void animatePanelHeight(JPanel panel, int targetHeight, Runnable onFinish) {
        int steps = Math.max(1, CARD_ANIMATION_DURATION / CARD_TIMER_DELAY);
        int delta = Math.max(1, targetHeight / steps);
        final int[] curr = {0};
        Timer t = new Timer(CARD_TIMER_DELAY, null);
        t.addActionListener((ActionEvent e) -> {
            if (panel.getParent() != taskListPanel) {
                ((Timer)e.getSource()).stop();
                if (onFinish != null) onFinish.run();
                return;
            }
            curr[0] = Math.min(targetHeight, curr[0] + delta);
            try {
                taskListLayout.setComponentConstraints(panel, "growx, gapbottom 10, h " + curr[0] + "!");
            } catch (IllegalArgumentException ignored) {}
            taskListPanel.revalidate(); taskListPanel.repaint();
            if (curr[0] >= targetHeight) {
                ((Timer)e.getSource()).stop();
                try {
                    taskListLayout.setComponentConstraints(panel, "growx, gapbottom 10");
                } catch (IllegalArgumentException ignored) {}
                if (onFinish != null) onFinish.run();
            }
        });
        t.start();
    }

    private void animatePanelCollapse(JPanel panel, Runnable onFinish) {
        int initial = panel.getHeight();
        int steps = Math.max(1, CARD_ANIMATION_DURATION / CARD_TIMER_DELAY);
        int delta = Math.max(1, initial / steps);
        final int[] curr = {initial};
        Timer t = new Timer(CARD_TIMER_DELAY, null);
        t.addActionListener(e -> {
            if (panel.getParent() != taskListPanel) {
                ((Timer)e.getSource()).stop();
                if (onFinish != null) onFinish.run();
                return;
            }
            curr[0] = Math.max(0, curr[0] - delta);
            try {
                taskListLayout.setComponentConstraints(panel, "growx, gapbottom 10, h " + curr[0] + "!");
            } catch (IllegalArgumentException ignored) {}
            taskListPanel.revalidate(); taskListPanel.repaint();
            if (curr[0] <= 0) {
                ((Timer)e.getSource()).stop();
                if (onFinish != null) onFinish.run();
            }
        });
        t.start();
    }

    private void toggleEditTaskCard(Task taskToEdit, TaskCardPanel cardPanel) {
        if (editTaskCardPanel != null) {
            final int oldPosition = findComponentPosition(editTaskCardPanel);
            
            final TaskCardPanel previousCard = activeEditCardPanel;
            final int previousCardHeight = previousCard.getPreferredSize().height;
            
            animatePanelCollapse(editTaskCardPanel, () -> {
                taskListPanel.remove(editTaskCardPanel);
                
                if (oldPosition >= 0 && oldPosition < taskListPanel.getComponentCount() + 1 && previousCard != cardPanel) {
                    taskListPanel.add(previousCard, "growx, gapbottom 10", oldPosition);
                    animatePanelHeight(previousCard, previousCardHeight, null);
                }
                
                taskListPanel.revalidate();
                taskListPanel.repaint();
            });
        }
        
        final int position = findComponentPosition(cardPanel);
        
        if (position >= 0) {
            animatePanelCollapse(cardPanel, () -> {
                taskListPanel.remove(cardPanel);
                
                activeEditCardPanel = cardPanel;
                editTaskCardPanel = new EditTaskPanel(new EditTaskPanel.Listener() {
                    public void onSaveEdit(String title, String desc, String folder, LocalDateTime due, TaskStatus status) {
                        taskController.handleEditTaskRequest(taskToEdit.getTask_id(), title, desc, folder, due, status);
                        
                        final int editPosition = findComponentPosition(editTaskCardPanel);
                        
                        final int originalCardHeight = cardPanel.getPreferredSize().height;
                        
                        animatePanelCollapse(editTaskCardPanel, () -> {
                            taskListPanel.remove(editTaskCardPanel);
                            
                            if (editPosition >= 0 && editPosition < taskListPanel.getComponentCount() + 1) {
                                taskListPanel.add(cardPanel, "growx, gapbottom 10", editPosition);
                                animatePanelHeight(cardPanel, originalCardHeight, null);
                            }
                            
                            taskListPanel.revalidate();
                            taskListPanel.repaint();
                            editTaskCardPanel = null;
                            activeEditCardPanel = null;
                            refreshTaskListDisplay();
                        });
                    }
                    
                    public void onCancelEdit() {
                        final int editPosition = findComponentPosition(editTaskCardPanel);
                        
                        final int originalCardHeight = cardPanel.getPreferredSize().height;
                        
                        animatePanelCollapse(editTaskCardPanel, () -> {
                            taskListPanel.remove(editTaskCardPanel);
                            
                            if (editPosition >= 0 && editPosition < taskListPanel.getComponentCount() + 1) {
                                taskListPanel.add(cardPanel, "growx, gapbottom 10", editPosition);
                                animatePanelHeight(cardPanel, originalCardHeight, null);
                            }
                            
                            taskListPanel.revalidate();
                            taskListPanel.repaint();
                            editTaskCardPanel = null;
                            activeEditCardPanel = null;
                        });
                    }
                }, taskToEdit);
                
                editTaskCardPanel.setFolders(currentFolderList);
                taskListPanel.add(editTaskCardPanel, "growx, gapbottom 10, h 0!", position);
                taskListPanel.revalidate();
                taskListPanel.repaint();
                
                Dimension full = editTaskCardPanel.getPreferredSize();
                Timer scrollTimer = new Timer(150, e -> {
                    JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, taskListPanel);
                    if (scrollPane != null) {
                        SwingUtilities.invokeLater(() -> {
                            JViewport viewport = scrollPane.getViewport();
                            Rectangle cardBounds = editTaskCardPanel.getBounds();
                            Point viewPos = SwingUtilities.convertPoint(taskListPanel, cardBounds.getLocation(), viewport);
                            viewPos.y += 30;
                            viewport.scrollRectToVisible(new Rectangle(viewPos, cardBounds.getSize()));
                        });
                    }
                });
                scrollTimer.setRepeats(false);
                scrollTimer.start();

                
                animatePanelHeight(editTaskCardPanel, full.height, null);
            });
        }
    }

    private void toggleViewTaskCard(Task taskToView, TaskCardPanel cardPanel) {        
        if (viewTaskCardPanel != null) {
            final int oldPosition = findComponentPosition(viewTaskCardPanel);
            
            final TaskCardPanel previousCard = activeViewCardPanel;
            final int previousCardHeight = previousCard.getPreferredSize().height;
            
            animatePanelCollapse(viewTaskCardPanel, () -> {
                taskListPanel.remove(viewTaskCardPanel);
                
                if (oldPosition >= 0 && oldPosition < taskListPanel.getComponentCount() + 1 && previousCard != cardPanel) {
                    taskListPanel.add(previousCard, "growx, gapbottom 10", oldPosition);
                    animatePanelHeight(previousCard, previousCardHeight, null);
                }
                
                taskListPanel.revalidate();
                taskListPanel.repaint();
            });
        }
        
        final int position = findComponentPosition(cardPanel);
        
        if (position >= 0) {
            animatePanelCollapse(cardPanel, () -> {
                taskListPanel.remove(cardPanel);
                
                activeViewCardPanel = cardPanel;
                viewTaskCardPanel = new ViewTaskPanel(new ViewTaskPanel.Listener() {
                    public void onClose() {
                        final int viewPosition = findComponentPosition(viewTaskCardPanel);
                        
                        final int originalCardHeight = cardPanel.getPreferredSize().height;
                        
                        animatePanelCollapse(viewTaskCardPanel, () -> {
                            taskListPanel.remove(viewTaskCardPanel);
                            
                            if (viewPosition >= 0 && viewPosition < taskListPanel.getComponentCount() + 1) {
                                taskListPanel.add(cardPanel, "growx, gapbottom 10", viewPosition);
                                animatePanelHeight(cardPanel, originalCardHeight, null);
                            }
                            
                            taskListPanel.revalidate();
                            taskListPanel.repaint();
                            viewTaskCardPanel = null;
                            activeViewCardPanel = null;
                        });
                    }
                }, taskToView);
                taskListPanel.add(viewTaskCardPanel, "growx, gapbottom 10, h 0!", position);                
                taskListPanel.revalidate();
                taskListPanel.repaint();
                
                Dimension full = viewTaskCardPanel.getPreferredSize();

                Timer scrollTimer = new Timer(150, e -> {
                    JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, taskListPanel);
                    if (scrollPane == null) return;

                    // Let layout stabilize first
                    SwingUtilities.invokeLater(() -> {
                        JViewport viewport = scrollPane.getViewport();
                        Rectangle viewRect = viewport.getViewRect();

                        // Convert card's location relative to viewport
                        Point cardPos = SwingUtilities.convertPoint(viewTaskCardPanel.getParent(), viewTaskCardPanel.getLocation(), viewport);
                        int cardTop = cardPos.y;
                        int cardBottom = cardTop + viewTaskCardPanel.getHeight();

                        int targetY;

                        // If card is fully visible, do nothing
                        if (cardTop >= viewRect.y && cardBottom <= viewRect.y + viewRect.height) {
                            return;
                        }

                        // If card is taller than viewport, scroll to top
                        if (viewTaskCardPanel.getHeight() > viewRect.height) {
                            targetY = cardTop;
                        }
                        // If bottom is cut off, scroll down just enough
                        else if (cardBottom > viewRect.y + viewRect.height) {
                            targetY = cardBottom - viewRect.height;
                        }
                        // If top is cut off, scroll up
                        else {
                            targetY = cardTop;
                        }

                        // Clamp target
                        int maxY = Math.max(0, taskListPanel.getHeight() - viewRect.height);
                        targetY = Math.max(0, Math.min(targetY, maxY));

                        // Smooth scroll
                        final int startY = viewRect.y;
                        final int distance = targetY - startY;
                        final int[] step = {0};
                        final int totalSteps = 20;

                        Timer animator = new Timer(10, null);
                        animator.addActionListener(ev -> {
                            step[0]++;
                            double t = step[0] / (double) totalSteps;
                            double eased = 1 - Math.pow(1 - t, 3); // ease-out
                            int currentY = startY + (int) (distance * eased);
                            viewport.setViewPosition(new Point(viewRect.x, currentY));

                            if (step[0] >= totalSteps) {
                                animator.stop();
                                viewport.setViewPosition(new Point(viewRect.x, targetY));
                            }
                        });
                        animator.start();
                    });
                });

                scrollTimer.setRepeats(false);
                scrollTimer.start();                
                animatePanelHeight(viewTaskCardPanel, full.height, null);
            });
        }
    }

    private int findComponentPosition(Component component) {
        Component[] components = taskListPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == component) {
                return i;
            }
        }
        return -1;
    }

    public void refreshTaskListDisplay() {
        List<Task> tasksToDisplay = taskController.getTasksByFilters(filterCriteria);
        SwingUtilities.invokeLater(() -> {
            taskListPanel.removeAll();
            if (tasksToDisplay != null && !tasksToDisplay.isEmpty()) {
                int idx = 0;
                for (Task task : tasksToDisplay) {
                    final TaskCardPanel[] holder = new TaskCardPanel[1];                    
                    TaskCardPanel card = new TaskCardPanel(task, new TaskCardPanel.Listener() {
                        public void onToggleComplete(Task t) { taskController.handleTaskCompletionToggle(t); }
                        public void onView(Task t) { toggleViewTaskCard(t, holder[0]); }
                        public void onEdit(Task t) { toggleEditTaskCard(t, holder[0]); }
                        public void onDelete(Task t) { taskController.handleDeleteTaskRequest(t.getTask_id()); }
                    });
                    holder[0] = card;
                    Dimension full = card.getPreferredSize();
                    taskListPanel.add(card, "growx, gapbottom 10, h 0!");
                    int baseDelay = firstRun ? FIRST_RUN_DELAY : 0;
                    int delay = baseDelay + idx * STAGGER_DELAY;
                    Timer starter = new Timer(CARD_TIMER_DELAY, null);
                    starter.setInitialDelay(delay);
                    starter.setRepeats(false);
                    starter.addActionListener(e -> animatePanelHeight(card, full.height, null));
                    starter.start();
                    idx++;
                }
                firstRun = false;
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
