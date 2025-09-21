package service.sync;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

import model.Task;
import model.TaskHandlerV2;
import model.TaskStatus;
import model.Folder;
import model.sync.SyncResponse;

public final class ResponseApplier {
    private final TaskHandlerV2 handler;

    public ResponseApplier(TaskHandlerV2 handler) {
        this.handler = handler;
    }

    public void apply(SyncResponse response) {
        // Track tasks that were deleted in this sync to avoid re-adding them from serverChanges
        java.util.Set<String> deletedTaskIds = new java.util.HashSet<>();
        
        if (response.getProcessedCommands() != null) {
            for (var result : response.getProcessedCommands()) {
                switch (result.getCommandType()) {
                    case "CREATE":
                    case "CREATE_TASK":
                        if (result.getEntityId() != null) {
                            // Use entityId (task ID) instead of clientId (command ID) for marking as synced
                            handler.markTaskSynced(result.getEntityId());
                            
                            // If server provided a new ID, update the task ID mapping
                            if (result.getServerId() != null && !result.getEntityId().equals(result.getServerId())) {
                                handler.updateTaskId(result.getEntityId(), result.getServerId());
                            }
                        }
                        break;
                    case "UPDATE":
                    case "UPDATE_TASK":
                        if (result.getEntityId() != null) {
                            // Use entityId (task ID) instead of clientId (command ID)
                            handler.markTaskSynced(result.getEntityId());
                        }
                        break;
                    case "DELETE":
                    case "DELETE_TASK":
                        if (result.getEntityId() != null) {
                            // Track entityId for deletion filtering
                            deletedTaskIds.add(result.getEntityId());
                            System.out.println("ResponseApplier: Processing DELETE for task " + result.getEntityId());
                            // Use entityId (task ID) for removal
                            handler.removeTaskById(result.getEntityId());
                        }
                        break;
                }
            }
        }

        if (response.getServerChanges() != null) {
            for (Map<String, Object> change : response.getServerChanges()) {
                Task t = convertServerDataToTask(change);
                if (t != null) {
                    // Skip tasks that were just deleted in this sync batch
                    if (deletedTaskIds.contains(t.getTask_id())) {
                        System.out.println("ResponseApplier: Skipping re-add of deleted task: " + t.getTask_id());
                        continue;
                    }
                    
                    // If server provided only folderId (no folder_name), try to resolve
                    if ((t.getFolder_name() == null || t.getFolder_name().isEmpty()) && t.getFolder_id() != null) {
                        List<Folder> folders = handler.getFoldersList();
                        if (folders != null) {
                            for (Folder f : folders) {
                                if (f != null && t.getFolder_id().equals(f.getFolder_id())) {
                                    t = t.toBuilder().folderName(f.getFolder_name()).build();
                                    break;
                                }
                            }
                        }
                    }
                    handler.addOrReplaceTask(t);
                }
            }
        }

        if (response.getConflicts() != null) {
            for (var conflict : response.getConflicts()) {
                Task t = convertServerDataToTask(conflict.getServerData());
                if (t != null) handler.addOrReplaceTask(t);
            }
        }

        if (response.getServerTimestamp() != null) {
            handler.setLastSync(response.getServerTimestamp().toLocalDateTime());
        }

        // Optimized folder handling - only fetch if server provided new folders or version changed
        if (response.getFolders() != null && !response.getFolders().isEmpty()) {
            // Server provided folders in response - use them directly
            System.out.println("ResponseApplier: Server provided " + response.getFolders().size() + " folders:");
            for (Folder f : response.getFolders()) {
                System.out.println("  - " + f.getFolder_name() + " -> " + f.getFolder_id());
            }
            handler.setFoldersList(response.getFolders(), response.getFolderVersion());
            System.out.println("ResponseApplier: Updated folders from sync response (" + 
                             response.getFolders().size() + " folders)");
        } else {
            System.out.println("ResponseApplier: Skipped folder refresh - no changes detected");
        }
    }

    public static Task convertServerDataToTask(Map<String, Object> data) {
        try {
            String id = (String) data.get("task_id");
            if (id == null) id = (String) data.get("entityId");
            if (id == null) return null;

            Map<String, Object> payload = data;
            if (data.containsKey("data") && data.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inner = (Map<String, Object>) data.get("data");
                payload = inner;
            }

            Task.Builder builder = new Task.Builder(id);
            if (payload.containsKey("task_title")) builder.taskTitle((String) payload.get("task_title"));
            else if (payload.containsKey("title")) builder.taskTitle((String) payload.get("title"));

            if (payload.containsKey("description")) builder.description((String) payload.get("description"));

            if (payload.containsKey("status")) {
                TaskStatus parsed = TaskStatus.parse(String.valueOf(payload.get("status")));
                if (parsed != null) builder.status(parsed);
            }

            LocalDateTime dueDt = parseDateTime(payload.get("due_date"));
            if (dueDt == null) dueDt = parseDateTime(payload.get("dueDate"));
            if (dueDt != null) builder.dueDate(dueDt);

            LocalDateTime createdDt = parseDateTime(payload.get("created_at"));
            if (createdDt == null) createdDt = parseDateTime(payload.get("createdAt"));
            if (createdDt != null) builder.createdAt(createdDt);

            LocalDateTime updatedDt = parseDateTime(payload.get("updated_at"));
            if (updatedDt == null) updatedDt = parseDateTime(payload.get("updatedAt"));
            if (updatedDt != null) builder.updatedAt(updatedDt);
            if (payload.get("last_sync") != null) {
                LocalDateTime lastSyncDt = parseDateTime(payload.get("last_sync"));
                if (lastSyncDt != null) builder.lastSync(lastSyncDt);
            }
            if (payload.get("deleted_at") != null) {
                LocalDateTime deletedDt = parseDateTime(payload.get("deleted_at"));
                if (deletedDt != null) builder.deletedAt(deletedDt);
            }

            if (payload.containsKey("folder_id")) builder.folderId((String) payload.get("folder_id"));
            else if (payload.containsKey("folderId")) builder.folderId((String) payload.get("folderId"));
            if (payload.containsKey("folder_name")) builder.folderName((String) payload.get("folder_name"));

            // sync_status is managed by TaskHandlerV2/OptimizedSyncService; avoid forcing here
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime parseDateTime(Object val) {
        if (val == null) return null;
        String s = String.valueOf(val);
        try {
            return java.time.OffsetDateTime.parse(s).toLocalDateTime();
        } catch (java.time.format.DateTimeParseException e) {
            try {
                return LocalDateTime.parse(s);
            } catch (java.time.format.DateTimeParseException e2) {
                return null;
            }
        }
    }
}
