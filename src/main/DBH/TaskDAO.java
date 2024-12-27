package main.DBH;

import main.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class TaskDAO {
    public static List<Task> loadTasksFromDatabase(int userId, boolean isDone) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND is_done = ?";
        try (Connection conn = PSQLtdldbh.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setBoolean(2, isDone);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("task_title"),
                        rs.getString("description"),
                        rs.getInt("user_id")
                );
                task.setIsDone(rs.getBoolean("is_done"));
                task.setDateAdded(rs.getTimestamp("date_added").toLocalDateTime());
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.out.println("Error loading tasks from the database");
            e.printStackTrace();
        }
        return tasks;
    }

    public static void saveTaskToDatabase(Task task) {
        String sql = "INSERT INTO tasks (task_title, description, date_added, is_done, user_id) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = PSQLtdldbh.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTaskTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setObject(3, task.getDateAdded());
            pstmt.setBoolean(4, task.getIsDone());
            pstmt.setInt(5, task.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                task.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("Error saving task to the database");
            e.printStackTrace();
        }
    }

    public static void updateTaskInDatabase(Task task) {
        String sql = "UPDATE tasks SET task_title = ?, description = ?, is_done = ? WHERE id = ?";
        try (Connection conn = PSQLtdldbh.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTaskTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setBoolean(3, task.getIsDone());
            pstmt.setInt(4, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating task in the database");
            e.printStackTrace();
        }
    }

    public static void deleteTaskFromDatabase(Task task) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = PSQLtdldbh.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting task from the database");
            e.printStackTrace();
        }
    }
}
