package model.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base sync command structure for API V2 command batch endpoint.
 * Represents an individual command operation (create, update, delete) for tasks.
 */
public class SyncCommand {
    @JsonProperty("command_type")
    private String commandType;
    
    @JsonProperty("entity_type") 
    private String entityType;
    
    @JsonProperty("entity_id")
    private String entityId;
    
    @JsonProperty("client_id")
    private String clientId;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("data")
    private Map<String, Object> data;

    public SyncCommand() {}

    public SyncCommand(String commandType, String entityType, String entityId, String clientId, 
                      LocalDateTime timestamp, Map<String, Object> data) {
        this.commandType = commandType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.data = data;
    }

    // Getters and setters
    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}