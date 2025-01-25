package DBH;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.PasswordUtil;
import model.Task;


public class TaskDAO {

    public static List<Task> loadTasksFromDatabase(int userId, boolean isDone, boolean isDeleted){
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.task_title, t.description, t.target_date, t.date_added, t.deleted_at, t.folder_id, f.folder_name " +
                     "FROM tasks t LEFT JOIN folders f ON t.folder_id = f.id " +
                     "WHERE (t.user_id = ? AND t.is_done = ?) AND t.deleted_at IS " + (isDeleted ? "NOT NULL" : "NULL");
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
                        .folderName(rs.getString("folder_name"))
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

    public static List<Task> loadTasksFromDatabaseByFolder(int userId, boolean isDone, String folderName){ //try to not use it
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.task_title, t.description, t.target_date, t.date_added, t.deleted_at, t.folder_id FROM tasks t LEFT JOIN folders f ON t.folder_id = f.id WHERE (t.user_id = ? AND t.is_done = ?) AND f.folder_name = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, userId);
            pstmt.setBoolean(2, isDone);
            pstmt.setString(3, folderName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                int folderId = rs.getInt("folder_id");
                Task task = new Task.Builder(userId)
                        .id(rs.getInt("id"))
                        .taskTitle(rs.getString("task_title"))
                        .description(rs.getString("description"))
                        .dateAdded(rs.getTimestamp("date_added").toLocalDateTime())
                        .folderId(folderId)
                        .folderName(folderName)
                        .build();
                task.setIsDone(isDone);
                tasks.add(task);
            }
        }catch (SQLException e){
            System.out.println("Error loading tasks from the database by folder");
            e.printStackTrace();
        }
        return tasks;
    }

    public static void loadTasksFromCloud(int userId){
        List<Task> tasks = new ArrayList<>();
        //get all the rows with sync_status = 'CLOUD' from the selected user
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND sync_status = 'CLOUD'";
        try (Connection conn = PSQLtdldbh.getCloudConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                Task task = new Task.Builder(userId)
                        .id(rs.getInt("id"))
                        .taskTitle(rs.getString("task_title"))
                        .description(rs.getString("description"))
                        .dateAdded(rs.getTimestamp("date_added").toLocalDateTime())
                        .targetDate(rs.getTimestamp("target_date") != null ? rs.getTimestamp("target_date").toLocalDateTime() : null)
                        .folderId(rs.getInt("folder_id"))
                        .deletedAt(rs.getTimestamp("deleted_at") != null ? rs.getTimestamp("deleted_at").toLocalDateTime() : null)
                        .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                        .build();
                    task.setIsDone(rs.getBoolean("is_done"));
                    tasks.add(task);
            }
        }catch (SQLException e){
            System.out.println("Error loading tasks from the cloud");
            e.printStackTrace();
        }
        saveTasksToDatabase(tasks);
    }

    public static void saveTaskToDatabase(Task task) {
        try (Connection conn = PSQLtdldbh.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get folder_id first
                int folderId;
                String folderSql = "SELECT id FROM folders WHERE FOLDER_NAME = ? AND user_id = ?";
                try (PreparedStatement folderStmt = conn.prepareStatement(folderSql)){
                    folderStmt.setString(1, task.getFolderName());
                    folderStmt.setInt(2, task.getUserId());
                    ResultSet rs = folderStmt.executeQuery();
                    if (!rs.next()){
                        throw new SQLException("Folder not found: " + task.getFolderName());
                    }
                    folderId = rs.getInt("id");
                }
    
                // Then insert task
                String taskSql = "INSERT INTO tasks (task_title, description, is_done, user_id, folder_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement taskStmt = conn.prepareStatement(taskSql, Statement.RETURN_GENERATED_KEYS)){
                    taskStmt.setString(1, task.getTaskTitle());
                    taskStmt.setString(2, task.getDescription());
                    taskStmt.setBoolean(3, task.getIsDone());
                    taskStmt.setInt(4, task.getUserId());
                    taskStmt.setInt(5, folderId);
                    taskStmt.executeUpdate();
                    
                    ResultSet rs = taskStmt.getGeneratedKeys();
                    if (rs.next()) {
                        task.setId(rs.getInt(1));
                    }
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error saving task to database");
            e.printStackTrace();
        }
    }

    public static void saveTasksToDatabase(List<Task> tasks){ //modify
        String sql = """
            INSERT INTO tasks (task_title, description, date_added, is_done, user_id, )
            """;

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

    public static void loadFoldersFromCloud(int userId){
        List<String> folders = new ArrayList<>();
        List<Integer> folderIds = new ArrayList<>();
    
        // First get cloud folders
        String cloudSql = "SELECT folder_name, id FROM folders WHERE user_id = ?";
        try (Connection conn = PSQLtdldbh.getCloudConnection();
                PreparedStatement pstmt = conn.prepareStatement(cloudSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                folders.add(rs.getString("folder_name"));
                folderIds.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading folders from cloud");
            return;
        }
    
        // Then insert only new folders
        String checkSql = "SELECT id FROM folders WHERE id = ?";
        String insertSql = "INSERT INTO folders (user_id, folder_name, id) VALUES (?, ?, ?)";
        
        try (Connection conn = PSQLtdldbh.getLocalConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            
            for (int i = 0; i < folders.size(); i++) {
                checkStmt.setInt(1, folderIds.get(i));
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {  // Only insert if folder doesn't exist
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, folders.get(i));
                    insertStmt.setInt(3, folderIds.get(i));
                    insertStmt.addBatch();
                }
            }
            insertStmt.executeBatch();
            System.out.println("Folders synced successfully");
        } catch (SQLException e) {
            System.out.println("Error syncing some folders");
        }
    }

    public static void saveFoldersToDatabase(List<String> folders, int userId){
        String sql = "INSERT INTO folders (user_id, folder_name) VALUES (?, ?)";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            for(String folder : folders){
                pstmt.setInt(1, userId);
                pstmt.setString(2, folder);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }catch (SQLException e){
            System.out.println("Error saving folders to the database");
            e.printStackTrace();
        }
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
        String sql = "UPDATE tasks SET task_title = ?, description = ?, is_done = ?, updated_at = ?, sync_status = 'MOD', target_date = ?, folder_id = ?, deleted_at = ? WHERE id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, task.getTaskTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setBoolean(3, task.getIsDone());
            pstmt.setInt(4, task.getId());
            pstmt.setObject(5, task.getUpdatedAt());
            pstmt.setObject(6, task.getTargetDate() != null ? task.getTargetDate() : null);
            pstmt.setInt(7, task.getFolderId());
            pstmt.setObject(8, task.getDeletedAt() != null ? task.getDeletedAt() : null);
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Error updating task in the database");
            e.printStackTrace();
        }
    }

    public static void updateLocalTasksToCloud(int userId){
        List <Task> tasks = new ArrayList<>();
        String sqlLocalTasks = "SELECT * FROM tasks WHERE sync_status = 'LOCAL' AND user_id = ?";

        //fetch local tasks
        try (Connection localConn = PSQLtdldbh.getLocalConnection(); 
             PreparedStatement pstmt = localConn.prepareStatement(sqlLocalTasks)){
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Task task = new Task.Builder(userId)
                    .id(rs.getInt("id"))
                    .taskTitle(rs.getString("task_title"))
                    .description(rs.getString("description"))
                    .dateAdded(rs.getTimestamp("date_added").toLocalDateTime())
                    .targetDate(rs.getTimestamp("target_date") != null ? rs.getTimestamp("target_date").toLocalDateTime() : null)
                    .deletedAt(rs.getTimestamp("deleted_at") != null ? rs.getTimestamp("deleted_at").toLocalDateTime() : null)
                    .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                    .folderId(rs.getInt("folder_id"))
                    .build();
                task.setIsDone(rs.getBoolean("is_done"));
                tasks.add(task);
            }
            //update the sync status to CLOUD
            String sqlUpdateSyncStatus = "UPDATE tasks SET sync_status = 'CLOUD' WHERE sync_status = 'LOCAL' AND user_id = ?";
            try (PreparedStatement updatePstmt = localConn.prepareStatement(sqlUpdateSyncStatus)){
                updatePstmt.setInt(1, userId);
                updatePstmt.executeUpdate();
            }catch(SQLException e){
                System.out.println("Error updating sync status to CLOUD");
                e.printStackTrace();
            }
        } catch(SQLException e){
            System.out.println("Error fetching local tasks for upload");
            e.printStackTrace();
        }
        //send them to cloud
        String sqlCloudTasks = "INSERT INTO tasks (task_title, description, date_added, is_done, target_date, deleted_at, updated_at, sync_status, last_sync, folder_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cloudConn = PSQLtdldbh.getCloudConnection();
             PreparedStatement pstmt = cloudConn.prepareStatement(sqlCloudTasks)){
            for(Task task : tasks){
                pstmt.setString(1, task.getTaskTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setObject(3, task.getDateAdded());
                pstmt.setBoolean(4, task.getIsDone());
                pstmt.setObject(5, task.getTargetDate() != null ? task.getTargetDate() : null);
                pstmt.setObject(6, task.getDeletedAt() != null ? task.getDeletedAt() : null);
                pstmt.setObject(7, task.getUpdatedAt() != null ? task.getUpdatedAt() : null);
                pstmt.setString(8, "CLOUD");
                pstmt.setObject(9, java.time.LocalDateTime.now());
                pstmt.setInt(10, task.getFolderId());
                pstmt.setInt(11, userId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }catch(SQLException e){
            System.out.println("Error uploading local tasks to the cloud");
            e.printStackTrace();
        }
        tasks.clear();
        String sqlUpdateMod = "SELECT * FROM tasks WHERE sync_status = 'MOD' AND user_id = ?";

        //fetch local modified tasks
        try (Connection localConn = PSQLtdldbh.getLocalConnection(); 
             PreparedStatement pstmt = localConn.prepareStatement(sqlUpdateMod)){
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Task task = new Task.Builder(userId)
                    .id(rs.getInt("id"))
                    .taskTitle(rs.getString("task_title"))
                    .description(rs.getString("description"))
                    .dateAdded(rs.getTimestamp("date_added").toLocalDateTime())
                    .targetDate(rs.getTimestamp("target_date") != null ? rs.getTimestamp("target_date").toLocalDateTime() : null)
                    .deletedAt(rs.getTimestamp("deleted_at") != null ? rs.getTimestamp("deleted_at").toLocalDateTime() : null)
                    .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                    .folderId(rs.getInt("folder_id"))
                    .build();
                task.setIsDone(rs.getBoolean("is_done"));
                tasks.add(task);
            }
            //update the sync status to CLOUD
            String sqlUpdateSyncStatus = "UPDATE tasks SET sync_status = 'CLOUD' WHERE sync_status = 'MOD' AND user_id = ?";
            try (PreparedStatement updatePstmt = localConn.prepareStatement(sqlUpdateSyncStatus)){
                updatePstmt.setInt(1, userId);
                updatePstmt.executeUpdate();
            }catch(SQLException e){
                System.out.println("Error updating sync status to CLOUD");
                e.printStackTrace();
            }
        } catch(SQLException e){
            System.out.println("Error fetching local tasks for upload");
            e.printStackTrace();
        }
        //send them to cloud
        sqlUpdateMod = "UPDATE tasks SET task_title = ?, description = ?, is_done = ?, target_date = ?, deleted_at = ?, updated_at = ?, sync_status = ?, last_sync = ?, folder_id = ? WHERE id = ?";

        try (Connection cloudConn = PSQLtdldbh.getCloudConnection();
             PreparedStatement pstmt = cloudConn.prepareStatement(sqlUpdateMod)){
            for(Task task : tasks){
                pstmt.setString(1, task.getTaskTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setBoolean(3, task.getIsDone());
                pstmt.setObject(4, task.getTargetDate() != null ? task.getTargetDate() : null);
                pstmt.setObject(5, task.getDeletedAt() != null ? task.getDeletedAt() : null);
                pstmt.setObject(6, task.getUpdatedAt() != null ? task.getUpdatedAt() : null);
                pstmt.setString(7, "CLOUD");
                pstmt.setObject(8, java.time.LocalDateTime.now());
                pstmt.setInt(9, task.getFolderId());
                pstmt.setInt(10, task.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }catch(SQLException e){
            System.out.println("Error uploading local tasks to the cloud");
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

    public static void deleteTaskFromDatabase(int taskId){
        String sql = "UPDATE tasks SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Error deleting task from the database");
            e.printStackTrace();
        }
    }

    public static void hardDeleteTaskFromDatabase(int taskId){
        String sql = "DELETE FROM public.tasks WHERE deleted_at IS NOT NULL AND id = ?";
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, taskId);
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
        String sql = "SELECT username, password, id FROM users WHERE username = ?";
        try (Connection conn = PSQLtdldbh.getCloudConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                String hashedPassword = rs.getString("password");
                if(PasswordUtil.checkPassword(password, hashedPassword)){
                    System.out.println("User validated from cloud");

                    // Try to insert user into local database if not already present
                    int userId = getUserId(username);
                    if (userId == -1) {
                        String insertSql = "INSERT INTO users (username, password, id) VALUES (?, ?, ?)";
                        try (Connection localConn = PSQLtdldbh.getConnection();
                                PreparedStatement insertPstmt = localConn.prepareStatement(insertSql)) {
                            insertPstmt.setString(1, username);
                            insertPstmt.setString(2, hashedPassword);
                            insertPstmt.setInt(3, rs.getInt("id"));
                            insertPstmt.executeUpdate();
                            System.out.println("User inserted into local database");
                        } catch (SQLException e) {
                            System.out.println("Error inserting user into local database");
                            e.printStackTrace();
                        }
                    }
                    return true;
                } else {
                    System.out.println("Invalid password");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error validating user from the cloud database, trying local database");
            e.printStackTrace();
        }

        // Try to validate from local database if cloud validation fails
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                String hashedPassword = rs.getString("password");
                if(PasswordUtil.checkPassword(password, hashedPassword)){
                    System.out.println("User validated from local database");
                    return true;
                } else {
                    System.out.println("Invalid password");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error validating user from the local database");
            e.printStackTrace();
        }
        return false;
    }

    public static int getUserId(String username){
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = PSQLtdldbh.getLocalConnection();
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
        try (Connection conn = PSQLtdldbh.getCloudConnection(); //register only on cloud
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, PasswordUtil.hashPassword(password));
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.out.println("Error registering user to the cloud database");
            e.printStackTrace();
            return;
        }

        // Add user to local database
        try (Connection conn = PSQLtdldbh.getLocalConnection(); //local database
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, PasswordUtil.hashPassword(password));
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.out.println("Error registering user to the local database");
            e.printStackTrace();
        }
    }

    public static void syncDatabases(int userId) {
        //since register works only on cloud, we need to sync the user data to the local database
        
        loadFoldersFromCloud(userId);
        updateLocalTasksToCloud(userId);
    }    
    

    /* sync try
     * 
     * try (Connection cloudConn = PSQLtdldbh.getCloudConnection();
                 PreparedStatement loadCloud = cloudConn.prepareStatement(sql);
                 PreparedStatement insertCloud = cloudConn.prepareStatement("INSERT INTO tasks (id, task_title, description, is_done, target_date, deleted_at, updated_at, sync_status, last_sync, folder_id, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                 PreparedStatement updateCloud = cloudConn.prepareStatement("UPDATE tasks SET task_title = ?, description = ?, is_done = ?, target_date = ?, deleted_at = ?, updated_at = ?, sync_status = ?, last_sync = ?, folder_id = ? WHERE id = ?");
                 PreparedStatement deleteCloud = cloudConn.prepareStatement("DELETE FROM tasks WHERE id = ?");
                 PreparedStatement updateLocal = conn.prepareStatement("UPDATE tasks SET sync_status = 'SYNCED' WHERE id = ?")){
                // Load local tasks
                loadLocal.setInt(1, userId);
                ResultSet rs = loadLocal.executeQuery();
                while (rs.next()){
                    insertCloud.setInt(1, rs.getInt("id"));
                    insertCloud.setString(2, rs.getString("task_title"));
                    insertCloud.setString(3, rs.getString("description"));
                    insertCloud.setBoolean(4, rs.getBoolean("is_done"));
                    insertCloud.setObject(5, rs.getObject("target_date"));
                    insertCloud.setObject(6, rs.getObject("deleted_at"));
                    insertCloud.setObject(7, rs.getObject("updated_at"));
                    insertCloud.setString(8, rs.getString("sync_status"));
                    insertCloud.setObject(9, rs.getObject("last_sync"));
                    insertCloud.setInt(10, rs.getInt("folder_id"));
                    insertCloud.setInt(11, userId);
                    insertCloud.addBatch();
                }

                // Load cloud tasks
                loadCloud.setInt(1, userId);
                rs = loadCloud.executeQuery();
                while (rs.next()){
                    int id = rs.getInt("id");
                    Task task = new Task.Builder(userId)
                            .id(id)
                            .taskTitle(rs.getString("task_title"))
                            .description(rs.getString("description"))
                            .isDone(rs.getBoolean("is_done"))
                            .targetDate(rs.getTimestamp("target_date"))
                            .deletedAt(rs.getTimestamp("deleted_at"))
                            .updatedAt(rs.getTimestamp("updated_at"))
                            .syncStatus(rs.getString("sync_status"))
                            .lastSync(rs.getTimestamp("last_sync"))
                            .folderId(rs.getInt("folder_id"))
                            .build();
     */


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
