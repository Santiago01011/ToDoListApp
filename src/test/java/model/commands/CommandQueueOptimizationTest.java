package model.commands;

import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for command queue optimizations including command deduplication and merging.
 */
@DisplayName("CommandQueue Optimization Tests")
class CommandQueueOptimizationTest {
    
    private CommandQueue commandQueue;
    private String testUserId;
    private final String testTaskId = "test-task-456";
    
    @BeforeEach
    void setUp() {
        // Use unique user ID for each test to avoid file conflicts
        testUserId = "test-user-" + UUID.randomUUID().toString();
        commandQueue = new CommandQueue(testUserId);
        
        // Clear any existing commands
        commandQueue.clearCommands();
    }
    
    @Test
    @DisplayName("Should merge consecutive UPDATE commands for same task")
    void shouldMergeConsecutiveUpdateCommands() {
        // Create first update command
        Map<String, Object> changes1 = new HashMap<>();
        changes1.put("title", "Original Title");
        changes1.put("status", TaskStatus.pending);
        
        UpdateTaskCommand cmd1 = UpdateTaskCommand.create(testTaskId, testUserId, changes1);
        commandQueue.enqueue(cmd1);
        
        assertEquals(1, commandQueue.getPendingCommandCount());
        
        // Create second update command for same task
        Map<String, Object> changes2 = new HashMap<>();
        changes2.put("description", "New Description");
        changes2.put("status", TaskStatus.in_progress);
        
        UpdateTaskCommand cmd2 = UpdateTaskCommand.create(testTaskId, testUserId, changes2);
        commandQueue.enqueue(cmd2);
        
        // Should still have only 1 command with merged fields
        assertEquals(1, commandQueue.getPendingCommandCount(), 
            "Commands should be merged when updating same task");
        
        Command mergedCommand = commandQueue.getPendingCommands().get(0);
        assertTrue(mergedCommand instanceof UpdateTaskCommand);
        
        UpdateTaskCommand merged = (UpdateTaskCommand) mergedCommand;
        Map<String, Object> mergedFields = merged.changedFields();
        
        // Should contain all fields from both commands
        assertEquals("Original Title", mergedFields.get("title"));
        assertEquals("New Description", mergedFields.get("description"));
        assertEquals(TaskStatus.in_progress, mergedFields.get("status")); // Latest value wins
    }
    
    @Test
    @DisplayName("Should not merge CREATE and UPDATE commands")
    void shouldNotMergeCreateAndUpdateCommands() {
        // Create a CREATE command
        CreateTaskCommand createCmd = CreateTaskCommand.create(
            testTaskId, testUserId, "Test Task", "Description", 
            TaskStatus.pending, LocalDateTime.now(), "folder1"
        );
        commandQueue.enqueue(createCmd);
        
        // Create an UPDATE command for same task
        Map<String, Object> changes = new HashMap<>();
        changes.put("title", "Updated Title");
        
        UpdateTaskCommand updateCmd = UpdateTaskCommand.create(testTaskId, testUserId, changes);
        commandQueue.enqueue(updateCmd);
        
        // Should have 2 separate commands
        assertEquals(2, commandQueue.getPendingCommandCount(),
            "CREATE and UPDATE commands should not be merged");
    }
    
    @Test
    @DisplayName("Should not merge UPDATE commands for different tasks")
    void shouldNotMergeUpdateCommandsForDifferentTasks() {
        // Create update for first task
        Map<String, Object> changes1 = new HashMap<>();
        changes1.put("title", "Task 1 Title");
        
        UpdateTaskCommand cmd1 = UpdateTaskCommand.create("task1", testUserId, changes1);
        commandQueue.enqueue(cmd1);
        
        // Create update for second task
        Map<String, Object> changes2 = new HashMap<>();
        changes2.put("title", "Task 2 Title");
        
        UpdateTaskCommand cmd2 = UpdateTaskCommand.create("task2", testUserId, changes2);
        commandQueue.enqueue(cmd2);
        
        // Should have 2 separate commands
        assertEquals(2, commandQueue.getPendingCommandCount(),
            "UPDATE commands for different tasks should not be merged");
    }
    
    @Test
    @DisplayName("Should preserve command order when merging")
    void shouldPreserveCommandOrderWhenMerging() {
        // Create commands for different tasks interspersed with same task
        Map<String, Object> changes1 = new HashMap<>();
        changes1.put("title", "Task A - Update 1");
        commandQueue.enqueue(UpdateTaskCommand.create("taskA", testUserId, changes1));
        
        Map<String, Object> changes2 = new HashMap<>();
        changes2.put("title", "Task B - Update 1");
        commandQueue.enqueue(UpdateTaskCommand.create("taskB", testUserId, changes2));
        
        Map<String, Object> changes3 = new HashMap<>();
        changes3.put("description", "Task A - Update 2");
        commandQueue.enqueue(UpdateTaskCommand.create("taskA", testUserId, changes3));
        
        assertEquals(2, commandQueue.getPendingCommandCount(),
            "Should have 2 commands after merging taskA updates");
        
        // Verify the merged command for taskA contains both changes
        Command taskACommand = commandQueue.getPendingCommands().stream()
            .filter(cmd -> cmd.getEntityId().equals("taskA"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(taskACommand);
        assertTrue(taskACommand instanceof UpdateTaskCommand);
        
        UpdateTaskCommand merged = (UpdateTaskCommand) taskACommand;
        Map<String, Object> mergedFields = merged.changedFields();
        
        assertEquals("Task A - Update 1", mergedFields.get("title"));
        assertEquals("Task A - Update 2", mergedFields.get("description"));
    }
    
    @Test
    @DisplayName("Should handle empty change fields gracefully")
    void shouldHandleEmptyChangeFieldsGracefully() {
        Map<String, Object> emptyChanges = new HashMap<>();
        
        UpdateTaskCommand cmd = UpdateTaskCommand.create(testTaskId, testUserId, emptyChanges);
        commandQueue.enqueue(cmd);
        
        assertEquals(1, commandQueue.getPendingCommandCount());
        
        // Add another update with actual changes
        Map<String, Object> realChanges = new HashMap<>();
        realChanges.put("title", "Real Title");
        
        UpdateTaskCommand cmd2 = UpdateTaskCommand.create(testTaskId, testUserId, realChanges);
        commandQueue.enqueue(cmd2);
        
        assertEquals(1, commandQueue.getPendingCommandCount(),
            "Commands should be merged even when one has empty changes");
        
        Command merged = commandQueue.getPendingCommands().get(0);
        assertTrue(merged instanceof UpdateTaskCommand);
        
        UpdateTaskCommand mergedUpdate = (UpdateTaskCommand) merged;
        assertEquals("Real Title", mergedUpdate.changedFields().get("title"));
    }
}
