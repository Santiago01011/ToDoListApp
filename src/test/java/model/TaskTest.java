package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Comprehensive tests for the immutable Task class.
 * Tests builder pattern, immutability, validation, and object contract.
 */
@DisplayName("Task Tests")
class TaskTest {

    private static final String SAMPLE_TASK_ID = "test-task-123";
    private static final String SAMPLE_TITLE = "Sample Task";
    private static final String SAMPLE_DESCRIPTION = "Sample Description";
    private static final LocalDateTime SAMPLE_DATE = LocalDateTime.of(2025, 1, 15, 10, 30);

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create task with minimum required fields")
        void testMinimalTaskCreation() {
            Task task = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle(SAMPLE_TITLE)
                .build();

            assertEquals(SAMPLE_TASK_ID, task.getTask_id());
            assertEquals(SAMPLE_TITLE, task.getTitle());
            assertEquals(TaskStatus.pending, task.getStatus()); // Default status
            assertEquals("new", task.getSync_status()); // Default sync status
            assertNotNull(task.getCreated_at());
            assertNotNull(task.getUpdated_at());
        }

        @Test
        @DisplayName("Should create task with all fields")
        void testCompleteTaskCreation() {
            Task task = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle(SAMPLE_TITLE)
                .description(SAMPLE_DESCRIPTION)
                .status(TaskStatus.in_progress)
                .sync_status("cloud")
                .dueDate(SAMPLE_DATE)
                .createdAt(SAMPLE_DATE)
                .updatedAt(SAMPLE_DATE.plusHours(1))
                .lastSync(SAMPLE_DATE.plusHours(2))
                .folderId("folder-123")
                .folderName("Work")
                .build();

            assertEquals(SAMPLE_TASK_ID, task.getTask_id());
            assertEquals(SAMPLE_TITLE, task.getTitle());
            assertEquals(SAMPLE_DESCRIPTION, task.getDescription());
            assertEquals(TaskStatus.in_progress, task.getStatus());
            assertEquals("cloud", task.getSync_status());
            assertEquals(SAMPLE_DATE, task.getDue_date());
            assertEquals(SAMPLE_DATE, task.getCreated_at());
            assertEquals(SAMPLE_DATE.plusHours(1), task.getUpdated_at());
            assertEquals(SAMPLE_DATE.plusHours(2), task.getLast_sync());
            assertEquals("folder-123", task.getFolder_id());
            assertEquals("Work", task.getFolder_name());
        }

        @Test
        @DisplayName("Should validate required fields")
        void testBuilderValidation() {
            // Test null task ID
            assertThrows(IllegalArgumentException.class, () -> {
                new Task.Builder(null)
                    .taskTitle(SAMPLE_TITLE)
                    .build();
            });

            // Test empty task ID
            assertThrows(IllegalArgumentException.class, () -> {
                new Task.Builder("")
                    .taskTitle(SAMPLE_TITLE)
                    .build();
            });

            // Test null title
            assertThrows(IllegalArgumentException.class, () -> {
                new Task.Builder(SAMPLE_TASK_ID)
                    .taskTitle(null)
                    .build();
            });

            // Test empty title
            assertThrows(IllegalArgumentException.class, () -> {
                new Task.Builder(SAMPLE_TASK_ID)
                    .taskTitle("")
                    .build();
            });
        }

