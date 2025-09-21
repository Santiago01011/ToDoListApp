package model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Immutable Task class following the command queue pattern.
 * Once created, a Task instance cannot be modified - use the Builder or toBuilder() for changes.
 */
public final class Task {
    // All fields are final for immutability
    private final String task_id;
    private final String task_title;
    private final String description;
    private final TaskStatus status;
    private final String sync_status;
    private final LocalDateTime due_date;
    private final LocalDateTime created_at;
    private final LocalDateTime updated_at;
    private final LocalDateTime deleted_at;
    private final LocalDateTime last_sync;
    private final String folder_id;
    private final String folder_name;
    private final Map<String, String> metadata; // For journal backlinks and other metadata

    // Private constructor - only Builder can create instances
    private Task(Builder builder) {
        this.task_id = builder.task_id;
        this.task_title = builder.task_title;
        this.description = builder.description;
        this.status = builder.status;
        this.sync_status = builder.sync_status;
        this.due_date = builder.due_date;
        this.created_at = builder.created_at;
        this.updated_at = builder.updated_at;
        this.deleted_at = builder.deleted_at;
        this.last_sync = builder.last_sync;
        this.folder_id = builder.folder_id;
        this.folder_name = builder.folder_name;
        this.metadata = builder.metadata != null ? Map.copyOf(builder.metadata) : Map.of();
    }

