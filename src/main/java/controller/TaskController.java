package controller;

import model.TaskHandler;
import model.Task;
import UI.TaskDashboardFrame;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import DBH.NewDBHandler;

import java.time.LocalDateTime;

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
        List<String> folders = taskHandler.getFolderList();
        view.updateFolderList(folders);
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

    public void handleNewTaskRequest() {
        System.out.println("Controller: New task request received.");
        // TODO: Implement logic to show a 'New Task' dialog/frame

    }

    public void handleSyncRequest() {
        System.out.println("Controller: Sync request received.");
        CompletableFuture<Boolean> syncFuture = dbHandler.startSyncProcess();
        syncFuture.thenAcceptAsync(succes -> {
            if ( succes ){
                System.out.println("Controller: Async sync completed successfully. Updating UI.");
                view.refreshTaskListDisplay(taskHandler.userTasksList);
                view.updateLastSyncLabel(taskHandler.getLastSync());
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
            // Set the deleted_at timestamp using updateTask
            // Pass null for fields not being changed
            taskHandler.updateTask(task, null, null, null, null, null, LocalDateTime.now());
            // Refresh the view to hide the deleted task
            view.refreshTaskListDisplay(taskHandler.userTasksList);
        } else {
            System.err.println("Controller: Could not find task with ID " + taskId + " to delete.");
        }
    }

    public void handleWindowClosing() {
        taskHandler.saveTasksToJson();
    }

    public List<String> getFolderList() {
        return taskHandler.getFolderList();
    }

    public LocalDateTime getLastSyncTime() {
        return taskHandler.getLastSync();
    }

    public void handleTaskCompletionToggle(String task_id, boolean isSelected) {
        System.out.println("Controller: Task completion toggle for ID " + task_id + " isSelected: " + isSelected);
        Task task = taskHandler.getTaskById(task_id);
        if (task != null) {
            taskHandler.updateTask(task, null, null, isSelected ? "completed" : "pending", null, null, null);
            printUserTasks();
            view.refreshTaskListDisplay(taskHandler.userTasksList);
        } else {
             System.err.println("Controller: Could not find task with ID " + task_id + " to toggle completion.");
        }
    }

}