        @Test
        @DisplayName("Should support fluent builder pattern")
        void testFluentBuilder() {
            Task task = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle(SAMPLE_TITLE)
                .description(SAMPLE_DESCRIPTION)
                .status(TaskStatus.completed)
                .folderId("folder-456")
                .build();

            assertNotNull(task);
            assertEquals(SAMPLE_TASK_ID, task.getTask_id());
            assertEquals(TaskStatus.completed, task.getStatus());
            assertEquals("folder-456", task.getFolder_id());
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should throw exception when trying to use deprecated setters")
        @SuppressWarnings("deprecation")
        void testDeprecatedSettersThrowExceptions() {
            Task task = createSampleTask();

            assertThrows(UnsupportedOperationException.class, () -> task.setTask_id("new-id"));
            assertThrows(UnsupportedOperationException.class, () -> task.setTitle("new-title"));
            assertThrows(UnsupportedOperationException.class, () -> task.setDescription("new-desc"));
            assertThrows(UnsupportedOperationException.class, () -> task.setStatus(TaskStatus.completed));
            assertThrows(UnsupportedOperationException.class, () -> task.setSync_status("new-status"));
            assertThrows(UnsupportedOperationException.class, () -> task.setDue_date(LocalDateTime.now()));
            assertThrows(UnsupportedOperationException.class, () -> task.setCreated_at(LocalDateTime.now()));
            assertThrows(UnsupportedOperationException.class, () -> task.setUpdated_at(LocalDateTime.now()));
            assertThrows(UnsupportedOperationException.class, () -> task.setDeleted_at(LocalDateTime.now()));
            assertThrows(UnsupportedOperationException.class, () -> task.setLast_sync(LocalDateTime.now()));
            assertThrows(UnsupportedOperationException.class, () -> task.setFolder_id("new-folder"));
            assertThrows(UnsupportedOperationException.class, () -> task.setFolder_name("new-name"));
        }

        @Test
        @DisplayName("Should provide immutable with* methods")
        void testWithMethods() {
            Task original = createSampleTask();
            
            Task withNewTitle = original.withTitle("New Title");
            Task withNewDescription = original.withDescription("New Description");
            Task withNewStatus = original.withStatus(TaskStatus.completed);
            Task withNewSyncStatus = original.withSyncStatus("synced");
            Task withNewDueDate = original.withDueDate(SAMPLE_DATE.plusDays(1));
            Task withNewFolderId = original.withFolderId("new-folder");

            // Original should be unchanged
            assertEquals(SAMPLE_TITLE, original.getTitle());
            assertEquals(SAMPLE_DESCRIPTION, original.getDescription());
            assertEquals(TaskStatus.pending, original.getStatus());

            // New instances should have changes
            assertEquals("New Title", withNewTitle.getTitle());
            assertEquals("New Description", withNewDescription.getDescription());
            assertEquals(TaskStatus.completed, withNewStatus.getStatus());
            assertEquals("synced", withNewSyncStatus.getSync_status());
            assertEquals(SAMPLE_DATE.plusDays(1), withNewDueDate.getDue_date());
            assertEquals("new-folder", withNewFolderId.getFolder_id());

            // All should be different instances
            assertNotSame(original, withNewTitle);
            assertNotSame(original, withNewDescription);
            assertNotSame(original, withNewStatus);
        }

        @Test
        @DisplayName("Should provide toBuilder for modifications")
        void testToBuilder() {
            Task original = createSampleTask();
            
            Task modified = original.toBuilder()
                .taskTitle("Modified Title")
                .status(TaskStatus.completed)
                .folderId("modified-folder")
                .build();

            // Original unchanged
            assertEquals(SAMPLE_TITLE, original.getTitle());
            assertEquals(TaskStatus.pending, original.getStatus());

            // Modified has changes but keeps other fields
            assertEquals("Modified Title", modified.getTitle());
            assertEquals(TaskStatus.completed, modified.getStatus());
            assertEquals("modified-folder", modified.getFolder_id());
            assertEquals(original.getTask_id(), modified.getTask_id());
            assertEquals(original.getDescription(), modified.getDescription());
        }
    }

    @Nested
    @DisplayName("Object Contract Tests")
    class ObjectContractTests {

        @Test
        @DisplayName("Should implement equals based on task_id")
        void testEquals() {
            Task task1 = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle("Title 1")
                .description("Description 1")
                .build();

            Task task2 = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle("Title 2")
                .description("Description 2")
                .status(TaskStatus.completed)
                .build();

            Task task3 = new Task.Builder("different-id")
                .taskTitle("Title 1")
                .description("Description 1")
                .build();

            // Same ID should be equal regardless of other fields
            assertEquals(task1, task2);
            assertNotEquals(task1, task3);
            assertNotEquals(task2, task3);

            // Reflexive
            assertEquals(task1, task1);

            // Symmetric
            assertEquals(task1, task2);
            assertEquals(task2, task1);

            // Null check
            assertNotEquals(task1, null);

            // Different class
            assertNotEquals(task1, "not a task");
        }

