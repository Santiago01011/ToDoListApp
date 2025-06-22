package model.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import model.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TaskHandlerV2 with user-specific storage and command queue.
 */
@DisplayName("TaskHandlerV2 Integration Tests")
class TaskHandlerV2Test {

    @TempDir
    Path tempDir;
    
    private String originalUserHome;
    private String testUserId = "test-user";
    private TaskHandlerV2 taskHandler;

    @BeforeEach
    void setUp() {
        // Store original user.home and set temporary directory
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
        
        // Create TaskHandlerV2 with command queue enabled
        taskHandler = new TaskHandlerV2(testUserId, true);
    }

    @AfterEach
    void tearDown() {
        // Restore original user.home
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    @DisplayName("Should create task and generate command")
    void testCreateTaskWithCommand() {
        String title = "Test Task";
        String description = "Test Description";
        TaskStatus status = TaskStatus.pending;
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        String folderId = "test-folder";
        
        Task createdTask = taskHandler.createTask(title, description, status, dueDate, folderId);
        
        assertNotNull(createdTask);
        assertEquals(title, createdTask.getTitle());
        assertEquals(description, createdTask.getDescription());
        assertEquals(status, createdTask.getStatus());
        assertEquals(folderId, createdTask.getFolder_id());
        
        // Verify command was created
        CommandQueue commandQueue = taskHandler.getCommandQueue();
        assertEquals(1, commandQueue.getPendingCommandCount());
        assertTrue(commandQueue.hasPendingCommands());
        
        List<Command> commands = commandQueue.getPendingCommands();
        assertEquals(CommandType.CREATE_TASK, commands.get(0).getType());
        assertEquals(createdTask.getTask_id(), commands.get(0).getEntityId());
    }

    @Test
    @DisplayName("Should retrieve all tasks including local changes")
    void testGetAllTasksWithLocalChanges() {
        // Create some tasks
        Task task1 = taskHandler.createTask("Task 1", "Description 1", TaskStatus.pending, null, "folder1");
        Task task2 = taskHandler.createTask("Task 2", "Description 2", TaskStatus.in_progress, null, "folder2");
        
        List<Task> allTasks = taskHandler.getAllTasks();
        
        assertEquals(2, allTasks.size());
        
        // Verify tasks are in the list
        boolean foundTask1 = false;
        boolean foundTask2 = false;
        
        for (Task task : allTasks) {
            if (task.getTask_id().equals(task1.getTask_id())) {
                foundTask1 = true;
                assertEquals("Task 1", task.getTitle());
            } else if (task.getTask_id().equals(task2.getTask_id())) {
                foundTask2 = true;
                assertEquals("Task 2", task.getTitle());
            }
        }
        
        assertTrue(foundTask1, "Task 1 should be found in getAllTasks");
        assertTrue(foundTask2, "Task 2 should be found in getAllTasks");
    }

    @Test
    @DisplayName("Should handle user isolation correctly")
    void testUserIsolation() {
        // Create task with first user
        Task task1 = taskHandler.createTask("User 1 Task", "Description", TaskStatus.pending, null, "folder");
        
        // Create second TaskHandlerV2 for different user
        TaskHandlerV2 taskHandler2 = new TaskHandlerV2("different-user", true);
        Task task2 = taskHandler2.createTask("User 2 Task", "Description", TaskStatus.pending, null, "folder");
        
        // Verify isolation
        List<Task> user1Tasks = taskHandler.getAllTasks();
        List<Task> user2Tasks = taskHandler2.getAllTasks();
        
        assertEquals(1, user1Tasks.size());
        assertEquals(1, user2Tasks.size());
          assertEquals("User 1 Task", user1Tasks.get(0).getTitle());
        assertEquals("User 2 Task", user2Tasks.get(0).getTitle());
        
        // Verify command queue isolation
        assertEquals(1, taskHandler.getCommandQueue().getPendingCommandCount());
        assertEquals(1, taskHandler2.getCommandQueue().getPendingCommandCount());
        
        assertNotEquals(
            taskHandler.getCommandQueue().getPendingCommands().get(0).getEntityId(),
            taskHandler2.getCommandQueue().getPendingCommands().get(0).getEntityId()
        );
    }

    @Test
    @DisplayName("Should persist and reload data correctly")
    void testDataPersistenceAndReload() {
        // Create a task
        Task originalTask = taskHandler.createTask("Persistent Task", "Description", TaskStatus.pending, null, "folder");
        
        assertEquals(1, taskHandler.getAllTasks().size());
        assertEquals(1, taskHandler.getCommandQueue().getPendingCommandCount());
        
        // Create new TaskHandlerV2 instance for same user - should reload data
        TaskHandlerV2 reloadedHandler = new TaskHandlerV2(testUserId, true);
        
        List<Task> reloadedTasks = reloadedHandler.getAllTasks();
        assertEquals(1, reloadedTasks.size());
        assertEquals("Persistent Task", reloadedTasks.get(0).getTitle());
        
        // Verify command queue was also reloaded
        assertEquals(1, reloadedHandler.getCommandQueue().getPendingCommandCount());
        
        List<Command> reloadedCommands = reloadedHandler.getCommandQueue().getPendingCommands();
        assertEquals(originalTask.getTask_id(), reloadedCommands.get(0).getEntityId());
    }

    @Test
    @DisplayName("Should handle command queue operations correctly")
    void testCommandQueueOperations() {
        // Create multiple tasks to generate commands
        taskHandler.createTask("Task 1", "Description 1", TaskStatus.pending, null, "folder1");
        taskHandler.createTask("Task 2", "Description 2", TaskStatus.in_progress, null, "folder2");
        taskHandler.createTask("Task 3", "Description 3", TaskStatus.completed, null, "folder3");
        
        CommandQueue commandQueue = taskHandler.getCommandQueue();
        assertEquals(3, commandQueue.getPendingCommandCount());
        
        List<Command> commands = commandQueue.getPendingCommands();
        assertEquals(3, commands.size());
        
        // All should be CREATE_TASK commands
        for (Command command : commands) {
            assertEquals(CommandType.CREATE_TASK, command.getType());
            assertEquals(testUserId, command.getUserId());
        }
        
        // Clear commands
        commandQueue.clearCommands();
        assertEquals(0, commandQueue.getPendingCommandCount());
        assertFalse(commandQueue.hasPendingCommands());
    }

    @Test
    @DisplayName("Should handle invalid user ID gracefully")
    void testInvalidUserId() {
        assertThrows(IllegalArgumentException.class, () -> new TaskHandlerV2(null, true));
        assertThrows(IllegalArgumentException.class, () -> new TaskHandlerV2("", true));
    }

    @Test
    @DisplayName("Should work without command queue if disabled")
    void testWithoutCommandQueue() {
        // Create TaskHandlerV2 with command queue disabled
        TaskHandlerV2 handlerWithoutQueue = new TaskHandlerV2(testUserId, false);
        
        // Should still be able to create tasks
        Task task = handlerWithoutQueue.createTask("Task", "Description", TaskStatus.pending, null, "folder");
        assertNotNull(task);
        
        List<Task> tasks = handlerWithoutQueue.getAllTasks();
        assertEquals(1, tasks.size());
        
        // Command queue should be null or have no commands
        CommandQueue queue = handlerWithoutQueue.getCommandQueue();
        if (queue != null) {
            assertEquals(0, queue.getPendingCommandCount());
        }
    }
}
