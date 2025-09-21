package debug;

import model.Folder;
import model.TaskHandlerV2;
import model.Task;
import model.TaskStatus;
import COMMON.UserProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class FolderCreateAndEditTest {

    @Test
    public void testCreateAndEditTaskFolders() throws Exception {
        String userId = "folder-test-" + UUID.randomUUID().toString();
        UserProperties.setProperty("userUUID", userId);
        
        // Clean up any existing data
        String tasksPath = UserProperties.getUserDataFilePath(userId, "tasks.json");
        File f = new File(tasksPath);
        if (f.exists()) f.delete();

        TaskHandlerV2 handler = new TaskHandlerV2(userId);
        
        // Create test folders
        Folder kukaFolder = new Folder.Builder("kuka-folder-id")
            .folderName("Kuka")
            .build();
        Folder defaultFolder = new Folder.Builder("default-folder-id")
            .folderName("Default Folder")
            .build();
            
        handler.setFoldersList(List.of(kukaFolder, defaultFolder));

        System.out.println("=== FOLDER CREATE AND EDIT TEST ===");
        
        // Test 1: Create task in Default Folder
        Task task1 = handler.createTask("Test Task 1", "Description 1", TaskStatus.pending, null, "default-folder-id");
        System.out.println("Created task1 in folder: " + task1.getFolder_name() + " (ID: " + task1.getFolder_id() + ")");
        
        // Test 2: Create task in Kuka Folder  
        Task task2 = handler.createTask("Test Task 2", "Description 2", TaskStatus.pending, null, "kuka-folder-id");
        System.out.println("Created task2 in folder: " + task2.getFolder_name() + " (ID: " + task2.getFolder_id() + ")");
        
        // Test 3: Edit task1 to move from Default to Kuka
        System.out.println("\n=== TESTING TASK FOLDER EDIT ===");
        handler.updateTask(task1, null, null, null, null, "Kuka");
        
        // Get updated task
        Task updatedTask1 = handler.getTaskById(task1.getTask_id());
        System.out.println("After edit - task1 folder: " + (updatedTask1 != null ? updatedTask1.getFolder_name() : "NULL") + 
                          " (ID: " + (updatedTask1 != null ? updatedTask1.getFolder_id() : "NULL") + ")");
        
        // Test 4: Edit task2 to move from Kuka to Default
        handler.updateTask(task2, null, null, null, null, "Default Folder");
        
        Task updatedTask2 = handler.getTaskById(task2.getTask_id());
        System.out.println("After edit - task2 folder: " + (updatedTask2 != null ? updatedTask2.getFolder_name() : "NULL") + 
                          " (ID: " + (updatedTask2 != null ? updatedTask2.getFolder_id() : "NULL") + ")");
        
        System.out.println("\n=== COMMAND QUEUE STATUS ===");
        System.out.println("Pending commands: " + handler.getCommandQueue().getPendingCommandCount());
    }
}