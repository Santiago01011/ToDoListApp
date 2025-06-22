package DBH;

import COMMON.JSONUtils;
import model.TaskHandler;
import model.Task;
import model.TaskStatus;
import model.Folder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.HashMap;



public class DBHandler {
    private TaskHandler taskHandler;
    private UUID userUUID = null;
    
    /**
     * Constructor that initializes the DBHandler with a TaskHandler
     * to access the task list.
     *
     * @param taskHandler The TaskHandler instance to be used for task management
     */
    public DBHandler(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    /**
     * Inserts tasks into the database from a JSON string and updates the user's task list.
     *
     * This method executes a database function that validates and inserts a list of tasks created by the
     * user with the new status, and returns a JSON String with success and failed key-value pairs. For each
     * successful task, updates the local task list with the new task IDs, sets the sync status to "cloud", and 
     * updates the last sync timestamp.
     *
     * @param userUUID   The UUID of the user logged in.
     * @param jsonContent A JSON string containing data of new tasks to insert in the database.
     */
    private void insertTasksFromJSON(UUID userUUID, String jsonContent) {
        String query = "SELECT * FROM todo.insert_tasks_from_jsonb(?, ?::jsonb)";
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            pstmt.setString(2, jsonContent);

            try (ResultSet rs = pstmt.executeQuery()) {
                if ( rs.next() ) {
                    Map<String, Object> resultMap = JSONUtils.fromJsonString(rs.getString(2));
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> successList = (List<Map<String, Object>>) resultMap.get("success");
                    for (Map<String, Object> successItem : successList) {
                        String oldUUID = (String) successItem.get("old");
                        String newUUID = (String) successItem.get("new");                        // Find and replace the task with updated values
                        for (int i = 0; i < taskHandler.userTasksList.size(); i++) {
                            Task task = taskHandler.userTasksList.get(i);
                            if (task.getTask_id().equals(oldUUID)) {
                                Task updatedTask = new Task.Builder(newUUID)
                                    .taskTitle(task.getTitle())
                                    .description(task.getDescription())
                                    .status(task.getStatus())
                                    .sync_status("cloud")
                                    .dueDate(task.getDue_date())
                                    .createdAt(task.getCreated_at())
                                    .updatedAt(taskHandler.getLastSync())
                                    .deletedAt(task.getDeleted_at())
                                    .lastSync(taskHandler.getLastSync())
                                    .folderId(task.getFolder_id())
                                    .folderName(task.getFolder_name())
                                    .build();
                                taskHandler.userTasksList.set(i, updatedTask);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting tasks from JSON: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Insert Error reading JSON content: " + e.getMessage());
        }
    }

    /**
     * Updates tasks in the database from a JSON string and updates the user's task list.
     * <p>
     * This method executes a database function that validates and updates a list of tasks with the updated status,
     * and returns a JSON String with success and failed key-value pairs. For each successful task, updates the local
     * task list setting the sync status from "local" to "cloud", delete the "updated" copy of the task 
     * and sets the last sync timestamp.
     * </p>
     * @param userUUID
     * @param jsonContent
     */
    private void updateTasksFromJSON(UUID userUUID, String jsonContent) {
        String query = "SELECT * FROM todo.update_tasks_from_jsonb(?, ?::jsonb)";   
        // System.out.println("Updating tasks from JSON for user: " + userUUID);
        System.out.println("JSON Content: " + jsonContent);
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            pstmt.setString(2, jsonContent);

            try (ResultSet rs = pstmt.executeQuery()) {
                if ( rs.next() ){
                    Map<String, Object> resultMap = JSONUtils.fromJsonString(rs.getString(2));
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> successList = (List<Map<String, Object>>) resultMap.get("success");
                    for (Map<String, Object> successItem : successList) {
                        String taskId = (String) successItem.get("task_id");
                        taskHandler.clearShadowUpdate(taskId);
                        taskHandler.userTasksList.removeIf(t -> "to_update".equals(t.getSync_status()) && t.getTask_id().equals(taskId));                        // Find and replace tasks with updated sync status
                        for (int i = 0; i < taskHandler.userTasksList.size(); i++) {
                            Task t = taskHandler.userTasksList.get(i);
                            if (t.getTask_id().equals(taskId) && "local".equals(t.getSync_status())) {
                                Task updatedTask = new Task.Builder(t.getTask_id())
                                    .taskTitle(t.getTitle())
                                    .description(t.getDescription())
                                    .status(t.getStatus())
                                    .sync_status("cloud")
                                    .dueDate(t.getDue_date())
                                    .createdAt(t.getCreated_at())
                                    .updatedAt(taskHandler.getLastSync())
                                    .deletedAt(t.getDeleted_at())
                                    .lastSync(taskHandler.getLastSync())
                                    .folderId(t.getFolder_id())
                                    .folderName(t.getFolder_name())
                                    .build();
                                taskHandler.userTasksList.set(i, updatedTask);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating tasks from JSON: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading JSON content: " + e.getMessage());
        }
    }


    
    /**
     * Retrieves tasks for a specific user from the cloud database that have been modified
     * since the provided last synchronization timestamp.
     * <p>
     * This method executes a database function {@code todo.retrieve_tasks_modified_since_in_jsonb}
     * which returns the task data in JSONB format. The JSONB result is then parsed to reconstruct
     * a list of {@link Task} objects.
     * </p>
     * <p>
     * If {@code lastSync} is null, it implies that all tasks for the user should be retrieved.
     * </p>
     *
     * @param userUUID
     * @param lastSync The timestamp of the last synchronization. Only tasks modified
     *                 at or after this time will be retrieved. Can be null to fetch
     *                 all user tasks.
     * @return A List of {@link Task} objects retrieved from the cloud database.
     *         Returns an empty list if no tasks are found or if an error occurs during
     *         retrieval or processing.
     */
    private List<Task> retrieveTasksFromCloud(UUID userUUID, OffsetDateTime lastSync) {
        String query = "SELECT * FROM todo.retrieve_tasks_modified_since_in_jsonb(?, ?)";
        //System.out.println("Retrieving tasks from cloud for user: " + userUUID + " after last sync: " + lastSync);
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            pstmt.setObject(2, lastSync != null ? lastSync : null);

            List<Task> tasks = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String jsonbResult = rs.getString(2);
                    //System.out.println("JSONB Result: " + jsonbResult);
                    Map<String, Object> resultMap = JSONUtils.fromJsonString(jsonbResult);
                    @SuppressWarnings("unchecked")
                    List<String> columns = (List<String>) resultMap.get("columns");
                    @SuppressWarnings("unchecked")                    List<List<Object>> data = (List<List<Object>>) resultMap.get("data");
                    for (List<Object> row : data) {
                        // Use Builder pattern for immutable Task construction
                        Task.Builder taskBuilder = Task.builder();
                        
                        for (int i = 0; i < columns.size(); i++) {
                            String columnName = columns.get(i);
                            Object value = row.get(i);
                            if (value == null) continue;
                            
                            switch (columnName) {
                                case "folder_id" -> taskBuilder.folderId((String) value);
                                case "folder_name" -> taskBuilder.folderName((String) value);
                                case "task_id" -> taskBuilder = new Task.Builder((String) value); // Initialize with ID
                                case "task_title" -> taskBuilder.taskTitle((String) value);
                                case "description" -> taskBuilder.description((String) value);
                                case "sync_status" -> taskBuilder.sync_status((String) value);
                                case "status" -> taskBuilder.status(TaskStatus.valueOf((String) value));
                                case "due_date" -> taskBuilder.dueDate(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                                case "created_at" -> taskBuilder.createdAt(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                                case "updated_at" -> taskBuilder.updatedAt(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                                case "last_sync" -> taskBuilder.lastSync(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                                case "deleted_at" -> taskBuilder.deletedAt(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                            }
                        }
                        
                        // Build the immutable task
                        try {
                            Task task = taskBuilder.build();
                            tasks.add(task);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Skipping invalid task: " + e.getMessage());
                            // Skip tasks that don't meet validation requirements
                        }
                    }
                }
            }
            return tasks;
        } catch (SQLException e) {
            System.err.println("Error retrieving tasks from cloud: " + e.getMessage());
            return new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Retrieve error reading JSON content: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * <h4>Retrieves a list of folders accessible to the user from the cloud database.</h4>
     * <p>This method executes a database function {@code todo.get_accessible_folders}
     * which returns the folder data. The result is then parsed to reconstruct
     * a list of {@link Folder} objects.</p>
     *
     * @param userUUID The UUID of the user for whom to retrieve accessible folders.
     * @return A List of {@link Folder} objects representing the user's accessible folders.
     */
    private List<Folder> getAccessibleFolders(UUID userUUID) {
        String query = "SELECT * FROM todo.get_accessible_folders(?)";
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            List<Folder> folders = new ArrayList<>();
            pstmt.setObject(1, userUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Folder folder = new Folder.Builder(rs.getObject("folder_id", UUID.class).toString())
                            .folderName(rs.getString("folder_name"))
                            .build();
                    folders.add(folder);
                }
                return folders;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving folders: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * <h4>Merges cloud tasks into the local userTasksList.</h4>
     * <p>This method compares tasks from the cloud with local tasks and updates the local task list accordingly.
     * It handles the following scenarios:</p>
     * <ol>
     * <li>If a task in cloudTasks is not present in userTasksList, it is added.</li>
     * <li>If a task is present in both, but the cloud version has a newer updated_at timestamp,
     * the local task is replaced with the cloud version.</li>
     * <li>If a task is present in both with the same id and updated_at timestamp, the local version is kept.</li>
     * </ol>
     * <p>Additionally, if a task in cloudTasks has a deleted_at timestamp, it is removed from the local list.</p>
     * <p>Finally, it removes any tasks from userTasksList that have a deleted_at timestamp.</p>
     * 
     * @param cloudTasks A list of tasks retrieved from the cloud database in the sync process.
    */
    private void mergeTasks(List<Task> cloudTasks) {
        Map<String, Task> localTaskMap = new HashMap<>();
        for (Task localTask : taskHandler.userTasksList) {
            localTaskMap.put(localTask.getTask_id(), localTask);
        }
        List<String> tasksToRemove = new ArrayList<>();
        for (Task cloudTask : cloudTasks) {
            if (cloudTask.getDeleted_at() != null) {
                if (localTaskMap.containsKey(cloudTask.getTask_id())) {
                    tasksToRemove.add(cloudTask.getTask_id());
                    taskHandler.clearShadowUpdate(cloudTask.getTask_id());
                }
                continue;
            }
            Task localTask = localTaskMap.get(cloudTask.getTask_id());
            if (localTask == null) {
                taskHandler.userTasksList.add(cloudTask);
            } else {
                LocalDateTime localUpdated = localTask.getLast_sync();
                LocalDateTime cloudUpdated = cloudTask.getLast_sync();
                if (cloudUpdated != null && (localUpdated == null || cloudUpdated.isAfter(localUpdated))) {
                    int idx = taskHandler.userTasksList.indexOf(localTask);
                    taskHandler.userTasksList.set(idx, cloudTask);
                }
            }
        }
        if (!tasksToRemove.isEmpty()) {
            taskHandler.userTasksList.removeIf(task -> tasksToRemove.contains(task.getTask_id()));
        }
        taskHandler.userTasksList.removeIf(task -> task.getDeleted_at() != null);
    }

    /**
     * <h4>Starts the synchronization process asynchronously.</h4>
     * <p>This method runs the syncTasks() method in a separate thread using CompletableFuture.</p>
     * @return A CompletableFuture that completes with true if the sync process was successful,
     *         or false if an error occurred.
     */
    public CompletableFuture<Boolean> startSyncProcess(){
        return CompletableFuture.supplyAsync(() -> {
            if (userUUID == null)
                throw new IllegalStateException("User UUID is not set. Cannot start sync process.");
            try {
                syncTasks();
                return true;
            } catch (Exception e) {
                System.err.println("Error during sync process: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * <h4>Synchronizes tasks between the local userTasksList and the cloud database.</h4>
     * <p>This method retrieves tasks from the cloud that have been modified since the last sync,
     * and merges them with the local task list. It also prepares and sends any new or updated
     * tasks to the cloud for synchronization.</p>
     */
    private void syncTasks() {
        if ( taskHandler.userTasksList.isEmpty() && taskHandler.getShadowUpdatesForSync().isEmpty() ) {
            List<Task> retrievedTasks = retrieveTasksFromCloud(userUUID, null);
            mergeTasks(retrievedTasks);
            if (taskHandler.userTasksList.isEmpty())
                return;
            taskHandler.setLastSync(LocalDateTime.now());
            return;
        }
        OffsetDateTime retrieveLastSync = taskHandler.getLastSync().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        taskHandler.setLastSync(LocalDateTime.now());
        String insertJsonContent = taskHandler.prepareSyncJsonContent("new");
        if ( insertJsonContent != null ) insertTasksFromJSON(userUUID, insertJsonContent);
        String updateJsonContent = taskHandler.prepareSyncJsonContent("to_update");
        if ( updateJsonContent != null ) updateTasksFromJSON(userUUID, updateJsonContent);
        List<Task> cloudTasks = retrieveTasksFromCloud(userUUID, retrieveLastSync);
        if ( !cloudTasks.isEmpty() ) mergeTasks(cloudTasks);
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = UUID.fromString(userUUID);
    }
    
    public String getUserUUID() {
        return userUUID.toString();
    }

    /**
     * Public wrapper to retrieve the current user's accessible folders.
     * @return List of accessible Folder objects
     */
    public List<Folder> fetchAccessibleFolders() {
        return getAccessibleFolders(userUUID);
    }
}