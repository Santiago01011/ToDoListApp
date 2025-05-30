package model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import model.TaskStatus;

public class Task {
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

    public Task() {}

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
        @JsonProperty("folder_name") String folder_name
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
    }

    public static class Builder {
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

        public Task build() {
            Task task = new Task();
            task.task_id = this.task_id;
            task.folder_id = this.folder_id;
            task.folder_name = this.folder_name;
            task.task_title = this.task_title;
            task.description = this.description;
            task.sync_status = this.sync_status;
            task.last_sync = this.last_sync;
            task.status = this.status;
            task.due_date = this.due_date;
            task.created_at = this.created_at;
            task.updated_at = this.updated_at;
            task.deleted_at = this.deleted_at;
            return task;
        }
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getTitle() {
        return task_title;
    }

    public void setTitle(String task_title) {
        this.task_title = task_title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = TaskStatus.valueOf(status);
    }

    public String getSync_status() {
        return sync_status;
    }

    public void setSync_status(String sync_status) {
        this.sync_status = sync_status;
    }

    public LocalDateTime getDue_date() {
        return due_date;
    }

    public void setDue_date(LocalDateTime due_date) {
        this.due_date = due_date;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public LocalDateTime getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(LocalDateTime deleted_at) {
        this.deleted_at = deleted_at;
    }

    public LocalDateTime getLast_sync() {
        return last_sync;
    }

    public void setLast_sync(LocalDateTime last_sync) {
        this.last_sync = last_sync;
    }

    public String getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(String folder_id) {
        this.folder_id = folder_id;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public void setFolder_name(String folder_name) {
        this.folder_name = folder_name;
    }

    public Set<String> getModifiedFields() {
        return java.util.Collections.emptySet();
    }

    public void clearModifiedFields() {}

    public void addModifiedField(String fieldName) {}

    public String viewTaskDesc() {
        return "Task ID: " + task_id + ", Title: " + task_title + ", Description: " + description + "\n" +
                "Status: " + status + ", sync_status: " + sync_status + ", Due Date: " + due_date + 
                ", Created At: " + created_at + "\n" + "Updated At: " + updated_at + ", Deleted At: " + 
                deleted_at + ", Last Sync: " + last_sync + ", Folder ID: " + folder_id;
    }
}