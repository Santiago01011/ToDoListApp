package DBH;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.Task;
import model.TaskHandler;

public class DBHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Inserts tasks into the database from a JSON string and updates the provided task list.
     *
     * @param userUUID   The UUID of the user logged in.
     * @param jsonContent A JSON string containing data of new tasks to insert in the DataBase.
     * @param taskList   The user task list to be updated after insertion.
     * @return The updated list of tasks.
     */
    private List<Task> insertTasksFromJSON(UUID userUUID, String jsonContent, List<Task> taskList) {
        String query = "SELECT * FROM todo.insert_tasks_from_jsonb(?, ?::jsonb)";

        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setObject(1, userUUID);
            pstmt.setString(2, jsonContent);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String jsonResult = rs.getString("log_tasks");
                    JsonNode rootNode = MAPPER.readTree(jsonResult);
                    JsonNode successArray = rootNode.get("success");

                    if (successArray != null && successArray.isArray()) {
                        successArray.forEach(successItem -> updateTaskListFromJson(successItem, taskList));
                    }
                }
                return taskList;
            } catch (Exception e) {
                System.err.println("Error processing insert results: " + e.getMessage());
                return taskList;
            }

        } catch (SQLException e) {
            System.err.println("Error inserting tasks from JSON: " + e.getMessage());
            return taskList;
        }
    }

    /**
     * Updates the task list based on a JSON node representing a successful task operation.
     * This method processes the response from database operations and updates the local task list.
     *
     * @param successItem The JSON node containing task data
     * @param taskList The list of tasks to be updated
     */
    private void updateTaskListFromJson(JsonNode successItem, List<Task> taskList) {
        JsonNode taskArray = successItem.get("task");

        if (taskArray != null && taskArray.isArray() && taskArray.size() >= 2) {
            String tempId = taskArray.get(0).asText();
            String dbId = taskArray.get(1).asText();

            taskList.stream()
                    .filter(task -> tempId.equals(task.getTask_id()))
                    .findFirst()
                    .ifPresent(task -> {
                        task.setTask_id(dbId);
                        task.setSync_status("cloud");
                    });
        }
    }

    /**
     * Updates existing tasks in the database from a JSON string and updates the provided task list.
     *
     * @param userUUID The UUID of the user logged in
     * @param jsonContent A JSON string containing data of updated tasks to reflect in the DataBase
     * @param taskList The user task list to be updated
     * @return The updated list of tasks
     */
    public List<Task> updateTasksFromJSON(UUID userUUID, String jsonContent, List<Task> taskList) {
        String query = "SELECT * FROM todo.update_tasks_from_jsonb(?, ?::jsonb)";

        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            pstmt.setString(2, jsonContent);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String jsonResult = rs.getString("log_tasks");
                    JsonNode rootNode = MAPPER.readTree(jsonResult);
                    JsonNode successArray = rootNode.get("success");

                    if (successArray != null && successArray.isArray()) {
                        successArray.forEach(successItem -> updateTaskListFromJson(successItem, taskList));
                    }
                }
                return taskList;
            } catch (Exception e) {
                System.err.println("Error processing update results: " + e.getMessage());
                return taskList;
            }
        } catch (SQLException e) {
            System.err.println("Error updating tasks from JSON: " + e.getMessage());
            return taskList;
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
            List<Task> updatedTaskList = new ArrayList<>();

            Map<String, Task> taskMap = new HashMap<>();
            for (Task task : taskList) {
                if (task != null && task.getTask_id() != null) {
                    taskMap.put(task.getTask_id(), task);
                }
            }

            if (insertJsonContent != null) {
                updatedTaskList = insertTasksFromJSON(uuid, insertJsonContent, taskList);
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
                updatedTaskList = updateTasksFromJSON(uuid, updateJsonContent, taskList);
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
     * @param taskHandler The TaskHandler containing the user's task list
     * @param user_id The UUID of the user as a string
     */
    public void startSyncProcess(TaskHandler taskHandler, String user_id) {
        try {
            UUID userUUID = UUID.fromString(user_id);

            if (taskHandler.userTasksList == null || taskHandler.userTasksList.isEmpty()) {
                System.out.println("First-time sync or empty local list detected.");
                
                List<Task> cloudTasks = retrieveTasksFromDB(userUUID);
                
                if (!cloudTasks.isEmpty()) {
                    taskHandler.userTasksList = new ArrayList<>(cloudTasks);
                    
                    taskHandler.prepareLocalTasksJson();
                    
                    System.out.println("Initial sync complete - retrieved " + cloudTasks.size() + " tasks from cloud.");
                    return;
                }
            }

            List<Task> cloudTasks = retrieveTasksFromDB(userUUID);

            List<Task> mergedTasks = mergeTasks(taskHandler.userTasksList, cloudTasks);

            String insertJsonContent = taskHandler.prepareSyncJsonContent("new");
            String updateJsonContent = taskHandler.prepareSyncJsonContent("updated");
            
            System.out.println("Update JSON Content: " + updateJsonContent);

            if (insertJsonContent == null && updateJsonContent == null) {
                System.out.println("No tasks to sync.");
                taskHandler.userTasksList = mergedTasks;
                taskHandler.prepareLocalTasksJson();
                return;
            }

            taskHandler.userTasksList = syncTasks(user_id, insertJsonContent, updateJsonContent, mergedTasks);

            taskHandler.prepareLocalTasksJson();
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Retrieves all tasks for a specific user from the database.
     * This method queries the database and transforms the JSON result into Task objects.
     *
     * @param userUUID The UUID of the user whose tasks are to be retrieved
     * @return A list of Task objects retrieved from the database
     */
    public List<Task> retrieveTasksFromDB(UUID userUUID) {
        String query = "SELECT * FROM todo.retrieve_tasks_in_jsonb(?)";
        List<Task> cloudTasks = new ArrayList<>();
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String jsonResult = rs.getString("log_tasks");
                    System.out.println("Retrieved JSON: " + jsonResult);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(jsonResult);
                    JsonNode dataArray = rootNode.get("data");
                    JsonNode columnsNode = rootNode.get("columns");
                    
                    if (dataArray == null || !dataArray.isArray() || columnsNode == null || !columnsNode.isArray()) {
                        System.err.println("Invalid JSON structure: missing data or columns array");
                        continue;
                    }
                    
                    Map<String, Integer> columnMap = new HashMap<>();
                    for (int i = 0; i < columnsNode.size(); i++) {
                        columnMap.put(columnsNode.get(i).asText(), i);
                    }
                    
                    for (JsonNode dataRow : dataArray) {
                        if (!dataRow.isArray()) continue;
                        
                        Task task = new Task();
                        
                        setTaskField(task, "folder_id", dataRow, columnMap);
                        setTaskField(task, "folder_name", dataRow, columnMap);
                        setTaskField(task, "task_id", dataRow, columnMap);
                        setTaskField(task, "task_title", dataRow, columnMap);
                        setTaskField(task, "description", dataRow, columnMap);
                        setTaskField(task, "sync_status", dataRow, columnMap);
                        setTaskField(task, "status", dataRow, columnMap);
                        setDateField(task, "due_date", dataRow, columnMap);
                        setDateField(task, "created_at", dataRow, columnMap);
                        setDateField(task, "last_sync", dataRow, columnMap);
                        
                        cloudTasks.add(task);
                    }
                }
                return cloudTasks;
            } catch (Exception e) {
                System.err.println("Error processing retrieve results: " + e.getMessage());
                e.printStackTrace();
                return cloudTasks;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving tasks from database: " + e.getMessage());
            return cloudTasks;
        }
    }

    /**
     * Helper method to set string field values on a Task object from database results.
     * This method safely handles null values and ensures proper field assignment.
     *
     * @param task The Task object to update
     * @param fieldName The name of the field to set
     * @param dataRow The JSON node containing the data
     * @param columnMap A map of column names to indices
     */
    private void setTaskField(Task task, String fieldName, JsonNode dataRow, Map<String, Integer> columnMap) {
        if (!columnMap.containsKey(fieldName)) return;
        
        int index = columnMap.get(fieldName);
        if (index >= dataRow.size() || dataRow.get(index) == null || dataRow.get(index).isNull()) return;
        
        String value = dataRow.get(index).asText();
        if ("null".equals(value)) return;
        
        switch (fieldName) {
            case "folder_id":
                task.setFolder_id(value);
                break;
            case "folder_name":
                task.setFolder_name(value);
                break;
            case "task_id":
                task.setTask_id(value);
                break;
            case "task_title":
                task.setTask_title(value);
                break;
            case "description":
                task.setDescription(value);
                break;
            case "sync_status":
                task.setSync_status(value);
                break;
            case "status":
                task.setStatus(value);
                break;
        }
    }

    /**
     * Helper method to set date field values on a Task object from database results.
     * This method safely parses date strings and handles null values.
     *
     * @param task The Task object to update
     * @param fieldName The name of the field to set
     * @param dataRow The JSON node containing the data
     * @param columnMap A map of column names to indices
     */
    private void setDateField(Task task, String fieldName, JsonNode dataRow, Map<String, Integer> columnMap) {
        if (!columnMap.containsKey(fieldName)) return;
        
        int index = columnMap.get(fieldName);
        if (index >= dataRow.size() || dataRow.get(index) == null || dataRow.get(index).isNull()) return;
        
        String value = dataRow.get(index).asText();
        if ("null".equals(value)) return;
        
        try {
            LocalDateTime dateTime = OffsetDateTime.parse(value).toLocalDateTime();
            switch (fieldName) {
                case "due_date":
                    task.setDue_date(dateTime);
                    break;
                case "created_at":
                    task.setCreated_at(dateTime);
                    break;
                case "last_sync":
                    task.setLast_sync(dateTime);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error parsing date for " + fieldName + ": " + value + " - " + e.getMessage());
        }
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
