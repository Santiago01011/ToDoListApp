import model.TaskHandler;
import DBH.DBHandler;
import COMMON.UserProperties;
import java.io.File;



public class AppLauncher {
    public static void main(String[] args) {

        TaskHandler taskHandler = new TaskHandler();
        DBHandler dbHandler = new DBHandler();
        // get user properties
        String dbUrl = (String) UserProperties.getProperty("dbUrl");
        System.out.println("Database URL: " + dbUrl);

        // Add tasks
        // taskHandler.addTask("Buy groceries", "Milk, bread, eggs", "pending" ,"2025-01-10T10:00", "Personal", "new");
        // taskHandler.addTask("Finish project", "Complete the final report", "completed", "2025-01-15T15:00", "Work",  "update");
        // taskHandler.addTask("Call mom", "Check in and say hi", "completed", "2025-01-12T18:00", "Default",  "new");

        // Prepare JSON files for sync
       File insertJsonFile = taskHandler.prepareSyncJson("new");
       File updateJsonFile = taskHandler.prepareSyncJson("update");
        // save the userTasksList to json file
        taskHandler.prepareLocalTasksJson();
        // print userTasksList for verification
        System.out.println("\nTasks in memory:");
        taskHandler.userTasksList.forEach(task -> System.out.println(task.viewTaskDesc()));
        // Sync tasks with the database
        dbHandler.syncTasks("user-uuid-1234", insertJsonFile.getAbsolutePath(), updateJsonFile.getAbsolutePath());
    }
}