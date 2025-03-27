import model.TaskHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AppLauncher {
    private static final String BASE_DIRECTORY = System.getProperty("user.home") + File.separator + ".todoapp";

    public static void main(String[] args) {
        ensureBaseDirectoryExists();

        TaskHandler taskHandler = new TaskHandler();

        // // Add tasks
        // taskHandler.addTask("Buy groceries", "Milk, bread, eggs", "2025-01-10T10:00", "Personal", "1", "new");
        // taskHandler.addTask("Finish project", "Complete the final report", "2025-01-15T15:00", "Work", "1", "update");
        // taskHandler.addTask("Call mom", "Check in and say hi", "2025-01-12T18:00", "Personal", "1", "new");

        // // Prepare JSON files for sync
        // File insertJsonFile = taskHandler.prepareSyncJson("new");
        // File updateJsonFile = taskHandler.prepareSyncJson("update");

        // // Print the lists in memory for verification
        // System.out.println("Tasks in memory:");
        // taskHandler.userTasksList.forEach(task -> System.out.println(task.viewTaskDesc()));

        // // Print the contents of the JSON files
        // System.out.println("\nContents of insert JSON file:");
        // printJsonFile(insertJsonFile);

        // System.out.println("\nContents of update JSON file:");
        // printJsonFile(updateJsonFile);

        // print userTasksList for verification
        System.out.println("\nTasks in memory:");
        taskHandler.userTasksList.forEach(task -> System.out.println(task.viewTaskDesc()));
        // Sync tasks with the database
        // dbHandler.syncTasks("user-uuid-1234", insertJsonFile.getAbsolutePath(), updateJsonFile.getAbsolutePath());
    }

    private static void ensureBaseDirectoryExists() {
        File baseDirectory = new File(BASE_DIRECTORY);
        if (!baseDirectory.exists()) {
            if (baseDirectory.mkdirs()) {
                System.out.println("Created base directory: " + BASE_DIRECTORY);
            } else {
                System.err.println("Failed to create base directory: " + BASE_DIRECTORY);
            }
        }
    }

    private static void printJsonFile(File jsonFile) {
        try {
            String content = Files.readString(jsonFile.toPath());
            System.out.println(content);
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + jsonFile.getName());
            e.printStackTrace();
        }
    }
}