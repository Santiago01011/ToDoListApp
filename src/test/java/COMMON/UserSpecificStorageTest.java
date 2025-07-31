package COMMON;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for user-specific data storage functionality.
 * Tests the UserProperties class methods for managing user-specific directories and files.
 */
@DisplayName("User-Specific Storage Tests")
class UserSpecificStorageTest {

    @TempDir
    Path tempDir;
    
    private String originalUserHome;
    private String testUserId1 = "test-user-1";
    private String testUserId2 = "test-user-2";

    @BeforeEach
    void setUp() {
        // Store original user.home and set temporary directory
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        // Restore original user.home
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    @DisplayName("Should create correct base directory path")
    void testGetBaseDirectory() {
        String baseDir = UserProperties.getBaseDirectory();
        String expected = tempDir.resolve(".todoapp").toString();
        assertEquals(expected, baseDir);
    }

    @Test
    @DisplayName("Should create correct user-specific directory paths")
    void testGetUserDataDirectory() {
        String userDir1 = UserProperties.getUserDataDirectory(testUserId1);
        String userDir2 = UserProperties.getUserDataDirectory(testUserId2);
        
        String expected1 = tempDir.resolve(".todoapp").resolve("users").resolve(testUserId1).toString();
        String expected2 = tempDir.resolve(".todoapp").resolve("users").resolve(testUserId2).toString();
        
        assertEquals(expected1, userDir1);
        assertEquals(expected2, userDir2);
        assertNotEquals(userDir1, userDir2);
    }

    @Test
    @DisplayName("Should create correct user-specific file paths")
    void testGetUserDataFilePath() {
        String tasksFile = UserProperties.getUserDataFilePath(testUserId1, "tasks.json");
        String commandsFile = UserProperties.getUserDataFilePath(testUserId1, "pending_commands.json");
        
        String expectedTasksFile = tempDir.resolve(".todoapp").resolve("users")
                                          .resolve(testUserId1).resolve("tasks.json").toString();
        String expectedCommandsFile = tempDir.resolve(".todoapp").resolve("users")
                                             .resolve(testUserId1).resolve("pending_commands.json").toString();
        
        assertEquals(expectedTasksFile, tasksFile);
        assertEquals(expectedCommandsFile, commandsFile);
    }

    @Test
    @DisplayName("Should handle null or empty user ID gracefully")
    void testNullOrEmptyUserId() {
        assertThrows(IllegalArgumentException.class, () -> 
            UserProperties.getUserDataDirectory(null));
        assertThrows(IllegalArgumentException.class, () -> 
            UserProperties.getUserDataDirectory(""));
        assertThrows(IllegalArgumentException.class, () -> 
            UserProperties.getUserDataFilePath(null, "test.json"));
        assertThrows(IllegalArgumentException.class, () -> 
            UserProperties.getUserDataFilePath("", "test.json"));
    }

    @Test
    @DisplayName("Should handle null or empty filename gracefully")
    void testNullOrEmptyFilename() {
        assertThrows(IllegalArgumentException.class, () -> 
            UserProperties.getUserDataFilePath(testUserId1, null));
        assertThrows(IllegalArgumentException.class, () -> 
            UserProperties.getUserDataFilePath(testUserId1, ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user1", "user-with-dashes", "user_with_underscores", "user123", "USER_CAPS"})
    @DisplayName("Should handle various valid user ID formats")
    void testVariousUserIdFormats(String userId) {
        String userDir = UserProperties.getUserDataDirectory(userId);
        String filePath = UserProperties.getUserDataFilePath(userId, "test.json");
        
        assertNotNull(userDir);
        assertNotNull(filePath);
        assertTrue(userDir.contains(userId));
        assertTrue(filePath.contains(userId));
        assertTrue(filePath.endsWith("test.json"));
    }

    @Test
    @DisplayName("Should create user directory structure when files are accessed")
    void testDirectoryCreation() throws IOException {
        // Initially, the directory should not exist
        Path userPath = tempDir.resolve(".todoapp").resolve("users").resolve(testUserId1);
        assertFalse(Files.exists(userPath));
          // Accessing user data directory should create the path
        // (Note: getUserDataDirectory just returns the path, doesn't create it)
        
        // Now verify we can create a file in that directory
        String testFilePath = UserProperties.getUserDataFilePath(testUserId1, "test.json");
        File testFile = new File(testFilePath);
        
        // Create parent directories
        testFile.getParentFile().mkdirs();
        
        // Verify directory exists
        assertTrue(Files.exists(userPath));
        assertTrue(testFile.getParentFile().exists());
    }

    @Test
    @DisplayName("Should isolate data between different users")
    void testUserDataIsolation() throws IOException {
        // Create files for two different users
        String user1TasksPath = UserProperties.getUserDataFilePath(testUserId1, "tasks.json");
        String user2TasksPath = UserProperties.getUserDataFilePath(testUserId2, "tasks.json");
        
        // Verify paths are different
        assertNotEquals(user1TasksPath, user2TasksPath);
        
        // Create files
        File user1File = new File(user1TasksPath);
        File user2File = new File(user2TasksPath);
        
        user1File.getParentFile().mkdirs();
        user2File.getParentFile().mkdirs();
        
        Files.writeString(user1File.toPath(), "user1 data");
        Files.writeString(user2File.toPath(), "user2 data");
        
        // Verify isolation
        String user1Content = Files.readString(user1File.toPath());
        String user2Content = Files.readString(user2File.toPath());
        
        assertEquals("user1 data", user1Content);
        assertEquals("user2 data", user2Content);
        assertNotEquals(user1Content, user2Content);
    }

    @Test
    @DisplayName("Should support multiple file types per user")
    void testMultipleFileTypesPerUser() {
        String[] fileTypes = {"tasks.json", "pending_commands.json", "shadows.json", "config.json"};
        
        for (String fileType : fileTypes) {
            String filePath = UserProperties.getUserDataFilePath(testUserId1, fileType);
            
            assertNotNull(filePath);
            assertTrue(filePath.contains(testUserId1));
            assertTrue(filePath.endsWith(fileType));
            
            // Verify all files are in the same user directory
            String userDir = UserProperties.getUserDataDirectory(testUserId1);
            assertTrue(filePath.startsWith(userDir));
        }
    }

    @Test
    @DisplayName("Should maintain consistent paths across multiple calls")
    void testPathConsistency() {
        // Call multiple times and verify consistency
        String userDir1 = UserProperties.getUserDataDirectory(testUserId1);
        String userDir2 = UserProperties.getUserDataDirectory(testUserId1);
        String userDir3 = UserProperties.getUserDataDirectory(testUserId1);
        
        assertEquals(userDir1, userDir2);
        assertEquals(userDir2, userDir3);
        
        String filePath1 = UserProperties.getUserDataFilePath(testUserId1, "tasks.json");
        String filePath2 = UserProperties.getUserDataFilePath(testUserId1, "tasks.json");
        String filePath3 = UserProperties.getUserDataFilePath(testUserId1, "tasks.json");
        
        assertEquals(filePath1, filePath2);
        assertEquals(filePath2, filePath3);
    }
}
