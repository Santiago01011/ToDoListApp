package model.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response structure from API V2 sync command batch endpoint.
 * Contains the results of processing the command batch.
 */
public class SyncResponse {
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("server_timestamp")
    private LocalDateTime serverTimestamp;
    
    @JsonProperty("processed_commands")
    private List<CommandResult> processedCommands;
    
    @JsonProperty("server_changes")
    private List<Map<String, Object>> serverChanges;
    
    @JsonProperty("conflicts")
    private List<ConflictResult> conflicts;
    
    @JsonProperty("error_message")
    private String errorMessage;

    public SyncResponse() {}

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public LocalDateTime getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(LocalDateTime serverTimestamp) { this.serverTimestamp = serverTimestamp; }

    public List<CommandResult> getProcessedCommands() { return processedCommands; }
    public void setProcessedCommands(List<CommandResult> processedCommands) { this.processedCommands = processedCommands; }

    public List<Map<String, Object>> getServerChanges() { return serverChanges; }
    public void setServerChanges(List<Map<String, Object>> serverChanges) { this.serverChanges = serverChanges; }

    public List<ConflictResult> getConflicts() { return conflicts; }
    public void setConflicts(List<ConflictResult> conflicts) { this.conflicts = conflicts; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    /**
     * Result of processing an individual command.
     */
    public static class CommandResult {
        @JsonProperty("client_id")
        private String clientId;
        
        @JsonProperty("command_type")
        private String commandType;
        
        @JsonProperty("success")
        private boolean success;
        
        @JsonProperty("server_id")
        private String serverId;
        
        @JsonProperty("error_message")
        private String errorMessage;

        public CommandResult() {}

        // Getters and setters
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getCommandType() { return commandType; }
        public void setCommandType(String commandType) { this.commandType = commandType; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getServerId() { return serverId; }
        public void setServerId(String serverId) { this.serverId = serverId; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * Result of a conflict during sync processing.
     */
    public static class ConflictResult {
        @JsonProperty("entity_id")
        private String entityId;
        
        @JsonProperty("conflict_type")
        private String conflictType;
        
        @JsonProperty("server_data")
        private Map<String, Object> serverData;
        
        @JsonProperty("client_data")
        private Map<String, Object> clientData;

        public ConflictResult() {}

        // Getters and setters
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }

        public String getConflictType() { return conflictType; }
        public void setConflictType(String conflictType) { this.conflictType = conflictType; }

        public Map<String, Object> getServerData() { return serverData; }
        public void setServerData(Map<String, Object> serverData) { this.serverData = serverData; }

        public Map<String, Object> getClientData() { return clientData; }
        public void setClientData(Map<String, Object> clientData) { this.clientData = clientData; }
    }
}