package model.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base sync command structure for API V2 command batch endpoint.
 * Represents an individual command operation (create, update, delete) for tasks.
 * Uses the NEW format with "type" field and "entityId".
 */
public class SyncCommand {
    @JsonProperty("entityId")  // Required UUID of the task
    private String entityId;
    
    @JsonProperty("type")  // Required: CREATE_TASK, UPDATE_TASK, DELETE_TASK
    private String type;
    
    @JsonProperty("data")  // Required: The task data
    private Map<String, Object> data;
    
    @JsonProperty("timestamp")  // Optional timestamp
    private LocalDateTime timestamp;
    
    @JsonProperty("commandId")  // Optional command ID
    private String commandId;
    
    @JsonProperty("changedFields")  // Optional: for UPDATE_TASK commands
    private Map<String, Object> changedFields;

    public SyncCommand() {}

    public SyncCommand(String entityId, String type, Map<String, Object> data, 
                      LocalDateTime timestamp, String commandId) {
        this.entityId = entityId;
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
        this.commandId = commandId;
    }

    public SyncCommand(String entityId, String type, Map<String, Object> data, 
                      LocalDateTime timestamp, String commandId, Map<String, Object> changedFields) {
        this.entityId = entityId;
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
        this.commandId = commandId;
        this.changedFields = changedFields;
    }

    // Getters and setters
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getCommandId() { return commandId; }
    public void setCommandId(String commandId) { this.commandId = commandId; }

    public Map<String, Object> getChangedFields() { return changedFields; }
    public void setChangedFields(Map<String, Object> changedFields) { this.changedFields = changedFields; }
}