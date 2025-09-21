package controller;

import model.TaskHandlerV2;
import model.TaskStatus;
import model.Task;
import model.FiltersCriteria;
import UI.LoginFrame;
import UI.TaskDashboardFrame;
import COMMON.UserProperties;
import service.OptimizedSyncService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import DBH.DBHandler;
import DBH.NeonPool;
import java.sql.Connection;

import java.time.LocalDateTime;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class TaskController {

    private TaskHandlerV2 taskHandlerV2;
    private TaskDashboardFrame view;
    private DBHandler dbHandler;
    private OptimizedSyncService optimizedSyncService;
    private Connection dbConnection;

    public TaskController(TaskHandlerV2 taskHandlerV2, TaskDashboardFrame view, DBHandler dbHandler) {
        this.taskHandlerV2 = taskHandlerV2;
        this.view = view;
        this.dbHandler = dbHandler;
        try {
            this.dbConnection = NeonPool.getConnection();
            this.optimizedSyncService = new OptimizedSyncService(taskHandlerV2, dbConnection);
        } catch (Exception ex) {
            System.err.println("Controller: DB connection error: " + ex.getMessage());
        }
    }

    /**
     * Helper method to perform sync and update UI.
     * @param uiUpdates Runnable to execute on the EDT after successful sync
     * @param errorMessage Message to log on sync failure
     */
    private void performSyncWithUIUpdate(Runnable uiUpdates, String errorMessage) {
        if (optimizedSyncService != null) {
            optimizedSyncService.performOptimizedSync().thenAcceptAsync(r -> {
                SwingUtilities.invokeLater(uiUpdates);
            }).exceptionally(ex -> {
                System.out.println("Controller: " + errorMessage + ": " + ex.getMessage());
                return null;
            });
        }
    }

    /**
     * Sets the user UUID for sync operations.
     * This method should be called after user authentication.
     */
    public void setUserUUID(String userUUID) {
        if (dbHandler != null) {
            dbHandler.setUserUUID(userUUID);
        }
        
    }

    public void loadInitialTasks() {
        if (optimizedSyncService != null) {
            performSyncWithUIUpdate(() -> {
                view.updateFolderList(taskHandlerV2.getFolderNamesList());
                view.refreshTaskListDisplay();
            }, "Initial sync failed");
        } else {
            view.refreshTaskListDisplay();
        }
    }    public void loadInitialFolderList() {
        System.out.println("Controller: loadInitialFolderList() called");
        System.out.println("Controller: Before sync - Available folders: " + taskHandlerV2.getFolderNamesList());
        
        if (optimizedSyncService != null) {
            // CRITICAL FIX: Load folders synchronously to prevent timing issues
            try {
                System.out.println("Controller: Loading folders synchronously to prevent timing issues...");
                // First, try to load folders from local cache/storage
                List<String> existingFolders = taskHandlerV2.getFolderNamesList();
                if (existingFolders.isEmpty()) {
                    System.out.println("Controller: No local folders found, performing synchronous sync...");
                    // Force synchronous folder loading before UI is ready
                    optimizedSyncService.performOptimizedSync().get(); // .get() makes it synchronous
                }
                
                System.out.println("Controller: After folder loading - Available folders: " + taskHandlerV2.getFolderNamesList());
                
                // Now update UI with loaded folders
                SwingUtilities.invokeLater(() -> {
                    view.updateFolderList(taskHandlerV2.getFolderNamesList());
                    view.refreshTaskListDisplay();
                });
            } catch (Exception e) {
                System.err.println("Controller: Error during synchronous folder loading: " + e.getMessage());
                // Fallback to async loading
                performSyncWithUIUpdate(() -> {
                    System.out.println("Controller: Fallback async - Available folders: " + taskHandlerV2.getFolderNamesList());
                    view.updateFolderList(taskHandlerV2.getFolderNamesList());
                    view.refreshTaskListDisplay();
                }, "Folder load via DB failed");
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                view.updateFolderList(taskHandlerV2.getFolderNamesList());
                view.refreshTaskListDisplay();
            });
        }
    }

    public void loadInitialSyncTime() {
        view.updateLastSyncLabel(getLastSyncTime());
    }

    // Resolve selected folder name to its ID when possible (preferred),
    // then filter by id; fallback to name comparison for older tasks.
    public List<Task> getTasksByFolder(List<Task> sourceList ,String selectedFolder) {
        if (selectedFolder == null) return sourceList;
        String folderId = taskHandlerV2.getFolderIdByName(selectedFolder);
        if (folderId != null) {
            return sourceList.stream()
                    .filter(task -> Objects.equals(task.getFolder_id(), folderId)
                                 || Objects.equals(task.getFolder_name(), selectedFolder))
                    .toList();
        }
        return sourceList.stream()
                .filter(task -> Objects.equals(task.getFolder_name(), selectedFolder))
                .toList();
    }

    public List<Task> getTasksByStatus(List<Task> sourceList, TaskStatus status) {
        return sourceList.stream()
                .filter(task -> task.getStatus().equals(status))
                .toList();
    }    
    
    /**
    * Filters tasks based on the provided criteria object.
    * Applies filters sequentially.
    *
    * @param criteria The TaskFilterCriteria record containing filter settings.
    * @return A new list containing tasks matching the criteria.
    */
    public List<Task> getTasksByFilters(FiltersCriteria criteria) {
        List<Task> filteredTasks = new ArrayList<>(taskHandlerV2.getAllTasks());
        if (criteria.folderName() != null && !criteria.folderName().equals("All Folders"))
            filteredTasks = getTasksByFolder(filteredTasks, criteria.folderName());
        if ( criteria.statuses() == null && criteria.statuses().isEmpty() )
            return filteredTasks;
        if ( criteria.statuses().contains(TaskStatus.completed) || criteria.statuses().contains(TaskStatus.pending) || criteria.statuses().contains(TaskStatus.in_progress) ) {
            filteredTasks = filteredTasks.stream()
                .filter(task -> criteria.statuses().contains(task.getStatus()))
                .collect(Collectors.toList());
        }
        if ( criteria.statuses().contains(TaskStatus.newest) ) {
            filteredTasks = filteredTasks.stream()
                .filter(task -> task.getCreated_at() != null && task.getCreated_at().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Task::getCreated_at).reversed())
                .collect(Collectors.toList());
        }
        if ( criteria.statuses().contains(TaskStatus.incoming_due) ) {
            filteredTasks = filteredTasks.stream()
                .filter(task -> task.getDue_date() != null && task.getDue_date().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Task::getDue_date))
                .collect(Collectors.toList());
        }
        if ( criteria.statuses().contains(TaskStatus.overdue) ) {
            filteredTasks = filteredTasks.stream()
                .filter(task -> task.getDue_date() != null && task.getDue_date().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        }
        return filteredTasks;
    }    
    
    /**
     * Handles creation of a new task from the UI input.
     */
    public void handleCreateTask(String title, String description, String folderName, LocalDateTime dueDate, TaskStatus status) {
        System.out.println("Controller: Creating new task: " + title);
        System.out.println("Controller: Folder name from UI: '" + folderName + "'");
        
        // Get folder ID for the folder name
        String folderId = taskHandlerV2.getFolderIdByName(folderName);
        System.out.println("Controller: Resolved folder ID: '" + folderId + "'");
        
        // Debug: print available folders
        List<String> availableFolders = taskHandlerV2.getFolderNamesList();
        System.out.println("Controller: Available folders: " + availableFolders);
        
        // Use command-driven approach via TaskHandlerV2
        Task createdTask = taskHandlerV2.createTask(title, description, status, dueDate, folderId);
        System.out.println("Controller: Created task with folder_id: " + createdTask.getFolder_id() + ", folder_name: " + createdTask.getFolder_name());
        
        view.refreshTaskListDisplay();
        performSyncWithUIUpdate(() -> {
            view.updateLastSyncLabel(getLastSyncTime());
            view.refreshTaskListDisplay();
        }, "Exception during DB sync after create");
    }

    public void handleSyncRequest() {
        System.out.println("Controller: Sync request received - using DB-direct sync.");
        if (optimizedSyncService == null) return;
        performSyncWithUIUpdate(() -> {
            view.updateLastSyncLabel(getLastSyncTime());
            view.refreshTaskListDisplay();
            loadInitialFolderList();
        }, "Exception during DB sync");
    }

    public void handleHistoryRequest() {
        System.out.println("Controller: History request received.");
        view.displayTaskHistory();
    }

    public void handleDeleteTaskRequest(String taskId) {
        System.out.println("Controller: Delete task request for ID " + taskId);
        Task task = getTaskById(taskId);
        if (task != null) {
            taskHandlerV2.deleteTask(task, "User deleted");
            view.refreshTaskListDisplay();
            performSyncWithUIUpdate(() -> view.refreshTaskListDisplay(), "Exception during DB sync after delete");
        } else {
            System.err.println("Controller: Could not find task with ID " + taskId + " to delete.");
        }
    }    public void handleWindowClosing() {
        taskHandlerV2.saveTasksToJson();
        if( !Boolean.valueOf((String) UserProperties.getProperty("rememberMe")) ){
            System.out.println("Controller: Logging out user and clearing credentials.");
            UserProperties.logOut();
        }
        try { if (dbConnection != null && !dbConnection.isClosed()) dbConnection.close(); } catch (Exception ignore) {}
    }    public List<String> getFolderList() {
    return taskHandlerV2.getFolderNamesList();
    }    public LocalDateTime getLastSyncTime() {
        return taskHandlerV2.getLastSync() != null ? taskHandlerV2.getLastSync() : UserProperties.getProperty("lastSyncTime") != null ? LocalDateTime.parse((String) UserProperties.getProperty("lastSyncTime")) : null;
    }    public void handleTaskCompletionToggle(Task task) {
        if (task != null) {
            TaskStatus newStatus = !task.getStatus().equals(TaskStatus.completed) ? TaskStatus.completed : TaskStatus.pending;
            taskHandlerV2.updateTask(task, null, null, newStatus, null, null);
            view.refreshTaskListDisplay();
            performSyncWithUIUpdate(() -> view.refreshTaskListDisplay(), "Exception during DB sync after toggle");
        } else {
             System.err.println("Controller: Could not find task to toggle completion.");
        }
    }    
    // --- User Action Handlers ---    
    public void handleLogoutRequest() {
        System.out.println("Controller: Logout request received.");
        taskHandlerV2.saveTasksToJson();
        UserProperties.logOut();
        view.dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame("Login");
            loginFrame.setController(new UserController());
        });
        System.out.println("Controller: User logged out, dashboard closed.");
    }

    public void handleChangeUsernameRequest() {
        System.out.println("Controller: Change Username request received.");
        String newUsername = JOptionPane.showInputDialog(view, "Enter new username:");
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            // Call API to change username
            System.out.println("Attempting to change username to: " + newUsername);
        }
    }

    public void handleDeleteAccountRequest() {
        System.out.println("Controller: Delete Account request received.");
        // TODO: Implement logic to show a dialog for delete user and validate JWT to allow change
    }

    public void handleEditTaskRequest(
        String task_id, String title, String desc, 
        String folder, LocalDateTime due, TaskStatus status) 
    {
        System.out.println("Controller: Edit task request for ID " + task_id);
        Task task = getTaskById(task_id);
        if (task != null) {
            taskHandlerV2.updateTask(task, title, desc, status, due, folder);
            view.refreshTaskListDisplay();
            performSyncWithUIUpdate(() -> view.refreshTaskListDisplay(), "Exception during DB sync after edit");
        } else {
            System.err.println("Controller: Could not find task with ID " + task_id + " to edit.");
        }
    }    
    
    /**
     * Retrieves the task history for the current user.
     * 
     * @return List of Task objects representing the task history
     */
    public List<Task> getTaskHistory() {
        return new ArrayList<>(
            taskHandlerV2.getAllTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.completed)
                .collect(Collectors.toList())
            );
    }

    // --- Helper Methods ---
    
    /**
     * Get a task by its ID using TaskHandlerV2
     */
    private Task getTaskById(String taskId) {
        List<Task> allTasks = taskHandlerV2.getAllTasks();
        return allTasks.stream()
            .filter(task -> task.getTask_id().equals(taskId))
            .findFirst()
            .orElse(null);
    }

    // --- UI Data Methods ---
}
