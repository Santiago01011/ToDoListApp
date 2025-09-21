package debug;

import model.Folder;
import model.Task;
import model.TaskHandlerV2;
import model.TaskStatus;
import COMMON.UserProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LiveTaskCreationTest {

    @Test
    public void testCreateTaskInKukaFolder() throws Exception {
        String userId = "live-test-" + UUID.randomUUID().toString();
        UserProperties.setProperty("userUUID", userId);
        
        // Clean up any existing data
        String tasksPath = UserProperties.getUserDataFilePath(userId, "tasks.json");
        File f = new File(tasksPath);
        if (f.exists()) f.delete();

        TaskHandlerV2 handler = new TaskHandlerV2(userId);
        
        // Create the folders that exist in the real app
        Folder kukaFolder = new Folder.Builder("0198b44f-e927-726d-90a0-10580c15685a")
            .folderName("Kuka")
            .build();
        Folder defaultFolder = new Folder.Builder("019669ce-a4d3-7e55-8375-294f932646bf")
            .folderName("Default Folder")
            .build();
            
        handler.setFoldersList(List.of(kukaFolder, defaultFolder));

        System.out.println("=== LIVE TASK CREATION TEST ===");
        
        // Test 1: Create task with folder name (like UI does)
        String resolvedFolderId = handler.getFolderIdByName("Kuka");
        System.out.println("Resolved 'Kuka' to: " + resolvedFolderId);
        
        Task task = handler.createTask("Live Test Task", "Testing folder assignment", 
                                     TaskStatus.pending, LocalDateTime.now().plusDays(1), resolvedFolderId);
        
        System.out.println("Created task:");
        System.out.println("  Title: " + task.getTitle());
        System.out.println("  Folder ID: " + task.getFolder_id());
        System.out.println("  Folder Name: " + task.getFolder_name());
        
        // Save and reload to verify persistence
        handler.saveTasksToJson();
        
        TaskHandlerV2 loader = new TaskHandlerV2(userId);
        List<Task> loadedTasks = loader.getAllTasks();
        
        System.out.println("\nAfter reload:");
        for (Task t : loadedTasks) {
            System.out.println("  Task: " + t.getTitle() + " -> Folder: " + t.getFolder_name() + " (" + t.getFolder_id() + ")");
        }
    }
}