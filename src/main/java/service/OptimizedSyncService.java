package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import service.sync.LogTasksUtil;
import service.sync.TaskAssembler;
import model.Folder;
import model.TaskHandlerV2;
import model.commands.Command;
import service.sync.CommandConverter;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Optimized database-direct sync service that uses PostgreSQL functions for efficient sync operations.
 * This service leverages the todo schema functions for:
 * - Batch command processing via todo.merge_task_commands()
 * - Conditional data fetching via todo.retrieve_tasks_modified_since_in_jsonb()
 * - Efficient notification handling via todo.get_pending_notifications()
 */
public class OptimizedSyncService {
    private final TaskHandlerV2 taskHandler;
    private final String userId;
    private final ObjectMapper objectMapper;
    private Connection dbConnection;
    private final java.util.concurrent.atomic.AtomicBoolean syncRunning = new java.util.concurrent.atomic.AtomicBoolean(false);
    private final java.util.concurrent.atomic.AtomicBoolean rerunRequested = new java.util.concurrent.atomic.AtomicBoolean(false);
    private static final int BATCH_SIZE = 50;
    
    public OptimizedSyncService(TaskHandlerV2 taskHandler, Connection dbConnection) {
        this.taskHandler = taskHandler;
        this.userId = taskHandler.getUserId();
        this.dbConnection = dbConnection;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * Optimized sync process using PostgreSQL functions
     */
    public CompletableFuture<SyncResult> performOptimizedSync() {
        return CompletableFuture.supplyAsync(() -> {
            if (!syncRunning.compareAndSet(false, true)) {
                rerunRequested.set(true);
                SyncResult skipped = new SyncResult();
                skipped.success = true;
                return skipped;
            }
            try {
                SyncResult result = new SyncResult();
                if (!isValidUUID(userId) || dbConnection == null) {
                    // Skip DB sync when userId is not a UUID (e.g., tests) or DB not configured
                    return result;
                }
                
                // 1. Get pending commands from command queue
                List<Command> pendingCommands = taskHandler.getCommandQueue().getPendingCommands();
                if (!pendingCommands.isEmpty()) {
                    result.commandsSynced = syncCommandsToDatabase(pendingCommands);
                }
                
                // 2. Fetch incremental changes from server
                // IMPORTANT: For first sync we must pass NULL to the DB function to retrieve ALL tasks.
                // Using EPOCH may not be treated as full fetch by the function.
                LocalDateTime lastSync = taskHandler.getLastSync();
                result.tasksReceived = fetchIncrementalChanges(lastSync); // null => full fetch
                
                // 3. Handle notifications efficiently
                result.notificationsProcessed = processNotifications();
                
                // 4. Update folder cache if needed
                updateFolderCacheIfNeeded();
                
                // Fallback bootstrap: if no tasks were retrieved and local storage is empty, fetch all visible tasks
                try {
                    List<model.Task> localNow = taskHandler.getAllTasks();
                    if (result.tasksReceived == 0 && (localNow == null || localNow.isEmpty())) {
                        int fetched = fetchAllTasksForUser();
                        result.tasksReceived += fetched;
                    }
                } catch (Exception e) {
                    System.err.println("Bootstrap fetchAllTasksForUser failed: " + e.getMessage());
                }

                // 5. Mark sync complete
                taskHandler.setLastSync(LocalDateTime.now());
                
                return result;
                
            } catch (SQLException e) {
                System.err.println("DB sync error: " + e.getMessage());
                throw new RuntimeException("Database sync failed", e);
            } finally {
                syncRunning.set(false);
                if (rerunRequested.getAndSet(false)) {
                    performOptimizedSync();
                }
            }
        });
    }

    private int fetchAllTasksForUser() throws SQLException {
        // Full fetch: pass NULL timestamp so the DB returns all accessible tasks for the user
        String sql = "SELECT * FROM todo.retrieve_tasks_modified_since_in_jsonb(?::uuid, ?::timestamptz)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setObject(1, UUID.fromString(userId));
            stmt.setNull(2, java.sql.Types.TIMESTAMP);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JsonNode node = parseFunctionRow(rs);
                    if (node != null) {
                        JsonNode data = node.get("data");
                        int count = (data != null && data.isArray()) ? data.size() : 0;
                        if (count > 0) applyIncrementalChanges(node);
                        System.out.println("Bootstrap fetched tasks (via function, NULL ts): " + count);
                        return count;
                    }
                }
            } catch (Exception ex) {
                throw new SQLException("Failed to parse result JSON for full fetch", ex);
            }
        }
        return 0;
    }

    private static boolean isValidUUID(String s) {
        if (s == null) return false;
        try { java.util.UUID.fromString(s); return true; } catch (IllegalArgumentException ex) { return false; }
    }
    
    /**
     * Sync commands to database using todo.merge_task_commands()
     */
    private int syncCommandsToDatabase(List<Command> commands) throws SQLException {
        if (commands.isEmpty()) return 0;        
        List<List<Command>> batches = createBatches(commands, BATCH_SIZE);
        int totalSynced = 0;
        
        for (List<Command> batch : batches) {
            JsonNode commandsBatch = buildCommandsBatch(batch);
            JsonNode result = callMergeTaskCommands(commandsBatch);
            totalSynced += processSyncResult(result, batch);
        }
        return totalSynced;
    }

    /** Build the JSON batch payload expected by todo.merge_task_commands */
    private JsonNode buildCommandsBatch(List<Command> commands) {
        // Convert each queued Command into a transport SyncCommand
        ArrayNode commandsArray = objectMapper.createArrayNode();
        for (Command c : commands) {
            var syncCmd = CommandConverter.toSyncCommand(c);
            if (syncCmd != null) {
                commandsArray.add(objectMapper.valueToTree(syncCmd));
            }
        }
        // Wrap in an object to allow future metadata without breaking the function
        ObjectNode root = objectMapper.createObjectNode();
        root.put("user_id", userId);
        root.set("commands", commandsArray);
        // Optional hints; DB function may ignore
        LocalDateTime last = taskHandler.getLastSync();
        if (last != null) root.put("last_sync", last.toString());
        return root;
    }

    /** 
     * Call todo.merge_task_commands(user_id uuid, payload jsonb) and parse JSON result 
     */
    private JsonNode callMergeTaskCommands(JsonNode commandsBatch) throws SQLException {
        final String sql = "SELECT todo.merge_task_commands(?::uuid, ?::jsonb)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setObject(1, UUID.fromString(userId));
            stmt.setString(2, commandsBatch.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String resultJson = rs.getString(1);
                    if (resultJson != null && !resultJson.isEmpty()) {
                        return objectMapper.readTree(resultJson);
                    }
                }
            }
        } catch (Exception e) {
            throw new SQLException("Failed to call merge_task_commands", e);
        }
        return objectMapper.createObjectNode();
    }
    
    /**
     * Process sync result from database
     */
    private int processSyncResult(JsonNode result, List<Command> originalCommands) {
        int processed = 0;
        JsonNode success = result.get("success");
        if (success != null && success.isArray()) {
            Set<String> successfulCommandIds = new HashSet<>();
            for (JsonNode successItem : success) {
                String commandId = successItem.get("commandId").asText();
                successfulCommandIds.add(commandId);
            }
            taskHandler.getCommandQueue().removeCommands(successfulCommandIds);
            
            processed = successfulCommandIds.size();

            // Echo fetch: ensure local store reflects server truth for created/updated tasks
            try {
                List<String> idsToFetch = new ArrayList<>();
                for (Command c : originalCommands) {
                    if (!successfulCommandIds.contains(c.getCommandId())) continue;
                    switch (c.getType()) {
                        case CREATE_TASK:
                        case UPDATE_TASK:
                            if (c.getEntityId() != null) idsToFetch.add(c.getEntityId());
                            break;
                        case DELETE_TASK:
                            // Proactively remove locally to reflect deletion
                            if (c.getEntityId() != null) {
                                taskHandler.removeTaskById(c.getEntityId());
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (!idsToFetch.isEmpty()) {
                    int fetched = fetchTasksByIds(idsToFetch);
                    if (fetched > 0) {
                        System.out.println("Post-merge echo fetched tasks: " + fetched);
                    }
                }
            } catch (Exception e) {
                System.err.println("Post-merge echo fetch failed: " + e.getMessage());
            }
        }
        
        JsonNode conflicts = result.get("conflicts");
        if (conflicts != null && conflicts.isArray()) {
            processConflicts(conflicts);
        }
        
        JsonNode serverChanges = result.get("server_changes");
        if (serverChanges != null && serverChanges.isArray()) {
            applyServerChanges(serverChanges);
        }
        
        return processed;
    }

    /**
     * Fetch specific tasks by their IDs and upsert into local store.
     * Used as a post-merge echo to ensure locally created/updated tasks appear immediately.
     */
    private int fetchTasksByIds(List<String> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return 0;
        if (!isValidUUID(userId) || dbConnection == null) return 0;

        // Function-only approach: use incremental function around lastSync and filter by IDs
        LocalDateTime since = taskHandler.getLastSync() != null
                ? taskHandler.getLastSync().minusSeconds(5)
                : null; // for brand-new sessions, allow NULL full fetch then filter by IDs

        String sql = "SELECT * FROM todo.retrieve_tasks_modified_since_in_jsonb(?::uuid, ?::timestamptz)";

        int count = 0;
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setObject(1, UUID.fromString(userId));
            if (since != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(since));
            } else {
                stmt.setNull(2, java.sql.Types.TIMESTAMP);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JsonNode logTasks = LogTasksUtil.parseFunctionRow(rs, objectMapper);
                    if (logTasks != null) {
                        JsonNode data = logTasks.get("data");
                        if (data != null && data.isArray()) {
                            Set<String> idSet = new HashSet<>(ids);
                            for (JsonNode rawRow : data) {
                                JsonNode row = LogTasksUtil.normalizeRow(logTasks, rawRow, objectMapper);
                                String id = LogTasksUtil.textOf(row, "task_id", "id");
                                if (id == null || !idSet.contains(id)) continue;

                                boolean deleted = LogTasksUtil.boolOf(row, false, "deleted", "is_deleted");
                                if (!deleted && row.hasNonNull("deleted_at") && !row.get("deleted_at").asText().isEmpty()) {
                                    deleted = true;
                                }
                                if (deleted) {
                                    taskHandler.removeTaskById(id);
                                    continue;
                                }
                                model.Task existing = taskHandler.getTaskById(id);
                                model.Task task = TaskAssembler.mergeFromPayload(existing, row, taskHandler.getLastSync());
                                taskHandler.addOrReplaceTask(task);
                                count++;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                throw new SQLException("Failed to parse function result for echo fetch", ex);
            }
        }
        return count;
    }
    
    /**
     * Fetch incremental changes using todo.retrieve_tasks_modified_since_in_jsonb()
     */
    private int fetchIncrementalChanges(LocalDateTime lastSyncNullable) throws SQLException {
        if (!isValidUUID(userId) || dbConnection == null) return 0;
        String sql = "SELECT * FROM todo.retrieve_tasks_modified_since_in_jsonb(?::uuid, ?::timestamptz)";

        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setObject(1, UUID.fromString(userId));
            if (lastSyncNullable != null) stmt.setTimestamp(2, Timestamp.valueOf(lastSyncNullable));
            else stmt.setNull(2, java.sql.Types.TIMESTAMP);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JsonNode node = LogTasksUtil.parseFunctionRow(rs, objectMapper);
                    if (node == null) return 0;
                    JsonNode data = node.get("data");
                    int count = (data != null && data.isArray()) ? data.size() : 0;
                    if (count > 0) applyIncrementalChanges(node);
                    return count;
                }
            }
        } catch (Exception e) {
            throw new SQLException("Failed to fetch incremental changes", e);
        }

        return 0;
    }

    // Parse one row from the function which may return either:
    // - columns: jsonb, data: jsonb, last_sync: text/timestamptz
    // - OR a single json/jsonb column named result/log_tasks
    private JsonNode parseFunctionRow(ResultSet rs) { return LogTasksUtil.parseFunctionRow(rs, objectMapper); }
    
    /**
     * Process notifications using todo.get_pending_notifications()
     */
    private int processNotifications() throws SQLException {
        if (!isValidUUID(userId) || dbConnection == null) return 0;
        String sql = "SELECT * FROM todo.get_pending_notifications(?::uuid, ?::timestamptz)";
        List<UUID> processedNotifications = new ArrayList<>();
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setObject(1, UUID.fromString(userId));
            stmt.setTimestamp(2, taskHandler.getLastSync() != null ? 
                Timestamp.valueOf(taskHandler.getLastSync()) : null);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID notificationId = (UUID) rs.getObject("notification_id");
                    String eventType = rs.getString("event_type");
                    UUID entityId = (UUID) rs.getObject("entity_id");
                    String entityType = rs.getString("entity_type");
                    String eventData = rs.getString("event_data");
                    handleNotification(eventType, entityId, entityType, eventData);
                    processedNotifications.add(notificationId);
                }
            }
        }
        
        if (!processedNotifications.isEmpty()) {
            markNotificationsDelivered(processedNotifications);
        }
        
        return processedNotifications.size();
    }
    
    /**
     * Mark notifications as delivered using todo.mark_notifications_delivered()
     */
    private void markNotificationsDelivered(List<UUID> notificationIds) throws SQLException {
        String sql = "SELECT todo.mark_notifications_delivered(?::uuid[])";
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            Array uuidArray = dbConnection.createArrayOf("uuid", notificationIds.toArray());
            stmt.setArray(1, uuidArray);
            stmt.executeQuery();
        }
    }
    
    /**
     * Update folder cache conditionally
     */
    private void updateFolderCacheIfNeeded() {
        if (!isValidUUID(userId) || dbConnection == null) return;
        // Note: Current FolderCacheService implementation handles cache validity internally
        // This method would be enhanced when FolderCacheService API is expanded
        try {
            List<Folder> folders = fetchFoldersFromDatabase();
            if (folders != null && !folders.isEmpty()) {
                taskHandler.setFoldersList(folders);
            }
        } catch (SQLException e) {
            System.err.println("Failed to update folder cache: " + e.getMessage());
        }
    }
    
    // Helper methods for data conversion and processing
    
    private void processConflicts(JsonNode conflicts) {
        // Handle field-level conflicts with server resolution
        for (JsonNode conflict : conflicts) {
            System.out.println("Conflict detected: " + conflict.toString());
            //TODO: Implement conflict resolution strategy
        }
    }
    
    private void applyServerChanges(JsonNode serverChanges) {
        if (serverChanges == null || !serverChanges.isArray()) return;
        for (JsonNode change : serverChanges) {
            try {
                String entityId = LogTasksUtil.textOf(change, "entityId", "id", "task_id");
                JsonNode payload = change.get("data");
                if (payload == null) payload = change.get("task");
                if (payload == null) payload = change;

                boolean deleted = LogTasksUtil.boolOf(payload, false, "deleted", "is_deleted");
                if (!deleted && payload.hasNonNull("deleted_at") && !payload.get("deleted_at").asText().isEmpty()) {
                    deleted = true;
                }
                if (deleted) {
                    if (entityId != null) taskHandler.removeTaskById(entityId);
                    continue;
                }

                String id = entityId != null ? entityId : LogTasksUtil.textOf(payload, "task_id", "id");
                if (id == null) continue;
                model.Task existing = taskHandler.getTaskById(id);
                model.Task task = TaskAssembler.mergeFromPayload(existing, payload, taskHandler.getLastSync());
                taskHandler.addOrReplaceTask(task);
                System.out.println("Applying server change for task: \"" + id + "\"");
            } catch (Exception ex) {
                System.err.println("Server change apply error: " + ex.getMessage());
            }
        }
    }
    
    private void applyIncrementalChanges(JsonNode logTasks) {
        if (logTasks == null) return;
        JsonNode data = logTasks.get("data");
        if (data == null || !data.isArray()) return;

        System.out.println("Applying " + data.size() + " incremental changes");
        for (JsonNode rawRow : data) {
            try {
                JsonNode row = LogTasksUtil.normalizeRow(logTasks, rawRow, objectMapper);
                String id = LogTasksUtil.textOf(row, "task_id", "id");
                if (id == null) continue;
                boolean deleted = LogTasksUtil.boolOf(row, false, "deleted", "is_deleted");
                if (!deleted && row.hasNonNull("deleted_at") && !row.get("deleted_at").asText().isEmpty()) {
                    deleted = true;
                }

                if (deleted) {
                    taskHandler.removeTaskById(id);
                    continue;
                }
                model.Task existing = taskHandler.getTaskById(id);
                model.Task task = TaskAssembler.mergeFromPayload(existing, row, taskHandler.getLastSync());
                taskHandler.addOrReplaceTask(task);
            } catch (Exception ex) {
                System.err.println("Incremental apply error: " + ex.getMessage());
            }
        }
    }
    
    private void handleNotification(String eventType, UUID entityId, String entityType, String eventData) {
        switch (eventType) {
            case "task_created":
            case "task_updated":
            case "task_deleted":
                // Refresh local data for the entity
                break;
            default:
                System.out.println("Unknown notification type: " + eventType);
        }
    }
    
    private List<Folder> fetchFoldersFromDatabase() throws SQLException {
        // Fetch folders from database via function to respect sharing/access rules
        List<Folder> folders = new ArrayList<>();
        String sql = "SELECT * FROM todo.get_accessible_folders(?::uuid)";

        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setObject(1, UUID.fromString(userId));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String id = getColumnValue(rs, "folder_id", "id");
                    if (id == null) continue;
                    String name = getColumnValue(rs, "folder_name", "name", "title");

                    LocalDateTime createdAt = getTimestamp(rs, "created_at");
                    LocalDateTime deletedAt = getTimestamp(rs, "deleted_at");
                    LocalDateTime lastSync = getTimestamp(rs, "last_sync");

                    Folder folder = new Folder.Builder(id)
                            .folderName(name)
                            .createdAt(createdAt)
                            .deletedAt(deletedAt)
                            .lastSync(lastSync)
                            .build();
                    folders.add(folder);
                }
            }
        }

        return folders;
    }

    // removed legacy local helpers; using services in service.sync package

    private static String getColumnValue(ResultSet rs, String... names) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int c = md.getColumnCount();
            for (String n : names) {
                for (int i = 1; i <= c; i++) {
                    if (n.equalsIgnoreCase(md.getColumnLabel(i))) {
                        return rs.getString(i);
                    }
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    private static LocalDateTime getTimestamp(ResultSet rs, String name) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int c = md.getColumnCount();
            for (int i = 1; i <= c; i++) {
                if (name.equalsIgnoreCase(md.getColumnLabel(i))) {
                    Timestamp ts = rs.getTimestamp(i);
                    return ts != null ? ts.toLocalDateTime() : null;
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    

    // If the DB function returns rows as arrays with a parallel "columns" list, normalize each row
    // into an object-like node so downstream code can read by field names.
    // prefer LogTasksUtil.normalizeRow
    
    private List<List<Command>> createBatches(List<Command> commands, int batchSize) {
        List<List<Command>> batches = new ArrayList<>();
        for (int i = 0; i < commands.size(); i += batchSize) {
            int end = Math.min(i + batchSize, commands.size());
            batches.add(commands.subList(i, end));
        }
        return batches;
    }
    
    /**
     * Result class for sync operations
     */
    public static class SyncResult {
        public int commandsSynced = 0;
        public int tasksReceived = 0;
        public int notificationsProcessed = 0;
        public boolean success = true;
        public String errorMessage = null;
        
        @Override
        public String toString() {
            return String.format("SyncResult{commandsSynced=%d, tasksReceived=%d, notificationsProcessed=%d, success=%s}", 
                commandsSynced, tasksReceived, notificationsProcessed, success);
        }
    }
}
