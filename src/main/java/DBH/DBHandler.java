package DBH;

import COMMON.JSONUtils;
import model.TaskHandler;
import model.Task;
import model.TaskStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

/**
 * Database handler for folder management and legacy database operations.
 * Task sync operations have been migrated to SyncService using API V2.
 */
public class DBHandler {
    private UUID userUUID = null;
    
    /**
     * Constructor for DBHandler.
     * Note: Task sync functionality has been moved to SyncService.
     */
    public DBHandler() {
        // Constructor simplified - task sync moved to SyncService

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

    public void setUserUUID(String userUUID) {
        this.userUUID = UUID.fromString(userUUID);
    }
    
    public String getUserUUID() {
        return userUUID != null ? userUUID.toString() : null;
    }

    /**
     * Public wrapper to retrieve the current user's accessible folders.
     * @return List of accessible Folder objects
     */
    public List<Folder> fetchAccessibleFolders() {
        if (userUUID == null) {
            System.err.println("User UUID is not set. Cannot fetch folders.");
            return new ArrayList<>();
        }
        return getAccessibleFolders(userUUID);
    }
}