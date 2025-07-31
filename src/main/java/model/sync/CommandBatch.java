package model.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command batch structure for API V2 sync endpoint.
 * Contains a batch of sync commands to be processed together.
 */
public class CommandBatch {
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("client_timestamp")
    private LocalDateTime clientTimestamp;
    
    @JsonProperty("last_sync")
    private LocalDateTime lastSync;
    
    @JsonProperty("commands")
    private List<SyncCommand> commands;

    public CommandBatch() {}

    public CommandBatch(String userId, LocalDateTime clientTimestamp, LocalDateTime lastSync, List<SyncCommand> commands) {
        this.userId = userId;
        this.clientTimestamp = clientTimestamp;
        this.lastSync = lastSync;
        this.commands = commands;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getClientTimestamp() { return clientTimestamp; }
    public void setClientTimestamp(LocalDateTime clientTimestamp) { this.clientTimestamp = clientTimestamp; }

    public LocalDateTime getLastSync() { return lastSync; }
    public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }

    public List<SyncCommand> getCommands() { return commands; }
    public void setCommands(List<SyncCommand> commands) { this.commands = commands; }
}