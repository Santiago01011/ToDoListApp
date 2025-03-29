package DBH;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.Task;
import model.TaskHandler;

public class DBHandler {

    public List<Task> insertTasksFromJSON(UUID userUUID, String jsonContent, List<Task> taskList) {
        String query = "SELECT * FROM todo.insert_tasks_from_jsonb(?, ?::jsonb)";
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            pstmt.setString(2, jsonContent);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String jsonResult = rs.getString(2);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(jsonResult);
                    JsonNode successArray = rootNode.get("success");
                    if (successArray == null || !successArray.isArray()) {
                        continue;
                    }
                    for (JsonNode successItem : successArray) {
                        JsonNode taskArray = successItem.get("task");
                        if (taskArray == null || !taskArray.isArray() || taskArray.size() < 2) {
                            continue;
                        }
                        String dbId = taskArray.get(1).asText();
                        String tempId = taskArray.get(0).asText();
                        taskList.stream()
                            .filter(task -> tempId.equals(task.getTask_id()))
                            .findFirst()
                            .ifPresent(task -> {
                                task.setTask_id(dbId);
                                task.setSync_status("cloud");
                            });
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

    public void updateTasksFromJSON(UUID userUUID, String jsonContent) {
        String query = "SELECT * FROM update_tasks_from_json(?, ?)";
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userUUID);
            pstmt.setString(2, jsonContent);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating tasks from JSON: " + e.getMessage());
        }
    }

    public List<Task> syncTasks(String userUuid, String insertJsonContent, String updateJsonContent, List<Task> taskList) {
        try {
            UUID uuid = UUID.fromString(userUuid);
            List<Task> updatedTaskList = new ArrayList<>();
            if(insertJsonContent != null)
                updatedTaskList = insertTasksFromJSON(uuid, insertJsonContent, taskList);
            //updateTasksFromJSON(uuid, updateJsonContent);
            return updatedTaskList;
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            // Log the error or handle it gracefully without stopping the program
            return taskList; 
        }
        
    }

    /* Prepare JSON content for sync process */
    public void startSyncProcess(TaskHandler taskHandler) {
        String insertJsonContent = taskHandler.prepareSyncJsonContent("new");
        String updateJsonContent = taskHandler.prepareSyncJsonContent("update");
        taskHandler.userTasksList = syncTasks("01959f92-0d81-78ab-9c17-c180be5d9a37", insertJsonContent, updateJsonContent, taskHandler.userTasksList);
        taskHandler.prepareLocalTasksJson();
    }
}
