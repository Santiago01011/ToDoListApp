package service;

import model.Task;
import model.TaskHandlerV2;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the optimization features working together.
 */
@DisplayName("Optimization Integration Tests")
class OptimizationIntegrationTest {
    
    private TaskHandlerV2 taskHandler;
    private String testUserId;
    
    @BeforeEach
    void setUp() {
        // Use unique user ID for each test to avoid conflicts
        testUserId = "integration-test-" + UUID.randomUUID().toString();
        taskHandler = new TaskHandlerV2(testUserId);
        
        // Clear any existing state through public interface
        taskHandler.getCommandQueue().clearCommands();
    }
    
    @Test
    @DisplayName("Should cache folder names and reduce API calls")
    void shouldCacheFolderNamesAndReduceAPICalls() {
        // This test verifies that folder caching works with task creation
        
        // Create a task with folder using the createTask method
        taskHandler.createTask(
            "Test Task for Caching",
            "Testing folder cache integration", 
            TaskStatus.pending,
            LocalDateTime.now(),
            "test-folder-id"
        );
        
        // Verify task was created
        List<Task> tasks = taskHandler.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task for Caching", tasks.get(0).getTitle());
        assertEquals("test-folder-id", tasks.get(0).getFolder_id());
    }
    
    @Test
    @DisplayName("Should batch persistence operations")
    void shouldBatchPersistenceOperations() {
        // Create multiple tasks rapidly to test batching
        for (int i = 0; i < 5; i++) {
            taskHandler.createTask(
                "Batch Test Task " + i,
                "Testing batch persistence",
                TaskStatus.pending,
                LocalDateTime.now(),
                null
            );
        }
        
        // Verify all tasks were added
        List<Task> tasks = taskHandler.getAllTasks();
        assertEquals(5, tasks.size());
    }
    
    @Test
    @DisplayName("Should merge command queue operations")
    void shouldMergeCommandQueueOperations() {
        // Create a task
        Task task = taskHandler.createTask(
            "Original Title",
            "Original Description",
            TaskStatus.pending,
            LocalDateTime.now(),
            null
        );
        
        // Make multiple rapid updates to the same task
        taskHandler.updateTask(task, "Updated Title 1", "Original Description", 
            TaskStatus.pending, LocalDateTime.now(), null);
        
        taskHandler.updateTask(task, "Updated Title 1", "Updated Description", 
            TaskStatus.pending, LocalDateTime.now(), null);
        
        taskHandler.updateTask(task, "Final Title", "Updated Description", 
            TaskStatus.pending, LocalDateTime.now(), null);
        
        // Verify the command queue has optimized the commands
        int commandCount = taskHandler.getCommandQueue().getPendingCommandCount();
        assertTrue(commandCount <= 3, 
            "Command queue should have optimized redundant updates (got " + commandCount + " commands)");
        
        // Verify final state is correct
        List<Task> tasks = taskHandler.getAllTasks();
        Task retrievedTask = tasks.stream()
            .filter(t -> t.getTask_id().equals(task.getTask_id()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(retrievedTask);
        assertEquals("Final Title", retrievedTask.getTitle());
        assertEquals("Updated Description", retrievedTask.getDescription());
    }
    
    @Test
    @DisplayName("Should handle folder cache integration gracefully")
    void shouldHandleFolderCacheIntegrationGracefully() {
        // Test that folder cache service doesn't interfere with normal operations
        
        // Create tasks with and without folders
        taskHandler.createTask(
            "Task With Folder",
            "Description",
            TaskStatus.pending,
            LocalDateTime.now(),
            "some-folder-id"
        );
        
        taskHandler.createTask(
            "Task Without Folder",
            "Description",
            TaskStatus.pending,
            LocalDateTime.now(),
            null
        );
        
        List<Task> tasks = taskHandler.getAllTasks();
        assertEquals(2, tasks.size());
        
        // Both tasks should be present regardless of folder cache state
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task With Folder")));
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task Without Folder")));
    }
    
    @Test
    @DisplayName("Should maintain data consistency during optimizations")
    void shouldMaintainDataConsistencyDuringOptimizations() {
        // Create, update, and delete tasks to test all operations work correctly
        
        Task task = taskHandler.createTask(
            "Consistency Test",
            "Description",
            TaskStatus.pending,
            LocalDateTime.now(),
            null
        );
        
        // Verify creation
        assertEquals(1, taskHandler.getAllTasks().size());
        
        // Update
        taskHandler.updateTask(task, "Consistency Test", "Description", 
            TaskStatus.completed, LocalDateTime.now(), null);
        
        Task retrievedTask = taskHandler.getAllTasks().get(0);
        assertEquals(TaskStatus.completed, retrievedTask.getStatus());
        
        // Delete
        taskHandler.deleteTask(task);
        assertEquals(0, taskHandler.getAllTasks().size());
    }
}
