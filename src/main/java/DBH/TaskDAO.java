package DBH;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.PasswordUtil;
import model.Task;


public class TaskDAO {

    public static List<Task> loadTasksFromDatabase(int userId, boolean isDone, boolean isDeleted){
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.task_title, t.description, t.target_date, t.date_added, t.deleted_at, t.folder_id FROM tasks t LEFT JOIN folders f ON t.folder_id = f.id WHERE (t.user_id = ? AND t.is_done = ?) AND t.deleted_at IS " 
        + (isDeleted ? "NOT NULL" : "NULL");
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, userId);
            pstmt.setBoolean(2, isDone);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                Task task = new Task.Builder(userId)
                        .id(rs.getInt("id"))
                        .taskTitle(rs.getString("task_title"))
                        .description(rs.getString("description"))
                        .dateAdded(rs.getTimestamp("date_added").toLocalDateTime())
                        .folderId(rs.getInt("folder_id"))
                        .build();
                task.setIsDone(isDone);
                tasks.add(task);
            }
        }catch (SQLException e){
            System.out.println("Error loading tasks from the database");
            e.printStackTrace();
        }
        return tasks;
    }

    public static void saveTaskToDatabase(Task task){
        String sql = "INSERT INTO tasks (task_title, description, is_done, user_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = PSQLtdldbh.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTaskTitle());
            pstmt.setString(2, task.getDescription());
            //pstmt.setObject(3, task.getDateAdded());
            pstmt.setBoolean(3, task.getIsDone());
            pstmt.setInt(4, task.getUserId());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                task.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("Error saving task to the database");
            e.printStackTrace();
        }
    }

    public static void saveTasksToDatabase(List<Task> tasks){
        String sql = "INSERT INTO tasks (task_title, description, date_added, is_done, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = PSQLtdldbh.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)){
            for(Task task : tasks){
                pstmt.setString(1, task.getTaskTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setObject(3, task.getDateAdded());
                pstmt.setBoolean(4, task.getIsDone());
                pstmt.setInt(5, task.getUserId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }catch (SQLException e){
            System.out.println("Error saving tasks to the database");
            e.printStackTrace();
        }
    }

    public static List<String> loadFoldersFromDatabase(int userId){
        List<String> folders = new ArrayList<>();
        String sql = "SELECT folder_name FROM folders WHERE user_id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                folders.add(rs.getString("folder_name"));
            }
        }catch (SQLException e){
            System.out.println("Error loading folders from the database");
            e.printStackTrace();
        }
        return folders;
   }

    public static void saveFolderToDatabase(String folderName, int userId){
        String sql = "INSERT INTO folders (user_id, folder_name) VALUES (?, ?)";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, userId);
            pstmt.setString(2, folderName);
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Error saving folder to the database");
            e.printStackTrace();
        }
   }

    public static void updateTaskInDatabase(Task task){
        String sql = "UPDATE tasks SET task_title = ?, description = ?, is_done = ? WHERE id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, task.getTaskTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setBoolean(3, task.getIsDone());
            pstmt.setInt(4, task.getId());
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Error updating task in the database");
            e.printStackTrace();
        }
    }

    public static void updateDoneTaskInDatabase(Task task){
        String sql = "UPDATE tasks SET is_done = ? WHERE id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setBoolean(1, task.getIsDone());
            pstmt.setInt(2, task.getId());
            pstmt.executeUpdate();
        }catch(SQLException e){
                System.out.println("Error updating done tasks in the database");
                e.printStackTrace();
        }
    }

    public static void editTasksInDatabase(List <Task> tasks){
        String sql = "UPDATE tasks SET task_title = ?, description = ? WHERE id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            for(Task task : tasks){
                pstmt.setString(1, task.getTaskTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setInt(3, task.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }catch(SQLException e){
            System.out.println("Error editing tasks in the database");
            e.printStackTrace();
        }
    }

    public static void deleteTaskFromDatabase(Task task){
        String sql = "UPDATE tasks SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, task.getId());
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Error deleting task from the database");
            e.printStackTrace();
        }
    }

    public static void hardDeleteTaskFromDatabase(Task task){
        String sql = "DELETE FROM public.tasks WHERE deleted_at IS NOT NULL AND id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, task.getId());
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Error hard deleting task from the database");
            e.printStackTrace();
        }
    }

    public static void restoreTaskFromDatabase(Task task){
        String sql = "UPDATE tasks SET deleted_at = NULL WHERE id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, task.getId());
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Error restoring task from the database");
            e.printStackTrace();
        }
    }

    public static boolean validateUserFromDatabase(String username, String password){
        String sql = "SELECT username, password FROM users WHERE username = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                String hashedPassword = rs.getString("password");
                if(PasswordUtil.checkPassword(password, hashedPassword)){
                    System.out.println("User validated");
                    return true;
                }else{
                    System.out.println("Invalid password");
                }
            }
        }catch (SQLException e){
            System.out.println("Error validating user from the database");
            e.printStackTrace();
            if(e.getMessage().contains("this database is empty")){
                System.out.println("Database is empty");
                H2Manager.createTablesIfNotExist();
            }
        }
        return false;
    }

    public static int getUserId(String username){
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error getting user id from the database :c");
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean getUsername(String username){
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        }catch (SQLException e){
            System.out.println("Error getting username from the database");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean getEmail(String email){
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        }catch (SQLException e){
            System.out.println("Error getting email from the database");
            e.printStackTrace();
        }
        return false;
    }

    public static void registerUser(String username, String email, String password){
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = PSQLtdldbh.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, PasswordUtil.hashPassword(password));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error registering user to the database");
            e.printStackTrace();
        }
    }


    public static void changePassword(String username, String password){
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PasswordUtil.hashPassword(password));
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error changing password in the database");
            e.printStackTrace();
        }
    }
    // This method is going to be used only once for migrate the passwords
    /*public static void migratePasswords() {
        // Get all users
        String selectSql = "SELECT id, username, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            
            ResultSet rs = selectStmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String currentPassword = rs.getString("password");
                
                // Hash current password if not already hashed
                if (!currentPassword.startsWith("$2a$")) {
                    String hashedPassword = PasswordUtil.hashPassword(currentPassword);
                    
                    // Update with new hashed password
                    updateStmt.setString(1, hashedPassword);
                    updateStmt.setInt(2, id);
                    updateStmt.addBatch();
                }
            }
            
            updateStmt.executeBatch();
            System.out.println("Password migration completed successfully");
            
        } catch (SQLException e) {
            System.out.println("Error migrating passwords");
            e.printStackTrace();
        }
    }*/


}
