package model.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import model.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Command Queue functionality.
 * Tests command creation, execution, persistence, and user isolation.
 */
@DisplayName("Command Queue Tests")
class CommandQueueTest {

    @TempDir
    Path tempDir;
    
    private String originalUserHome;
    private String testUserId1 = "test-user-1";
    private String testUserId2 = "test-user-2";
    private CommandQueue commandQueue1;
    private CommandQueue commandQueue2;

    @BeforeEach
    void setUp() {
        // Store original user.home and set temporary directory
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
        
        // Create command queues for different users
        commandQueue1 = new CommandQueue(testUserId1);
        commandQueue2 = new CommandQueue(testUserId2);
    }

    @AfterEach
    void tearDown() {
        // Restore original user.home
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    @DisplayName("Should create empty command queue for new user")
    void testEmptyCommandQueue() {
        CommandQueue newQueue = new CommandQueue("new-user");
        
        assertEquals(0, newQueue.getPendingCommandCount());
        assertTrue(newQueue.getPendingCommands().isEmpty());
        assertFalse(newQueue.hasPendingCommands());
    }

    @Test
    @DisplayName("Should add and track create task commands")
    void testCreateTaskCommand() {
        Task task = createSampleTask();        CreateTaskCommand command = CreateTaskCommand.create(
            task.getTask_id(), 
            testUserId1, 
            task.getTitle(), 
            task.getDescription(), 
            task.getStatus(), 
            task.getDue_date(), 
            task.getFolder_id()
        );
        
        commandQueue1.enqueue(command);
        
        assertEquals(1, commandQueue1.getPendingCommandCount());
        assertTrue(commandQueue1.hasPendingCommands());
        
        List<Command> commands = commandQueue1.getPendingCommands();
        assertEquals(1, commands.size());
        assertEquals(CommandType.CREATE_TASK, commands.get(0).getType());
        assertEquals(task.getTask_id(), commands.get(0).getEntityId());
    }

    @Test
    @DisplayName("Should maintain command order and timestamps")
    void testCommandOrderAndTimestamps() throws InterruptedException {
        Task task1 = createSampleTask();
        Task task2 = createSampleTask();
          CreateTaskCommand command1 = CreateTaskCommand.create(
            task1.getTask_id(), testUserId1, task1.getTitle(), 
            task1.getDescription(), task1.getStatus(), task1.getDue_date(), task1.getFolder_id()
        );
        Thread.sleep(10); // Ensure different timestamps
        CreateTaskCommand command2 = CreateTaskCommand.create(
            task2.getTask_id(), testUserId1, task2.getTitle(), 
            task2.getDescription(), task2.getStatus(), task2.getDue_date(), task2.getFolder_id()
        );
        
        commandQueue1.enqueue(command1);
        commandQueue1.enqueue(command2);
        
        List<Command> commands = commandQueue1.getPendingCommands();
        assertEquals(2, commands.size());
        
        // Commands should be in order of addition
        assertEquals(task1.getTask_id(), commands.get(0).getEntityId());
        assertEquals(task2.getTask_id(), commands.get(1).getEntityId());
        
        // Second command should have later timestamp
        assertTrue(commands.get(1).getTimestamp().isAfter(commands.get(0).getTimestamp()));
    }

    @Test
    @DisplayName("Should persist commands to user-specific file")
    void testCommandPersistence() throws IOException {
        Task task = createSampleTask();
        CreateTaskCommand command = CreateTaskCommand.create(
            task.getTask_id(), testUserId1, task.getTitle(), 
            task.getDescription(), task.getStatus(), task.getDue_date(), task.getFolder_id()
        );
        
        commandQueue1.enqueue(command);
        
        // Verify file exists in correct user directory
        String expectedPath = tempDir.resolve(".todoapp").resolve("users")
                                    .resolve(testUserId1).resolve("pending_commands.json").toString();
        File commandFile = new File(expectedPath);
        
        assertTrue(commandFile.exists());
        assertTrue(commandFile.length() > 0);
        
        // Verify file content is valid JSON
        String content = Files.readString(commandFile.toPath());
        assertFalse(content.trim().isEmpty());
        assertTrue(content.contains(task.getTask_id()));
    }

    @Test
    @DisplayName("Should load persisted commands on initialization")
    void testCommandLoading() {
        Task task = createSampleTask();
        CreateTaskCommand command = CreateTaskCommand.create(
            task.getTask_id(), testUserId1, task.getTitle(), 
            task.getDescription(), task.getStatus(), task.getDue_date(), task.getFolder_id()
        );
        
        // Add command to first queue instance
        commandQueue1.enqueue(command);
        assertEquals(1, commandQueue1.getPendingCommandCount());
        
        // Create new queue instance for same user - should load persisted commands
        CommandQueue reloadedQueue = new CommandQueue(testUserId1);
        assertEquals(1, reloadedQueue.getPendingCommandCount());
        
        List<Command> commands = reloadedQueue.getPendingCommands();
        assertEquals(task.getTask_id(), commands.get(0).getEntityId());
        assertEquals(CommandType.CREATE_TASK, commands.get(0).getType());
    }

    @Test
    @DisplayName("Should isolate commands between different users")
    void testUserCommandIsolation() {
        Task task1 = createSampleTask();
        Task task2 = createSampleTask();
        
        CreateTaskCommand command1 = CreateTaskCommand.create(
            task1.getTask_id(), testUserId1, task1.getTitle(), 
            task1.getDescription(), task1.getStatus(), task1.getDue_date(), task1.getFolder_id()
        );
        CreateTaskCommand command2 = CreateTaskCommand.create(
            task2.getTask_id(), testUserId2, task2.getTitle(), 
            task2.getDescription(), task2.getStatus(), task2.getDue_date(), task2.getFolder_id()
        );
        
        // Add commands to different user queues
        commandQueue1.enqueue(command1);
        commandQueue2.enqueue(command2);
        
        // Verify isolation
        assertEquals(1, commandQueue1.getPendingCommandCount());
        assertEquals(1, commandQueue2.getPendingCommandCount());
        
        List<Command> commands1 = commandQueue1.getPendingCommands();
        List<Command> commands2 = commandQueue2.getPendingCommands();
        
        assertEquals(task1.getTask_id(), commands1.get(0).getEntityId());
        assertEquals(task2.getTask_id(), commands2.get(0).getEntityId());
        assertNotEquals(commands1.get(0).getEntityId(), commands2.get(0).getEntityId());
    }

    @Test
    @DisplayName("Should clear all commands")
    void testClearCommands() {
        // Add several commands
        Task task1 = createSampleTask();
        Task task2 = createSampleTask();
        
        CreateTaskCommand command1 = CreateTaskCommand.create(
            task1.getTask_id(), testUserId1, task1.getTitle(), 
            task1.getDescription(), task1.getStatus(), task1.getDue_date(), task1.getFolder_id()
        );
        CreateTaskCommand command2 = CreateTaskCommand.create(
            task2.getTask_id(), testUserId1, task2.getTitle(), 
            task2.getDescription(), task2.getStatus(), task2.getDue_date(), task2.getFolder_id()
        );
        
        commandQueue1.enqueue(command1);
        commandQueue1.enqueue(command2);
        
        assertEquals(2, commandQueue1.getPendingCommandCount());
        assertTrue(commandQueue1.hasPendingCommands());
        
        // Clear commands
        commandQueue1.clearCommands();
        
        assertEquals(0, commandQueue1.getPendingCommandCount());
        assertFalse(commandQueue1.hasPendingCommands());
        assertTrue(commandQueue1.getPendingCommands().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user1", "user-test", "user_123", "CAPS_USER"})
    @DisplayName("Should work with various user ID formats")
    void testVariousUserIdFormats(String userId) {
        CommandQueue queue = new CommandQueue(userId);
        Task task = createSampleTask();
        CreateTaskCommand command = CreateTaskCommand.create(
            task.getTask_id(), userId, task.getTitle(), 
            task.getDescription(), task.getStatus(), task.getDue_date(), task.getFolder_id()
        );
        
        queue.enqueue(command);
        
        assertEquals(1, queue.getPendingCommandCount());
        assertTrue(queue.hasPendingCommands());
        
        // Verify persistence works for all user ID formats
        CommandQueue reloadedQueue = new CommandQueue(userId);
        assertEquals(1, reloadedQueue.getPendingCommandCount());
    }

    @Test
    @DisplayName("Should handle invalid user ID gracefully")
    void testInvalidUserId() {
        assertThrows(IllegalArgumentException.class, () -> new CommandQueue(null));
        assertThrows(IllegalArgumentException.class, () -> new CommandQueue(""));
    }

    @Test
    @DisplayName("Should handle file system errors gracefully")
    void testFileSystemErrorHandling() {
        // This test verifies that the queue handles missing directories gracefully
        CommandQueue queue = new CommandQueue("new-user-with-no-existing-data");
        
        // Should not throw exception even if no file exists yet
        assertEquals(0, queue.getPendingCommandCount());
        assertFalse(queue.hasPendingCommands());
        
        // Should be able to add commands even for new user
        Task task = createSampleTask();
        CreateTaskCommand command = CreateTaskCommand.create(
            task.getTask_id(), "new-user-with-no-existing-data", task.getTitle(), 
            task.getDescription(), task.getStatus(), task.getDue_date(), task.getFolder_id()
        );
        
        assertDoesNotThrow(() -> queue.enqueue(command));
        assertEquals(1, queue.getPendingCommandCount());
    }

    @Test
    @DisplayName("Should preserve command data integrity across save/load cycles")
    void testCommandDataIntegrity() {
        Task task = createSampleTask();
        CreateTaskCommand originalCommand = CreateTaskCommand.create(
            task.getTask_id(), testUserId1, task.getTitle(), 
            task.getDescription(), task.getStatus(), task.getDue_date(), task.getFolder_id()
        );
        
        commandQueue1.enqueue(originalCommand);
        
        // Reload queue
        CommandQueue reloadedQueue = new CommandQueue(testUserId1);
        List<Command> reloadedCommands = reloadedQueue.getPendingCommands();
        
        assertEquals(1, reloadedCommands.size());
        CreateTaskCommand reloadedCommand = (CreateTaskCommand) reloadedCommands.get(0);
        
        // Verify all data is preserved
        assertEquals(originalCommand.getEntityId(), reloadedCommand.getEntityId());
        assertEquals(originalCommand.getType(), reloadedCommand.getType());
        assertEquals(originalCommand.getUserId(), reloadedCommand.getUserId());
        assertEquals(originalCommand.title(), reloadedCommand.title());
        assertEquals(originalCommand.description(), reloadedCommand.description());
        assertEquals(originalCommand.status(), reloadedCommand.status());
        assertEquals(originalCommand.folderId(), reloadedCommand.folderId());
    }

    /**
     * Helper method to create a sample task for testing
     */
    private Task createSampleTask() {
        return new Task.Builder("task-" + System.currentTimeMillis())
            .taskTitle("Sample Task")
            .description("Sample Description")
            .status(TaskStatus.pending)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .folderId("sample-folder")
            .build();
    }
}
