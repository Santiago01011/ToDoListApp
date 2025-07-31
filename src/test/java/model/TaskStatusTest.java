package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the TaskStatus enum.
 * Tests enum values, string conversion, and validation.
 */
@DisplayName("TaskStatus Tests")
class TaskStatusTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have all expected enum values")
        void testAllEnumValues() {
            TaskStatus[] values = TaskStatus.values();
            
            assertEquals(6, values.length);
            
            // Check all expected values exist
            assertTrue(containsValue(values, TaskStatus.pending));
            assertTrue(containsValue(values, TaskStatus.in_progress));
            assertTrue(containsValue(values, TaskStatus.completed));
            assertTrue(containsValue(values, TaskStatus.incoming_due));
            assertTrue(containsValue(values, TaskStatus.overdue));
            assertTrue(containsValue(values, TaskStatus.newest));
        }

        @Test
        @DisplayName("Should support valueOf conversion")
        void testValueOf() {
            assertEquals(TaskStatus.pending, TaskStatus.valueOf("pending"));
            assertEquals(TaskStatus.in_progress, TaskStatus.valueOf("in_progress"));
            assertEquals(TaskStatus.completed, TaskStatus.valueOf("completed"));
            assertEquals(TaskStatus.incoming_due, TaskStatus.valueOf("incoming_due"));
            assertEquals(TaskStatus.overdue, TaskStatus.valueOf("overdue"));
            assertEquals(TaskStatus.newest, TaskStatus.valueOf("newest"));
        }

        @Test
        @DisplayName("Should throw exception for invalid valueOf")
        void testInvalidValueOf() {
            assertThrows(IllegalArgumentException.class, () -> {
                TaskStatus.valueOf("invalid_status");
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                TaskStatus.valueOf("PENDING"); // Case sensitive
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                TaskStatus.valueOf("");
            });
            
            assertThrows(NullPointerException.class, () -> {
                TaskStatus.valueOf(null);
            });
        }

        private boolean containsValue(TaskStatus[] values, TaskStatus target) {
            for (TaskStatus value : values) {
                if (value == target) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nested
    @DisplayName("String Conversion Tests")
    class StringConversionTests {

        @Test
        @DisplayName("Should convert status to display string")
        void testGetStatusToString() {
            assertEquals("Pending", TaskStatus.getStatusToString(TaskStatus.pending));
            assertEquals("In Progress", TaskStatus.getStatusToString(TaskStatus.in_progress));
            assertEquals("Completed", TaskStatus.getStatusToString(TaskStatus.completed));
            assertEquals("Incoming Due", TaskStatus.getStatusToString(TaskStatus.incoming_due));
            assertEquals("Overdue", TaskStatus.getStatusToString(TaskStatus.overdue));
            assertEquals("Newest", TaskStatus.getStatusToString(TaskStatus.newest));
        }

        @Test
        @DisplayName("Should handle null input in getStatusToString")
        void testGetStatusToStringWithNull() {
            // Assuming the method handles null gracefully
            // If not, this test should expect an exception
            assertDoesNotThrow(() -> {
                String result = TaskStatus.getStatusToString(null);
                // Result could be null or a default string
                assertTrue(result == null || result.equals("Unknown") || result.equals(""));
            });
        }

        @Test
        @DisplayName("Should provide consistent name() method")
        void testNameMethod() {
            assertEquals("pending", TaskStatus.pending.name());
            assertEquals("in_progress", TaskStatus.in_progress.name());
            assertEquals("completed", TaskStatus.completed.name());
            assertEquals("incoming_due", TaskStatus.incoming_due.name());
            assertEquals("overdue", TaskStatus.overdue.name());
            assertEquals("newest", TaskStatus.newest.name());
        }

        @Test
        @DisplayName("Should provide consistent toString() method")
        void testToStringMethod() {
            assertEquals("pending", TaskStatus.pending.toString());
            assertEquals("in_progress", TaskStatus.in_progress.toString());
            assertEquals("completed", TaskStatus.completed.toString());
            assertEquals("incoming_due", TaskStatus.incoming_due.toString());
            assertEquals("overdue", TaskStatus.overdue.toString());
            assertEquals("newest", TaskStatus.newest.toString());
        }
    }

    @Nested
    @DisplayName("Enum Behavior Tests")
    class EnumBehaviorTests {

        @Test
        @DisplayName("Should support ordinal values")
        void testOrdinalValues() {
            // Test that ordinal values are consistent
            assertTrue(TaskStatus.pending.ordinal() >= 0);
            assertTrue(TaskStatus.in_progress.ordinal() >= 0);
            assertTrue(TaskStatus.completed.ordinal() >= 0);
            assertTrue(TaskStatus.incoming_due.ordinal() >= 0);
            assertTrue(TaskStatus.overdue.ordinal() >= 0);
            assertTrue(TaskStatus.newest.ordinal() >= 0);
            
            // Each ordinal should be unique
            TaskStatus[] values = TaskStatus.values();
            for (int i = 0; i < values.length; i++) {
                assertEquals(i, values[i].ordinal());
            }
        }

        @Test
        @DisplayName("Should support compareTo")
        void testCompareTo() {
            // Enum comparison is based on ordinal values
            assertTrue(TaskStatus.pending.compareTo(TaskStatus.in_progress) < 0);
            assertTrue(TaskStatus.completed.compareTo(TaskStatus.pending) > 0);
            assertEquals(0, TaskStatus.pending.compareTo(TaskStatus.pending));
        }

        @Test
        @DisplayName("Should be serializable")
        void testSerializability() {
            // Enums are inherently Serializable
            assertTrue(java.io.Serializable.class.isAssignableFrom(TaskStatus.class));
        }
    }

    @Nested
    @DisplayName("Usage Scenarios Tests")
    class UsageScenariosTests {

        @Test
        @DisplayName("Should work in switch statements")
        void testSwitchStatement() {
            String result = switch (TaskStatus.pending) {
                case pending -> "Task is pending";
                case in_progress -> "Task is in progress";
                case completed -> "Task is completed";
                case incoming_due -> "Task is due soon";
                case overdue -> "Task is overdue";
                case newest -> "Task is newest";
            };
            
            assertEquals("Task is pending", result);
        }

        @Test
        @DisplayName("Should work in collections")
        void testInCollections() {
            java.util.Set<TaskStatus> statusSet = java.util.EnumSet.allOf(TaskStatus.class);
            assertEquals(6, statusSet.size());
            assertTrue(statusSet.contains(TaskStatus.pending));
            assertTrue(statusSet.contains(TaskStatus.completed));
        }

        @Test
        @DisplayName("Should work as map keys")
        void testAsMapKeys() {
            java.util.Map<TaskStatus, String> statusMap = new java.util.EnumMap<>(TaskStatus.class);
            statusMap.put(TaskStatus.pending, "Pending tasks");
            statusMap.put(TaskStatus.completed, "Completed tasks");
            
            assertEquals("Pending tasks", statusMap.get(TaskStatus.pending));
            assertEquals("Completed tasks", statusMap.get(TaskStatus.completed));
            assertNull(statusMap.get(TaskStatus.overdue));
        }

        @Test
        @DisplayName("Should support filtering operations")
        void testFilteringOperations() {
            TaskStatus[] allStatuses = TaskStatus.values();
            
            // Filter completed-like statuses
            java.util.List<TaskStatus> completedStatuses = java.util.Arrays.stream(allStatuses)
                .filter(status -> status == TaskStatus.completed)
                .collect(java.util.stream.Collectors.toList());
            
            assertEquals(1, completedStatuses.size());
            assertEquals(TaskStatus.completed, completedStatuses.get(0));
            
            // Filter active statuses (not completed)
            java.util.List<TaskStatus> activeStatuses = java.util.Arrays.stream(allStatuses)
                .filter(status -> status != TaskStatus.completed)
                .collect(java.util.stream.Collectors.toList());
            
            assertEquals(5, activeStatuses.size());
            assertFalse(activeStatuses.contains(TaskStatus.completed));
        }
    }

    @Nested
    @DisplayName("Integration with Task Class Tests")
    class TaskIntegrationTests {

        @Test
        @DisplayName("Should work as Task status field")
        void testWithTask() {
            Task task = new Task.Builder("test-task")
                .taskTitle("Test Task")
                .status(TaskStatus.in_progress)
                .build();
            
            assertEquals(TaskStatus.in_progress, task.getStatus());
            
            Task updatedTask = task.withStatus(TaskStatus.completed);
            assertEquals(TaskStatus.completed, updatedTask.getStatus());
            assertEquals(TaskStatus.in_progress, task.getStatus()); // Original unchanged
        }

        @Test
        @DisplayName("Should support all status transitions")
        void testStatusTransitions() {
            Task task = new Task.Builder("test-task")
                .taskTitle("Test Task")
                .status(TaskStatus.pending)
                .build();
            
            // Test all possible status transitions
            for (TaskStatus status : TaskStatus.values()) {
                Task updatedTask = task.withStatus(status);
                assertEquals(status, updatedTask.getStatus());
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle concurrent access")
        void testConcurrentAccess() {
            // Enums are thread-safe by nature
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(10);
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(100);
            java.util.concurrent.atomic.AtomicReference<Exception> exception = new java.util.concurrent.atomic.AtomicReference<>();
            
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> {
                    try {
                        TaskStatus status = TaskStatus.valueOf("pending");
                        String displayName = TaskStatus.getStatusToString(status);
                        assertNotNull(status);
                        assertEquals("Pending", displayName);
                    } catch (Exception e) {
                        exception.set(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail("Test was interrupted");
            }
            
            assertNull(exception.get(), "No exception should occur during concurrent access");
            executor.shutdown();
        }

        @Test
        @DisplayName("Should maintain reference equality")
        void testReferenceEquality() {
            TaskStatus status1 = TaskStatus.valueOf("pending");
            TaskStatus status2 = TaskStatus.pending;
            TaskStatus status3 = TaskStatus.values()[0]; // Assuming pending is first
            
            // All should be the same reference
            assertSame(status1, status2);
            if (TaskStatus.values()[0] == TaskStatus.pending) {
                assertSame(status1, status3);
            }
        }
    }
}
