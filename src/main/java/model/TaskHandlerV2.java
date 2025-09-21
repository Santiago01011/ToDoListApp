package model;

import model.commands.*;
import service.FolderCacheService;
import service.BatchPersistenceService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.ArrayList;

/**
 * Next-generation TaskHandler that implements the Command Queue pattern
 * for robust offline-first operation and conflict-free synchronization.
 * 
 * This class provides backward compatibility with the existing TaskHandler
 * while introducing the new command-driven architecture.
 */
public class TaskHandlerV2 {
    private final CommandQueue commandQueue;
    private final String userId;
    private final FolderCacheService folderCache;
    private final BatchPersistenceService batchPersistence;
    private List<Folder> userFoldersList = new java.util.ArrayList<>();

    // New local storage (replace legacy TaskHandler usage)
    private List<Task> userTasksList = new java.util.ArrayList<>();
    private String tasksJsonFile;
    private java.time.LocalDateTime lastSync = null;
    
    /**
     * Create a new TaskHandlerV2 instance with optimizations enabled
     * 
     * @param userId The current user's ID
     */    
    public TaskHandlerV2(String userId) {
        this.userId = userId;
        this.commandQueue = new CommandQueue(userId);
        this.folderCache = new FolderCacheService(userId);
        this.batchPersistence = new BatchPersistenceService(2000); // 2 second batch delay
        this.batchPersistence.setTaskHandler(this);
        this.tasksJsonFile = COMMON.UserProperties.getUserDataFilePath(userId, "tasks.json");
        
        try {
            System.out.println("TaskHandlerV2: forcePersistence -> target file: " + tasksJsonFile);
            List<Task> loaded = loadTasksFromJson();
            if (loaded != null) this.userTasksList = loaded;
        } catch (Exception e) {
            System.err.println("TaskHandlerV2: failed to load tasks: " + e.getMessage());
            this.userTasksList = new java.util.ArrayList<>();
        }
        
        // Register shutdown hook for proper cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("TaskHandlerV2: Shutting down...");
            batchPersistence.shutdown();
        }));
    }
    

    /**
     * Add or replace a task in the underlying storage (used when applying server changes).
     */
    public synchronized void addOrReplaceTask(Task task) {
        Task existing = getTaskById(task.getTask_id());
        if (existing == null) {
            userTasksList.add(task);
        } else {
            int idx = userTasksList.indexOf(existing);
            if (idx >= 0) userTasksList.set(idx, task);
            else userTasksList.add(task);
        }
        saveTasksToJsonAsync();
    }

    /**
     * Remove a task by id from underlying storage.
     */
    public synchronized void removeTaskById(String taskId) {
        userTasksList.removeIf(t -> t.getTask_id().equals(taskId));
        saveTasksToJsonAsync();
    }

    /**
     * Update a local task's id (used when server assigns a definitive id for a newly created task).
     */
    public synchronized void updateTaskId(String clientId, String serverId) {
        Task existing = getTaskById(clientId);
        
        if (existing == null) return;

        Task updated = existing.toBuilder()
            .folderId(existing.getFolder_id())
            .folderName(existing.getFolder_name())
            .sync_status("cloud")
            .lastSync(getLastSync())
            .build();
        int idx = userTasksList.indexOf(existing);
        if (idx >= 0) userTasksList.set(idx, updated);
    }

    /**
     * Mark a task as synced (set sync_status to 'cloud' and update last_sync).
     */
    public synchronized void markTaskSynced(String taskId) {
        Task t = getTaskById(taskId);
        if (t != null) {
            Task updated = t.toBuilder().sync_status("cloud").lastSync(getLastSync()).build();
            int idx = userTasksList.indexOf(t);
            if (idx >= 0) userTasksList.set(idx, updated);
            saveTasksToJsonAsync();
        }
    }

    /**
     * Get a task by id from underlying storage.
     */
    public Task getTaskById(String taskId) {
        return userTasksList.stream()
            .filter(task -> task.getTask_id().equals(taskId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get all tasks for the current user.
     * If command queue is enabled, returns the projected state with pending commands applied.
     */
    public List<Task> getAllTasks() {
        List<Task> baseTasks = new ArrayList<>(userTasksList);
        List<Task> projected = commandQueue.getProjectedTasks(baseTasks);

        // Build a lookup map folder_id -> folder_name using cached folders
        Map<String, String> folderIdToName = new HashMap<>();
        
        // First, try to use cached folders for better performance
        List<Folder> cachedFolders = folderCache.getCachedFolders();
        for (Folder f : cachedFolders) {
            if (f != null && f.getFolder_id() != null) {
                folderIdToName.put(f.getFolder_id(), f.getFolder_name());
            }
        }
        
        // Fallback to in-memory folders if cache is empty
        if (folderIdToName.isEmpty() && userFoldersList != null) {
            for (Folder f : userFoldersList) {
                if (f != null && f.getFolder_id() != null) {
                    folderIdToName.put(f.getFolder_id(), f.getFolder_name());
                }
            }
        }

        // Resolve folder_name for projected tasks when missing but folder_id is present
        List<Task> resolved = new ArrayList<>();
        for (Task t : projected) {
            if ((t.getFolder_name() == null || t.getFolder_name().isEmpty()) && t.getFolder_id() != null) {
                // Try cache first for optimal performance
                String resolvedName = folderCache.getFolderName(t.getFolder_id()).orElse(null);
                if (resolvedName == null) {
                    // Fallback to in-memory lookup
                    resolvedName = folderIdToName.get(t.getFolder_id());
                }
                
                if (resolvedName != null) {
                    resolved.add(t.toBuilder().folderName(resolvedName).build());
                    continue;
                }
            }
            resolved.add(t);
        }

        return resolved;
    }
    
    /**
     * Create a new task
     */
    public Task createTask(String title, String description, TaskStatus status, 
                          LocalDateTime dueDate, String folderId) {
        String taskId = UUID.randomUUID().toString();
        
        // Create and enqueue a CreateTaskCommand
        CreateTaskCommand command = CreateTaskCommand.create(
            taskId, userId, title, description, status, dueDate, folderId
        );
        commandQueue.enqueue(command);

        // Resolve folder name from folder ID for local display
        String folderName = null;
        if (folderId != null) {
            // Try cache first for optimal performance
            folderName = folderCache.getFolderName(folderId).orElse(null);
            if (folderName == null) {
                // Fallback to in-memory lookup
                folderName = userFoldersList.stream()
                    .filter(f -> folderId.equals(f.getFolder_id()))
                    .findFirst()
                    .map(Folder::getFolder_name)
                    .orElse(null);
            }
        }

        // Return the projected view
        Task task = new Task.Builder(taskId)
            .taskTitle(title)
            .description(description)
            .status(status)
            .sync_status("pending")
            .dueDate(dueDate)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .folderId(folderId)
            .folderName(folderName)
            .build();
        
        // Add task to local list immediately for offline-first operation
        userTasksList.add(task);
        saveTasksToJsonAsync();
        
        return task;
    }
    
    /**
     * Update an existing task
     */
    public void updateTask(Task task, String title, String description, TaskStatus status, 
                          LocalDateTime dueDate, String folderName) {
        Map<String, Object> changes = buildChangeMap(title, description, status, dueDate, folderName);
        if (!changes.isEmpty()) {
            UpdateTaskCommand command = UpdateTaskCommand.create(
                task.getTask_id(), userId, changes
            );
            commandQueue.enqueue(command);
            
            // Apply changes locally immediately for offline-first operation
            applyUpdateToLocalTask(task, changes);
        }
    }
    
    /**
     * Apply updates to the local task list immediately (for offline-first operation)
     */
    private void applyUpdateToLocalTask(Task originalTask, Map<String, Object> changes) {
        Task.Builder builder = originalTask.toBuilder();
        
        if (changes.containsKey("title")) {
            builder.taskTitle((String) changes.get("title"));
        }
        if (changes.containsKey("description")) {
            builder.description((String) changes.get("description"));
        }
        if (changes.containsKey("status")) {
            builder.status((TaskStatus) changes.get("status"));
        }
        if (changes.containsKey("dueDate")) {
            Object dueDateValue = changes.get("dueDate");
            if (dueDateValue instanceof LocalDateTime) {
                builder.dueDate((LocalDateTime) dueDateValue);
            }
        }
        if (changes.containsKey("folderId")) {
            String folderId = (String) changes.get("folderId");
            builder.folderId(folderId);
            
            // Also resolve folder name for display
            String folderName = userFoldersList.stream()
                .filter(f -> folderId.equals(f.getFolder_id()))
                .findFirst()
                .map(Folder::getFolder_name)
                .orElse(null);
            if (folderName != null) {
                builder.folderName(folderName);
            }
        }
        
        // Update timestamp and sync status
        builder.updatedAt(LocalDateTime.now());
        builder.sync_status("pending");
        
        Task updatedTask = builder.build();
        
        // Replace in the local task list
        for (int i = 0; i < userTasksList.size(); i++) {
            Task taskInList = userTasksList.get(i);
            if (taskInList.getTask_id().equals(originalTask.getTask_id())) {
                userTasksList.set(i, updatedTask);
                break;
            }
        }
        
        // Schedule persistence
        saveTasksToJsonAsync();
    }
    
    /**
     * Delete a task (soft delete)
     */
    public void deleteTask(Task task) {
        deleteTask(task, "User deleted");
    }
    
    /**
     * Delete a task with a reason (soft delete)
     */
    public void deleteTask(Task task, String reason) {
        // Enqueue delete command
        DeleteTaskCommand command = DeleteTaskCommand.create(
            task.getTask_id(), userId, reason
        );
        commandQueue.enqueue(command);
    }
    
    /**
     * Get the count of pending commands waiting for synchronization
     */
    public int getPendingChangesCount() {
    return commandQueue.getPendingCommandCount();
    }
    
    /**
     * Check if there are any pending changes
     */
    public boolean hasPendingChanges() {
        return getPendingChangesCount() > 0;
    }
    
    /**
     * Get the command queue for advanced operations (sync, etc.)
     */
    public CommandQueue getCommandQueue() {
        return commandQueue;
    }
    
    /**
     * Schedule batched save to JSON for better performance
     */
    public void saveTasksToJson() {
        // Explicit save is synchronous to ensure file exists immediately (e.g., tests, manual saves)
        batchPersistence.flushImmediately();
    }

    /**
     * Schedule batched save to JSON for better performance (async).
     */
    public void saveTasksToJsonAsync() {
        batchPersistence.schedulePersistence();
    }
    
    /**
     * Force immediate persistence to JSON (used by batch service and critical operations)
     */
    public void forcePersistence() {
        try {
            // Persist the projected state (base tasks + pending commands applied)
            // so that newly created/updated/deleted tasks are reflected on disk even before sync.
            List<Task> toPersist = getAllTasks();

            java.util.Map<String, Object> structure = COMMON.JSONUtils.buildJsonStructure(toPersist.stream());
            structure.put("last_sync", getLastSync() != null ? getLastSync().toString() : null);
            // Persist folders list so folder metadata survives restarts
            structure.put("folders", userFoldersList != null ? userFoldersList : new ArrayList<>());

            // Ensure parent directories exist and create file if missing with default structure
            java.io.File outFile = new java.io.File(tasksJsonFile);
            if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
                boolean mk = outFile.getParentFile().mkdirs();
                System.out.println("TaskHandlerV2: Created parent directories: " + mk);
            }
            if (!outFile.exists()) {
                System.out.println("TaskHandlerV2: Creating default JSON file at: " + tasksJsonFile);
                COMMON.JSONUtils.createDefaultJsonFile(tasksJsonFile);
            }
            COMMON.JSONUtils.writeJsonFile(structure, tasksJsonFile);
            System.out.println("TaskHandlerV2: Wrote tasks JSON: exists=" + outFile.exists() + ", len=" + outFile.length());

        } catch (Exception e) {
            System.err.println("Error persisting tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load persisted tasks file
    private List<Task> loadTasksFromJson() {
        try {
            java.io.File f = new java.io.File(tasksJsonFile);
            if (!f.exists()) return new ArrayList<>();
            java.util.Map<String, Object> m = COMMON.JSONUtils.readJsonFile(f);
            java.util.List<java.util.List<Object>> rows = COMMON.JSONUtils.getTasksData(m);
            Object colsObj = m.get("columns");
            List<String> columns = null;
            if (colsObj instanceof List) {
                columns = new ArrayList<>();
                for (Object o : (List<?>) colsObj) {
                    columns.add(String.valueOf(o));
                }
            }
            List<Task> out = new ArrayList<>();
            // Load persisted folders list if present
            try {
                Object foldersObj = m.get("folders");
                if (foldersObj instanceof List) {
                    List<Folder> loadedFolders = new ArrayList<>();
                    List<?> foldersList = (List<?>) foldersObj;
                    for (Object fo : foldersList) {
                        try {
                            // Use ObjectMapper conversion for safety
                            Folder folder = COMMON.JSONUtils.getMapper().convertValue(fo, Folder.class);
                            if (folder != null) {
                                loadedFolders.add(folder);
                            }
                        } catch (Exception e) {
                            System.err.println("TaskHandlerV2: Failed to convert folder object: " + e.getMessage());
                        }
                    }
                    if (!loadedFolders.isEmpty()) {
                        this.userFoldersList = loadedFolders;
                    }
                }
            } catch (Exception e) {
                System.err.println("TaskHandlerV2: Error loading folders: " + e.getMessage());
            }
            if (rows != null && columns != null) {
                for (java.util.List<Object> row : rows) {
                    Task t = createTaskFromRow(columns, row);
                    if (t != null) out.add(t);
                }
            }
            return out;
        } catch (Exception e) {
            System.err.println("TaskHandlerV2: loadTasksFromJson error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // shadows removed — commands replace shadow persistence

    // Create Task from columns/row (same format used by JSONUtils.buildJsonStructure)
    private Task createTaskFromRow(List<String> columns, List<Object> row) {
        try {
            if (columns == null || row == null) return null;
            java.util.Map<String, Object> taskMap = new java.util.LinkedHashMap<>();
            for (int i = 0; i < Math.min(columns.size(), row.size()); i++) {
                taskMap.put(columns.get(i), row.get(i));
            }

            model.TaskStatus status = model.TaskStatus.pending;
            String statusStr = (String) taskMap.get("status");
            if (statusStr != null) {
                model.TaskStatus parsed = model.TaskStatus.parse(statusStr);
                if (parsed != null) status = parsed;
            }

            java.time.LocalDateTime dueDate = null;
            String dueDateStr = (String) taskMap.get("due_date");
            if (dueDateStr != null) {
                try { dueDate = java.time.LocalDateTime.parse(dueDateStr); } catch (Exception ignored) {}
            }

            java.time.LocalDateTime createdAt = null;
            String createdAtStr = (String) taskMap.get("created_at");
            if (createdAtStr != null) {
                try { createdAt = java.time.LocalDateTime.parse(createdAtStr); } catch (Exception ignored) {}
            }

            java.time.LocalDateTime lastSyncDt = null;
            String lastSyncStr = (String) taskMap.get("last_sync");
            if (lastSyncStr != null) {
                try { lastSyncDt = java.time.LocalDateTime.parse(lastSyncStr); } catch (Exception ignored) {}
            }

            java.time.LocalDateTime deletedAt = null;
            String deletedAtStr = (String) taskMap.get("deleted_at");
            if (deletedAtStr != null) {
                try { deletedAt = java.time.LocalDateTime.parse(deletedAtStr); } catch (Exception ignored) {}
            }

            return new Task.Builder((String) taskMap.get("task_id"))
                .taskTitle((String) taskMap.get("task_title"))
                .folderId((String) taskMap.get("folder_id"))
                .folderName((String) taskMap.get("folder_name"))
                .description((String) taskMap.get("description"))
                .sync_status((String) taskMap.get("sync_status"))
                .status(status)
                .dueDate(dueDate)
                .createdAt(createdAt)
                .updatedAt(lastSyncDt)
                .lastSync(lastSyncDt)
                .deletedAt(deletedAt)
                .build();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Build a map of changed fields for update commands
     */
    private Map<String, Object> buildChangeMap(String title, String description, 
                                             TaskStatus status, LocalDateTime dueDate, String folderName) {
        Map<String, Object> changes = new HashMap<>();
        
        if (title != null && !title.trim().isEmpty()) {
            changes.put("title", title);
        }
        if (description != null) {
            changes.put("description", description);
        }
        if (status != null) {
            changes.put("status", status);
        }
        if (dueDate != null) {
            changes.put("dueDate", dueDate);
        }
        if (folderName != null) {
            // Resolve folder name to folder ID when possible. The UI may pass a folder name.
            String folderId = null;
            try {
                folderId = getFolderIdByName(folderName);
            } catch (Exception ignored) {}
            // If not found, the caller may already be passing an ID; accept it if it looks like a UUID
            if (folderId == null) {
                if (folderName.matches("[0-9a-fA-F\\-]{36}")) {
                    folderId = folderName;
                }
            }
            if (folderId != null) {
                changes.put("folderId", folderId);
            } else {
                // As a fallback, still include the raw value (server will validate); prefer explicit mapping
                changes.put("folderId", folderName);
            }
        }
        
        return changes;
    }
    
    /**
     * Get the user ID for this handler
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Check if command queue mode is enabled
     */
    public boolean isCommandQueueEnabled() {
    return true;
    }
    
    /**
     * Get last sync time
     */
    public LocalDateTime getLastSync() {
    return lastSync;
    }
    
    /**
     * Set last sync time
     */
    public void setLastSync(LocalDateTime lastSync) {
    this.lastSync = lastSync;
    saveTasksToJsonAsync();
    }
    
    /**
     * Set folders list and update cache
     */
    public void setFoldersList(List<Folder> foldersList) {
        this.userFoldersList = (foldersList != null) ? new java.util.ArrayList<>(foldersList) : new java.util.ArrayList<>();
        // Update folder cache for better performance
        folderCache.updateFolders(this.userFoldersList);
    }
    
    /**
     * Set folders list with version tracking
     */
    public void setFoldersList(List<Folder> foldersList, String folderVersion) {
        this.userFoldersList = (foldersList != null) ? new java.util.ArrayList<>(foldersList) : new java.util.ArrayList<>();
        // Update folder cache with version for conditional fetching
        folderCache.updateFolders(this.userFoldersList, folderVersion);
    }

    /**
     * Get folders list (wrapper to avoid touching deprecated legacy API in callers)
     */
    public List<Folder> getFoldersList() {
        // Try cache first for better performance
        List<Folder> cached = folderCache.getCachedFolders();
        return !cached.isEmpty() ? cached : new java.util.ArrayList<>(userFoldersList);
    }

    /**
     * Get folder names list (wrapper)
     */
    public List<String> getFolderNamesList() {
        List<Folder> folders = getFoldersList();
        if (folders == null || folders.isEmpty()) return java.util.Collections.emptyList();
        return folders.stream().map(Folder::getFolder_name).toList();
    }
    
    /**
     * Check if we need to fetch folders based on version
     */
    public boolean needsFolderRefresh(String serverFolderVersion) {
        return !folderCache.hasVersion(serverFolderVersion);
    }

    /**
     * Resolve folder ID by name (wrapper)
     */
    public String getFolderIdByName(String folderName) {
        System.out.println("TaskHandlerV2.getFolderIdByName: Input folderName = '" + folderName + "'");
        System.out.println("TaskHandlerV2.getFolderIdByName: userFoldersList size = " + userFoldersList.size());
        
        if (folderName == null) return null;
        
        // Try in-memory list first
        String fromMemory = userFoldersList.stream()
                .filter(f -> folderName.equals(f.getFolder_name()))
                .findFirst()
                .map(Folder::getFolder_id)
                .orElse(null);
        System.out.println("TaskHandlerV2.getFolderIdByName: fromMemory result = '" + fromMemory + "'");
        if (fromMemory != null) return fromMemory;
        
        // Fallback to cache (covers cases where setFoldersList hasn't run yet or list is stale)
        try {
            List<Folder> cached = folderCache.getCachedFolders();
            System.out.println("TaskHandlerV2.getFolderIdByName: cached size = " + (cached != null ? cached.size() : "null"));
            for (Folder f : cached) {
                if (f != null && folderName.equals(f.getFolder_name())) {
                    System.out.println("TaskHandlerV2.getFolderIdByName: Found in cache = '" + f.getFolder_id() + "'");
                    return f.getFolder_id();
                }
            }
        } catch (Exception e) {
            System.err.println("TaskHandlerV2: Cache lookup failed: " + e.getMessage());
        }
        
        System.out.println("TaskHandlerV2.getFolderIdByName: No match found, returning null");
        return null;
    }
}
