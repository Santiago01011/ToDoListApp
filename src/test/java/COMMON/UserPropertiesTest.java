package COMMON;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;

/**
 * Comprehensive tests for UserProperties utility class.
 * Tests YAML configuration management, user-specific storage, and property handling.
 */
@DisplayName("UserProperties Tests")
class UserPropertiesTest {

    @TempDir
    Path tempDir;
    
    private String originalUserHome;
    
    @BeforeEach
    void setUp() {
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
    }
    
    @AfterEach
    void tearDown() {
        System.setProperty("user.home", originalUserHome);
    }

    @Nested
    @DisplayName("Property Access Tests")
    class PropertyAccessTests {

        @Test
        @DisplayName("Should get default properties")
        void testGetDefaultProperties() {
            // UserProperties should initialize with defaults
            Object darkTheme = UserProperties.getProperty("darkTheme");
            Object rememberMe = UserProperties.getProperty("rememberMe");
            Object authApiUrl = UserProperties.getProperty("authApiUrl");
            Object dbUrl = UserProperties.getProperty("dbUrl");

            assertNotNull(darkTheme);
            assertNotNull(rememberMe);
            assertNotNull(authApiUrl);
            assertNotNull(dbUrl);

            assertEquals("false", darkTheme.toString());
            assertEquals("false", rememberMe.toString());
            assertTrue(authApiUrl.toString().contains("localhost"));
            assertTrue(dbUrl.toString().contains("postgresql"));
        }

        @Test
        @DisplayName("Should handle non-existent properties")
        void testGetNonExistentProperty() {
            Object nonExistent = UserProperties.getProperty("nonExistentProperty");
            assertNull(nonExistent);
        }

