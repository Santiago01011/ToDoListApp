import model.TaskHandler;
import DBH.DBHandler;

import java.io.File;

public class AppLauncher {
    public static void main(String[] args) {
        TaskHandler taskHandler = new TaskHandler();
        DBHandler dbHandler = new DBHandler();

        // Add tasks
        taskHandler.addTask("Buy groceries", "Milk, bread, eggs", "2025-01-10T10:00", "Personal", "1");
        taskHandler.addTask("Finish project", "Complete the final report", "2025-01-15T15:00", "Work", "1");

        // Prepare JSON files for sync
        File insertJsonFile = taskHandler.prepareSyncJson("insert");
        File updateJsonFile = taskHandler.prepareSyncJson("update");

        // Print the lists in memory for verification
        System.out.println("Tasks in memory:");
        taskHandler.userTasksList.forEach(task -> System.out.println(task.getTaskTitle()));

        // Sync tasks with the database
        //dbHandler.syncTasks("user-uuid-1234", insertJsonFile.getAbsolutePath(), updateJsonFile.getAbsolutePath());
    }
}