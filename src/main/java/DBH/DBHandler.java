package DBH;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import COMMON.JSONUtils;
import model.Task;
import model.TaskHandler;

public class DBHandler {
    private TaskHandler taskHandler;

    /**
     * Constructor that initializes the DBHandler with a TaskHandler
     * to manage tasks synchronization between local and cloud storage.
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
                    Map<String, Object> resultMap = JSONUtils.fromJsonString(rs.getString(1));
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> successList = (List<Map<String, Object>>) resultMap.get("success");
                    LocalDateTime lastSync = LocalDateTime.parse((String) resultMap.get("last_sync"));

                    for (Map<String, Object> successItem : successList) {
                        String oldUUID = (String) successItem.get("old");
                        String newUUID = (String) successItem.get("new");

                        taskHandler.userTasksList.stream()
                            .filter(task -> task.getTask_id().equals(oldUUID))
                            .findFirst()
                            .ifPresent(task -> {
                                task.setTask_id(newUUID);
                                task.setLast_sync(lastSync);
                                task.setUpdated_at(lastSync);
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

            try (ResultSet rs = pstmt.executeQuery()) {
                if ( rs.next() ){
                    Map<String, Object> resultMap = JSONUtils.fromJsonString(rs.getString(1));
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> successList = (List<Map<String, Object>>) resultMap.get("success");
                    LocalDateTime lastSync = LocalDateTime.parse((String) resultMap.get("last_sync"));
                    for (Map<String, Object> successItem : successList) {
                        taskHandler.userTasksList.stream()
                        .filter(task -> task.getTask_id().equals(successItem.get("task_id")))
                        .forEach(task -> {
                            if ( task.getSync_status().equals("local") ){
                                task.setLast_sync(lastSync);
                                task.setUpdated_at(lastSync);
                                task.setSync_status("cloud");
                            }
                            if ( task.getSync_status().equals("updated") ){
                                taskHandler.userTasksList.remove(task);
                            }
                            /*else throws taskError */
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

    /**
     * Synchronizes tasks between the local application and the database.
     * This method coordinates the process of inserting new tasks,
     * updating existing ones, and refreshing the local task list with the latest data.
     *
     * @param userUuid The UUID of the user as a string
     * @param insertJsonContent JSON content containing new tasks to be inserted
     * @param updateJsonContent JSON content containing existing tasks to be updated
     * @param taskList The current list of tasks to be synchronized
     * @return The updated list of tasks after synchronization
     */
    public List<Task> syncTasks(String userUuid, String insertJsonContent, String updateJsonContent, List<Task> taskList) {
        try {
            UUID uuid = UUID.fromString(userUuid);
            @SuppressWarnings("unused")
            List<Task> updatedTaskList = new ArrayList<>();

            Map<String, Task> taskMap = new HashMap<>();
            for (Task task : taskList) {
                if (task != null && task.getTask_id() != null) {
                    taskMap.put(task.getTask_id(), task);
                }
            }

            if (insertJsonContent != null) {
                insertTasksFromJSON(uuid, insertJsonContent);
            }

            Map<String, Task> tasksToUpdate = new HashMap<>();
            List<Task> tasksToRemove = new ArrayList<>();

            for (Task task : taskList) {
                if (task != null && "updated".equals(task.getSync_status()) && task.getTask_id() != null) {
                    String taskId = task.getTask_id();
                    tasksToUpdate.put(taskId, task);
                    tasksToRemove.add(task);
                }
            }

            if (updateJsonContent != null) {
                updateTasksFromJSON(uuid, updateJsonContent);
            }

            List<Task> refreshedTasks = retrieveTasksFromDB(uuid);
            for (Task refreshedTask : refreshedTasks) {
                if (refreshedTask != null && refreshedTask.getTask_id() != null) {
                    refreshedTask.setSync_status("cloud");
                    taskMap.put(refreshedTask.getTask_id(), refreshedTask);
                }
            }

            for (Task task : taskList) {
                if (task != null && !tasksToRemove.contains(task) && "local".equals(task.getSync_status())) {
                    task.setSync_status("cloud");
                    task.setLast_sync(LocalDateTime.now());
                    taskMap.put(task.getTask_id(), task);
                }
            }

            taskList.removeAll(tasksToRemove);

            return new ArrayList<>(taskMap.values());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            return taskList;
        }
    }

