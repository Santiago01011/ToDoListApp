package model.commands;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Command for soft-deleting a task.
 * The task is marked as deleted but not physically removed from the database.
 */
public record DeleteTaskCommand(
    @JsonProperty("commandId") String commandId,
    @JsonProperty("entityId") String entityId,
    @JsonProperty("userId") String userId,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("reason") String reason // Optional reason for deletion
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
        return CommandType.DELETE_TASK;
    }
    
    /**
     * Factory method to create a DeleteTaskCommand with generated ID and timestamp
     */
    public static DeleteTaskCommand create(String entityId, String userId, String reason) {
        return new DeleteTaskCommand(
            java.util.UUID.randomUUID().toString(),
            entityId,
            userId,
            LocalDateTime.now(),
            reason
        );
    }
    
    /**
     * Factory method to create a DeleteTaskCommand without reason
     */
    public static DeleteTaskCommand create(String entityId, String userId) {
        return create(entityId, userId, null);
    }
}
