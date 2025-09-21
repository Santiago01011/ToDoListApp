package model.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Command batch structure for API V2 sync endpoint.
 * Contains a batch of sync commands to be processed together.
 */
public class CommandBatch {
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("client_timestamp")
    private OffsetDateTime clientTimestamp;
    
    @JsonProperty("last_sync")
    private OffsetDateTime lastSync;
    
    @JsonProperty("commands")
    private List<SyncCommand> commands;
    
    // Optimization fields for conditional data fetching
    @JsonProperty("folder_version")
    private String folderVersion;
    
    @JsonProperty("include_folders")
    private boolean includeFolders = false;

    public CommandBatch() {}

    public CommandBatch(String userId, OffsetDateTime clientTimestamp, OffsetDateTime lastSync, List<SyncCommand> commands) {
        this.userId = userId;
        this.clientTimestamp = clientTimestamp;
        this.lastSync = lastSync;
        this.commands = commands;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public OffsetDateTime getClientTimestamp() { return clientTimestamp; }
    public void setClientTimestamp(OffsetDateTime clientTimestamp) { this.clientTimestamp = clientTimestamp; }

    public OffsetDateTime getLastSync() { return lastSync; }
    public void setLastSync(OffsetDateTime lastSync) { this.lastSync = lastSync; }

    public List<SyncCommand> getCommands() { return commands; }
    public void setCommands(List<SyncCommand> commands) { this.commands = commands; }

    // Optimization fields
    public String getFolderVersion() { return folderVersion; }
    public void setFolderVersion(String folderVersion) { this.folderVersion = folderVersion; }

    public boolean isIncludeFolders() { return includeFolders; }
    public void setIncludeFolders(boolean includeFolders) { this.includeFolders = includeFolders; }
}