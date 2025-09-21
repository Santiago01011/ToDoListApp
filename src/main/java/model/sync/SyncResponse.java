package model.sync;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import model.Folder;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response structure from API V2 sync command batch endpoint.
 * Contains the results of processing the command batch.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncResponse {
    @JsonProperty("success")
    private List<CommandResult> processedCommands;
    
    @JsonProperty("conflicts")
    private List<ConflictResult> conflicts;
    
    @JsonProperty("failed")
    private List<CommandResult> failedCommands;
    
    @JsonProperty("server_timestamp")
    @JsonAlias("serverTimestamp")
    private OffsetDateTime serverTimestamp;
    
    @JsonProperty("server_changes")
    @JsonAlias("serverChanges")
    private List<Map<String, Object>> serverChanges;
    
    @JsonProperty("error_message")
    @JsonAlias({"errorMessage", "error"})
    private String errorMessage;
    
    // Optimization fields for conditional data fetching
    @JsonProperty("folders")
    private List<Folder> folders;
    
    @JsonProperty("folder_version")
    @JsonAlias("folderVersion")
    private String folderVersion;

    public SyncResponse() {}

    // Helper method to check if sync was successful (no failed commands)
    public boolean isSuccess() { 
        return failedCommands == null || failedCommands.isEmpty(); 
    }

    public OffsetDateTime getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(OffsetDateTime serverTimestamp) { this.serverTimestamp = serverTimestamp; }

    public List<CommandResult> getProcessedCommands() { return processedCommands; }
    public void setProcessedCommands(List<CommandResult> processedCommands) { this.processedCommands = processedCommands; }
    
    // Jackson setter for "success" JSON field - maps to processedCommands
    @JsonProperty("success")
    public void setSuccess(List<CommandResult> success) { this.processedCommands = success; }

    public List<Map<String, Object>> getServerChanges() { return serverChanges; }
    public void setServerChanges(List<Map<String, Object>> serverChanges) { this.serverChanges = serverChanges; }

    public List<ConflictResult> getConflicts() { return conflicts; }
    public void setConflicts(List<ConflictResult> conflicts) { this.conflicts = conflicts; }

    public List<CommandResult> getFailedCommands() { return failedCommands; }
    public void setFailedCommands(List<CommandResult> failedCommands) { this.failedCommands = failedCommands; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Optimization fields getters and setters
    public List<Folder> getFolders() { return folders; }
    public void setFolders(List<Folder> folders) { this.folders = folders; }

    public String getFolderVersion() { return folderVersion; }
    public void setFolderVersion(String folderVersion) { this.folderVersion = folderVersion; }

    /**
     * Result of processing an individual command.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
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

        @JsonProperty("entity_id")
        @JsonAlias("entityId")
        private String entityId;
        
        // Support for legacy API response format
        @JsonProperty("error")
        public void setError(String error) {
            this.errorMessage = error;
        }
        
        @JsonProperty("commandId") 
        public void setCommandId(String commandId) {
            this.clientId = commandId;
        }
        
        // Support for server response format variations
        @JsonProperty("type")
        public void setType(String type) {
            this.commandType = type;
        }
        
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

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

    // Tolerate new fields from server response
    @JsonProperty("hasConflicts")
    public void setHasConflicts(Object ignored) { /* no-op for compatibility */ }
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