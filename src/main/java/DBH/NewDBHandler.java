package DBH;

import COMMON.JSONUtils;
import model.TaskHandler;
import model.Task;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.HashMap;



public class NewDBHandler {
    private TaskHandler taskHandler;
    private UUID userUUID = null;
    
    /**
     * Constructor that initializes the NewDBHandler with a TaskHandler
     * to access the task list.
     *
     * @param taskHandler The TaskHandler instance to be used for task management
     */
    public NewDBHandler(TaskHandler taskHandler) {
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
                        String newUUID = (String) successItem.get("new");

                        taskHandler.userTasksList.stream()
                            .filter(task -> task.getTask_id().equals(oldUUID))
                            .findFirst()
                            .ifPresent(task -> {
                                task.setTask_id(newUUID);
                                task.setLast_sync(taskHandler.getLastSync());
                                task.setUpdated_at(taskHandler.getLastSync());
                                task.setSync_status("cloud");
                            });
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
     * 
     * This method executes a database function that validates and updates a list of tasks with the updated status,
     * and returns a JSON String with success and failed key-value pairs. For each successful task, updates the local
     * task list setting the sync status from "local" to "cloud", delete the "updated" copy of the task 
     * and sets the last sync timestamp.
     * @param userUUID
     * @param jsonContent
     */
    private void updateTasksFromJSON(UUID userUUID, String jsonContent) {
        String query = "SELECT * FROM todo.update_tasks_from_jsonb(?, ?::jsonb)";   
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            pstmt.setString(2, jsonContent);

            System.out.println("Updating tasks from JSON for user: " + userUUID);
            System.out.println("JSON content: " + jsonContent);

            try (ResultSet rs = pstmt.executeQuery()) {
                if ( rs.next() ){
                    Map<String, Object> resultMap = JSONUtils.fromJsonString(rs.getString(2));
                    System.out.println("Result map: " + resultMap);
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> successList = (List<Map<String, Object>>) resultMap.get("success");
                    for (Map<String, Object> successItem : successList) {
                        String taskId = (String) successItem.get("task_id");
                        taskHandler.userTasksList.removeIf(task -> "to_update".equals(task.getSync_status()) && task.getTask_id().equals(taskId));
                        taskHandler.userTasksList.stream()
                            .filter(task -> task.getTask_id().equals(taskId) && "local".equals(task.getSync_status()))
                            .forEach(task -> {
                                task.setLast_sync(taskHandler.getLastSync());
                                task.setUpdated_at(taskHandler.getLastSync());
                                task.setSync_status("cloud");
                            });
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating tasks from JSON: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading JSON content: " + e.getMessage());
        }
    }

    private List<Task> retrieveTasksFromCloud(UUID userUUID, OffsetDateTime lastSync) {
        String query = "SELECT * FROM todo.retrieve_tasks_modified_since_in_jsonb(?, ?)";
        System.out.println("Retrieving tasks from cloud for user: " + userUUID + " after last sync: " + lastSync);
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            pstmt.setObject(2, lastSync != null ? lastSync : null);

            List<Task> tasks = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String jsonbResult = rs.getString(2);
                    if( !JSONUtils.isValidJsonStructure(jsonbResult, "columns", "data", "last_sync") ){
                        System.err.println("Invalid JSON structure in result: " + jsonbResult);
                        return tasks;
                    } 
                    Map<String, Object> resultMap = JSONUtils.fromJsonString(jsonbResult);
                    @SuppressWarnings("unchecked")
                    List<String> columns = (List<String>) resultMap.get("columns");
                    @SuppressWarnings("unchecked")
                    List<List<Object>> data = (List<List<Object>>) resultMap.get("data");
                    for (List<Object> row : data) {
                        Task task = new Task();
                        for (int i = 0; i < columns.size(); i++) {
                            String columnName = columns.get(i);
                            Object value = row.get(i);
                            if (value == null) continue;
                            switch (columnName) {
                                case "folder_id" -> task.setFolder_id((String) value);
                                case "folder_name" -> task.setFolder_name((String) value);
                                case "task_id" -> task.setTask_id((String) value);
                                case "task_title" -> task.setTitle((String) value);
                                case "description" -> task.setDescription((String) value);
                                case "sync_status" -> task.setSync_status((String) value);
                                case "status" -> task.setStatus((String) value);
                                case "due_date" -> task.setDue_date(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                                case "created_at" -> task.setCreated_at(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                                case "last_sync" -> task.setLast_sync(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                                case "deleted_at" -> task.setDeleted_at(java.time.OffsetDateTime.parse((String) value).toLocalDateTime());
                            }
                        }
                        tasks.add(task);
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
     * Merges cloud tasks into the local userTasksList according to the following rules:
     * 1. If a task in cloudTasks is not present in userTasksList, add it.
     * 2. If a task is present in both, but cloud has a newer updated_at, replace local with cloud.
     * 3. If a task is present in both with same id and updated_at, keep the local one.
     */
    private void mergeTasks(List<Task> cloudTasks) {
        Map<String, Task> localTaskMap = new HashMap<>();
        for (Task localTask : taskHandler.userTasksList) {
            localTaskMap.put(localTask.getTask_id(), localTask);
        }
        for (Task cloudTask : cloudTasks) {
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
    }

    public CompletableFuture<Boolean> startSyncProcess(){
        return CompletableFuture.supplyAsync(() -> {
            try {
                syncTasks();
                return true;
            } catch (Exception e) {
                System.err.println("Error during sync process: " + e.getMessage());
                return false;
            }
        });
    }

    private void syncTasks() {
        if ( userUUID == null ) {
            System.err.println("User UUID is not set. Cannot start sync process.");
            return;
        }
        if ( taskHandler.userTasksList.isEmpty()) {
            System.err.println("No tasks found in local storage. Retrieving from cloud.");
            taskHandler.userTasksList = retrieveTasksFromCloud(userUUID, null);
            if ( taskHandler.userTasksList.isEmpty() ) {
                System.err.println("No tasks found in the cloud for user: " + userUUID);
                return;
            }
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

}