    /**
     * Initiates and manages the entire synchronization process between local and cloud tasks.
     * This method handles both first-time synchronization (when the local task list is empty)
     * and regular synchronization, ensuring data consistency across platforms.
     *
     * @param user_id The UUID of the user as a string
     */
    public void startSyncProcess(String user_id) {
        try {
            UUID userUUID = UUID.fromString(user_id);
            LocalDateTime syncTime = LocalDateTime.now();

            if (isFirstTimeSync()) {
                System.out.println("First-time sync or empty local list detected.");
                
                List<Task> cloudTasks = retrieveTasksFromDB(userUUID);
                
                if (!cloudTasks.isEmpty()) {
                    taskHandler.userTasksList = new ArrayList<>(cloudTasks);
                    
                    // Save tasks and update the last sync timestamp
                    taskHandler.saveTasksToJson();
                    try {
                        JSONUtils.updateLastSync(syncTime.toString());
                        taskHandler.setLastSync(syncTime);
                    } catch (IOException e) {
                        System.err.println("Error updating last sync timestamp: " + e.getMessage());
                    }
                    
                    System.out.println("Initial sync complete - retrieved " + cloudTasks.size() + " tasks from cloud.");
                    return;
                }
            }

            List<Task> cloudTasks = retrieveTasksFromDB(userUUID);

            List<Task> mergedTasks = mergeTasks(taskHandler.userTasksList, cloudTasks);

            String insertJsonContent = taskHandler.prepareSyncJsonContent("new");
            String updateJsonContent = taskHandler.prepareSyncJsonContent("updated");
            
            if (insertJsonContent == null && updateJsonContent == null) {
                System.out.println("No tasks to sync.");
                taskHandler.userTasksList = mergedTasks;
                taskHandler.saveTasksToJson();
                return;
            }

            // Log the number of tasks being synced instead of the entire content
            if (insertJsonContent != null) {
                System.out.println("Syncing new tasks...");
            }
            if (updateJsonContent != null) {
                System.out.println("Syncing updated tasks...");
            }

            taskHandler.userTasksList = syncTasks(user_id, insertJsonContent, updateJsonContent, mergedTasks);

            // Save tasks and update the last sync timestamp
            taskHandler.saveTasksToJson();
            try {
                JSONUtils.updateLastSync(syncTime.toString());
                taskHandler.setLastSync(syncTime);
                System.out.println("Sync completed successfully at " + syncTime);
            } catch (IOException e) {
                System.err.println("Error updating last sync timestamp: " + e.getMessage());
            }
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Retrieves all tasks for a specific user from the database.
     * This method queries the database and transforms the JSON result into Task objects.
     *
     * @param userUUID The UUID of the user whose tasks are to be retrieved
     * @return A list of Task objects retrieved from the database with sync_status set to "cloud"
     *         or an empty list if no tasks are found or an error occurs
     */
    private List<Task> retrieveTasksFromDB(UUID userUUID) {
        String query = "SELECT * FROM todo.retrieve_tasks_in_jsonb(?)";
        List<Task> cloudTasks = new ArrayList<>();
        
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setObject(1, userUUID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String jsonResult = rs.getString("log_tasks");
                    
                    try {
                        Map<String, Object> resultMap = JSONUtils.fromJsonString(jsonResult);
                        
                        @SuppressWarnings("unchecked")
                        List<String> columns = (List<String>) resultMap.get("columns");
                        
                        @SuppressWarnings("unchecked")
                        List<List<Object>> data = (List<List<Object>>) resultMap.get("data");
                        
                        if (columns != null && data != null) {
                            for (List<Object> row : data) {
                                Map<String, Object> taskMap = new HashMap<>();
                                
                                for (int i = 0; i < columns.size() && i < row.size(); i++) {
                                    taskMap.put(columns.get(i), row.get(i));
                                }
                                
                                Task task = createTaskFromMap(taskMap);
                                if (task != null) {
                                    task.setSync_status("cloud");
                                    cloudTasks.add(task);
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error parsing task data: " + e.getMessage());
                    }
                }
            }
            
            return cloudTasks;
            
        } catch (SQLException e) {
            System.err.println("Error retrieving tasks from database: " + e.getMessage());
            return cloudTasks;
        }
    }
    
    /**
     * Creates a Task object from a map of task properties.
     * 
     * @param taskMap Map containing task properties
     * @return A new Task object, or null if the task_id is missing
     */
    private Task createTaskFromMap(Map<String, Object> taskMap) {
        String taskId = (String) taskMap.get("task_id");
        if (taskId == null) {
            taskId = UUID.randomUUID().toString();
        }
        
        try {
            return new Task.Builder(taskId)
                .folderId((String) taskMap.get("folder_id"))
                .folderName((String) taskMap.get("folder_name"))
                .taskTitle((String) taskMap.get("task_title"))
                .description((String) taskMap.get("description"))
                .sync_status((String) taskMap.get("sync_status"))
                .status((String) taskMap.get("status"))
                .dueDate(parseTimeValue(taskMap.get("due_date")))
                .createdAt(parseTimeValue(taskMap.get("created_at")))
                .updatedAt(parseTimeValue(taskMap.get("updated_at")))
                .deletedAt(parseTimeValue(taskMap.get("deleted_at")))
                .lastSync(parseTimeValue(taskMap.get("last_sync")))
                .build();
        } catch (Exception e) {
            System.err.println("Error creating task from map: " + e.getMessage() + " for task ID: " + taskId);
            return null;
        }
    }
    
    /**
     * Parse a time value from a database result object to LocalDateTime.
     * Handles String or already parsed LocalDateTime values.
     * 
     * @param value The time value to parse
     * @return LocalDateTime object or null if parsing fails
     */
    private LocalDateTime parseTimeValue(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        
        try {
            if (value instanceof String) {
                String dateStr = (String) value;
                return OffsetDateTime.parse(dateStr).toLocalDateTime();
            }
        } catch (Exception e) {
            // Silently return null if parsing fails
        }
        
        return null;
    }

    /**
     * Helper method to check if the task list is empty or null.
     */
    private boolean isFirstTimeSync() {
        return taskHandler.userTasksList == null || taskHandler.userTasksList.isEmpty();
    }

    /**
     * Merges local and cloud task lists with intelligent conflict resolution.
     * This method implements a sophisticated strategy to determine which version of a task to keep
     * based on sync status, timestamps, and task attributes.
     *
     * @param localTasks The list of tasks from the local application
     * @param cloudTasks The list of tasks from the cloud database
     * @return A merged list of tasks with conflicts resolved
     */
    public List<Task> mergeTasks(List<Task> localTasks, List<Task> cloudTasks) {
        List<Task> localTasksCopy = new ArrayList<>(localTasks != null ? localTasks : new ArrayList<>());
        List<Task> cloudTasksCopy = new ArrayList<>(cloudTasks != null ? cloudTasks : new ArrayList<>());
        Map<String, Task> taskMap = new HashMap<>();

        if (cloudTasksCopy.isEmpty() && localTasksCopy.isEmpty()) {
            return new ArrayList<>();
        }

        for (Task cloudTask : cloudTasksCopy) {
            if (cloudTask != null && cloudTask.getTask_id() != null) {
                cloudTask.setSync_status("cloud");
                taskMap.put(cloudTask.getTask_id(), cloudTask);
            }
        }

        if (localTasksCopy.isEmpty()) {
            return new ArrayList<>(taskMap.values());
        }

        for (Task localTask : localTasksCopy) {
            if (localTask == null) continue;
            
            String taskId = localTask.getTask_id();
            
            if (taskId == null) continue;
            
            if (localTask.getSync_status() == null) {
                localTask.setSync_status("new");
            }
            
            if (taskMap.containsKey(taskId)) {
                Task cloudTask = taskMap.get(taskId);
                
                LocalDateTime localLastSync = localTask.getLast_sync();
                LocalDateTime cloudLastSync = cloudTask.getLast_sync();
                
                String localStatus = localTask.getSync_status();
                
                if ("updated".equals(localStatus)) {
                    taskMap.put(taskId, localTask);
                } else if ("local".equals(localStatus)) {
                    localTask.setSync_status("updated");
                    taskMap.put(taskId, localTask);
                } else if (localLastSync == null && cloudLastSync == null) {
                    if ("new".equals(localStatus)) {
                        localTask.setSync_status("new");
                        taskMap.put(taskId, localTask);
                    }
                } else if (localLastSync == null) {
                    if ("new".equals(localStatus)) {
                        localTask.setSync_status("new");
                        taskMap.put(taskId, localTask);
                    }
                } else if (cloudLastSync == null) {
                    localTask.setSync_status("updated");
                    taskMap.put(taskId, localTask);
                } else if (localLastSync.isAfter(cloudLastSync)) {
                    localTask.setSync_status("updated");
                    taskMap.put(taskId, localTask);
                } 
            } else {
                if (!"updated".equals(localTask.getSync_status())) {
                    localTask.setSync_status("new");
                }
                taskMap.put(taskId, localTask);
            }
        }

        return new ArrayList<>(taskMap.values());
    }
}
