package test;

import model.Task;
import model.TaskHandler;
import model.TaskStatus;
import model.sync.CommandBatch;
import model.sync.CommandFactory;
import model.sync.SyncCommand;
import service.SyncService;
import COMMON.JSONUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple tests for the new sync functionality.
 */
public class SyncTests {

    public static void testCommandCreation() {
        System.out.println("=== Testing Command Creation ===");
        
        // Create a test task
        Task task = new Task.Builder("test-task-123")
            .taskTitle("Test Task")
            .description("Test Description")
            .status(TaskStatus.pending)
            .dueDate(LocalDateTime.now().plusDays(1))
            .folderName("Test Folder")
            .folderId("folder-123")
            .createdAt(LocalDateTime.now())
            .sync_status("new")
            .build();

        // Test CREATE command
        SyncCommand createCmd = CommandFactory.createTaskCommand(task);
        System.out.println("CREATE Command Type: " + createCmd.getCommandType());
        System.out.println("Entity Type: " + createCmd.getEntityType());
        System.out.println("Entity ID: " + createCmd.getEntityId());
        System.out.println("Data contains title: " + createCmd.getData().containsKey("task_title"));

        // Test UPDATE command
        Task shadowTask = new Task.Builder("test-task-123")
            .taskTitle("Updated Title")
            .description("Updated Description")
            .sync_status("to_update")
            .updatedAt(LocalDateTime.now())
            .build();

        SyncCommand updateCmd = CommandFactory.updateTaskCommand(shadowTask);
        System.out.println("UPDATE Command Type: " + updateCmd.getCommandType());
        System.out.println("Update data keys: " + updateCmd.getData().keySet());

        // Test DELETE command
        Task deleteTask = new Task.Builder("test-task-123")
            .deletedAt(LocalDateTime.now())
            .sync_status("to_update")
            .build();

        SyncCommand deleteCmd = CommandFactory.deleteTaskCommand(deleteTask);
        System.out.println("DELETE Command Type: " + deleteCmd.getCommandType());
        System.out.println("Delete data contains deleted_at: " + deleteCmd.getData().containsKey("deleted_at"));
    }

    public static void testCommandBatchSerialization() {
        System.out.println("\n=== Testing Command Batch Serialization ===");
        
        try {
            // Create test commands
            List<SyncCommand> commands = new ArrayList<>();
            
            Task task1 = new Task.Builder("task-1")
                .taskTitle("Task 1")
                .status(TaskStatus.pending)
                .sync_status("new")
                .build();
            commands.add(CommandFactory.createTaskCommand(task1));

            Task task2 = new Task.Builder("task-2")
                .taskTitle("Updated Task 2")
                .sync_status("to_update")
                .build();
            commands.add(CommandFactory.updateTaskCommand(task2));

            // Create command batch
            CommandBatch batch = new CommandBatch(
                "user-uuid-123",
                LocalDateTime.now(),
                LocalDateTime.now().minusHours(1),
                commands
            );

            // Test serialization
            String json = JSONUtils.toJsonString(batch);
            System.out.println("Command batch serialized successfully");
            System.out.println("JSON length: " + json.length());
            System.out.println("Contains user_id: " + json.contains("\"user_id\""));
            System.out.println("Contains commands: " + json.contains("\"commands\""));
            
        } catch (IOException e) {
            System.err.println("Serialization test failed: " + e.getMessage());
        }
    }

    public static void testSyncServiceInitialization() {
        System.out.println("\n=== Testing SyncService Initialization ===");
        
        TaskHandler taskHandler = new TaskHandler();
        SyncService syncService = new SyncService(taskHandler);
        
        syncService.setUserUUID("test-user-uuid");
        System.out.println("SyncService created and user UUID set");
        System.out.println("User UUID: " + syncService.getUserUUID());
    }

    public static void main(String[] args) {
        System.out.println("Running Sync API V2 Tests...");
        
        testCommandCreation();
        testCommandBatchSerialization();
        testSyncServiceInitialization();
        
        System.out.println("\n=== All Tests Completed ===");
    }
}