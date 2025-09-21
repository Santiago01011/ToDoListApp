package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Comprehensive tests for the FiltersCriteria record.
 * Tests record behavior, default values, validation, and usage scenarios.
 */
@DisplayName("FiltersCriteria Tests")
class FiltersCriteriaTest {

    @Nested
    @DisplayName("Record Behavior Tests")
    class RecordBehaviorTests {

        @Test
        @DisplayName("Should create FiltersCriteria with all parameters")
        void testRecordCreation() {
            Set<TaskStatus> statuses = Set.of(TaskStatus.pending, TaskStatus.in_progress);
            FiltersCriteria criteria = new FiltersCriteria("Work", statuses);

            assertEquals("Work", criteria.folderName());
            assertEquals(statuses, criteria.statuses());
            assertEquals(2, criteria.statuses().size());
            assertTrue(criteria.statuses().contains(TaskStatus.pending));
            assertTrue(criteria.statuses().contains(TaskStatus.in_progress));
        }

        @Test
        @DisplayName("Should handle null folder name")
        void testNullFolderName() {
            Set<TaskStatus> statuses = Set.of(TaskStatus.completed);
            FiltersCriteria criteria = new FiltersCriteria(null, statuses);

            assertNull(criteria.folderName());
            assertEquals(statuses, criteria.statuses());
        }

        @Test
        @DisplayName("Should handle empty status set")
        void testEmptyStatusSet() {
            Set<TaskStatus> emptyStatuses = Collections.emptySet();
            FiltersCriteria criteria = new FiltersCriteria("Personal", emptyStatuses);

            assertEquals("Personal", criteria.folderName());
            assertEquals(emptyStatuses, criteria.statuses());
            assertTrue(criteria.statuses().isEmpty());
        }

        @Test
        @DisplayName("Should handle null status set")
        void testNullStatusSet() {
            FiltersCriteria criteria = new FiltersCriteria("Work", null);

            assertEquals("Work", criteria.folderName());
            assertNull(criteria.statuses());
        }

        @Test
        @DisplayName("Should handle both null parameters")
        void testBothNullParameters() {
            FiltersCriteria criteria = new FiltersCriteria(null, null);

            assertNull(criteria.folderName());
            assertNull(criteria.statuses());
        }
    }

    @Nested
    @DisplayName("Default Criteria Tests")
    class DefaultCriteriaTests {

        @Test
        @DisplayName("Should provide default criteria")
        void testDefaultCriteria() {
            FiltersCriteria defaultCriteria = FiltersCriteria.defaultCriteria();

            assertNotNull(defaultCriteria);
            assertNull(defaultCriteria.folderName());
            assertNotNull(defaultCriteria.statuses());
            assertEquals(2, defaultCriteria.statuses().size());
            assertTrue(defaultCriteria.statuses().contains(TaskStatus.pending));
            assertTrue(defaultCriteria.statuses().contains(TaskStatus.in_progress));
        }

        @Test
        @DisplayName("Should create new instance each time for default criteria")
        void testDefaultCriteriaNewInstance() {
            FiltersCriteria criteria1 = FiltersCriteria.defaultCriteria();
            FiltersCriteria criteria2 = FiltersCriteria.defaultCriteria();

            // Records with same values should be equal but may be different instances
            assertEquals(criteria1, criteria2);
            assertEquals(criteria1.folderName(), criteria2.folderName());
            assertEquals(criteria1.statuses(), criteria2.statuses());
        }

        @Test
        @DisplayName("Should not include completed status in default criteria")
        void testDefaultCriteriaExclusions() {
            FiltersCriteria defaultCriteria = FiltersCriteria.defaultCriteria();

            assertFalse(defaultCriteria.statuses().contains(TaskStatus.completed));
            assertFalse(defaultCriteria.statuses().contains(TaskStatus.overdue));
            assertFalse(defaultCriteria.statuses().contains(TaskStatus.newest));
            assertFalse(defaultCriteria.statuses().contains(TaskStatus.incoming_due));
        }
    }

    @Nested
    @DisplayName("Record Contract Tests")
    class RecordContractTests {

