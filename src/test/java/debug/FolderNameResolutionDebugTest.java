package debug;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import model.TaskHandlerV2;
import model.Folder;
import model.Task;
import model.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Test to debug folder name resolution issue
 */
@DisplayName("Folder Name Resolution Debug Test")
class FolderNameResolutionDebugTest {
    
    private TaskHandlerV2 taskHandler;
    private final String testUserId = "test-user-folder-resolution";
    
    @BeforeEach
    void setUp() {
        taskHandler = new TaskHandlerV2(testUserId);
        
        // Set up folders similar to the actual data
        List<Folder> folders = java.util.Arrays.asList(
            new Folder("0198b44f-e927-726d-90a0-10580c15685a", "Kuka", "cloud", 
                       LocalDateTime.now(), null, LocalDateTime.now()),
            new Folder("019669ce-a4d3-7e55-8375-294f932646bf", "Default Folder", "cloud", 
                       LocalDateTime.now(), null, LocalDateTime.now())
        );
        taskHandler.setFoldersList(folders);
    }
    
    @Test
    @DisplayName("Should resolve folder name 'Kuka' to correct ID")
    void testFolderNameResolution() {
        // Test the exact folder name resolution that's failing
        String resolvedId = taskHandler.getFolderIdByName("Kuka");
        System.out.println("=== FOLDER RESOLUTION TEST ===");
        System.out.println("Input: 'Kuka'");
        System.out.println("Expected ID: '0198b44f-e927-726d-90a0-10580c15685a'");
        System.out.println("Actual ID: '" + resolvedId + "'");
        
        assertEquals("0198b44f-e927-726d-90a0-10580c15685a", resolvedId, 
                    "Should resolve 'Kuka' to correct folder ID");
        
        // Also test Default Folder
        String defaultId = taskHandler.getFolderIdByName("Default Folder");
        System.out.println("Default Folder ID: '" + defaultId + "'");
        assertEquals("019669ce-a4d3-7e55-8375-294f932646bf", defaultId,
                    "Should resolve 'Default Folder' to correct ID");
    }
    
    @Test
    @DisplayName("Should create task with correct folder when folder name is resolved")
    void testTaskCreationWithFolderResolution() {
        String folderName = "Kuka";
        String expectedFolderId = "0198b44f-e927-726d-90a0-10580c15685a";
        
        // Create task with folder name (simulating UI flow)
        String resolvedFolderId = taskHandler.getFolderIdByName(folderName);
        Task createdTask = taskHandler.createTask("Test Task", "Description", 
                                                TaskStatus.pending, null, resolvedFolderId);
        
        System.out.println("=== TASK CREATION TEST ===");
        System.out.println("Folder name: '" + folderName + "'");
        System.out.println("Resolved folder ID: '" + resolvedFolderId + "'");
        System.out.println("Task folder ID: '" + createdTask.getFolder_id() + "'");
        System.out.println("Task folder name: '" + createdTask.getFolder_name() + "'");
        
        assertEquals(expectedFolderId, resolvedFolderId, "Folder resolution should work");
        assertEquals(expectedFolderId, createdTask.getFolder_id(), "Task should have correct folder ID");
        assertEquals(folderName, createdTask.getFolder_name(), "Task should have correct folder name");
    }
}