        @Test
        @DisplayName("Should implement hashCode consistently with equals")
        void testHashCode() {
            Task task1 = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle("Title 1")
                .build();

            Task task2 = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle("Title 2")
                .status(TaskStatus.completed)
                .build();

            Task task3 = new Task.Builder("different-id")
                .taskTitle("Title 1")
                .build();

            // Equal objects must have equal hash codes
            assertEquals(task1.hashCode(), task2.hashCode());

            // Different objects should have different hash codes (though not guaranteed)
            assertNotEquals(task1.hashCode(), task3.hashCode());
        }

        @Test
        @DisplayName("Should provide meaningful toString")
        void testToString() {
            Task task = createSampleTask();
            String toString = task.toString();

            assertTrue(toString.contains(SAMPLE_TASK_ID));
            assertTrue(toString.contains(SAMPLE_TITLE));
            assertTrue(toString.contains("pending"));
            assertTrue(toString.contains("Task{"));
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should provide detailed viewTaskDesc")
        void testViewTaskDesc() {
            Task task = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle(SAMPLE_TITLE)
                .description(SAMPLE_DESCRIPTION)
                .status(TaskStatus.in_progress)
                .sync_status("cloud")
                .dueDate(SAMPLE_DATE)
                .folderId("folder-123")
                .build();

            String description = task.viewTaskDesc();

            assertTrue(description.contains(SAMPLE_TASK_ID));
            assertTrue(description.contains(SAMPLE_TITLE));
            assertTrue(description.contains(SAMPLE_DESCRIPTION));
            assertTrue(description.contains("in_progress"));
            assertTrue(description.contains("cloud"));
            assertTrue(description.contains("folder-123"));
        }

        @Test
        @DisplayName("Should handle null values in viewTaskDesc")
        void testViewTaskDescWithNullValues() {
            Task task = new Task.Builder(SAMPLE_TASK_ID)
                .taskTitle(SAMPLE_TITLE)
                .build();

            String description = task.viewTaskDesc();

            assertTrue(description.contains(SAMPLE_TASK_ID));
            assertTrue(description.contains(SAMPLE_TITLE));
            // Should handle null values gracefully
            assertFalse(description.contains("null Description"));
        }
    }

    @Nested
    @DisplayName("Static Factory Methods Tests")
    class StaticFactoryMethodsTests {

        @Test
        @DisplayName("Should create builder via static factory method")
        void testStaticBuilderFactory() {
            Task.Builder builder = Task.builder();
            assertNotNull(builder);

            Task task = builder
                .taskTitle(SAMPLE_TITLE)
                .build();

            // Should have auto-generated ID and defaults
            assertNotNull(task.getTask_id());
            assertEquals(SAMPLE_TITLE, task.getTitle());
        }
    }

    @Nested
    @DisplayName("Legacy Compatibility Tests")
    class LegacyCompatibilityTests {

        @Test
        @DisplayName("Should handle legacy modified fields methods gracefully")
        @SuppressWarnings("deprecation")
        void testLegacyModifiedFieldsMethods() {
            Task task = createSampleTask();

            // These methods should not throw exceptions but return empty/no-op results
            assertDoesNotThrow(() -> {
                java.util.Set<String> modifiedFields = task.getModifiedFields();
                assertTrue(modifiedFields.isEmpty());
            });

            assertDoesNotThrow(() -> task.clearModifiedFields());
            assertDoesNotThrow(() -> task.addModifiedField("test"));
        }
    }

    /**
     * Helper method to create a sample task for testing
     */
    private Task createSampleTask() {
        return new Task.Builder(SAMPLE_TASK_ID)
            .taskTitle(SAMPLE_TITLE)
            .description(SAMPLE_DESCRIPTION)
            .status(TaskStatus.pending)
            .sync_status("new")
            .createdAt(SAMPLE_DATE)
            .updatedAt(SAMPLE_DATE)
            .build();
    }
}
