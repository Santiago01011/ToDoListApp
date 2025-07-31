package service;

import model.Task;
import model.TaskHandler;
import model.TaskStatus;
import model.sync.CommandBatch;
import model.sync.CommandFactory;
import model.sync.SyncCommand;
import model.sync.SyncResponse;
import COMMON.UserProperties;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for handling API V2 command-based sync operations.
 * Replaces the direct database sync functionality in DBHandler.
 */
public class SyncService {
    private TaskHandler taskHandler;
    private String userUUID;

    public SyncService(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getUserUUID() {
        return userUUID;
    }

    /**
     * Starts the synchronization process asynchronously using API V2 commands.
     */
    public CompletableFuture<Boolean> startSyncProcess() {
        return CompletableFuture.supplyAsync(() -> {
            if (userUUID == null)
                throw new IllegalStateException("User UUID is not set. Cannot start sync process.");
            try {
                syncWithAPI();
                return true;
            } catch (Exception e) {
                System.err.println("Error during API sync process: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Performs the actual sync with the API V2 command endpoint.
     */
    private void syncWithAPI() throws IOException, InterruptedException {
        LocalDateTime syncStartTime = LocalDateTime.now();
        LocalDateTime lastSync = taskHandler.getLastSync();

        // Build command batch
        List<SyncCommand> commands = buildSyncCommands();
        
        if (commands.isEmpty() && lastSync != null) {
            // No local changes, but still check for server changes
            commands = new ArrayList<>();
        }

        CommandBatch batch = new CommandBatch(
            userUUID,
            syncStartTime,
            lastSync,
            commands
        );

        // Send to API
        SyncResponse response = APIService.syncCommands(batch);

        if (response.isSuccess()) {
            // Process the response
            processSyncResponse(response);
            
            // Update last sync time
            taskHandler.setLastSync(response.getServerTimestamp() != null ? 
                response.getServerTimestamp() : syncStartTime);
        } else {
            throw new RuntimeException("Sync failed: " + response.getErrorMessage());
        }
    }

    /**
     * Builds a list of sync commands from current task state.
     */
    private List<SyncCommand> buildSyncCommands() {
        List<SyncCommand> commands = new ArrayList<>();

        // Commands for new tasks
        for (Task task : taskHandler.userTasksList) {
            if ("new".equals(task.getSync_status())) {
                commands.add(CommandFactory.createTaskCommand(task));
            }
        }

        // Commands for updated tasks (from shadow updates)
        for (Task shadowTask : taskHandler.getShadowUpdatesForSync()) {
            if (shadowTask.getDeleted_at() != null) {
                commands.add(CommandFactory.deleteTaskCommand(shadowTask));
            } else {
                commands.add(CommandFactory.updateTaskCommand(shadowTask));
            }
        }

        return commands;
    }

    /**
     * Processes the response from the sync API and updates local task state.
     */
    private void processSyncResponse(SyncResponse response) {
        // Process successful command results
        if (response.getProcessedCommands() != null) {
            for (SyncResponse.CommandResult result : response.getProcessedCommands()) {
                processCommandResult(result);
            }
        }

        // Process server changes
        if (response.getServerChanges() != null) {
            for (Map<String, Object> changeData : response.getServerChanges()) {
                processServerChange(changeData);
            }
        }

        // Handle conflicts (for now, server wins)
        if (response.getConflicts() != null) {
            for (SyncResponse.ConflictResult conflict : response.getConflicts()) {
                System.out.println("Conflict detected for entity " + conflict.getEntityId() + 
                    ": " + conflict.getConflictType());
                // For now, accept server data
                processServerChange(conflict.getServerData());
            }
        }
    }

    /**
     * Processes the result of a single command execution.
     */
    private void processCommandResult(SyncResponse.CommandResult result) {
        if (!result.isSuccess()) {
            System.err.println("Command failed - Client ID: " + result.getClientId() + 
                ", Type: " + result.getCommandType() + 
                ", Error: " + result.getErrorMessage());
            return;
        }

        String clientId = result.getClientId();
        String serverId = result.getServerId();

        switch (result.getCommandType()) {
            case "CREATE":
                // Update task ID from temporary client ID to server ID
                Task newTask = taskHandler.userTasksList.stream()
                    .filter(task -> task.getTask_id().equals(clientId))
                    .findFirst()
                    .orElse(null);
                
                if (newTask != null) {
                    newTask.setTask_id(serverId);
                    newTask.setSync_status("cloud");
                    newTask.setLast_sync(taskHandler.getLastSync());
                }
                break;

            case "UPDATE":
                // Clear shadow update and mark task as synced
                taskHandler.clearShadowUpdate(clientId);
                taskHandler.userTasksList.removeIf(t -> 
                    "to_update".equals(t.getSync_status()) && t.getTask_id().equals(clientId));
                
                taskHandler.userTasksList.stream()
                    .filter(t -> t.getTask_id().equals(clientId) && "local".equals(t.getSync_status()))
                    .forEach(t -> {
                        t.setSync_status("cloud");
                        t.setLast_sync(taskHandler.getLastSync());
                    });
                break;

            case "DELETE":
                // Remove task from local list
                taskHandler.userTasksList.removeIf(t -> t.getTask_id().equals(clientId));
                taskHandler.clearShadowUpdate(clientId);
                break;
        }
    }

    /**
     * Processes a server change and updates local tasks accordingly.
     */
    private void processServerChange(Map<String, Object> changeData) {
        String taskId = (String) changeData.get("task_id");
        if (taskId == null) return;

        // Convert server data to Task object
        Task serverTask = convertServerDataToTask(changeData);
        if (serverTask == null) return;

        // Check if we have this task locally
        Task localTask = taskHandler.getTaskById(taskId);
        
        if (localTask == null) {
            // New task from server
            taskHandler.userTasksList.add(serverTask);
        } else {
            // Update existing task if server version is newer
            LocalDateTime serverLastSync = serverTask.getLast_sync();
            LocalDateTime localLastSync = localTask.getLast_sync();
            
            if (serverLastSync != null && 
                (localLastSync == null || serverLastSync.isAfter(localLastSync))) {
                
                int index = taskHandler.userTasksList.indexOf(localTask);
                if (index >= 0) {
                    taskHandler.userTasksList.set(index, serverTask);
                }
            }
        }
    }

    /**
     * Converts server change data to a Task object.
     */
    private Task convertServerDataToTask(Map<String, Object> data) {
        try {
            Task.Builder builder = new Task.Builder((String) data.get("task_id"));
            
            if (data.containsKey("task_title")) {
                builder.taskTitle((String) data.get("task_title"));
            }
            if (data.containsKey("description")) {
                builder.description((String) data.get("description"));
            }
            if (data.containsKey("status")) {
                try {
                    builder.status(TaskStatus.valueOf((String) data.get("status")));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid status value: " + data.get("status"));
                    builder.status(TaskStatus.pending);
                }
            }
            if (data.containsKey("due_date") && data.get("due_date") != null) {
                builder.dueDate(LocalDateTime.parse((String) data.get("due_date")));
            }
            if (data.containsKey("created_at") && data.get("created_at") != null) {
                builder.createdAt(LocalDateTime.parse((String) data.get("created_at")));
            }
            if (data.containsKey("updated_at") && data.get("updated_at") != null) {
                builder.updatedAt(LocalDateTime.parse((String) data.get("updated_at")));
            }
            if (data.containsKey("last_sync") && data.get("last_sync") != null) {
                builder.lastSync(LocalDateTime.parse((String) data.get("last_sync")));
            }
            if (data.containsKey("deleted_at") && data.get("deleted_at") != null) {
                builder.deletedAt(LocalDateTime.parse((String) data.get("deleted_at")));
            }
            if (data.containsKey("folder_id")) {
                builder.folderId((String) data.get("folder_id"));
            }
            if (data.containsKey("folder_name")) {
                builder.folderName((String) data.get("folder_name"));
            }
            
            builder.sync_status("cloud");
            
            return builder.build();
        } catch (Exception e) {
            System.err.println("Error converting server data to task: " + e.getMessage());
            return null;
        }
    }
}