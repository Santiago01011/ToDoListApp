package model.commands;

import java.time.LocalDateTime;
import model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Command for creating a new task.
 * Represents the complete initial state of the task.
 */
public record CreateTaskCommand(
    @JsonProperty("commandId") String commandId,
    @JsonProperty("entityId") String entityId,
    @JsonProperty("userId") String userId,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("title") String title,
    @JsonProperty("description") String description,
    @JsonProperty("status") TaskStatus status,
    @JsonProperty("dueDate") LocalDateTime dueDate,
    @JsonProperty("folderId") String folderId
) implements Command {
    
    // Implement Command interface methods by delegating to record components
    @Override
    public String getCommandId() { return commandId; }
    
    @Override
    public String getEntityId() { return entityId; }
    
    @Override
    public String getUserId() { return userId; }
    
    @Override
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public CommandType getType() {
        return CommandType.CREATE_TASK;
    }
    
    /**
     * Factory method to create a CreateTaskCommand with generated ID and timestamp
     */
    public static CreateTaskCommand create(
            String entityId, 
            String userId, 
            String title, 
            String description, 
            TaskStatus status, 
            LocalDateTime dueDate, 
            String folderId) {
        return new CreateTaskCommand(
            java.util.UUID.randomUUID().toString(),
            entityId,
            userId,
            LocalDateTime.now(),
            title,
            description,
            status,
            dueDate,
            folderId
        );
    }
}