    // Jackson constructor for JSON deserialization
    @JsonCreator
    public Task(
        @JsonProperty("task_id") String task_id,
        @JsonProperty("task_title") String task_title,
        @JsonProperty("description") String description,
        @JsonProperty("status") TaskStatus status,
        @JsonProperty("sync_status") String sync_status,
        @JsonProperty("due_date") LocalDateTime due_date,
        @JsonProperty("created_at") LocalDateTime created_at,
        @JsonProperty("updated_at") LocalDateTime updated_at,
        @JsonProperty("deleted_at") LocalDateTime deleted_at,
        @JsonProperty("last_sync") LocalDateTime last_sync,
        @JsonProperty("folder_id") String folder_id,
        @JsonProperty("folder_name") String folder_name,
        @JsonProperty("metadata") Map<String, String> metadata
    ) {
        this.task_id = task_id;
        this.task_title = task_title;
        this.description = description;
        this.status = status;
        this.sync_status = sync_status;
        this.due_date = due_date;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.deleted_at = deleted_at;
        this.last_sync = last_sync;
        this.folder_id = folder_id;
        this.folder_name = folder_name;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Factory method to create a new Builder with auto-generated task ID
     */
    public static Builder builder() {
        return new Builder(UUID.randomUUID().toString());
    }

    /**
     * Create a builder pre-populated with this task's values for modification
     */
    public Builder toBuilder() {
        return new Builder(this.task_id)
            .taskTitle(this.task_title)
            .description(this.description)
            .status(this.status)
            .sync_status(this.sync_status)
            .dueDate(this.due_date)
            .createdAt(this.created_at)
            .updatedAt(this.updated_at)
            .deletedAt(this.deleted_at)
            .lastSync(this.last_sync)
            .folderId(this.folder_id)
            .folderName(this.folder_name)
            .metadata(this.metadata);
    }

    // Convenience "with" methods for common updates
    public Task withTitle(String newTitle) {
        return this.toBuilder().taskTitle(newTitle).build();
    }

    public Task withDescription(String newDescription) {
        return this.toBuilder().description(newDescription).build();
    }

    public Task withStatus(TaskStatus newStatus) {
        return this.toBuilder().status(newStatus).build();
    }

    public Task withSyncStatus(String newSyncStatus) {
        return this.toBuilder().sync_status(newSyncStatus).build();
    }

    public Task withUpdatedAt(LocalDateTime timestamp) {
        return this.toBuilder().updatedAt(timestamp).build();
    }

    public Task withDeletedAt(LocalDateTime timestamp) {
        return this.toBuilder().deletedAt(timestamp).build();
    }

    public Task withDueDate(LocalDateTime dueDate) {
        return this.toBuilder().dueDate(dueDate).build();
    }

    public Task withFolderId(String folderId) {
        return this.toBuilder().folderId(folderId).build();
    }    public static class Builder {
        private String task_id;
        private String task_title;
        private String description;
        private TaskStatus status; 
        private String sync_status;
        private LocalDateTime due_date;
        private LocalDateTime created_at;
        private LocalDateTime updated_at;
        private LocalDateTime deleted_at;
        private LocalDateTime last_sync;
        private String folder_id;
        private String folder_name;
        private Map<String, String> metadata;

        public Builder() {}

        public Builder(String task_id) {
            this.task_id = task_id;
        }

        public Builder taskTitle(String task_title) {
            this.task_title = task_title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder sync_status(String sync_status) {
            this.sync_status = sync_status;
            return this;
        }

        public Builder dueDate(LocalDateTime due_date) {
            this.due_date = due_date;
            return this;
        }

        public Builder createdAt(LocalDateTime created_at) {
            this.created_at = created_at;
            return this;
        }

        public Builder updatedAt(LocalDateTime updated_at) {
            this.updated_at = updated_at;
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

        public Builder folderId(String folder_id) {
            this.folder_id = folder_id;
            return this;
        }

        public Builder folderName(String folder_name) {
            this.folder_name = folder_name;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Task build() {
            // Add validation
            if (task_id == null || task_id.trim().isEmpty()) {
                throw new IllegalArgumentException("Task ID cannot be null or empty");
            }
            if (task_title == null || task_title.trim().isEmpty()) {
                throw new IllegalArgumentException("Task title cannot be null or empty");
            }
            if (status == null) {
                this.status = TaskStatus.pending; // Default status
            }
            if (created_at == null) {
                this.created_at = LocalDateTime.now();
            }
            if (updated_at == null) {
                this.updated_at = this.created_at;
            }
            if (sync_status == null) {
                this.sync_status = "new";
            }

            return new Task(this);
        }
    }

    // Only getters - no setters for immutability
    public String getTask_id() {
        return task_id;
    }

    public String getTitle() {
        return task_title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getSync_status() {
        return sync_status;
    }

    public LocalDateTime getDue_date() {
        return due_date;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public LocalDateTime getDeleted_at() {
        return deleted_at;
    }

    public LocalDateTime getLast_sync() {
        return last_sync;
    }

    public String getFolder_id() {
        return folder_id;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    // Legacy compatibility methods (deprecated)
    @Deprecated
    public void setTask_id(String task_id) {
        throw new UnsupportedOperationException("Task is immutable. Use toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setTitle(String task_title) {
        throw new UnsupportedOperationException("Task is immutable. Use withTitle() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Task is immutable. Use withDescription() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setStatus(TaskStatus status) {
        throw new UnsupportedOperationException("Task is immutable. Use withStatus() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setStatus(String status) {
        throw new UnsupportedOperationException("Task is immutable. Use withStatus() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setSync_status(String sync_status) {
        throw new UnsupportedOperationException("Task is immutable. Use withSyncStatus() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setDue_date(LocalDateTime due_date) {
        throw new UnsupportedOperationException("Task is immutable. Use withDueDate() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setCreated_at(LocalDateTime created_at) {
        throw new UnsupportedOperationException("Task is immutable. Use toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setUpdated_at(LocalDateTime updated_at) {
        throw new UnsupportedOperationException("Task is immutable. Use withUpdatedAt() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setDeleted_at(LocalDateTime deleted_at) {
        throw new UnsupportedOperationException("Task is immutable. Use withDeletedAt() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setLast_sync(LocalDateTime last_sync) {
        throw new UnsupportedOperationException("Task is immutable. Use toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setFolder_id(String folder_id) {
        throw new UnsupportedOperationException("Task is immutable. Use withFolderId() or toBuilder() to create a modified copy.");
    }

    @Deprecated
    public void setFolder_name(String folder_name) {
        throw new UnsupportedOperationException("Task is immutable. Use toBuilder() to create a modified copy.");
    }

    // Legacy compatibility methods for removed functionality
    @Deprecated
    public java.util.Set<String> getModifiedFields() {
        return java.util.Collections.emptySet();
    }

    @Deprecated
    public void clearModifiedFields() {
        // No-op - immutable objects don't track modifications
    }

    @Deprecated
    public void addModifiedField(String fieldName) {
        // No-op - immutable objects don't track modifications
    }

    // Object contract methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(task_id, task.task_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task_id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "task_id='" + task_id + '\'' +
                ", title='" + task_title + '\'' +
                ", status=" + status +
                ", sync_status='" + sync_status + '\'' +
                '}';
    }

    public String viewTaskDesc() {
        return "Task ID: " + task_id + ", Title: " + task_title + ", Description: " + description + "\n" +
                "Status: " + status + ", sync_status: " + sync_status + ", Due Date: " + due_date + 
                ", Created At: " + created_at + "\n" + "Updated At: " + updated_at + ", Deleted At: " + 
                deleted_at + ", Last Sync: " + last_sync + ", Folder ID: " + folder_id;
    }
}