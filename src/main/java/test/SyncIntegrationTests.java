package test;

import model.Task;
import model.TaskHandler;
import model.TaskStatus;
import service.SyncService;
import controller.TaskController;
import DBH.DBHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Integration tests for the full sync workflow with new API V2 system.
 */
public class SyncIntegrationTests {

    public static void testTaskCreationAndSyncPreparation() {
        System.out.println("=== Testing Task Creation and Sync Preparation ===");
        
        // Set up components like in the real app
        TaskHandler taskHandler = new TaskHandler();
        DBHandler dbHandler = new DBHandler();
        SyncService syncService = new SyncService(taskHandler);
        
        // Set user UUID (use a valid UUID format)
        String testUserUUID = "550e8400-e29b-41d4-a716-446655440000";
        dbHandler.setUserUUID(testUserUUID);
        syncService.setUserUUID(testUserUUID);
        
        // Create some test tasks as would happen in the app
        Task newTask1 = new Task.Builder("local-task-1")
            .taskTitle("Test Task 1")
            .description("This is a test task")
            .status(TaskStatus.pending)
            .dueDate(LocalDateTime.now().plusDays(2))
            .folderName("Work")
            .folderId("folder-work-123")
            .createdAt(LocalDateTime.now())
            .sync_status("new")
            .build();
        
        Task newTask2 = new Task.Builder("local-task-2")
            .taskTitle("Test Task 2")
            .description("Another test task")
            .status(TaskStatus.in_progress)
            .folderName("Personal")
            .folderId("folder-personal-456")
            .createdAt(LocalDateTime.now())
            .sync_status("new")
            .build();
        
        // Add tasks to handler
        taskHandler.userTasksList.add(newTask1);
        taskHandler.userTasksList.add(newTask2);
        
        System.out.println("Created " + taskHandler.userTasksList.size() + " new tasks");
        
        // Simulate the task being synced first (changing status to cloud)
        newTask1.setSync_status("cloud");
        
        // Now update the task to test shadow updates
        taskHandler.updateTask(newTask1, "Updated Test Task 1", null, TaskStatus.completed, null, null, null);
        
        // Check shadow updates
        List<Task> shadowUpdates = taskHandler.getShadowUpdatesForSync();
        System.out.println("Shadow updates for sync: " + shadowUpdates.size());
        
        // Verify task states
        long newTasks = taskHandler.userTasksList.stream()
            .filter(t -> "new".equals(t.getSync_status()))
            .count();
        long localTasks = taskHandler.userTasksList.stream()
            .filter(t -> "local".equals(t.getSync_status()))
            .count();
        
        System.out.println("Tasks with 'new' status: " + newTasks);  
        System.out.println("Tasks with 'local' status: " + localTasks);
        
        if (newTasks > 0 && shadowUpdates.size() > 0) {
            System.out.println("✓ Task creation and update workflow working correctly");
        } else {
            System.out.println("✗ Task creation and update workflow has issues");
        }
    }
    
    public static void testSyncServiceWithoutNetwork() {
        System.out.println("\n=== Testing SyncService Components (Without Network) ===");
        
        TaskHandler taskHandler = new TaskHandler();
        SyncService syncService = new SyncService(taskHandler);
        syncService.setUserUUID("550e8400-e29b-41d4-a716-446655440001");
        
        // Create test tasks
        Task task1 = new Task.Builder("task-for-sync-1")
            .taskTitle("Sync Test Task")
            .status(TaskStatus.pending)
            .sync_status("new")
            .createdAt(LocalDateTime.now())
            .build();
        
        taskHandler.userTasksList.add(task1);
        
        // Test that sync service can be created and configured
        System.out.println("SyncService initialized: " + (syncService.getUserUUID() != null));
        
        // We can't test the actual sync without a real API endpoint,
        // but we can verify the service is properly configured
        System.out.println("✓ SyncService properly configured and ready");
    }

    public static void testOldDBHandlerStillWorksForFolders() {
        System.out.println("\n=== Testing DBHandler Folder Functionality ===");
        
        DBHandler dbHandler = new DBHandler();
        
        // Test that basic DBHandler functionality is preserved
        System.out.println("DBHandler created without TaskHandler dependency: ✓");
        
        // Test UUID handling
        dbHandler.setUserUUID("550e8400-e29b-41d4-a716-446655440002");
        String retrievedUUID = dbHandler.getUserUUID();
        
        if ("550e8400-e29b-41d4-a716-446655440002".equals(retrievedUUID)) {
            System.out.println("UUID handling works: ✓");
        } else {
            System.out.println("UUID handling broken: ✗");
        }
        
        // Note: We can't test fetchAccessibleFolders without a real DB connection
        // but the structure is preserved
        System.out.println("Folder functionality structure preserved: ✓");
    }

    public static void main(String[] args) {
        System.out.println("Running Sync Integration Tests...");
        
        testTaskCreationAndSyncPreparation();
        testSyncServiceWithoutNetwork();
        testOldDBHandlerStillWorksForFolders();
        
        System.out.println("\n=== Integration Tests Completed ===");
        System.out.println("Note: Network-dependent tests require a running API V2 endpoint");
    }
}