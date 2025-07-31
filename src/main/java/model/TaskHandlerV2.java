package model;

import model.commands.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Next-generation TaskHandler that implements the Command Queue pattern
 * for robust offline-first operation and conflict-free synchronization.
 * 
 * This class provides backward compatibility with the existing TaskHandler
 * while introducing the new command-driven architecture.
 */
public class TaskHandlerV2 {
    private final TaskHandler legacyHandler;
    private final CommandQueue commandQueue;
    private final boolean useCommandQueue;
    private final String userId;
    
    /**
     * Create a new TaskHandlerV2 instance
     * 
     * @param userId The current user's ID
     * @param useCommandQueue Whether to enable the new command queue pattern (feature flag)
     */    public TaskHandlerV2(String userId, boolean useCommandQueue) {
        this.userId = userId;
        this.useCommandQueue = useCommandQueue;
        this.legacyHandler = new TaskHandler(userId); // Pass userId to legacy handler
        this.commandQueue = new CommandQueue(userId);
    }
    
    /**
     * Create a new TaskHandlerV2 with command queue enabled by default
     */
    public TaskHandlerV2(String userId) {
        this(userId, true);
    }
    
    /**
     * Get all tasks for the current user.
     * If command queue is enabled, returns the projected state with pending commands applied.
     */
    public List<Task> getAllTasks() {
        if (useCommandQueue) {
            List<Task> baseTasks = legacyHandler.userTasksList;
            return commandQueue.getProjectedTasks(baseTasks);
        } else {
            return legacyHandler.userTasksList;
        }
    }
    
    /**
     * Create a new task
     */
    public Task createTask(String title, String description, TaskStatus status, 
                          LocalDateTime dueDate, String folderId) {
        String taskId = UUID.randomUUID().toString();
        
        if (useCommandQueue) {
            // New path: Create command
            CreateTaskCommand command = CreateTaskCommand.create(
                taskId, userId, title, description, status, dueDate, folderId
            );
            commandQueue.enqueue(command);
            
            // Return the task as it would appear after the command is applied
            return new Task.Builder(taskId)
                .taskTitle(title)
                .description(description)
                .status(status)
                .sync_status("pending")
                .dueDate(dueDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .folderId(folderId)
                .build();
        } else {
            // Legacy path: Direct creation
            Task newTask = new Task.Builder(taskId)
                .taskTitle(title)
                .description(description)
                .status(status)
                .sync_status("new")
                .dueDate(dueDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .folderId(folderId)
                .build();
            
            legacyHandler.userTasksList.add(newTask);
            legacyHandler.saveTasksToJson();
            return newTask;
        }
    }
    
    /**
     * Update an existing task
     */
    public void updateTask(Task task, String title, String description, TaskStatus status, 
                          LocalDateTime dueDate, String folderName) {
        if (useCommandQueue) {
            // New path: Create update command
            Map<String, Object> changes = buildChangeMap(title, description, status, dueDate, folderName);
            if (!changes.isEmpty()) {
                UpdateTaskCommand command = UpdateTaskCommand.create(
                    task.getTask_id(), userId, changes
                );
                commandQueue.enqueue(command);
            }
        } else {
            // Legacy path: Direct update
            legacyHandler.updateTask(task, title, description, status, dueDate, folderName, null);
            legacyHandler.saveTasksToJson();
        }
    }
    
    /**
     * Delete a task (soft delete)
     */
    public void deleteTask(Task task) {
        deleteTask(task, "User deleted");
    }
    
    /**
     * Delete a task with a reason (soft delete)
     */
    public void deleteTask(Task task, String reason) {
        if (useCommandQueue) {
            // New path: Create delete command
            DeleteTaskCommand command = DeleteTaskCommand.create(
                task.getTask_id(), userId, reason
            );
            commandQueue.enqueue(command);
        } else {
            // Legacy path: Direct deletion
            legacyHandler.updateTask(task, null, null, null, null, null, LocalDateTime.now());
            legacyHandler.saveTasksToJson();
        }
    }
    
    /**
     * Get the count of pending commands waiting for synchronization
     */
    public int getPendingChangesCount() {
        if (useCommandQueue) {
            return commandQueue.getPendingCommandCount();
        } else {
            // Legacy: Count shadow updates (approximate)
            return legacyHandler.userTasksList.stream()
                .mapToInt(task -> "local".equals(task.getSync_status()) || "to_update".equals(task.getSync_status()) ? 1 : 0)
                .sum();
        }
    }
    
    /**
     * Check if there are any pending changes
     */
    public boolean hasPendingChanges() {
        return getPendingChangesCount() > 0;
    }
    
    /**
     * Get the command queue for advanced operations (sync, etc.)
     */
    public CommandQueue getCommandQueue() {
        return commandQueue;
    }
    
    /**
     * Get the legacy handler for backward compatibility
     */
    public TaskHandler getLegacyHandler() {
        return legacyHandler;
    }
    
    /**
     * Save tasks to JSON (legacy compatibility)
     */
    public void saveTasksToJson() {
        if (!useCommandQueue) {
            legacyHandler.saveTasksToJson();
        }
        // Note: In command queue mode, persistence happens automatically
        // when commands are enqueued
    }
    
    /**
     * Build a map of changed fields for update commands
     */
    private Map<String, Object> buildChangeMap(String title, String description, 
                                             TaskStatus status, LocalDateTime dueDate, String folderName) {
        Map<String, Object> changes = new HashMap<>();
        
        if (title != null && !title.trim().isEmpty()) {
            changes.put("title", title);
        }
        if (description != null) {
            changes.put("description", description);
        }
        if (status != null) {
            changes.put("status", status);
        }
        if (dueDate != null) {
            changes.put("dueDate", dueDate);
        }
        if (folderName != null) {
            changes.put("folderId", folderName); // Note: This should be resolved to folder ID
        }
        
        return changes;
    }
    
    /**
     * Get the user ID for this handler
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Check if command queue mode is enabled
     */
    public boolean isCommandQueueEnabled() {
        return useCommandQueue;
    }
    
    /**
     * Get last sync time
     */
    public LocalDateTime getLastSync() {
        return legacyHandler.getLastSync();
    }
    
    /**
     * Set last sync time
     */
    public void setLastSync(LocalDateTime lastSync) {
        legacyHandler.setLastSync(lastSync);
    }
    
    /**
     * Set folders list
     */
    public void setFoldersList(List<Folder> foldersList) {
        legacyHandler.setFoldersList(foldersList);
    }
}
