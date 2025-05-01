package DBH;

import COMMON.JSONUtils;
import model.TaskHandler;
import model.Task;
import model.Folder;

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
        // System.out.println("Updating tasks from JSON for user: " + userUUID);
        // System.out.println("JSON Content: " + jsonContent);
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
                        taskHandler.userTasksList.removeIf(t -> "to_update".equals(t.getSync_status()) && t.getTask_id().equals(taskId));
                        taskHandler.userTasksList.stream()
                            .filter(t -> t.getTask_id().equals(taskId) && "local".equals(t.getSync_status()))
                            .forEach(t -> {
                                t.setLast_sync(taskHandler.getLastSync());
                                t.setUpdated_at(taskHandler.getLastSync());
                                t.setSync_status("cloud");
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


    private List<Folder> getAccesibleFolders(UUID userUUID) {
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
                    System.out.println("Folder ID: " + folder.getFolder_id() + ", Folder Name: " + folder.getFolder_name());
                    folders.add(folder);
                }
                return folders;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving folders: " + e.getMessage());
            return new ArrayList<>();
        }

    }
    // public List<Folder> getAccessibleFolders(UUID userId) throws SQLException {
    //     String sql = "SELECT folder_id, folder_name FROM todo.get_accessible_folders(?)";
    //     try (var conn = NeonPool.getConnection();
    //          var ps = conn.prepareStatement(sql)) {
    //       ps.setObject(1, userId);
    //       try (var rs = ps.executeQuery()) {
    //         List<Folder> folders = new ArrayList<>();
    //         while (rs.next()) {
    //           folders.add(new Folder(rs.getObject("folder_id", UUID.class),
    //                                  rs.getString("folder_name")));
    //         }
    //         return folders;
    //       }
    //     }
    // }

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

    // TODO: note the error in case that the local list is empty but the shadow is not
    private void syncTasks() {
        taskHandler.setFoldersList(getAccesibleFolders(userUUID));
        if ( taskHandler.userTasksList.isEmpty()) {
            System.err.println("No tasks found in local storage. Retrieving from cloud.");
            List<Task> retrievedTasks = retrieveTasksFromCloud(userUUID, null);
            mergeTasks(retrievedTasks);
            if (taskHandler.userTasksList.isEmpty()) {
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