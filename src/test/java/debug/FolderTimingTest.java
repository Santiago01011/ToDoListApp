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

public class FolderTimingTest {

    @Test
    public void testTaskCreationWithEmptyFolderList() throws Exception {
        String userId = "timing-test-" + UUID.randomUUID().toString();
        UserProperties.setProperty("userUUID", userId);
        
        // Clean up any existing data
        String tasksPath = UserProperties.getUserDataFilePath(userId, "tasks.json");
        File f = new File(tasksPath);
        if (f.exists()) f.delete();

        TaskHandlerV2 handler = new TaskHandlerV2(userId);
        
        System.out.println("=== TIMING TEST: Task creation with empty folder list ===");
        
        // Test 1: Try to create task when no folders are loaded (simulates startup timing issue)
        String resolvedFolderId = handler.getFolderIdByName("Kuka");
        System.out.println("Resolved 'Kuka' with empty folder list: " + resolvedFolderId);
        
        Task task1 = handler.createTask("Timing Test Task 1", "Testing timing issue", 
                                      TaskStatus.pending, LocalDateTime.now().plusDays(1), resolvedFolderId);
        
        System.out.println("Task 1 (before folders loaded):");
        System.out.println("  Folder ID: " + task1.getFolder_id());
        System.out.println("  Folder Name: " + task1.getFolder_name());
        
        // Now load folders (simulates what happens after sync)
        Folder kukaFolder = new Folder.Builder("0198b44f-e927-726d-90a0-10580c15685a")
            .folderName("Kuka")
            .build();
        Folder defaultFolder = new Folder.Builder("019669ce-a4d3-7e55-8375-294f932646bf")
            .folderName("Default Folder")
            .build();
            
        handler.setFoldersList(List.of(kukaFolder, defaultFolder));
        System.out.println("Folders loaded after task creation");
        
        // Test 2: Create task after folders are loaded
        String resolvedFolderId2 = handler.getFolderIdByName("Kuka");
        System.out.println("Resolved 'Kuka' with loaded folders: " + resolvedFolderId2);
        
        Task task2 = handler.createTask("Timing Test Task 2", "Testing after folders loaded", 
                                      TaskStatus.pending, LocalDateTime.now().plusDays(1), resolvedFolderId2);
        
        System.out.println("Task 2 (after folders loaded):");
        System.out.println("  Folder ID: " + task2.getFolder_id());
        System.out.println("  Folder Name: " + task2.getFolder_name());
        
        System.out.println("\n=== CONCLUSION ===");
        if (task1.getFolder_id() == null && task2.getFolder_id() != null) {
            System.out.println("TIMING ISSUE CONFIRMED: Folder resolution fails when folders not loaded");
        } else {
            System.out.println("No timing issue detected");
        }
    }
}