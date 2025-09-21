package service;

import model.Task;
import model.Folder;
import model.TaskHandlerV2;
import model.commands.CommandQueue;
import model.commands.Command;
import service.sync.CommandConverter;
import service.sync.ResponseApplier;
import model.sync.CommandBatch;
import model.sync.SyncCommand;
import model.sync.SyncResponse;

import java.io.IOException;
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
    private TaskHandlerV2 taskHandlerV2; // New command-based handler
    private String userUUID;
    public SyncService(TaskHandlerV2 taskHandlerV2) {
        this.taskHandlerV2 = taskHandlerV2;
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
    java.time.OffsetDateTime syncStartTime = java.time.OffsetDateTime.now();
    java.time.LocalDateTime lastSync = taskHandlerV2.getLastSync();

    // Build command batch from the command queue
    List<SyncCommand> commands = buildSyncCommands();
        
        System.out.println("SyncService: Built " + commands.size() + " commands for sync");

    // Convert local lastSync (LocalDateTime) to OffsetDateTime for transport if present
    java.time.OffsetDateTime lastSyncOffset = lastSync != null ? lastSync.atOffset(java.time.ZoneOffset.systemDefault().getRules().getOffset(java.time.Instant.now())) : null;

        CommandBatch batch = new CommandBatch(
            userUUID,
            syncStartTime,
            lastSyncOffset,
            commands
        );
        
        // Optimization: Add folder version to request for conditional fetching
        List<Folder> cachedFolders = taskHandlerV2.getFoldersList();
        if (!cachedFolders.isEmpty()) {
            // Get current folder version from cache (would be provided by server in real impl)
            String currentFolderVersion = String.valueOf(cachedFolders.hashCode()); // Simplified version
            batch.setFolderVersion(currentFolderVersion);
        } else {
            // First sync - request folders
            batch.setIncludeFolders(true);
        }

        // Debug: Log the request being sent
        System.out.println("SyncService: Sending optimized batch to API:");
        System.out.println("  - User UUID: " + userUUID);
        System.out.println("  - Client Timestamp: " + syncStartTime);
        System.out.println("  - Last Sync: " + lastSync);
        System.out.println("  - Command Count: " + commands.size());
        System.out.println("  - Folder Version: " + batch.getFolderVersion());
        System.out.println("  - Include Folders: " + batch.isIncludeFolders());
        
        if (!commands.isEmpty()) {
            System.out.println("  - Commands: ");
            for (SyncCommand cmd : commands) {
                System.out.println("    * Type: " + cmd.getType() + ", EntityId: " + cmd.getEntityId() + ", CommandId: " + cmd.getCommandId());
            }
        }

        // Send to API
        try {
            // Debug: print detailed changedFields contents for UPDATE_TASK commands
            if (!commands.isEmpty()) {
                for (SyncCommand scmd : commands) {
                    if ("UPDATE_TASK".equals(scmd.getType())) {
                        Object data = scmd.getData();
                        Object changedFields = scmd.getChangedFields();
                        System.out.println("SyncService: UPDATE_TASK payload for entity " + scmd.getEntityId() + ": data=" + data + ", changedFields=" + changedFields);
                    }
                }
            }
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

            // Delegate handling to a helper to make it testable
            handleSyncResponse(response, syncStartTime);
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

        // Build commands from TaskHandlerV2's command queue
        if (taskHandlerV2 == null) {
            throw new IllegalStateException("SyncService requires a TaskHandlerV2 instance");
        }
        {
            System.out.println("SyncService: Building commands from CommandQueue");
            CommandQueue commandQueue = taskHandlerV2.getCommandQueue();
            List<Command> pendingCommands = commandQueue.getPendingCommands();
            
            System.out.println("SyncService: Found " + pendingCommands.size() + " pending commands");
            
            for (Command command : pendingCommands) {
                SyncCommand syncCommand = CommandConverter.toSyncCommand(command);
                if (syncCommand != null) commands.add(syncCommand);
            }
    }

        return commands;
    }

    /**
     * Convert a Command from the queue to a SyncCommand for API transmission
     */
    // Command conversion delegated to CommandConverter

    // Response processing is delegated to ResponseApplier

    // Processing of command results and server changes is handled by ResponseApplier

    // Fetch/merge helpers moved to ResponseApplier

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
                        taskHandlerV2.removeTaskById(entityId);
                    }
                } else if ("task_updated".equalsIgnoreCase(eventType) || "task_created".equalsIgnoreCase(eventType) || "SYNC_TASK".equalsIgnoreCase(eventType)) {
                    Map<String, Object> wrapper = new HashMap<>();
                    if (entityId != null) wrapper.put("entityId", entityId);
                    if (eventData != null) wrapper.put("data", eventData);
                    Task serverTask = ResponseApplier.convertServerDataToTask(wrapper);
                    if (serverTask != null) {
                        Task existing = taskHandlerV2.getTaskById(serverTask.getTask_id());
                        if (existing == null) {
                            taskHandlerV2.addOrReplaceTask(serverTask);
                        } else {
                            taskHandlerV2.addOrReplaceTask(serverTask);
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

    /**
     * Handle a SyncResponse: apply server changes, persist failures, remove acked commands,
     * fetch notifications, and update last sync time. Extracted for testability.
     */
    public void handleSyncResponse(model.sync.SyncResponse response, java.time.OffsetDateTime syncStartTime) {
        if (response == null) return;

        // Always apply server changes so local state is updated even when some commands failed
        try {
            new ResponseApplier(taskHandlerV2).apply(response);
        } catch (Exception apEx) {
            System.err.println("SyncService: Error applying server changes: " + apEx.getMessage());
        }

        // Remove acknowledged commands (both processed successes and explicit failures) to avoid retry storms
        java.util.Set<String> toRemove = new java.util.HashSet<>();
        if (response.getProcessedCommands() != null) {
            for (model.sync.SyncResponse.CommandResult r : response.getProcessedCommands()) {
                if (r.getClientId() != null) toRemove.add(r.getClientId());
            }
        }
        // Persist failed command details for diagnostics and also remove them from the queue
        if (response.getFailedCommands() != null && !response.getFailedCommands().isEmpty()) {
            java.util.List<java.util.Map<String, Object>> failures = new java.util.ArrayList<>();
            for (model.sync.SyncResponse.CommandResult r : response.getFailedCommands()) {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("clientId", r.getClientId());
                m.put("commandType", r.getCommandType());
                m.put("entityId", r.getEntityId());
                m.put("error", r.getErrorMessage());
                failures.add(m);
                if (r.getClientId() != null) toRemove.add(r.getClientId());
            }
            // Write failures to user data for later inspection
            try {
                String path = COMMON.UserProperties.getUserDataFilePath(userUUID, "failed_commands.json");
                java.util.Map<String, Object> report = new java.util.LinkedHashMap<>();
                report.put("timestamp", java.time.OffsetDateTime.now().toString());
                report.put("failures", failures);
                COMMON.JSONUtils.writeJsonFile(report, path);
                System.err.println("SyncService: Persisted failed command report to: " + path);
            } catch (Exception wex) {
                System.err.println("SyncService: Could not persist failed commands: " + wex.getMessage());
            }
        }

        if (!toRemove.isEmpty()) {
            try {
                taskHandlerV2.getCommandQueue().removeCommands(toRemove);
            } catch (Exception remEx) {
                System.err.println("SyncService: Error removing commands from queue: " + remEx.getMessage());
            }
        }

        // After processing commands and server changes, pull notifications (if any)
        try {
            fetchAndProcessNotifications();
        } catch (Exception notifEx) {
            System.err.println("SyncService: Notification processing failed: " + notifEx.getMessage());
        }

        // Update last sync time - convert server timestamp (OffsetDateTime) to LocalDateTime
        if (response.getServerTimestamp() != null) {
            taskHandlerV2.setLastSync(response.getServerTimestamp().toLocalDateTime());
        } else {
            taskHandlerV2.setLastSync(syncStartTime.toLocalDateTime());
        }
        if (response.getFailedCommands() != null && !response.getFailedCommands().isEmpty()) {
            System.err.println("SyncService: Sync completed with failures. See failed_commands.json for details.");
        } else {
            System.out.println("SyncService: API V2 sync successful");
        }
    }
}