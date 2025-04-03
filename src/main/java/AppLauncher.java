import DBH.DBHandler;
import model.Task;
import model.TaskHandler;

public class AppLauncher {
    public static void main(String[] args) {
        // Initialize DBHandler and TaskHandler
        DBHandler dbHandler = new DBHandler();
        TaskHandler taskHandler = new TaskHandler();

        // Mock user ID
        String userId = "01959f92-0d81-78ab-9c17-c180be5d9a37";
        dbHandler.startSyncProcess(taskHandler, userId);

        // Print initial task list
        System.out.println("Initial Task List:");
        taskHandler.userTasksList.forEach(task -> System.out.println(task.viewTaskDesc() + "\n"));

        // Find and update "Prepare presentation" task
        Task presentationTask = taskHandler.userTasksList.stream()
                .filter(task -> task.getTask_title() != null && task.getTask_title().contains("Prepare presentation updated"))
                .findFirst()
                .orElse(null);

        if (presentationTask != null) {
            System.out.println("Updating task: " + presentationTask.getTask_title());
            taskHandler.updateTask(presentationTask, "Prepare presentation", null, null, null, null);
        } else {
            System.out.println("Presentation task not found.");
        }

        // Print task list after update
        System.out.println("\nTask List After Update:");
        taskHandler.userTasksList.forEach(task -> System.out.println(task.viewTaskDesc() + "\n"));

        // Run sync again to push changes
        dbHandler.startSyncProcess(taskHandler, userId);

        // Print final task list
        System.out.println("\nFinal Task List After Sync:");
        taskHandler.userTasksList.forEach(task -> System.out.println(task.viewTaskDesc() + "\n"));
    }
}