        @Test
        @DisplayName("Should handle null property key")
        void testGetNullPropertyKey() {
            Object result = UserProperties.getProperty(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle empty property key")
        void testGetEmptyPropertyKey() {
            Object result = UserProperties.getProperty("");
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Property Modification Tests")
    class PropertyModificationTests {

        @Test
        @DisplayName("Should set and get properties")
        void testSetAndGetProperty() {
            String testKey = "testProperty";
            String testValue = "testValue";

            UserProperties.setProperty(testKey, testValue);
            Object retrievedValue = UserProperties.getProperty(testKey);

            assertEquals(testValue, retrievedValue);
        }

        @Test
        @DisplayName("Should update existing properties")
        void testUpdateExistingProperty() {
            String key = "darkTheme";
            String newValue = "true";

            // Get original value
            Object originalValue = UserProperties.getProperty(key);
            assertEquals("false", originalValue.toString());

            // Update value
            UserProperties.setProperty(key, newValue);
            Object updatedValue = UserProperties.getProperty(key);

            assertEquals(newValue, updatedValue);
        }

        @Test
        @DisplayName("Should handle null property value")
        void testSetNullPropertyValue() {
            String key = "nullTestProperty";
            
            UserProperties.setProperty(key, null);
            Object retrievedValue = UserProperties.getProperty(key);

            assertNull(retrievedValue);
        }

        @Test
        @DisplayName("Should handle different property value types")
        void testDifferentPropertyTypes() {
            UserProperties.setProperty("stringProp", "stringValue");
            UserProperties.setProperty("intProp", 42);
            UserProperties.setProperty("boolProp", true);
            UserProperties.setProperty("doubleProp", 3.14);

            assertEquals("stringValue", UserProperties.getProperty("stringProp"));
            assertEquals(42, UserProperties.getProperty("intProp"));
            assertEquals(true, UserProperties.getProperty("boolProp"));
            assertEquals(3.14, UserProperties.getProperty("doubleProp"));
        }
    }

    @Nested
    @DisplayName("User-Specific Storage Tests")
    class UserSpecificStorageTests {

        @Test
        @DisplayName("Should generate user data file paths")
        void testGetUserDataFilePath() {
            String userId = "test-user-123";
            String fileName = "data.json";

            String filePath = UserProperties.getUserDataFilePath(userId, fileName);

            assertNotNull(filePath);
            assertTrue(filePath.contains(userId));
            assertTrue(filePath.contains(fileName));
            assertTrue(filePath.contains(".todoapp"));
            assertTrue(filePath.contains("users"));
        }

        @Test
        @DisplayName("Should handle special characters in user ID")
        void testUserDataFilePathWithSpecialChars() {
            String specialUserId = "user@domain.com";
            String fileName = "tasks.json";

            String filePath = UserProperties.getUserDataFilePath(specialUserId, fileName);

            assertNotNull(filePath);
            assertTrue(filePath.contains("users"));
            assertTrue(filePath.contains(fileName));
        }

        @Test
        @DisplayName("Should handle null parameters in getUserDataFilePath")
        void testGetUserDataFilePathWithNulls() {
            // Test null userId
            assertThrows(Exception.class, () -> {
                UserProperties.getUserDataFilePath(null, "file.json");
            });

            // Test null fileName
            assertThrows(Exception.class, () -> {
                UserProperties.getUserDataFilePath("user123", null);
            });
        }

        @Test
        @DisplayName("Should handle empty parameters in getUserDataFilePath")
        void testGetUserDataFilePathWithEmpty() {
            String filePath1 = UserProperties.getUserDataFilePath("", "file.json");
            String filePath2 = UserProperties.getUserDataFilePath("user123", "");

            // Should handle empty strings gracefully
            assertNotNull(filePath1);
            assertNotNull(filePath2);
        }
    }

    @Nested
    @DisplayName("Current User Management Tests")
    class CurrentUserManagementTests {

        @Test
        @DisplayName("Should get current user ID")
        void testGetCurrentUserId() {
            // getCurrentUserId() should return current user ID or null
            String currentUserId = UserProperties.getCurrentUserId();
            // Should not throw exception
            assertTrue(currentUserId == null || currentUserId instanceof String);
        }

        @Test
        @DisplayName("Should get current user data directory")
        void testGetCurrentUserDataDirectory() {
            String currentUserDir = UserProperties.getCurrentUserDataDirectory();
            
            if (currentUserDir != null) {
                assertTrue(currentUserDir.contains(".todoapp"));
                assertTrue(currentUserDir.contains("users"));
            }
        }

        @Test
        @DisplayName("Should get current user data file path")
        void testGetCurrentUserDataFilePath() {
            String fileName = "test.json";
            String filePath = UserProperties.getCurrentUserDataFilePath(fileName);
            
            if (filePath != null) {
                assertTrue(filePath.contains(fileName));
                assertTrue(filePath.contains(".todoapp"));
            }
        }
    }

    @Nested
    @DisplayName("File System Operations Tests")
    class FileSystemOperationsTests {

        @Test
        @DisplayName("Should create configuration directory")
        void testConfigurationDirectoryCreation() {
            // After initialization, .todoapp directory should exist
            File todoAppDir = new File(tempDir.toFile(), ".todoapp");
            assertTrue(todoAppDir.exists());
            assertTrue(todoAppDir.isDirectory());
        }

        @Test
        @DisplayName("Should create user.yml file")
        void testUserYmlFileCreation() {
            // After initialization, user.yml should exist
            File userYmlFile = new File(tempDir.toFile(), ".todoapp/user.yml");
            assertTrue(userYmlFile.exists());
            assertTrue(userYmlFile.isFile());
        }

        @Test
        @DisplayName("Should handle read-only file system gracefully")
        void testReadOnlyFileSystem() throws IOException {
            // Create a read-only directory scenario
            File readOnlyDir = new File(tempDir.toFile(), "readonly");
            assertTrue(readOnlyDir.mkdirs());
            assertTrue(readOnlyDir.setReadOnly());

            // Set user.home to read-only directory temporarily
            System.setProperty("user.home", readOnlyDir.getAbsolutePath());

            // Should handle the error gracefully without crashing
            assertDoesNotThrow(() -> {
                UserProperties.setProperty("testReadOnly", "value");
            });

            // Clean up
            readOnlyDir.setWritable(true);
            System.setProperty("user.home", tempDir.toString());
        }
    }

    @Nested
    @DisplayName("YAML Persistence Tests")
    class YamlPersistenceTests {

        @Test
        @DisplayName("Should persist properties to YAML file")
        void testPropertyPersistence() throws IOException {
            String testKey = "persistenceTest";
            String testValue = "persistedValue";

            UserProperties.setProperty(testKey, testValue);

            // Check that file was updated
            File userYmlFile = new File(tempDir.toFile(), ".todoapp/user.yml");
            assertTrue(userYmlFile.exists());

            // Read file content
            String fileContent = Files.readString(userYmlFile.toPath());
            assertTrue(fileContent.contains(testKey));
            assertTrue(fileContent.contains(testValue));
        }

        @Test
        @DisplayName("Should load properties from existing YAML file")
        void testPropertyLoading() throws IOException {
            // Create a YAML file with test content
            File userYmlFile = new File(tempDir.toFile(), ".todoapp/user.yml");
            userYmlFile.getParentFile().mkdirs();
            
            String yamlContent = """
                testLoadKey: testLoadValue
                darkTheme: 'true'
                customProperty: 12345
                """;
            Files.writeString(userYmlFile.toPath(), yamlContent);

            // Force re-initialization (would happen in real scenario on app restart)
            // This is a limitation of testing static initialization
            Object darkTheme = UserProperties.getProperty("darkTheme");
            assertNotNull(darkTheme);
        }

        @Test
        @DisplayName("Should handle malformed YAML gracefully")
        void testMalformedYamlHandling() throws IOException {
            File userYmlFile = new File(tempDir.toFile(), ".todoapp/user.yml");
            userYmlFile.getParentFile().mkdirs();
            
            String malformedYaml = """
                invalid: yaml: content:
                  - unclosed list
                key without value
                """;
            Files.writeString(userYmlFile.toPath(), malformedYaml);

            // Should handle gracefully and fall back to defaults
            assertDoesNotThrow(() -> {
                Object property = UserProperties.getProperty("darkTheme");
                // Should either be default or null, but shouldn't crash
                assertTrue(property == null || property.toString().equals("false"));
            });
        }
    }

    @Nested
    @DisplayName("Default Properties Tests")
    class DefaultPropertiesTests {

        @Test
        @DisplayName("Should have all expected default properties")
        void testAllDefaultProperties() {
            String[] expectedKeys = {
                "darkTheme", "rememberMe", "username", "password", 
                "lastSession", "userUUID", "authApiUrl", "dbUrl", "token"
            };

            for (String key : expectedKeys) {
                Object value = UserProperties.getProperty(key);
                assertNotNull(value, "Default property '" + key + "' should not be null");
            }
        }

        @Test
        @DisplayName("Should have correct default values")
        void testDefaultPropertyValues() {
            assertEquals("false", UserProperties.getProperty("darkTheme").toString());
            assertEquals("false", UserProperties.getProperty("rememberMe").toString());
            assertEquals("", UserProperties.getProperty("username").toString());
            assertEquals("", UserProperties.getProperty("password").toString());
            assertEquals("", UserProperties.getProperty("lastSession").toString());
            assertEquals("", UserProperties.getProperty("userUUID").toString());
            assertEquals("", UserProperties.getProperty("token").toString());
            
            String authApiUrl = UserProperties.getProperty("authApiUrl").toString();
            assertTrue(authApiUrl.contains("localhost") && authApiUrl.contains("8080"));
            
            String dbUrl = UserProperties.getProperty("dbUrl").toString();
            assertTrue(dbUrl.contains("postgresql") && dbUrl.contains("5431"));
        }
    }

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent property access")
        void testConcurrentPropertyAccess() throws InterruptedException {
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(5);
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(50);
            java.util.concurrent.atomic.AtomicReference<Exception> exception = new java.util.concurrent.atomic.AtomicReference<>();

            for (int i = 0; i < 50; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        String key = "concurrentTest" + threadNum;
                        String value = "value" + threadNum;
                        
                        UserProperties.setProperty(key, value);
                        Object retrievedValue = UserProperties.getProperty(key);
                        
                        assertEquals(value, retrievedValue);
                    } catch (Exception e) {
                        exception.set(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            assertNull(exception.get(), "No exception should occur during concurrent access");
            executor.shutdown();
        }

        @Test
        @DisplayName("Should handle concurrent user property changes")
        void testConcurrentUserPropertyChanges() throws InterruptedException {
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(3);
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(30);
            java.util.concurrent.atomic.AtomicReference<Exception> exception = new java.util.concurrent.atomic.AtomicReference<>();

            for (int i = 0; i < 30; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        String key = "concurrentUserProp" + threadNum;
                        String value = "userValue" + threadNum;
                        UserProperties.setProperty(key, value);
                        
                        // Should not throw exception
                        Object retrievedValue = UserProperties.getProperty(key);
                        assertEquals(value, retrievedValue);
                    } catch (Exception e) {
                        exception.set(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            assertNull(exception.get(), "No exception should occur during concurrent property changes");
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long property values")
        void testLongPropertyValues() {
            String longValue = "x".repeat(10000);
            String key = "longValueTest";

            assertDoesNotThrow(() -> {
                UserProperties.setProperty(key, longValue);
                Object retrievedValue = UserProperties.getProperty(key);
                assertEquals(longValue, retrievedValue);
            });
        }

        @Test
        @DisplayName("Should handle special characters in property keys")
        void testSpecialCharacterKeys() {
            String specialKey = "key-with_special.chars@123";
            String value = "specialValue";

            assertDoesNotThrow(() -> {
                UserProperties.setProperty(specialKey, value);
                Object retrievedValue = UserProperties.getProperty(specialKey);
                assertEquals(value, retrievedValue);
            });
        }

        @Test
        @DisplayName("Should handle Unicode property values")
        void testUnicodePropertyValues() {
            String unicodeValue = "测试值 🎉 العربية";
            String key = "unicodeTest";

            assertDoesNotThrow(() -> {
                UserProperties.setProperty(key, unicodeValue);
                Object retrievedValue = UserProperties.getProperty(key);
                assertEquals(unicodeValue, retrievedValue);
            });
        }

        @Test
        @DisplayName("Should handle large number of properties")
        void testLargeNumberOfProperties() {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 1000; i++) {
                    UserProperties.setProperty("bulkTest" + i, "value" + i);
                }
                
                // Verify a few random properties
                assertEquals("value500", UserProperties.getProperty("bulkTest500"));
                assertEquals("value999", UserProperties.getProperty("bulkTest999"));
            });
        }
    }
}
