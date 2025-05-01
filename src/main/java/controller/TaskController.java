package controller;

import model.TaskHandler;
import model.Task;
import model.Folder;
import UI.LoginFrame;
import UI.TaskDashboardFrame;
import COMMON.UserProperties;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import DBH.NewDBHandler;

import java.time.LocalDateTime;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class TaskController {

    private TaskHandler taskHandler;
    private TaskDashboardFrame view;
    private NewDBHandler dbHandler;

    public void printUserTasks() {
        System.out.println("User Tasks:");
        for (Task task : taskHandler.userTasksList) {
            System.out.println(task.viewTaskDesc() + "\n");
        }
    }

    public TaskController(TaskHandler taskHandler, TaskDashboardFrame view, NewDBHandler dbHandler) {
        this.taskHandler = taskHandler;
        this.view = view;
        this.dbHandler = dbHandler;
    }

    public void loadInitialTasks() {
        view.refreshTaskListDisplay(taskHandler.userTasksList);
    }

    public void loadInitialFolderList() {
        // Fetch accessible folders asynchronously to avoid UI blocking and handle offline fallback
        CompletableFuture.supplyAsync(() -> {
            try {
                return dbHandler.fetchAccessibleFolders();
            } catch (Exception ex) {
                System.err.println("Warning: Unable to fetch folders (offline?): " + ex.getMessage());
                return taskHandler.getFoldersList();
            }
        }).thenAcceptAsync(folders -> {
            taskHandler.setFoldersList(folders);
            SwingUtilities.invokeLater(() ->
                view.updateFolderList(taskHandler.getFoldersNamesList())
            );
        });
    }

    public void loadInitialSyncTime() {
        view.updateLastSyncLabel(taskHandler.getLastSync());
    }

    // --- Action Handlers ---

    public void handleFilterByFolderRequest(String selectedFolder) {
        System.out.println("Controller: Filtering tasks by folder: " + selectedFolder);
        List<Task> filteredTasks;
        if ("All Folders".equals(selectedFolder) || selectedFolder == null) {
            filteredTasks = taskHandler.userTasksList;
        } else {
            filteredTasks = taskHandler.getTasksByFolder(selectedFolder);
        }
        view.refreshTaskListDisplay(filteredTasks);
    }

    /**
     * Handles creation of a new task from the UI input.
     */
    public void handleCreateTask(String title, String description, String folderName, String dueDate) {
        System.out.println("Controller: Creating new task: " + title);
        taskHandler.addTask(title, description, "pending", dueDate, folderName);
        view.refreshTaskListDisplay(taskHandler.userTasksList);
    }

    public void handleSyncRequest() {
        System.out.println("Controller: Sync request received.");
        CompletableFuture<Boolean> syncFuture = dbHandler.startSyncProcess();
        syncFuture.thenAcceptAsync(succes -> {
            if (succes) {
                System.out.println("Controller: Async sync completed successfully. Updating UI.");
                view.updateLastSyncLabel(taskHandler.getLastSync());
                view.refreshTaskListDisplay(taskHandler.userTasksList);
                // reload folders from DB
                loadInitialFolderList();
            }
        }).exceptionally(ex -> {
            System.out.println("Controller: Exception during sync: " + ex.getMessage());
            return null;
        });
    }

    public void handleHistoryRequest() {
        System.out.println("Controller: History request received.");
        // TODO: Implement logic to show a 'History' view/frame
    }

    public void handleFilterButtonClicked() {
        System.out.println("Controller: Filter button clicked.");
        // TODO: Implement logic to show filter options (e.g., a dialog)
    }

    public void handleUserButtonClicked() {
        System.out.println("Controller: User button clicked.");
        // TODO: Implement logic to show user profile/settings view
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

    public void handleEditTaskRequest(String taskId) {
        System.out.println("Controller: Edit task request for ID " + taskId);
        // TODO: Implement logic to show an edit dialog for the task
        // Task task = taskHandler.getTaskById(taskId);
        // if (task != null) {
        //     EditTaskDialog dialog = new EditTaskDialog(view, this, task); // Pass controller and task
        //     dialog.setVisible(true);
        //     // After dialog closes and potentially saves changes, refresh:
        //     // loadInitialTasks();
        // }
    }

    public void handleDeleteTaskRequest(String taskId) {
        System.out.println("Controller: Delete task request for ID " + taskId);
        Task task = taskHandler.getTaskById(taskId);
        if (task != null) {
            taskHandler.updateTask(task, null, null, null, null, null, LocalDateTime.now());
            view.refreshTaskListDisplay(taskHandler.userTasksList);
        } else {
            System.err.println("Controller: Could not find task with ID " + taskId + " to delete.");
        }
    }

    public void handleWindowClosing() {
        taskHandler.saveTasksToJson();
        if( !Boolean.valueOf((String) UserProperties.getProperty("rememberMe")) ){
            System.out.println("Controller: Logging out user and clearing credentials.");
            UserProperties.logOut();
        }
    }

    public List<String> getFolderList() {
        return taskHandler.getFoldersNamesList();
    }

    public LocalDateTime getLastSyncTime() {
        return taskHandler.getLastSync();
    }

    public void handleTaskCompletionToggle(Task task) {
        if (task != null) {
            taskHandler.updateTask(task, null, null, !task.getStatus().equals("completed") ? "completed" : "pending", null, null, null);
            view.refreshTaskListDisplay(taskHandler.userTasksList);
        } else {
             System.err.println("Controller: Could not find task to toggle completion.");
        }
    }

    // --- User Action Handlers ---

    public void handleLogoutRequest() {
        System.out.println("Controller: Logout request received.");
        UserProperties.setProperty("rememberMe", "false");
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
        // TODO: Implement logic to show a dialog for changing username
        String newUsername = JOptionPane.showInputDialog(view, "Enter new username:");
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            // Call API to change username
            System.out.println("Attempting to change username to: " + newUsername);
        }
    }

    public void handleDeleteAccountRequest() {
        System.out.println("Controller: Delete Account request received.");
        // TODO: Implement logic to permanently delete the user account
        // This is a destructive action, will be maneged through the API
        
    }

}
