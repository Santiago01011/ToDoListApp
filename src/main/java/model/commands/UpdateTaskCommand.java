package model.commands;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Command for updating an existing task.
 * Only contains the fields that were actually changed, enabling field-level merging.
 */
public record UpdateTaskCommand(
    @JsonProperty("commandId") String commandId,
    @JsonProperty("entityId") String entityId,
    @JsonProperty("userId") String userId,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("changedFields") Map<String, Object> changedFields
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
        return CommandType.UPDATE_TASK;
    }
    
    /**
     * Factory method to create an UpdateTaskCommand with generated ID and timestamp
     */
    public static UpdateTaskCommand create(
            String entityId, 
            String userId, 
            Map<String, Object> changedFields) {
        return new UpdateTaskCommand(
            java.util.UUID.randomUUID().toString(),
            entityId,
            userId,
            LocalDateTime.now(),
            new HashMap<>(changedFields) // Defensive copy
        );
    }
    
    /**
     * Builder pattern for easier command construction
     */
    public static class Builder {
        private final String entityId;
        private final String userId;
        private final Map<String, Object> changedFields = new HashMap<>();
        
        public Builder(String entityId, String userId) {
            this.entityId = entityId;
            this.userId = userId;
        }
        
        public Builder title(String title) {
            changedFields.put("title", title);
            return this;
        }
        
        public Builder description(String description) {
            changedFields.put("description", description);
            return this;
        }
        
        public Builder status(model.TaskStatus status) {
            changedFields.put("status", status);
            return this;
        }
        
        public Builder dueDate(LocalDateTime dueDate) {
            changedFields.put("dueDate", dueDate);
            return this;
        }
        
        public Builder folderId(String folderId) {
            changedFields.put("folderId", folderId);
            return this;
        }
        
        public UpdateTaskCommand build() {
            return UpdateTaskCommand.create(entityId, userId, changedFields);
        }
    }
}
