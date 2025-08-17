package model.commands;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.nio.file.*;

import model.Task;
import model.TaskStatus;
import COMMON.JSONUtils;
import COMMON.UserProperties;

/**
 * Central command queue that manages all pending commands for offline-first operation.
 * This class handles:
 * - Queuing commands when offline
 * - Projecting current state by applying commands to base data
 * - Synchronizing commands with the server
 * - Persisting commands to disk for reliability
 */
public class CommandQueue {
    private final List<Command> pendingCommands = new CopyOnWriteArrayList<>();
    private final String userId;
    private final String commandsFilePath;
    
    public CommandQueue(String userId) {
        this.userId = userId;
        this.commandsFilePath = UserProperties.getUserDataFilePath(userId, "pending_commands.json");
        loadFromFile();
    }
    
    /**
     * Add a command to the queue for later synchronization
     */
    public void enqueue(Command command) {
        if (!command.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Command user ID does not match queue user ID");
        }
        
        pendingCommands.add(command);
        persistToFile();
        
        // TODO: Trigger async sync attempt if online
        System.out.println("Command enqueued: " + command.getType() + " for entity " + command.getEntityId());
    }
    
    /**
     * Get the current projected state of tasks by applying all pending commands
     * to the base task data received from the server.
     */
    public List<Task> getProjectedTasks(List<Task> baseTasks) {
        if (pendingCommands.isEmpty()) {
            return new ArrayList<>(baseTasks);
        }
        
        // Create a map for efficient lookups and updates
        Map<String, Task> taskMap = baseTasks.stream()
            .collect(Collectors.toMap(Task::getTask_id, Function.identity()));
        
        // Apply each command in chronological order
        for (Command cmd : pendingCommands) {
            taskMap = applyCommand(taskMap, cmd);
        }
        
        return taskMap.values().stream()
            .filter(task -> task.getDeleted_at() == null)
            .collect(Collectors.toList());
    }
    
    /**
     * Apply a single command to the task map
     */
    private Map<String, Task> applyCommand(Map<String, Task> taskMap, Command cmd) {
        Map<String, Task> result = new HashMap<>(taskMap);
          switch (cmd.getType()) {
            case CREATE_TASK -> {
                CreateTaskCommand createCmd = (CreateTaskCommand) cmd;
                Task newTask = createTaskFromCommand(createCmd);
                result.put(newTask.getTask_id(), newTask);
            }
            case UPDATE_TASK -> {
                UpdateTaskCommand updateCmd = (UpdateTaskCommand) cmd;
                Task existingTask = result.get(updateCmd.getEntityId());
                if (existingTask != null) {
                    Task updatedTask = applyUpdatesToTask(existingTask, updateCmd);
                    result.put(updatedTask.getTask_id(), updatedTask);
                }
            }
            case DELETE_TASK -> {
                DeleteTaskCommand deleteCmd = (DeleteTaskCommand) cmd;
                Task existingTask = result.get(deleteCmd.getEntityId());
                if (existingTask != null) {
                    // Soft delete: set deleted_at timestamp
                    Task deletedTask = createDeletedTask(existingTask, deleteCmd.getTimestamp());
                    result.put(deletedTask.getTask_id(), deletedTask);
                }
            }
            default -> {
                // Future command types (appointments, financial entries) not yet implemented
                System.out.println("Unsupported command type: " + cmd.getType());
            }
        }
        
        return result;
    }
      /**
     * Create a new Task from a CreateTaskCommand
     */
    private Task createTaskFromCommand(CreateTaskCommand cmd) {
        return new Task.Builder(cmd.getEntityId())
            .taskTitle(cmd.title())
            .description(cmd.description())
            .status(cmd.status())
            .sync_status("pending") // Mark as pending sync
            .dueDate(cmd.dueDate())
            .createdAt(cmd.getTimestamp())
            .updatedAt(cmd.getTimestamp())
            .deletedAt(null)
            .lastSync(null)
            .folderId(cmd.folderId())
            .folderName(null) // Will be resolved server-side
            .build();
    }
      /**
     * Create a soft-deleted copy of an existing task
     */
    private Task createDeletedTask(Task existingTask, LocalDateTime deleteTimestamp) {
        return existingTask.toBuilder()
            .updatedAt(deleteTimestamp)
            .deletedAt(deleteTimestamp)
            .sync_status("pending")
            .build();
    }
    
    /**
     * Apply updates from an UpdateTaskCommand to an existing task
     */    private Task applyUpdatesToTask(Task existingTask, UpdateTaskCommand cmd) {
        Task.Builder builder = existingTask.toBuilder()
            .updatedAt(cmd.getTimestamp())
            .sync_status("pending");
        
        Map<String, Object> changes = cmd.changedFields();
        
        if (changes.containsKey("title")) {
            builder.taskTitle((String) changes.get("title"));
        }
        if (changes.containsKey("description")) {
            builder.description((String) changes.get("description"));
        }
        if (changes.containsKey("status")) {
            Object statusValue = changes.get("status");
            if (statusValue instanceof String) {
                builder.status(TaskStatus.valueOf((String) statusValue));
            } else if (statusValue instanceof TaskStatus) {
                builder.status((TaskStatus) statusValue);
            }
        }
        if (changes.containsKey("dueDate")) {
            Object dueDateValue = changes.get("dueDate");
            if (dueDateValue instanceof String) {
                builder.dueDate(LocalDateTime.parse((String) dueDateValue));
            } else if (dueDateValue instanceof LocalDateTime) {
                builder.dueDate((LocalDateTime) dueDateValue);
            }
        }
        if (changes.containsKey("folderId")) {
            builder.folderId((String) changes.get("folderId"));
        }
        
        return builder.build();
    }
    
    /**
     * Get all pending commands (for synchronization)
     */
    public List<Command> getPendingCommands() {
        return new ArrayList<>(pendingCommands);
    }
    
    /**
     * Clear all commands after successful synchronization
     */
    public void clearCommands() {
        pendingCommands.clear();
        persistToFile();
        System.out.println("Command queue cleared after successful sync");
    }
    
    /**
     * Remove specific commands by their IDs (partial sync success)
     */
    public void removeCommands(Set<String> commandIds) {
        pendingCommands.removeIf(cmd -> commandIds.contains(cmd.getCommandId()));
        persistToFile();
        System.out.println("Removed " + commandIds.size() + " commands from queue");
    }
    
    /**
     * Check if there are any pending commands
     */
    public boolean hasPendingCommands() {
        return !pendingCommands.isEmpty();
    }
    
    /**
     * Get the count of pending commands
     */
    public int getPendingCommandCount() {
        return pendingCommands.size();
    }    /**
     * Persist commands to disk for offline reliability
     */
    private void persistToFile() {
        try {
            String jsonData = CommandSerializer.serialize(pendingCommands);
            Files.writeString(Paths.get(commandsFilePath), jsonData);
        } catch (Exception e) {
            System.err.println("Failed to persist commands to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load commands from disk on startup
     */
    private void loadFromFile() {
        try {
            Path commandsPath = Paths.get(commandsFilePath);
            if (Files.exists(commandsPath)) {
                String jsonData = Files.readString(commandsPath);
                if (!jsonData.trim().isEmpty()) {
                    List<Command> loadedCommands = CommandSerializer.deserialize(jsonData);
                    pendingCommands.addAll(loadedCommands);
                    System.out.println("Loaded " + pendingCommands.size() + " commands from disk for user: " + userId);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load commands from file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
