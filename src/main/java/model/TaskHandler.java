package model;

import COMMON.JSONUtils;
import COMMON.UserProperties;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskHandler {
    private static final Logger LOGGER = Logger.getLogger(TaskHandler.class.getName());
    public List<Task> userTasksList;
    private List<Folder> userFoldersList;
    private LocalDateTime last_sync = null;
    private Map<String, Task> shadowUpdates = new HashMap<>();
    private String userId; // Track which user this handler is for
    private String tasksJsonFile; // User-specific tasks file path
    private String shadowsJsonFile; // User-specific shadows file path

    public LocalDateTime getLastSync() {
        return last_sync;
    }

    public void setLastSync(LocalDateTime last_sync) {
        this.last_sync = last_sync;
    }

    /**
     * Default constructor - uses current logged-in user
     */
    public TaskHandler() {
        this.userTasksList = new ArrayList<>();
        this.userFoldersList = new ArrayList<>();
        initializeForCurrentUser();
    }
    
    /**
     * Constructor for specific user
     */
    public TaskHandler(String userId) {
        this.userTasksList = new ArrayList<>();
        this.userFoldersList = new ArrayList<>();
        initializeForUser(userId);
    }
    
    /**
     * Initialize for the currently logged-in user
     */
    private void initializeForCurrentUser() {
        String currentUserId = (String) UserProperties.getProperty("userUUID");        if (currentUserId != null && !currentUserId.trim().isEmpty()) {
            initializeForUser(currentUserId);
        } else {
            // No user logged in - throw exception to enforce user-specific storage
            throw new IllegalStateException("No user is currently logged in. TaskHandler requires a valid user to be logged in for user-specific data storage.");
        }
    }
    
    /**
     * Initialize for a specific user
     */
    private void initializeForUser(String userId) {
        this.userId = userId;
        this.tasksJsonFile = UserProperties.getUserDataFilePath(userId, "tasks.json");
        this.shadowsJsonFile = UserProperties.getUserDataFilePath(userId, "shadows.json");
    }

    /**
     * Updates a task with the provided information based on its current synchronization status.
     * 
     * <p>This method handles different update behaviors depending on the task's sync status:
     * <ul>
     *   <li><b>new</b>: Updates the task directly or removes it if deleted_at is provided</li>
     *   <li><b>cloud</b>: Changes the task's sync status to "local" and creates a shadow copy with "to_update" status</li>
     *   <li><b>local</b>: Updates an existing shadow copy or creates a new one with the changes</li>
     * </ul>
     * 
     * <p>Shadow copies are used to track changes that need to be synchronized with the cloud.
     * 
     * @param task The task to update
     * @param title New title for the task (null if unchanged)
     * @param description New description for the task (null if unchanged)
     * @param status New status for the task (null if unchanged)
     * @param targetDate New due date for the task (null if unchanged)
     * @param folderName New folder name for the task (null if unchanged)
     * @param deleted_at Deletion timestamp if the task is being deleted, null otherwise
     */    public void updateTask(Task task, String title, String description, TaskStatus status, LocalDateTime targetDate, String folderName, LocalDateTime deleted_at) {
        LocalDateTime updateTime = LocalDateTime.now();
        final String taskId = task.getTask_id(); // Store ID for lambda usage
        
        if (task.getSync_status().equals("new")) {
            if ( deleted_at != null ){
                userTasksList.remove(task);
                return;
            }
            updateTaskFields(task, title, description, status, targetDate, folderName);
            // Update the updated_at field
            for (int i = 0; i < userTasksList.size(); i++) {
                if (userTasksList.get(i).getTask_id().equals(taskId)) {
                    Task currentTask = userTasksList.get(i);
                    Task updatedTask = new Task.Builder(currentTask.getTask_id())
                        .taskTitle(currentTask.getTitle())
                        .description(currentTask.getDescription())
                        .status(currentTask.getStatus())
                        .sync_status(currentTask.getSync_status())
                        .dueDate(currentTask.getDue_date())
                        .createdAt(currentTask.getCreated_at())
                        .updatedAt(updateTime)
                        .deletedAt(currentTask.getDeleted_at())
                        .lastSync(currentTask.getLast_sync())
                        .folderId(currentTask.getFolder_id())
                        .folderName(currentTask.getFolder_name())
                        .build();
                    userTasksList.set(i, updatedTask);
                    break;
                }
            }
            return;
        }
        if (task.getSync_status().equals("cloud")) {
            // Set original to local, create shadow with only changed fields
            Task updatedTask = null;
            for (int i = 0; i < userTasksList.size(); i++) {
                if (userTasksList.get(i) == task) {
                    updatedTask = new Task.Builder(task.getTask_id())
                        .taskTitle(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus())
                        .sync_status("local")
                        .dueDate(task.getDue_date())
                        .createdAt(task.getCreated_at())
                        .updatedAt(updateTime)
                        .deletedAt(task.getDeleted_at())
                        .lastSync(task.getLast_sync())
                        .folderId(task.getFolder_id())
                        .folderName(task.getFolder_name())
                        .build();
                    userTasksList.set(i, updatedTask);
                    break;
                }
            }
            
            Task.Builder builder = new Task.Builder(taskId)
                .sync_status("to_update")
                .updatedAt(updateTime);
            if (title != null && !title.isEmpty()) builder.taskTitle(title);
            if (description != null) builder.description(description);
            if (status != null) builder.status(status);
            if (targetDate != null) builder.dueDate(targetDate);
            if (folderName != null) builder.folderName(folderName);
            if (deleted_at != null) builder.deletedAt(deleted_at);

            Task newShadow = builder.build();
            shadowUpdates.put(taskId, newShadow);
            
            // Update the task with new field values
            for (int i = 0; i < userTasksList.size(); i++) {
                if (userTasksList.get(i).getTask_id().equals(taskId)) {
                    Task currentTask = userTasksList.get(i);
                    Task finalUpdatedTask = new Task.Builder(currentTask.getTask_id())
                        .taskTitle(currentTask.getTitle())
                        .description(currentTask.getDescription())
                        .status(currentTask.getStatus())
                        .sync_status(currentTask.getSync_status())
                        .dueDate(currentTask.getDue_date())
                        .createdAt(currentTask.getCreated_at())
                        .updatedAt(updateTime)
                        .deletedAt(currentTask.getDeleted_at())
                        .lastSync(currentTask.getLast_sync())
                        .folderId(currentTask.getFolder_id())
                        .folderName(currentTask.getFolder_name())
                        .build();
                    userTasksList.set(i, finalUpdatedTask);
                    break;
                }
            }
            
            if ( deleted_at != null ) userTasksList.removeIf(t -> t.getTask_id().equals(taskId));
            else if (updatedTask != null) updateTaskFields(updatedTask, title, description, status, targetDate, folderName);

            return;        }
        if (task.getSync_status().equals("local")) {
            // Update existing shadow or create if missing
            Task shadow = shadowUpdates.get(taskId);
            Task.Builder builder;
            if (shadow == null) {
                builder = new Task.Builder(taskId)
                    .sync_status("to_update")
                    .updatedAt(updateTime);
            } else {
                builder = new Task.Builder(taskId)
                    .sync_status("to_update")
                    .updatedAt(updateTime);
                // Copy over already set fields in shadow
                if (shadow.getTitle() != null) builder.taskTitle(shadow.getTitle());
                if (shadow.getDescription() != null) builder.description(shadow.getDescription());
                if (shadow.getStatus() != null) builder.status(shadow.getStatus());
                if (shadow.getDue_date() != null) builder.dueDate(shadow.getDue_date());
                if (shadow.getFolder_name() != null) builder.folderName(shadow.getFolder_name());
            }
            if (title != null && !title.isEmpty()) builder.taskTitle(title);
            if (description != null) builder.description(description);
            if (status != null) builder.status(status);
            if (targetDate != null) builder.dueDate(targetDate);
            if (folderName != null) builder.folderName(folderName);
            if (deleted_at != null) builder.deletedAt(deleted_at);

            Task newShadow = builder.build();
            shadowUpdates.put(taskId, newShadow);
            
            // Update the task with new updated_at time
            for (int i = 0; i < userTasksList.size(); i++) {
                if (userTasksList.get(i).getTask_id().equals(taskId)) {
                    Task currentTask = userTasksList.get(i);
                    Task updatedTask = new Task.Builder(currentTask.getTask_id())
                        .taskTitle(currentTask.getTitle())
                        .description(currentTask.getDescription())
                        .status(currentTask.getStatus())
                        .sync_status(currentTask.getSync_status())
                        .dueDate(currentTask.getDue_date())
                        .createdAt(currentTask.getCreated_at())
                        .updatedAt(updateTime)
                        .deletedAt(currentTask.getDeleted_at())
                        .lastSync(currentTask.getLast_sync())
                        .folderId(currentTask.getFolder_id())
                        .folderName(currentTask.getFolder_name())
                        .build();
                    userTasksList.set(i, updatedTask);
                    break;
                }
            }
            
            if ( deleted_at != null ) userTasksList.removeIf(t -> t.getTask_id().equals(taskId));
            else {
                // Find the updated task and apply field changes
                Task updatedTask = userTasksList.stream()
                    .filter(t -> t.getTask_id().equals(taskId))
                    .findFirst()
                    .orElse(null);
                if (updatedTask != null) {
                    updateTaskFields(updatedTask, title, description, status, targetDate, folderName);
                }
            }

            return;
        }
    }
      // Helper method to update task fields using immutable API
    private void updateTaskFields(Task task, String title, String description, TaskStatus status, LocalDateTime targetDate, String folderName) {
        // Find the task in the list and replace it with an updated version
        for (int i = 0; i < userTasksList.size(); i++) {
            if (userTasksList.get(i) == task) {
                Task.Builder builder = new Task.Builder(task.getTask_id())
                    .taskTitle(title != null && !title.isEmpty() ? title : task.getTitle())
                    .description(description != null ? description : task.getDescription())
                    .status(status != null ? status : task.getStatus())
                    .sync_status(task.getSync_status())
                    .dueDate(targetDate != null ? targetDate : task.getDue_date())
                    .createdAt(task.getCreated_at())
                    .updatedAt(task.getUpdated_at())
                    .deletedAt(task.getDeleted_at())
                    .lastSync(task.getLast_sync())
                    .folderId(task.getFolder_id())
                    .folderName(folderName != null ? folderName : task.getFolder_name());
                
                Task updatedTask = builder.build();
                userTasksList.set(i, updatedTask);
                break;
            }
        }
    }

    public String prepareSyncJsonContent(String sync_status) {
        List<Task> filteredTasks = new ArrayList<>();
        if ( sync_status.equals("to_update") ){
            filteredTasks = new ArrayList<>(shadowUpdates.values());
        }
        if ( sync_status.equals("new") ){
            filteredTasks = userTasksList.stream()
                .filter(task -> task.getSync_status().equals("new"))
                .toList();
        }
        if (filteredTasks.isEmpty()) return null;
        try {
            Map<String, Object> jsonbStructure = JSONUtils.buildJsonStructure(filteredTasks.stream());
            jsonbStructure.put("last_sync", getLastSync());
            return JSONUtils.toJsonString(jsonbStructure);
        } catch (IOException e) {
            System.err.println("Error creating sync JSON content: " + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Task> loadTasksFromJson() {
        
        File file = new File(tasksJsonFile);

        try{
            if( !JSONUtils.isValidJsonStructure(file, "columns", "data", "last_sync") ){                JSONUtils.createDefaultJsonFile(tasksJsonFile);
                System.err.println("Invalid JSON structure in file: " + tasksJsonFile);
                return new ArrayList<>();
            }
            Map<String, Object> wrapper = JSONUtils.readJsonFile(file);
            List<Task> tasks = new ArrayList<>();
            List<String> columns = (List<String>) wrapper.get("columns");
            List<List<Object>> data = (List<List<Object>>) wrapper.get("data");
            
            String lastSyncStr = (String) wrapper.get("last_sync");
            if (lastSyncStr != null) {
                setLastSync(LocalDateTime.parse(lastSyncStr));
            } else {
                setLastSync(LocalDateTime.now());
            }
            
            if (data != null) {
                for (List<Object> row : data) {
                    tasks.add(createTaskFromRow(columns, row));
                }
            }

            return tasks;
        } catch (IOException e) {
            System.err.println("Error loading tasks from JSON: " + e.getMessage());
            JSONUtils.createDefaultJsonFile(tasksJsonFile);
            return new ArrayList<>();
        }  
    }

    @SuppressWarnings("unchecked")
    private void loadShadowsFromJson() {
        File file = new File(shadowsJsonFile);
        if (!file.exists()) return;
        try {
            if (!JSONUtils.isValidJsonStructure(file, "columns", "data")) {
                System.err.println("Invalid JSON structure in shadows file: " + shadowsJsonFile);
                return;
            }
            Map<String, Object> wrapper = JSONUtils.readJsonFile(file);
            List<String> columns = (List<String>) wrapper.get("columns");
            List<List<Object>> data = (List<List<Object>>) wrapper.get("data");
            if (data != null) {
                for (List<Object> row : data) {
                    Task t = createTaskFromRow(columns, row);
                    if (t != null && t.getTask_id() != null) {
                        shadowUpdates.put(t.getTask_id(), t);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading shadows from JSON: " + e.getMessage());
        }
    }

    public Task createTaskFromRow(List<String> columns, List<Object> row) {
        Map<String, Object> taskMap = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            taskMap.put(columns.get(i), row.get(i));
        }

        TaskStatus status = TaskStatus.pending;
        String statusStr = (String) taskMap.get("status");
        if (statusStr != null) {
            try {
                status = TaskStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid status value found in JSON: " + statusStr + ". Defaulting to pending.", e);
            }
        }

        LocalDateTime dueDate = null;
        String dueDateStr = (String) taskMap.get("due_date");
        if (dueDateStr != null) {
            try {
                dueDate = LocalDateTime.parse(dueDateStr);
            } catch (DateTimeParseException e) {
                LOGGER.log(Level.WARNING, "Invalid due_date format in JSON: " + dueDateStr, e);
            }
        }

        LocalDateTime createdAt = null;
        String createdAtStr = (String) taskMap.get("created_at");
        if (createdAtStr != null) {
            try {
                createdAt = LocalDateTime.parse(createdAtStr);
            } catch (DateTimeParseException e) {
                LOGGER.log(Level.WARNING, "Invalid created_at format in JSON: " + createdAtStr, e);
            }
        }

        LocalDateTime lastSync = null;
        String lastSyncStr = (String) taskMap.get("last_sync");
        if (lastSyncStr != null) {
            try {
                lastSync = LocalDateTime.parse(lastSyncStr);
            } catch (DateTimeParseException e) {
                LOGGER.log(Level.WARNING, "Invalid last_sync format in JSON: " + lastSyncStr, e);
            }
        }

        LocalDateTime deletedAt = null;
        String deletedAtStr = (String) taskMap.get("deleted_at");
        if (deletedAtStr != null) {
            try {
                deletedAt = LocalDateTime.parse(deletedAtStr);
            } catch (DateTimeParseException e) {
                LOGGER.log(Level.WARNING, "Invalid deleted_at format in JSON: " + deletedAtStr, e);
            }
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
            .updatedAt(getLastSync())
            .lastSync(lastSync)
            .deletedAt(deletedAt)
            .build();
    }

    /**
     * Saves the current tasks to the JSON file.
     */
    public void saveTasksToJson() {
        try {
            Map<String, Object> jsonbStructure = JSONUtils.buildJsonStructure(userTasksList.stream());
            jsonbStructure.put("last_sync", getLastSync());
            JSONUtils.writeJsonFile(jsonbStructure, tasksJsonFile);
            saveShadowsToJson();
        } catch (IOException e) {
            System.err.println("Error saving tasks to JSON: " + e.getMessage());
        }
    }

    private void saveShadowsToJson() {
        try {
            List<Task> shadowList = new ArrayList<>(shadowUpdates.values());
            Map<String, Object> jsonbStructure = JSONUtils.buildJsonStructure(shadowList.stream());
            JSONUtils.writeJsonFile(jsonbStructure, shadowsJsonFile);
        } catch (IOException e) {
            System.err.println("Error saving shadows to JSON: " + e.getMessage());
        }
    }

    public Task getTaskById(String task_id) {
        return userTasksList.stream()
            .filter(task -> task.getTask_id().equals(task_id))
            .findFirst()
            .orElse(null);
    }

    public List<Task> getShadowUpdatesForSync() {
        List<Task> toSync = new ArrayList<>();
        for (Task t : shadowUpdates.values()) {
            if ("to_update".equals(t.getSync_status())) {
                toSync.add(t);
            }
        }
        return toSync;
    }

    public void clearShadowUpdate(String taskId) {
        shadowUpdates.remove(taskId);
    }

    public void setFoldersList(List<Folder> foldersList) {
        this.userFoldersList = foldersList;
    }

    public List<Folder> getFoldersList() {
        return userFoldersList;
    }

    /**
     * Returns a list of folder names extracted from the userFoldersList.
     * @return A list of strings containing folder names.
     */
    public List<String> getFoldersNamesList() {
        if (userFoldersList == null) {
            return new ArrayList<>();
        }
        return userFoldersList.stream()
            .map(Folder::getFolder_name)
            .toList();
    }

    public String getFolderIdByName(String folderName) {
        return userFoldersList.stream()
            .filter(folder -> folder.getFolder_name().equals(folderName))
            .findFirst()
            .map(Folder::getFolder_id)
            .orElse(null);
    }
}