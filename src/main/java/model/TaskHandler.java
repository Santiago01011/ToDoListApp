package model;

import COMMON.JSONUtils;

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
    private static final String TASKS_JSON_FILE = JSONUtils.BASE_DIRECTORY + File.separator + "tasks.json";
    private static final String SHADOWS_JSON_FILE = JSONUtils.BASE_DIRECTORY + File.separator + "shadows.json";
    private static final Logger LOGGER = Logger.getLogger(TaskHandler.class.getName());
    public List<Task> userTasksList;
    private List<Folder> userFoldersList;
    private LocalDateTime last_sync = null;
    private Map<String, Task> shadowUpdates = new HashMap<>();

    public LocalDateTime getLastSync() {
        return last_sync;
    }

    public void setLastSync(LocalDateTime last_sync) {
        this.last_sync = last_sync;
    }

    public TaskHandler() {
        this.userTasksList = loadTasksFromJson();
        loadShadowsFromJson();
        this.userFoldersList = new ArrayList<>();
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
     */
    public void updateTask(Task task, String title, String description, TaskStatus status, LocalDateTime targetDate, String folderName, LocalDateTime deleted_at) {
        LocalDateTime updateTime = LocalDateTime.now();
        if (task.getSync_status().equals("new")) {
            if ( deleted_at != null ){
                userTasksList.remove(task);
                return;
            }
            updateTaskFields(task, title, description, status, targetDate, folderName);
            task.setUpdated_at(updateTime);
            return;
        }
        if (task.getSync_status().equals("cloud")) {
            // Set original to local, create shadow with only changed fields
            task.setSync_status("local");
            task.setUpdated_at(updateTime);
            
            Task.Builder builder = new Task.Builder(task.getTask_id())
                .sync_status("to_update")
                .updatedAt(updateTime);
            if (title != null && !title.isEmpty()) builder.taskTitle(title);
            if (description != null) builder.description(description);
            if (status != null) builder.status(status);
            if (targetDate != null) builder.dueDate(targetDate);
            if (folderName != null) builder.folderName(folderName);
            if (deleted_at != null) builder.deletedAt(deleted_at);
            Task shadow = builder.build();
            shadowUpdates.put(task.getTask_id(), shadow);

            if ( deleted_at != null ) userTasksList.remove(task);
            else updateTaskFields(task, title, description, status, targetDate, folderName);
            
            return;
        }
        if (task.getSync_status().equals("local")) {
            // Update existing shadow or create if missing
            Task shadow = shadowUpdates.get(task.getTask_id());
            Task.Builder builder;
            if (shadow == null) {
                builder = new Task.Builder(task.getTask_id())
                    .sync_status("to_update")
                    .updatedAt(updateTime);
            } else {
                builder = new Task.Builder(task.getTask_id())
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
            shadowUpdates.put(task.getTask_id(), newShadow);
            task.setUpdated_at(updateTime);
            if ( deleted_at != null ) userTasksList.remove(task);
            else updateTaskFields(task, title, description, status, targetDate, folderName);

            return;
        }
    }
    
    // Helper method to update task fields
    private void updateTaskFields(Task task, String title, String description, TaskStatus status, LocalDateTime targetDate, String folderName) {
        if (title != null && !title.isEmpty()) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (status != null) {
            task.setStatus(status);
        }
        if (targetDate != null) {
            task.setDue_date(targetDate);
        }
        if (folderName != null) {
            task.setFolder_name(folderName);
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
        
        File file = new File(TASKS_JSON_FILE);

        try{
            if( !JSONUtils.isValidJsonStructure(file, "columns", "data", "last_sync") ){
                JSONUtils.createDefaultJsonFile(TASKS_JSON_FILE);
                System.err.println("Invalid JSON structure in file: " + TASKS_JSON_FILE);
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
            JSONUtils.createDefaultJsonFile(TASKS_JSON_FILE);
            return new ArrayList<>();
        }  
    }

    @SuppressWarnings("unchecked")
    private void loadShadowsFromJson() {
        File file = new File(SHADOWS_JSON_FILE);
        if (!file.exists()) return;
        try {
            if (!JSONUtils.isValidJsonStructure(file, "columns", "data")) {
                System.err.println("Invalid JSON structure in shadows file: " + SHADOWS_JSON_FILE);
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
            JSONUtils.writeJsonFile(jsonbStructure, TASKS_JSON_FILE);
            saveShadowsToJson();
        } catch (IOException e) {
            System.err.println("Error saving tasks to JSON: " + e.getMessage());
        }
    }

    private void saveShadowsToJson() {
        try {
            List<Task> shadowList = new ArrayList<>(shadowUpdates.values());
            Map<String, Object> jsonbStructure = JSONUtils.buildJsonStructure(shadowList.stream());
            JSONUtils.writeJsonFile(jsonbStructure, SHADOWS_JSON_FILE);
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