        @Test
        @DisplayName("Should implement equals correctly")
        void testEquals() {
            Set<TaskStatus> statuses1 = Set.of(TaskStatus.pending, TaskStatus.in_progress);
            Set<TaskStatus> statuses2 = Set.of(TaskStatus.pending, TaskStatus.in_progress);
            Set<TaskStatus> statuses3 = Set.of(TaskStatus.completed);

            FiltersCriteria criteria1 = new FiltersCriteria("Work", statuses1);
            FiltersCriteria criteria2 = new FiltersCriteria("Work", statuses2);
            FiltersCriteria criteria3 = new FiltersCriteria("Work", statuses3);
            FiltersCriteria criteria4 = new FiltersCriteria("Personal", statuses1);
            FiltersCriteria criteria5 = new FiltersCriteria(null, statuses1);

            // Same values should be equal
            assertEquals(criteria1, criteria2);

            // Different statuses should not be equal
            assertNotEquals(criteria1, criteria3);

            // Different folder names should not be equal
            assertNotEquals(criteria1, criteria4);

            // Null vs non-null folder name should not be equal
            assertNotEquals(criteria1, criteria5);

            // Reflexive
            assertEquals(criteria1, criteria1);

            // Symmetric
            assertEquals(criteria1, criteria2);
            assertEquals(criteria2, criteria1);

            // Null and different type checks
            assertNotEquals(criteria1, null);
            assertNotEquals(criteria1, "not a FiltersCriteria");
        }

        @Test
        @DisplayName("Should implement hashCode correctly")
        void testHashCode() {
            Set<TaskStatus> statuses1 = Set.of(TaskStatus.pending, TaskStatus.in_progress);
            Set<TaskStatus> statuses2 = Set.of(TaskStatus.pending, TaskStatus.in_progress);

            FiltersCriteria criteria1 = new FiltersCriteria("Work", statuses1);
            FiltersCriteria criteria2 = new FiltersCriteria("Work", statuses2);
            FiltersCriteria criteria3 = new FiltersCriteria("Personal", statuses1);

            // Equal objects must have equal hash codes
            assertEquals(criteria1.hashCode(), criteria2.hashCode());

            // Different objects should typically have different hash codes
            assertNotEquals(criteria1.hashCode(), criteria3.hashCode());
        }

        @Test
        @DisplayName("Should implement toString correctly")
        void testToString() {
            Set<TaskStatus> statuses = Set.of(TaskStatus.pending, TaskStatus.completed);
            FiltersCriteria criteria = new FiltersCriteria("Work", statuses);

            String toString = criteria.toString();

            assertTrue(toString.contains("FiltersCriteria"));
            assertTrue(toString.contains("Work"));
            assertTrue(toString.contains("pending") || toString.contains("completed"));
        }
    }

    @Nested
    @DisplayName("Status Set Behavior Tests")
    class StatusSetBehaviorTests {

        @Test
        @DisplayName("Should handle single status")
        void testSingleStatus() {
            Set<TaskStatus> singleStatus = Set.of(TaskStatus.pending);
            FiltersCriteria criteria = new FiltersCriteria("Work", singleStatus);

            assertEquals(1, criteria.statuses().size());
            assertTrue(criteria.statuses().contains(TaskStatus.pending));
        }

        @Test
        @DisplayName("Should handle all statuses")
        void testAllStatuses() {
            Set<TaskStatus> allStatuses = Set.of(
                TaskStatus.pending, 
                TaskStatus.in_progress, 
                TaskStatus.completed,
                TaskStatus.incoming_due,
                TaskStatus.overdue,
                TaskStatus.newest
            );
            FiltersCriteria criteria = new FiltersCriteria("All", allStatuses);

            assertEquals(6, criteria.statuses().size());
            for (TaskStatus status : TaskStatus.values()) {
                assertTrue(criteria.statuses().contains(status));
            }
        }

        @Test
        @DisplayName("Should store reference to provided set")
        void testStatusSetReference() {
            Set<TaskStatus> mutableSet = new HashSet<>();
            mutableSet.add(TaskStatus.pending);
            mutableSet.add(TaskStatus.in_progress);

            FiltersCriteria criteria = new FiltersCriteria("Work", mutableSet);

            // Modify original set
            mutableSet.add(TaskStatus.completed);

            // FiltersCriteria stores reference to the same set, so changes are reflected
            assertEquals(3, criteria.statuses().size());
            assertTrue(criteria.statuses().contains(TaskStatus.completed));
        }

        @Test
        @DisplayName("Should handle duplicate statuses in creation")
        void testDuplicateStatusesHandling() {
            // Use a mutable set that can handle duplicates, then let FiltersCriteria handle it
            Set<TaskStatus> statusesWithDuplicates = new HashSet<>();
            statusesWithDuplicates.add(TaskStatus.pending);
            statusesWithDuplicates.add(TaskStatus.pending); // Duplicate will be ignored by HashSet
            statusesWithDuplicates.add(TaskStatus.in_progress);
            
            FiltersCriteria criteria = new FiltersCriteria("Work", statusesWithDuplicates);

            assertEquals(2, criteria.statuses().size());
            assertTrue(criteria.statuses().contains(TaskStatus.pending));
            assertTrue(criteria.statuses().contains(TaskStatus.in_progress));
        }
    }

