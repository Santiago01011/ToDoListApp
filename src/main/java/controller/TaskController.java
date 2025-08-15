package controller;

import model.TaskHandler;
import model.TaskHandlerV2;
import model.TaskStatus;
import model.Task;
import model.Folder;
import model.FiltersCriteria;
import UI.LoginFrame;
import UI.TaskDashboardFrame;
import COMMON.UserProperties;
import service.SyncService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import DBH.DBHandler;

import java.time.LocalDateTime;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class TaskController {

    private TaskHandlerV2 taskHandlerV2;
    private TaskHandler legacyTaskHandler; // For backward compatibility with DBHandler
    private TaskDashboardFrame view;
    private DBHandler dbHandler;
    private SyncService syncService;

    public void printUserTasks() {
        System.out.println("User Tasks:");
        List<Task> tasks = taskHandlerV2.getAllTasks();
        for (Task task : tasks) {
            System.out.println(task.viewTaskDesc() + "\n");
        }
    }

    public TaskController(TaskHandlerV2 taskHandlerV2, TaskDashboardFrame view, DBHandler dbHandler) {
        this.taskHandlerV2 = taskHandlerV2;
        this.legacyTaskHandler = taskHandlerV2.getLegacyHandler(); // Keep reference for DBHandler compatibility
        this.view = view;
        this.dbHandler = dbHandler;
        this.syncService = new SyncService(taskHandlerV2); // Use TaskHandlerV2 for command-based sync
        // Copy user UUID from DBHandler to SyncService if available
        if (dbHandler.getUserUUID() != null) {
            this.syncService.setUserUUID(dbHandler.getUserUUID());
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
        if (syncService != null) {
            syncService.setUserUUID(userUUID);
        }
    }

    public void loadInitialTasks() {
        view.refreshTaskListDisplay();
    }    public void loadInitialFolderList() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return dbHandler.fetchAccessibleFolders();
            } catch (Exception ex) {
                System.err.println("Warning: Unable to fetch folders (offline?): " + ex.getMessage());
                return legacyTaskHandler.getFoldersList();
            }
        }).thenAcceptAsync(folders -> {
            legacyTaskHandler.setFoldersList(folders);
            SwingUtilities.invokeLater(() ->
                view.updateFolderList(legacyTaskHandler.getFoldersNamesList())
            );
        });
    }

    public void loadInitialSyncTime() {
        view.updateLastSyncLabel(getLastSyncTime());
    }

    public List<Task> getTasksByFolder(List<Task> sourceList ,String selectedFolder) {
        return sourceList.stream()
                .filter(task -> task.getFolder_name().equals(selectedFolder))
                .toList();                
    }

    public List<Task> getTasksByStatus(List<Task> sourceList, TaskStatus status) {
        return sourceList.stream()
                .filter(task -> task.getStatus().equals(status))
                .toList();
    }    /**
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
    }    /**
     * Handles creation of a new task from the UI input.
     */
    public void handleCreateTask(String title, String description, String folderName, LocalDateTime dueDate, TaskStatus status) {
        System.out.println("Controller: Creating new task: " + title);
        
        // Get folder ID for the folder name
        String folderId = legacyTaskHandler.getFolderIdByName(folderName);
        
        // Use command-driven approach via TaskHandlerV2
        taskHandlerV2.createTask(title, description, status, dueDate, folderId);
        view.refreshTaskListDisplay();
    }

    public void handleSyncRequest() {
        System.out.println("Controller: Sync request received - using API V2 sync.");
        CompletableFuture<Boolean> syncFuture = syncService.startSyncProcess();
        syncFuture.thenAcceptAsync(success -> {
            if (success) {
                System.out.println("Controller: API V2 sync completed successfully. Updating UI.");
                view.updateLastSyncLabel(getLastSyncTime());
                view.refreshTaskListDisplay();
                loadInitialFolderList();
            }
        }).exceptionally(ex -> {
            System.out.println("Controller: Exception during API V2 sync: " + ex.getMessage());
            return null;
        });
    }

    public void handleHistoryRequest() {
        System.out.println("Controller: History request received.");
        // TODO: Implement logic to show a 'History' view/frame
    }


    public void handleViewTaskRequest(String taskId) {
        System.out.println("Controller: View task request for ID " + taskId);
        // TODO: Implement logic to show a detailed view of the task
        // Task task = taskHandler.getTaskById(taskId);
        // if (task != null) {
        //     TaskDetailDialog dialog = new TaskDetailDialog(view, task);
        //     dialog.setVisible(true);
        // }
    }
    public void handleDeleteTaskRequest(String taskId) {
        System.out.println("Controller: Delete task request for ID " + taskId);
        Task task = getTaskById(taskId);
        if (task != null) {
            taskHandlerV2.deleteTask(task, "User deleted");
            view.refreshTaskListDisplay();
        } else {
            System.err.println("Controller: Could not find task with ID " + taskId + " to delete.");
        }
    }    public void handleWindowClosing() {
        taskHandlerV2.saveTasksToJson();
        if( !Boolean.valueOf((String) UserProperties.getProperty("rememberMe")) ){
            System.out.println("Controller: Logging out user and clearing credentials.");
            UserProperties.logOut();
        }
    }    public List<String> getFolderList() {
        return legacyTaskHandler.getFoldersNamesList();
    }    public LocalDateTime getLastSyncTime() {
        return taskHandlerV2.getLastSync() != null ? taskHandlerV2.getLastSync() : UserProperties.getProperty("lastSyncTime") != null ? LocalDateTime.parse((String) UserProperties.getProperty("lastSyncTime")) : null;
    }    public void handleTaskCompletionToggle(Task task) {
        if (task != null) {
            TaskStatus newStatus = !task.getStatus().equals(TaskStatus.completed) ? TaskStatus.completed : TaskStatus.pending;
            taskHandlerV2.updateTask(task, null, null, newStatus, null, null);
            view.refreshTaskListDisplay();
        } else {
             System.err.println("Controller: Could not find task to toggle completion.");
        }
    }    // --- User Action Handlers ---    
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
        // TODO: Implement logic to show a dialog for changing username and validate JWT to allow change
        String newUsername = JOptionPane.showInputDialog(view, "Enter new username:");
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            // Call API to change username
            System.out.println("Attempting to change username to: " + newUsername);
        }
    }

    public void handleDeleteAccountRequest() {
        System.out.println("Controller: Delete Account request received.");
        // TODO: Implement logic to show a dialog for delete user and validate JWT to allow change
        
    }    public void handleEditTaskRequest(String task_id, String title, String desc, String folder, LocalDateTime due,
            TaskStatus status) {
        System.out.println("Controller: Edit task request for ID " + task_id);
        Task task = getTaskById(task_id);
        if (task != null) {
            taskHandlerV2.updateTask(task, title, desc, status, due, folder);
            view.refreshTaskListDisplay();
        } else {
            System.err.println("Controller: Could not find task with ID " + task_id + " to edit.");
        }
    }    /**
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
