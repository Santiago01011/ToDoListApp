package COMMON;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class UserProperties {
    private static final String BASE_DIRECTORY = System.getProperty("user.home") + File.separator + ".todoapp";
    private static final String USER_PROPS_FILE = BASE_DIRECTORY + File.separator + "user.yml";
    private static Map<String, Object> properties = new HashMap<>();

    static {
        initialize();
    }

    private static void initialize() {
        createDirectory();
        properties = loadProperties();
        if (properties.isEmpty()) {
            createDefaultProperties();
        }
    }

    private static void createDirectory() {
        try {
            Files.createDirectories(Paths.get(System.getProperty("user.home"), ".todoapp"));
        } catch (IOException e) {
            handleError("Error creating directory", e);
        }
    }

    private static void createDefaultProperties() {
        properties.put("darkTheme", "false");
        properties.put("rememberMe", "false");
        properties.put("username", "");
        properties.put("password", "");
        properties.put("lastSession", "");
        properties.put("userUUID", "");
        properties.put("authApiUrl", "http://localhost:8080");
        properties.put("dbUrl", "jdbc:postgresql://127.0.0.1:5431/todo_list?user=task_manager&password=task_manager_password");
        properties.put("token", "");
        saveProperties();
    }

    private static Map<String, Object> loadProperties() {
        Yaml yaml = new Yaml();
        try {
            if (Files.exists(Paths.get(USER_PROPS_FILE))) {
                try (InputStream inputStream = new FileInputStream(USER_PROPS_FILE)) {
                    return yaml.load(inputStream);
                }
            }
        } catch (IOException e) {
            handleError("Error loading properties file", e);
        }
        return new HashMap<>();
    }

    public static void saveProperties() {
        try (Writer writer = new FileWriter(USER_PROPS_FILE)) {
            Yaml yaml = new Yaml();
            yaml.dump(properties, writer);
        } catch (IOException e) {
            handleError("Error saving properties file", e);
        }
    }

    public static void setProperty(String key, Object value) {
        properties.put(key, value);
        saveProperties();
    }

    public static Object getProperty(String key) {
        return properties.get(key);
    }    
      public static void logOut() {
        // Only clear session-related properties, preserve user data
        properties.put("rememberMe", "false");
        properties.put("username", "");
        properties.put("password", "");
        properties.put("lastSession", "");
        properties.put("userUUID", "");
        properties.put("token", "");
        
        // Save the cleared session properties
        saveProperties();
        
        // NOTE: User data (tasks.json, pending_commands.json, etc.) is preserved
        // in the user-specific directory: ~/.todoapp/users/{userId}/
        System.out.println("User logged out. Session cleared but user data preserved.");
    }

    private static void handleError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }

    /**
     * Get the base application directory path
     */
    public static String getBaseDirectory() {
        return BASE_DIRECTORY;
    }
    
    /**
     * Get the user-specific data directory for the given user ID
     * Creates the directory if it doesn't exist
     */
    public static String getUserDataDirectory(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        String userDir = BASE_DIRECTORY + File.separator + "users" + File.separator + userId;
        try {
            Files.createDirectories(Paths.get(userDir));
        } catch (IOException e) {
            handleError("Error creating user data directory for user: " + userId, e);
        }
        return userDir;
    }
    
    /**
     * Get the current logged-in user's ID
     * Returns null if no user is logged in or userUUID is empty
     */
    public static String getCurrentUserId() {
        String currentUserId = (String) getProperty("userUUID");
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return null;
        }
        return currentUserId;
    }
    
    /**
     * Get the current logged-in user's data directory
     * Returns null if no user is logged in
     */
    public static String getCurrentUserDataDirectory() {
        String currentUserId = (String) getProperty("userUUID");
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return null;
        }
        return getUserDataDirectory(currentUserId);
    }
    
    /**
     * Get the path for a user-specific file (e.g., tasks.json, pending_commands.json)
     */
    public static String getUserDataFilePath(String userId, String filename) {
        return getUserDataDirectory(userId) + File.separator + filename;
    }
    
    /**
     * Get the path for a current user's file
     */
    public static String getCurrentUserDataFilePath(String filename) {
        String userDir = getCurrentUserDataDirectory();
        if (userDir == null) {
            throw new IllegalStateException("No user is currently logged in");
        }
        return userDir + File.separator + filename;
    }
}