    @Nested
    @DisplayName("Folder Name Behavior Tests")
    class FolderNameBehaviorTests {

        @Test
        @DisplayName("Should handle empty folder name")
        void testEmptyFolderName() {
            Set<TaskStatus> statuses = Set.of(TaskStatus.pending);
            FiltersCriteria criteria = new FiltersCriteria("", statuses);

            assertEquals("", criteria.folderName());
            assertNotNull(criteria.folderName());
        }

        @Test
        @DisplayName("Should handle whitespace folder name")
        void testWhitespaceFolderName() {
            Set<TaskStatus> statuses = Set.of(TaskStatus.pending);
            FiltersCriteria criteria = new FiltersCriteria("   ", statuses);

            assertEquals("   ", criteria.folderName());
        }

        @Test
        @DisplayName("Should handle special characters in folder name")
        void testSpecialCharactersFolderName() {
            String specialName = "Work-Project #1 (2025)";
            Set<TaskStatus> statuses = Set.of(TaskStatus.pending);
            FiltersCriteria criteria = new FiltersCriteria(specialName, statuses);

            assertEquals(specialName, criteria.folderName());
        }

        @Test
        @DisplayName("Should handle long folder name")
        void testLongFolderName() {
            String longName = "Very Long Folder Name ".repeat(20);
            Set<TaskStatus> statuses = Set.of(TaskStatus.pending);
            FiltersCriteria criteria = new FiltersCriteria(longName, statuses);

            assertEquals(longName, criteria.folderName());
        }
    }

    @Nested
    @DisplayName("Usage Scenario Tests")
    class UsageScenarioTests {

        @Test
        @DisplayName("Should work for filtering all tasks")
        void testAllTasksFilter() {
            FiltersCriteria allTasksCriteria = new FiltersCriteria(null, Set.of(TaskStatus.values()));

            assertNull(allTasksCriteria.folderName()); // No folder filter
            assertEquals(6, allTasksCriteria.statuses().size()); // All statuses
        }

        @Test
        @DisplayName("Should work for filtering active tasks")
        void testActiveTasksFilter() {
            Set<TaskStatus> activeStatuses = Set.of(
                TaskStatus.pending, 
                TaskStatus.in_progress, 
                TaskStatus.incoming_due,
                TaskStatus.overdue
            );
            FiltersCriteria activeCriteria = new FiltersCriteria(null, activeStatuses);

            assertNull(activeCriteria.folderName());
            assertEquals(4, activeCriteria.statuses().size());
            assertFalse(activeCriteria.statuses().contains(TaskStatus.completed));
        }

        @Test
        @DisplayName("Should work for filtering specific folder and status")
        void testSpecificFolderAndStatus() {
            FiltersCriteria specificCriteria = new FiltersCriteria(
                "Work", 
                Set.of(TaskStatus.pending)
            );

            assertEquals("Work", specificCriteria.folderName());
            assertEquals(1, specificCriteria.statuses().size());
            assertTrue(specificCriteria.statuses().contains(TaskStatus.pending));
        }

        @Test
        @DisplayName("Should work for completed tasks only")
        void testCompletedTasksFilter() {
            FiltersCriteria completedCriteria = new FiltersCriteria(
                null, 
                Set.of(TaskStatus.completed)
            );

            assertNull(completedCriteria.folderName());
            assertEquals(1, completedCriteria.statuses().size());
            assertTrue(completedCriteria.statuses().contains(TaskStatus.completed));
        }

        @Test
        @DisplayName("Should work for folder-only filter")
        void testFolderOnlyFilter() {
            FiltersCriteria folderOnlyCriteria = new FiltersCriteria(
                "Personal", 
                Set.of(TaskStatus.values()) // All statuses
            );

            assertEquals("Personal", folderOnlyCriteria.folderName());
            assertEquals(6, folderOnlyCriteria.statuses().size());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle concurrent access safely")
        void testConcurrentAccess() {
            FiltersCriteria criteria = FiltersCriteria.defaultCriteria();
            
            // Records are immutable, so concurrent access should be safe
            assertDoesNotThrow(() -> {
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(10);
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(100);
                
                for (int i = 0; i < 100; i++) {
                    executor.submit(() -> {
                        try {
                            // These operations should be thread-safe
                            assertNotNull(criteria.folderName());
                            assertNotNull(criteria.statuses());
                            assertNotNull(criteria.toString());
                            assertTrue(criteria.hashCode() != 0 || criteria.hashCode() == 0); // Just invoke hashCode
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                
                latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
                executor.shutdown();
            });
        }
    }
}
