package service.sync;

import com.fasterxml.jackson.databind.JsonNode;
import model.Task;
import model.TaskStatus;

import java.time.LocalDateTime;

public final class TaskAssembler {
    private TaskAssembler() {}

    public static Task mergeFromPayload(Task existing, JsonNode payload, LocalDateTime handlerLastSync) {
        String id = LogTasksUtil.textOf(payload, "task_id", "id");
        Task.Builder b = (existing != null) ? existing.toBuilder() : new Task.Builder(id);

        String title = LogTasksUtil.textOf(payload, "task_title", "title", "name");
        if (title != null) b.taskTitle(safeTitle(title, id));
        else if (existing == null) b.taskTitle(safeTitle(null, id));

        String description = LogTasksUtil.textOf(payload, "description");
        if (description != null) b.description(description);

        String statusStr = LogTasksUtil.textOf(payload, "status");
        if (statusStr != null) {
            TaskStatus parsed = TaskStatus.parse(statusStr);
            if (parsed != null) b.status(parsed);
        }

        LocalDateTime due = LogTasksUtil.timeOfAny(payload, "due_date", "dueDate");
        if (due != null) b.dueDate(due);

        LocalDateTime created = LogTasksUtil.timeOfAny(payload, "created_at", "createdAt");
        if (created != null) b.createdAt(created);

        LocalDateTime updated = LogTasksUtil.timeOfAny(payload, "updated_at", "updatedAt");
        if (updated != null) b.updatedAt(updated);

        LocalDateTime deletedAt = LogTasksUtil.timeOfAny(payload, "deleted_at", "deletedAt");
        if (deletedAt != null) b.deletedAt(deletedAt);

        LocalDateTime rowLastSync = LogTasksUtil.timeOfAny(payload, "last_sync", "lastSync");
        b.lastSync(rowLastSync != null ? rowLastSync : handlerLastSync);

        String folderId = LogTasksUtil.textOf(payload, "folder_id", "folderId");
        if (folderId != null) b.folderId(folderId);

        String folderName = LogTasksUtil.textOf(payload, "folder_name", "folderName", "folder", "folder_title");
        if (folderName != null) b.folderName(folderName);

        return b.sync_status("cloud").build();
    }

    private static String safeTitle(String title, String id) {
        if (title != null) {
            String t = title.trim();
            if (!t.isEmpty()) return t;
        }
        if (id != null && id.length() >= 8) return "Task " + id.substring(0, 8);
        return "Untitled";
    }

    // parsing centralized in TaskStatus.parse
}
