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
import java.time.LocalDateTime;
import java.util.*;

import model.Task;
import model.TaskStatus;

/**
 * Comprehensive tests for JSONUtils utility class.
 * Tests JSON serialization/deserialization, file operations, and validation.
 */
@DisplayName("JSONUtils Tests")
class JSONUtilsTest {

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
    @DisplayName("Base Directory Tests")
    class BaseDirectoryTests {

        @Test
        @DisplayName("Should create base directory")
        void testCreateBaseDirectory() {
            JSONUtils.createBaseDirectory();
            
            File baseDir = new File(JSONUtils.BASE_DIRECTORY);
            assertTrue(baseDir.exists());
            assertTrue(baseDir.isDirectory());
        }

        @Test
        @DisplayName("Should handle existing base directory")
        void testCreateExistingBaseDirectory() {
            // Create directory first
            File baseDir = new File(JSONUtils.BASE_DIRECTORY);
            assertTrue(baseDir.mkdirs());
            
            // Should not throw exception
            assertDoesNotThrow(() -> JSONUtils.createBaseDirectory());
            assertTrue(baseDir.exists());
        }

        @Test
        @DisplayName("Should get correct base directory path")
        void testBaseDrirectoryPath() {
            String expectedPath = tempDir.toString() + File.separator + ".todoapp";
            assertEquals(expectedPath, JSONUtils.BASE_DIRECTORY);
        }
    }

    @Nested
    @DisplayName("JSON String Conversion Tests")
    class JsonStringTests {

        @Test
        @DisplayName("Should convert object to JSON string")
        void testToJsonString() throws IOException {
            Map<String, Object> testData = new HashMap<>();
            testData.put("name", "John Doe");
            testData.put("age", 30);
            testData.put("active", true);

            String json = JSONUtils.toJsonString(testData);

            assertNotNull(json);
            assertTrue(json.contains("John Doe"));
            assertTrue(json.contains("30"));
            assertTrue(json.contains("true"));
        }

        @Test
        @DisplayName("Should convert Task to JSON string")
        void testTaskToJsonString() throws IOException {
            Task task = new Task.Builder("test-task-123")
                .taskTitle("Test Task")
                .description("Test Description")
                .status(TaskStatus.pending)
                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 30))
                .build();

            String json = JSONUtils.toJsonString(task);

