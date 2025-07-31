package model.sync;

import model.Task;
import model.TaskStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating sync commands from task objects.
 * Handles the conversion of task data into command structures for API V2.
 */
public class CommandFactory {
    
    /**
     * Creates a CREATE command for a new task.
     */
    public static SyncCommand createTaskCommand(Task task) {
        Map<String, Object> data = new HashMap<>();
        data.put("task_title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("status", task.getStatus() != null ? task.getStatus().name() : TaskStatus.pending.name());
        data.put("due_date", task.getDue_date());
        data.put("folder_id", task.getFolder_id());
        data.put("folder_name", task.getFolder_name());
        data.put("created_at", task.getCreated_at());
        
        return new SyncCommand(
            "CREATE",
            "task",
            task.getTask_id(),
            task.getTask_id(), // client_id is same as task_id for new tasks
            LocalDateTime.now(),
            data
        );
    }
    
    /**
     * Creates an UPDATE command for an existing task.
     */
    public static SyncCommand updateTaskCommand(Task task) {
        Map<String, Object> data = new HashMap<>();
        
        // Only include fields that are not null (partial updates)
        if (task.getTitle() != null) {
            data.put("task_title", task.getTitle());
        }
        if (task.getDescription() != null) {
            data.put("description", task.getDescription());
        }
        if (task.getStatus() != null) {
            data.put("status", task.getStatus().name());
        }
        if (task.getDue_date() != null) {
            data.put("due_date", task.getDue_date());
        }
        if (task.getFolder_name() != null) {
            data.put("folder_name", task.getFolder_name());
        }
        if (task.getFolder_id() != null) {
            data.put("folder_id", task.getFolder_id());
        }
        
        data.put("updated_at", task.getUpdated_at());
        
        return new SyncCommand(
            "UPDATE",
            "task",
            task.getTask_id(),
            task.getTask_id(),
            LocalDateTime.now(),
            data
        );
    }
    
    /**
     * Creates a DELETE command for a task.
     */
    public static SyncCommand deleteTaskCommand(Task task) {
        Map<String, Object> data = new HashMap<>();
        data.put("deleted_at", task.getDeleted_at());
        
        return new SyncCommand(
            "DELETE",
            "task",
            task.getTask_id(),
            task.getTask_id(),
            LocalDateTime.now(),
            data
        );
    }
}