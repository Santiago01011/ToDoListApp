package service;

import model.Task;
import model.TaskHandler;
import model.TaskHandlerV2;
import model.TaskStatus;
import model.commands.CommandQueue;
import model.commands.Command;
import model.sync.CommandBatch;
import model.sync.CommandFactory;
import model.sync.SyncCommand;
import model.sync.SyncResponse;
import COMMON.UserProperties;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
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
    private TaskHandler taskHandler; // Legacy compatibility
    private TaskHandlerV2 taskHandlerV2; // New command-based handler
    private String userUUID;

    public SyncService(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
        this.taskHandlerV2 = null;
    }

    public SyncService(TaskHandlerV2 taskHandlerV2) {
        this.taskHandlerV2 = taskHandlerV2;
        this.taskHandler = taskHandlerV2.getLegacyHandler(); // Keep legacy reference for compatibility
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

    // Only process serverChanges after sync; do not reload all tasks from disk/server
    // Initial sync: let serverChanges bootstrap the local list

        // Build command batch
        List<SyncCommand> commands = buildSyncCommands();
        
        System.out.println("SyncService: Built " + commands.size() + " commands for sync");

        CommandBatch batch = new CommandBatch(
            userUUID,
            syncStartTime,
            lastSync,
            commands
        );

        // Debug: Log the request being sent
        System.out.println("SyncService: Sending batch to API:");
        System.out.println("  - User UUID: " + userUUID);
        System.out.println("  - Client Timestamp: " + syncStartTime);
        System.out.println("  - Last Sync: " + lastSync);
        System.out.println("  - Command Count: " + commands.size());
        if (!commands.isEmpty()) {
            System.out.println("  - Commands: ");
            for (SyncCommand cmd : commands) {
                System.out.println("    * Type: " + cmd.getType() + ", EntityId: " + cmd.getEntityId() + ", CommandId: " + cmd.getCommandId());
            }
        }

        // Send to API
        try {
            SyncResponse response = APIService.syncCommands(batch);
            
            // Debug: Print full response structure
            System.out.println("SyncService: Received response - Success commands: " + 
                (response.getProcessedCommands() != null ? response.getProcessedCommands().size() : 0) +
                ", Failed commands: " + (response.getFailedCommands() != null ? response.getFailedCommands().size() : 0) +
                ", Server changes: " + (response.getServerChanges() != null ? response.getServerChanges().size() : 0) +
                ", Conflicts: " + (response.getConflicts() != null ? response.getConflicts().size() : 0));

            // Debug: Log server changes in detail (supports nested 'data' payload)
            if (response.getServerChanges() != null && !response.getServerChanges().isEmpty()) {
                System.out.println("SyncService: Server changes received:");
                for (int i = 0; i < response.getServerChanges().size(); i++) {
                    Map<String, Object> change = response.getServerChanges().get(i);
                    String id = change.get("task_id") != null ? String.valueOf(change.get("task_id")) : String.valueOf(change.get("entityId"));
                    String title = null;
                    String status = null;
                    Object dataObj = change.get("data");
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> p = (Map<String, Object>) dataObj;
                        title = p.get("task_title") != null ? String.valueOf(p.get("task_title")) : (p.get("title") != null ? String.valueOf(p.get("title")) : null);
                        status = p.get("status") != null ? String.valueOf(p.get("status")) : null;
                    } else {
                        title = change.get("task_title") != null ? String.valueOf(change.get("task_title")) : null;
                        status = change.get("status") != null ? String.valueOf(change.get("status")) : null;
                    }
                    System.out.println("  [" + i + "] Task ID: " + id + 
                        ", Title: " + title + 
                        ", Status: " + status);
                }
            } else {
                System.out.println("SyncService: No server changes received from API");
            }

            // Debug: Log processed commands
            if (response.getProcessedCommands() != null && !response.getProcessedCommands().isEmpty()) {
                System.out.println("SyncService: Processed commands:");
                for (SyncResponse.CommandResult result : response.getProcessedCommands()) {
                    System.out.println("  - Type: " + result.getCommandType() + 
                        ", Client ID: " + result.getClientId() + 
                        ", Server ID: " + result.getServerId() + 
                        ", Success: " + result.isSuccess());
                }
            }

            // Debug: Log failed commands
            if (response.getFailedCommands() != null && !response.getFailedCommands().isEmpty()) {
                System.out.println("SyncService: Failed commands:");
                for (SyncResponse.CommandResult result : response.getFailedCommands()) {
                    System.out.println("  - Type: " + result.getCommandType() + 
                        ", Client ID: " + result.getClientId() + 
                        ", Error: " + result.getErrorMessage());
                }
            }

            if (response.isSuccess()) {
                System.out.println("SyncService: API V2 sync successful");
                // Process the response
                processSyncResponse(response);
                // Clear acknowledged commands from the queue (when using V2)
                if (taskHandlerV2 != null && response.getProcessedCommands() != null && !response.getProcessedCommands().isEmpty()) {
                    java.util.Set<String> toRemove = new java.util.HashSet<>();
                    for (SyncResponse.CommandResult r : response.getProcessedCommands()) {
                        if (r.getClientId() != null) toRemove.add(r.getClientId());
                    }
                    if (!toRemove.isEmpty()) {
                        taskHandlerV2.getCommandQueue().removeCommands(toRemove);
                    }
                }
                // After processing commands and server changes, pull notifications (if any)
                try {
                    fetchAndProcessNotifications();
                } catch (Exception notifEx) {
                    System.err.println("SyncService: Notification processing failed: " + notifEx.getMessage());
                }
                
                // Update last sync time
                taskHandler.setLastSync(response.getServerTimestamp() != null ? 
                    response.getServerTimestamp() : syncStartTime);
            } else {
                throw new RuntimeException("Sync failed: " + response.getErrorMessage());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("SyncService: Network error during sync: " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            System.err.println("SyncService: API error during sync: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Builds a list of sync commands from current task state.
     */
    private List<SyncCommand> buildSyncCommands() {
        List<SyncCommand> commands = new ArrayList<>();

        // Use command queue if TaskHandlerV2 is available
        if (taskHandlerV2 != null) {
            System.out.println("SyncService: Building commands from CommandQueue");
            CommandQueue commandQueue = taskHandlerV2.getCommandQueue();
            List<Command> pendingCommands = commandQueue.getPendingCommands();
            
            System.out.println("SyncService: Found " + pendingCommands.size() + " pending commands");
            
            for (Command command : pendingCommands) {
                SyncCommand syncCommand = convertCommandToSyncCommand(command);
                if (syncCommand != null) {
                    commands.add(syncCommand);
                }
            }
        } else {
            // Fallback to legacy approach
            System.out.println("SyncService: Using legacy sync approach (looking for tasks with sync_status)");
            
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
        }

        return commands;
    }

    /**
     * Convert a Command from the queue to a SyncCommand for API transmission
     */
    private SyncCommand convertCommandToSyncCommand(Command command) {
        System.out.println("SyncService: Converting command - Type: " + command.getType() + 
                          ", EntityId: " + command.getEntityId() + 
                          ", CommandId: " + command.getCommandId());
        
        switch (command.getType()) {
            case CREATE_TASK:
                // Cast to specific command type to access properties
                if (command instanceof model.commands.CreateTaskCommand createCmd) {
                    Map<String, Object> createData = new HashMap<>();
                    createData.put("title", createCmd.title());
                    createData.put("description", createCmd.description());
                    createData.put("status", createCmd.status().toString());
                    createData.put("due_date", createCmd.dueDate());
                    createData.put("folder_id", createCmd.folderId());
                    createData.put("created_at", LocalDateTime.now());
                    
                    SyncCommand syncCommand = new SyncCommand(
                        command.getEntityId(),    // entityId - the task ID
                        "CREATE_TASK",           // type - as expected by server
                        createData,              // data - task properties
                        command.getTimestamp(),  // timestamp
                        command.getCommandId()   // commandId
                    );
                    
                    System.out.println("SyncService: Created SyncCommand - " +
                                      "Type: " + syncCommand.getType() + 
                                      ", EntityId: " + syncCommand.getEntityId() +
                                      ", CommandId: " + syncCommand.getCommandId());
                    
                    return syncCommand;
                }
                break;
                
            case UPDATE_TASK:
                // Cast to UpdateTaskCommand if needed
                if (command instanceof model.commands.UpdateTaskCommand updateCmd) {
                    Map<String, Object> updateData = new HashMap<>();
                    Map<String, Object> changed = updateCmd.changedFields();
                    if (changed != null) {
                        // Pass through known fields; adapt case to server expectations (camelCase)
                        if (changed.containsKey("title")) updateData.put("title", changed.get("title"));
                        if (changed.containsKey("description")) updateData.put("description", changed.get("description"));
                        if (changed.containsKey("status")) {
                            Object st = changed.get("status");
                            updateData.put("status", st != null ? st.toString() : null);
                        }
                        if (changed.containsKey("dueDate")) updateData.put("due_date", changed.get("dueDate"));
                        if (changed.containsKey("folderId")) updateData.put("folder_id", changed.get("folderId"));
                    }
                    
                    return new SyncCommand(
                        command.getEntityId(),
                        "UPDATE_TASK",
                        updateData,
                        command.getTimestamp(),
                        command.getCommandId()
                    );
                }
                break;
                
            case DELETE_TASK:
                // Cast to DeleteTaskCommand if needed  
                if (command instanceof model.commands.DeleteTaskCommand) {
                    Map<String, Object> deleteData = new HashMap<>();
                    // Add delete-specific data if needed
                    
                    return new SyncCommand(
                        command.getEntityId(),
                        "DELETE_TASK",
                        deleteData,
                        command.getTimestamp(),
                        command.getCommandId()
                    );
                }
                break;
                
            default:
                System.err.println("SyncService: Unknown command type: " + command.getType());
                break;
        }
        System.err.println("SyncService: Failed to convert command: " + command.getType());
        return null;
    }

    /**
     * Processes the response from the sync API and updates local task state.
     */
    private void processSyncResponse(SyncResponse response) {
        int processedCommands = 0;
        int serverChanges = 0;
        int conflicts = 0;
        int failedCommands = 0;

        // Process successful command results
        if (response.getProcessedCommands() != null) {
            for (SyncResponse.CommandResult result : response.getProcessedCommands()) {
                result.setSuccess(true); // Commands in success array are successful
                processCommandResult(result);
                processedCommands++;
            }
        }

        // Process failed command results
        if (response.getFailedCommands() != null) {
            for (SyncResponse.CommandResult result : response.getFailedCommands()) {
                result.setSuccess(false); // Commands in failed array are failed
                processCommandResult(result);
                failedCommands++;
            }
        }

        // Process server changes
        if (response.getServerChanges() != null) {
            for (Map<String, Object> changeData : response.getServerChanges()) {
                processServerChange(changeData);
                serverChanges++;
            }
        }

        // Handle conflicts (for now, server wins)
        if (response.getConflicts() != null) {
            for (SyncResponse.ConflictResult conflict : response.getConflicts()) {
                System.out.println("SyncService: Conflict detected for entity " + conflict.getEntityId() + 
                    ": " + conflict.getConflictType() + " (server wins)");
                // For now, accept server data
                processServerChange(conflict.getServerData());
                conflicts++;
            }
        }

        System.out.println("SyncService: Processed " + processedCommands + " successful commands, " + 
            failedCommands + " failed commands, " + serverChanges + " server changes, " + conflicts + " conflicts");
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

        String resultType = result.getCommandType();
        switch (resultType) {
            case "CREATE":
            case "CREATE_TASK":
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
            case "UPDATE_TASK":
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
            case "DELETE_TASK":
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
        if (taskId == null && changeData.containsKey("entityId")) {
            taskId = (String) changeData.get("entityId");
        }
        if (taskId == null) return;

        // Convert server data to Task object (supports nested data payloads)
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
     * Fetches all tasks from the server and merges them with local tasks.
     */


    /**
     * Converts server change data to a Task object.
     */
    private Task convertServerDataToTask(Map<String, Object> data) {
        try {
            String id = (String) data.get("task_id");
            if (id == null) id = (String) data.get("entityId");
            if (id == null) return null;

            // Support for nested payload under "data" with camelCase keys
            Map<String, Object> payload = data;
            if (data.containsKey("data") && data.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inner = (Map<String, Object>) data.get("data");
                payload = inner;
            }

            Task.Builder builder = new Task.Builder(id);

            // Title
            if (payload.containsKey("task_title")) {
                builder.taskTitle((String) payload.get("task_title"));
            } else if (payload.containsKey("title")) {
                builder.taskTitle((String) payload.get("title"));
            }
            // Description
            if (payload.containsKey("description")) {
                builder.description((String) payload.get("description"));
            }
            // Status
            if (payload.containsKey("status")) {
                try {
                    builder.status(TaskStatus.valueOf((String) payload.get("status")));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid status value: " + payload.get("status"));
                    builder.status(TaskStatus.pending);
                }
            }
            // Dates
            // Dates (support ISO8601 with timezone offsets)
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
            // Folder
            if (payload.containsKey("folder_id")) {
                builder.folderId((String) payload.get("folder_id"));
            } else if (payload.containsKey("folderId")) {
                builder.folderId((String) payload.get("folderId"));
            }
            if (payload.containsKey("folder_name")) {
                builder.folderName((String) payload.get("folder_name"));
            }

            builder.sync_status("cloud");
            return builder.build();
        } catch (Exception e) {
            System.err.println("Error converting server data to task: " + e.getMessage());
            return null;
        }
    }

    // Parse ISO-8601 strings, including timezone offsets, to LocalDateTime using Java time
    private LocalDateTime parseDateTime(Object val) {
        if (val == null) return null;
        String s = String.valueOf(val);
        try {
            return OffsetDateTime.parse(s).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(s);
            } catch (DateTimeParseException e2) {
                System.err.println("SyncService: Failed to parse datetime '" + s + "'");
                return null;
            }
        }
    }

    // Post-sync: pull notifications and ack them
    private void fetchAndProcessNotifications() throws IOException, InterruptedException {
        List<Map<String, Object>> notifications = APIService.fetchPendingNotifications();
        if (notifications == null || notifications.isEmpty()) {
            System.out.println("SyncService: No pending notifications");
            return;
        }

        System.out.println("SyncService: Processing " + notifications.size() + " notifications");
        List<String> deliveredIds = new ArrayList<>();

        for (Map<String, Object> n : notifications) {
            String notifId = n.get("notification_id") != null ? String.valueOf(n.get("notification_id")) : null;
            String eventType = n.get("event_type") != null ? String.valueOf(n.get("event_type")) : null;
            String entityType = n.get("entity_type") != null ? String.valueOf(n.get("entity_type")) : null;
            String entityId = n.get("entity_id") != null ? String.valueOf(n.get("entity_id")) : null;

            @SuppressWarnings("unchecked")
            Map<String, Object> eventData = n.get("event_data") instanceof Map ? (Map<String, Object>) n.get("event_data") : null;

            System.out.println("SyncService: Notification - id=" + notifId + ", type=" + eventType + ", entity=" + entityType + ":" + entityId);

            if ("task".equalsIgnoreCase(entityType)) {
                if ("task_deleted".equalsIgnoreCase(eventType)) {
                    if (entityId != null) {
                        taskHandler.userTasksList.removeIf(t -> t.getTask_id().equals(entityId));
                    }
                } else if ("task_updated".equalsIgnoreCase(eventType) || "task_created".equalsIgnoreCase(eventType) || "SYNC_TASK".equalsIgnoreCase(eventType)) {
                    Map<String, Object> wrapper = new HashMap<>();
                    if (entityId != null) wrapper.put("entityId", entityId);
                    if (eventData != null) wrapper.put("data", eventData);
                    Task serverTask = convertServerDataToTask(wrapper);
                    if (serverTask != null) {
                        Task existing = taskHandler.getTaskById(serverTask.getTask_id());
                        if (existing == null) {
                            taskHandler.userTasksList.add(serverTask);
                        } else {
                            int idx = taskHandler.userTasksList.indexOf(existing);
                            if (idx >= 0) taskHandler.userTasksList.set(idx, serverTask);
                        }
                    }
                }
            }

            if (notifId != null) deliveredIds.add(notifId);
        }

        if (!deliveredIds.isEmpty()) {
            boolean ack = APIService.markNotificationsDelivered(deliveredIds);
            System.out.println("SyncService: Notifications ack result: " + ack + " (count=" + deliveredIds.size() + ")");
        }
    }
}