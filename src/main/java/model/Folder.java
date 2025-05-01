package model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Folder {
    private String folder_id;
    private String folder_name;
    private String sync_status;
    private LocalDateTime created_at;
    private LocalDateTime deleted_at;
    private LocalDateTime last_sync;

    public Folder() {}

    public Folder(
        @JsonProperty("folder_id")       String folder_id,
        @JsonProperty("folder_name")     String folder_name,
        @JsonProperty("sync_status")     String sync_status,
        @JsonProperty("created_at")      LocalDateTime created_at,
        @JsonProperty("deleted_at")      LocalDateTime deleted_at,
        @JsonProperty("last_sync")       LocalDateTime last_sync
    ) {
        this.folder_id   = folder_id;
        this.folder_name = folder_name;
        this.sync_status = sync_status;
        this.created_at  = created_at;
        this.deleted_at  = deleted_at;
        this.last_sync   = last_sync;
    }

    public static class Builder {
        private final String folder_id;
        private String folder_name;
        private String sync_status;
        private LocalDateTime created_at;
        private LocalDateTime deleted_at;
        private LocalDateTime last_sync;

        public Builder(String folder_id) {
            this.folder_id = folder_id;
        }

        public Builder folderName(String folder_name) {
            this.folder_name = folder_name;
            return this;
        }

        public Builder syncStatus(String sync_status) {
            this.sync_status = sync_status;
            return this;
        }

        public Builder createdAt(LocalDateTime created_at) {
            this.created_at = created_at;
            return this;
        }

        public Builder deletedAt(LocalDateTime deleted_at) {
            this.deleted_at = deleted_at;
            return this;
        }

        public Builder lastSync(LocalDateTime last_sync) {
            this.last_sync = last_sync;
            return this;
        }

        public Folder build() {
            return new Folder(
                folder_id,
                folder_name,
                sync_status,
                created_at,
                deleted_at,
                last_sync
            );
        }
    }

    // getters & setters

    public String getFolder_id()            { return folder_id; }
    public void setFolder_id(String id)     { this.folder_id = id; }

    public String getFolder_name()          { return folder_name; }
    public void setFolder_name(String name) { this.folder_name = name; }

    public String getSync_status()             { return sync_status; }
    public void setSync_status(String status)  { this.sync_status = status; }

    public LocalDateTime getCreated_at()       { return created_at; }
    public void setCreated_at(LocalDateTime t) { this.created_at = t; }

    public LocalDateTime getDeleted_at()       { return deleted_at; }
    public void setDeleted_at(LocalDateTime t) { this.deleted_at = t; }

    public LocalDateTime getLast_sync()        { return last_sync; }
    public void setLast_sync(LocalDateTime t)  { this.last_sync = t; }
}