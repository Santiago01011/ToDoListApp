package controller;

import model.TaskHandler;
import model.TaskStatus;
import model.Task;
import model.Folder;
import model.FiltersCriteria;
import UI.LoginFrame;
import UI.TaskDashboardFrame;
import COMMON.UserProperties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        view.refreshTaskListDisplay();
    }

    public void loadInitialFolderList() {
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
        view.updateLastSyncLabel(getLastSyncTime());
    }

    public List<Task> getTasksByFolder(List<Task> sourceList ,String selectedFolder) {
        return sourceList.stream()
                .filter(task -> task.getFolder_name().equals(selectedFolder))
                .toList();                
    }

    public List<Task> getTasksByStatus(List<Task> sourceList, String status) {
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
        List<Task> filteredTasks = new ArrayList<>(taskHandler.userTasksList);
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
        String id = UUID.randomUUID().toString();
        Task task = new Task.Builder(id)
            .taskTitle(title)
            .description(description)
            .dueDate(dueDate)
            .folderName(folderName)
            .folderId(taskHandler.getFolderIdByName(folderName))
            .status(status)
            .sync_status("new")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        taskHandler.userTasksList.add(task);
        view.refreshTaskListDisplay();
    }

    public void handleSyncRequest() {
        System.out.println("Controller: Sync request received.");
        CompletableFuture<Boolean> syncFuture = dbHandler.startSyncProcess();
        syncFuture.thenAcceptAsync(succes -> {
            if (succes) {
                System.out.println("Controller: Async sync completed successfully. Updating UI.");
                view.updateLastSyncLabel(getLastSyncTime());
                view.refreshTaskListDisplay();
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
        Task task = taskHandler.getTaskById(taskId);
        if (task != null) {
            taskHandler.updateTask(task, null, null, null, null, null, LocalDateTime.now());
            view.refreshTaskListDisplay();
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
        return taskHandler.getLastSync() != null ? taskHandler.getLastSync() : UserProperties.getProperty("lastSyncTime") != null ? LocalDateTime.parse((String) UserProperties.getProperty("lastSyncTime")) : null;
    }

    public void handleTaskCompletionToggle(Task task) {
        if (task != null) {
            taskHandler.updateTask(task, null, null, !task.getStatus().equals(TaskStatus.completed) ? TaskStatus.completed : TaskStatus.pending, null, null, null);
            view.refreshTaskListDisplay();
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
        
    }

    public void handleEditTaskRequest(String task_id, String title, String desc, String folder, LocalDateTime due,
            TaskStatus status) {
        System.out.println("Controller: Edit task request for ID " + task_id);
        Task task = taskHandler.getTaskById(task_id);
        if (task != null) {
            taskHandler.updateTask(task, title, desc, status, due, folder, null);
            view.refreshTaskListDisplay();
        } else {
            System.err.println("Controller: Could not find task with ID " + task_id + " to edit.");
        }
    }

}
