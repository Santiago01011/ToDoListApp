package DBH;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBHandler {

    public void insertTasksFromJSON(String userUUID, String jsonFilePath) {
        String query = "SELECT * FROM insert_tasks_from_json(?, ?)";
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userUUID);
            pstmt.setString(2, jsonFilePath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting tasks from JSON: " + e.getMessage());
        }
    }

    public void updateTasksFromJSON(String userUUID, String jsonFilePath) {
        String query = "SELECT * FROM update_tasks_from_json(?, ?)";
        try (Connection conn = NeonPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userUUID);
            pstmt.setString(2, jsonFilePath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating tasks from JSON: " + e.getMessage());
        }
    }

    public void syncTasks(String userUUID, String insertJsonFilePath, String updateJsonFilePath) {
        if (insertJsonFilePath != null) {
            insertTasksFromJSON(userUUID, insertJsonFilePath);
        }
        if (updateJsonFilePath != null) {
            updateTasksFromJSON(userUUID, updateJsonFilePath);
        }
    }
}