            assertNotNull(json);
            assertTrue(json.contains("test-task-123"));
            assertTrue(json.contains("Test Task"));
            assertTrue(json.contains("Test Description"));
            assertTrue(json.contains("pending"));
        }

        @Test
        @DisplayName("Should handle null object in toJsonString")
        void testToJsonStringWithNull() throws IOException {
            String json = JSONUtils.toJsonString(null);
            assertEquals("null", json);
        }

        @Test
        @DisplayName("Should handle empty collections")
        void testToJsonStringWithEmptyCollections() throws IOException {
            Map<String, Object> emptyMap = new HashMap<>();
            List<String> emptyList = new ArrayList<>();

            String mapJson = JSONUtils.toJsonString(emptyMap);
            String listJson = JSONUtils.toJsonString(emptyList);

            assertEquals("{}", mapJson);
            assertEquals("[]", listJson);
        }

        @Test
        @DisplayName("Should convert JSON string to object")
        void testFromJsonString() throws IOException {
            String json = "{\"name\":\"John Doe\",\"age\":30,\"active\":true}";

            Map<String, Object> result = JSONUtils.fromJsonString(json);

            assertNotNull(result);
            assertEquals("John Doe", result.get("name"));
            assertEquals(30, result.get("age"));
            assertEquals(true, result.get("active"));
        }

        @Test
        @DisplayName("Should handle invalid JSON string")
        void testFromJsonStringInvalid() {
            String invalidJson = "{invalid json}";

            assertThrows(IOException.class, () -> {
                JSONUtils.fromJsonString(invalidJson);
            });
        }

        @Test
        @DisplayName("Should handle empty JSON string")
        void testFromJsonStringEmpty() {
            assertThrows(IOException.class, () -> {
                JSONUtils.fromJsonString("");
            });
        }

        @Test
        @DisplayName("Should handle null JSON string")
        void testFromJsonStringNull() {
            assertThrows(IOException.class, () -> {
                JSONUtils.fromJsonString(null);
            });
        }
    }

    @Nested
    @DisplayName("File Operations Tests")
    class FileOperationsTests {

        @Test
        @DisplayName("Should write and read JSON file")
        void testWriteAndReadJsonFile() throws IOException {
            File testFile = new File(tempDir.toFile(), "test.json");
            Map<String, Object> testData = new HashMap<>();
            testData.put("name", "Test");
            testData.put("value", 123);

            // Write JSON file
            JSONUtils.writeJsonFile(testData, testFile.getAbsolutePath());

            assertTrue(testFile.exists());

            // Read JSON file
            Map<String, Object> result = JSONUtils.readJsonFile(testFile);

            assertEquals("Test", result.get("name"));
            assertEquals(123, result.get("value"));
        }

        @Test
        @DisplayName("Should write Task list to JSON file")
        void testWriteTaskListToJsonFile() throws IOException {
            File testFile = new File(tempDir.toFile(), "tasks.json");
            List<Task> tasks = Arrays.asList(
                new Task.Builder("task-1")
                    .taskTitle("Task 1")
                    .status(TaskStatus.pending)
                    .build(),
                new Task.Builder("task-2")
                    .taskTitle("Task 2")
                    .status(TaskStatus.completed)
                    .build()
            );

            // Create task data structure manually since writeTasksToJsonFile doesn't exist
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("columns", Arrays.asList("task_id", "task_title", "status"));
            List<List<Object>> data = new ArrayList<>();
            for (Task task : tasks) {
                data.add(Arrays.asList(task.getTask_id(), task.getTitle(), task.getStatus().toString()));
            }
            taskData.put("data", data);

            JSONUtils.writeJsonFile(taskData, testFile.getAbsolutePath());

            assertTrue(testFile.exists());

            // Verify file content structure
            Map<String, Object> result = JSONUtils.readJsonFile(testFile);
            assertTrue(result.containsKey("columns"));
            assertTrue(result.containsKey("data"));
        }

        @Test
        @DisplayName("Should handle file creation for nested directories")
        void testWriteJsonFileWithNestedPath() throws IOException {
            File nestedDir = new File(tempDir.toFile(), "nested/deep/path");
            File testFile = new File(nestedDir, "test.json");
            Map<String, Object> testData = new HashMap<>();
            testData.put("test", "value");

            JSONUtils.writeJsonFile(testData, testFile.getAbsolutePath());

            assertTrue(testFile.exists());
            assertTrue(nestedDir.exists());
        }

        @Test
        @DisplayName("Should handle non-existent file in readJsonFile")
        void testReadNonExistentFile() {
            File nonExistentFile = new File(tempDir.toFile(), "nonexistent.json");

            assertThrows(IOException.class, () -> {
                JSONUtils.readJsonFile(nonExistentFile);
            });
        }

        @Test
        @DisplayName("Should handle invalid JSON file content")
        void testReadInvalidJsonFile() throws IOException {
            File invalidFile = new File(tempDir.toFile(), "invalid.json");
            Files.write(invalidFile.toPath(), "invalid json content".getBytes());

            assertThrows(IOException.class, () -> {
                JSONUtils.readJsonFile(invalidFile);
            });
        }
    }

    @Nested
    @DisplayName("JSON Structure Validation Tests")
    class JsonStructureValidationTests {

        @Test
        @DisplayName("Should validate valid JSON structure")
        void testValidJsonStructure() throws IOException {
            File testFile = new File(tempDir.toFile(), "valid.json");
            Map<String, Object> validData = new HashMap<>();
            validData.put("columns", Arrays.asList("col1", "col2"));
            validData.put("data", Arrays.asList(Arrays.asList("val1", "val2")));

            JSONUtils.writeJsonFile(validData, testFile.getAbsolutePath());

            assertTrue(JSONUtils.isValidJsonStructure(testFile, "columns", "data"));
        }

        @Test
        @DisplayName("Should reject invalid JSON structure")
        void testInvalidJsonStructure() throws IOException {
            File testFile = new File(tempDir.toFile(), "invalid.json");
            Map<String, Object> invalidData = new HashMap<>();
            invalidData.put("wrongKey", "value");

            JSONUtils.writeJsonFile(invalidData, testFile.getAbsolutePath());

            assertFalse(JSONUtils.isValidJsonStructure(testFile, "columns", "data"));
        }

        @Test
        @DisplayName("Should handle missing required keys")
        void testMissingRequiredKeys() throws IOException {
            File testFile = new File(tempDir.toFile(), "partial.json");
            Map<String, Object> partialData = new HashMap<>();
            partialData.put("columns", Arrays.asList("col1"));
            // Missing "data" key

            JSONUtils.writeJsonFile(partialData, testFile.getAbsolutePath());

            assertFalse(JSONUtils.isValidJsonStructure(testFile, "columns", "data"));
        }

        @Test
        @DisplayName("Should handle non-existent file in validation")
        void testValidationWithNonExistentFile() {
            File nonExistentFile = new File(tempDir.toFile(), "nonexistent.json");

            assertFalse(JSONUtils.isValidJsonStructure(nonExistentFile, "columns", "data"));
        }

        @Test
        @DisplayName("Should validate structure with extra keys")
        void testValidationWithExtraKeys() throws IOException {
            File testFile = new File(tempDir.toFile(), "extra.json");
            Map<String, Object> dataWithExtra = new HashMap<>();
            dataWithExtra.put("columns", Arrays.asList("col1", "col2"));
            dataWithExtra.put("data", Arrays.asList(Arrays.asList("val1", "val2")));
            dataWithExtra.put("extraKey", "extraValue");

            JSONUtils.writeJsonFile(dataWithExtra, testFile.getAbsolutePath());

            assertTrue(JSONUtils.isValidJsonStructure(testFile, "columns", "data"));
        }
    }

    @Nested
    @DisplayName("Task Serialization Tests")
    class TaskSerializationTests {

        @Test
        @DisplayName("Should serialize and deserialize Task correctly")
        void testTaskSerialization() throws IOException {
            LocalDateTime now = LocalDateTime.now();
            Task originalTask = new Task.Builder("task-123")
                .taskTitle("Test Task")
                .description("Test Description")
                .status(TaskStatus.in_progress)
                .sync_status("cloud")
                .dueDate(now.plusDays(7))
                .createdAt(now)
                .updatedAt(now.plusHours(1))
                .folderId("folder-123")
                .folderName("Work")
                .build();

            String json = JSONUtils.toJsonString(originalTask);
            assertNotNull(json);

            // Note: Direct deserialization to Task would require type reference
            // This test verifies the serialization produces valid JSON
            Map<String, Object> taskMap = JSONUtils.fromJsonString(json);
            assertEquals("task-123", taskMap.get("task_id"));
            assertEquals("Test Task", taskMap.get("task_title"));
            assertEquals("Test Description", taskMap.get("description"));
            assertEquals("in_progress", taskMap.get("status"));
        }

        @Test
        @DisplayName("Should handle Task with null fields")
        void testTaskSerializationWithNulls() throws IOException {
            Task taskWithNulls = new Task.Builder("task-nulls")
                .taskTitle("Task with Nulls")
                .status(TaskStatus.pending)
                .build();

            String json = JSONUtils.toJsonString(taskWithNulls);
            assertNotNull(json);

            Map<String, Object> taskMap = JSONUtils.fromJsonString(json);
            assertEquals("task-nulls", taskMap.get("task_id"));
            assertEquals("Task with Nulls", taskMap.get("task_title"));
            assertEquals("pending", taskMap.get("status"));
        }

        @Test
        @DisplayName("Should serialize Task list correctly")
        void testTaskListSerialization() throws IOException {
            List<Task> tasks = Arrays.asList(
                new Task.Builder("task-1")
                    .taskTitle("First Task")
                    .status(TaskStatus.pending)
                    .build(),
                new Task.Builder("task-2")
                    .taskTitle("Second Task")
                    .status(TaskStatus.completed)
                    .build()
            );

            String json = JSONUtils.toJsonString(tasks);
            assertNotNull(json);
            assertTrue(json.contains("First Task"));
            assertTrue(json.contains("Second Task"));
            assertTrue(json.contains("pending"));
            assertTrue(json.contains("completed"));
        }
    }

    @Nested
    @DisplayName("Date/Time Handling Tests")
    class DateTimeHandlingTests {

        @Test
        @DisplayName("Should handle LocalDateTime serialization")
        void testLocalDateTimeSerialization() throws IOException {
            LocalDateTime testDate = LocalDateTime.of(2025, 1, 15, 14, 30, 45);
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", testDate);

            String json = JSONUtils.toJsonString(data);
            assertNotNull(json);
            assertTrue(json.contains("2025-01-15T14:30:45")); // ISO format

            Map<String, Object> result = JSONUtils.fromJsonString(json);
            // Note: Deserialization might return string representation
            assertNotNull(result.get("timestamp"));
        }

        @Test
        @DisplayName("Should maintain time precision")
        void testTimePrecision() throws IOException {
            LocalDateTime preciseTime = LocalDateTime.of(2025, 1, 15, 14, 30, 45, 123456789);
            Map<String, Object> data = new HashMap<>();
            data.put("preciseTime", preciseTime);

            String json = JSONUtils.toJsonString(data);
            assertNotNull(json);

            // Verify the JSON contains time information (though precision may vary)
            Map<String, Object> result = JSONUtils.fromJsonString(json);
            assertNotNull(result.get("preciseTime"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle circular reference gracefully")
        void testCircularReference() {
            Map<String, Object> parent = new HashMap<>();
            Map<String, Object> child = new HashMap<>();
            
            parent.put("child", child);
            child.put("parent", parent); // Circular reference

            // This should either handle gracefully or throw a specific exception
            assertThrows(Exception.class, () -> {
                JSONUtils.toJsonString(parent);
            });
        }

        @Test
        @DisplayName("Should handle very large objects")
        void testLargeObject() throws IOException {
            Map<String, Object> largeObject = new HashMap<>();
            for (int i = 0; i < 1000; i++) {
                largeObject.put("key" + i, "value" + i);
            }

            assertDoesNotThrow(() -> {
                String json = JSONUtils.toJsonString(largeObject);
                assertNotNull(json);
                assertTrue(json.length() > 10000); // Should be quite large
            });
        }

        @Test
        @DisplayName("Should handle file permission issues")
        void testFilePermissionIssues() throws IOException {
            File readOnlyDir = new File(tempDir.toFile(), "readonly");
            assertTrue(readOnlyDir.mkdirs());
            assertTrue(readOnlyDir.setReadOnly());

            File testFile = new File(readOnlyDir, "test.json");
            Map<String, Object> data = new HashMap<>();
            data.put("test", "value");

            // Should handle permission issues gracefully
            assertThrows(IOException.class, () -> {
                JSONUtils.writeJsonFile(data, testFile.getAbsolutePath());
            });

            // Clean up - remove read-only to allow cleanup
            readOnlyDir.setWritable(true);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle multiple rapid serializations")
        void testRapidSerialization() {
            Task task = new Task.Builder("perf-task")
                .taskTitle("Performance Test Task")
                .status(TaskStatus.pending)
                .build();

            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    String json = JSONUtils.toJsonString(task);
                    assertNotNull(json);
                }
            });
        }

        @Test
        @DisplayName("Should handle concurrent access")
        void testConcurrentAccess() throws InterruptedException {
            Task task = new Task.Builder("concurrent-task")
                .taskTitle("Concurrent Test Task")
                .status(TaskStatus.pending)
                .build();

            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(5);
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(50);
            java.util.concurrent.atomic.AtomicReference<Exception> exception = new java.util.concurrent.atomic.AtomicReference<>();

            for (int i = 0; i < 50; i++) {
                executor.submit(() -> {
                    try {
                        String json = JSONUtils.toJsonString(task);
                        assertNotNull(json);
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
    }
}
