package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Comprehensive tests for the Folder class.
 * Tests builder pattern, field validation, and object behavior.
 */
@DisplayName("Folder Tests")
class FolderTest {

    private static final String SAMPLE_FOLDER_ID = "folder-123";
    private static final String SAMPLE_FOLDER_NAME = "Work";
    private static final LocalDateTime SAMPLE_DATE = LocalDateTime.of(2025, 1, 15, 10, 30);

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create folder with minimum required fields")
        void testMinimalFolderCreation() {
            Folder folder = new Folder.Builder(SAMPLE_FOLDER_ID)
                .folderName(SAMPLE_FOLDER_NAME)
                .build();

            assertEquals(SAMPLE_FOLDER_ID, folder.getFolder_id());
            assertEquals(SAMPLE_FOLDER_NAME, folder.getFolder_name());
            assertNull(folder.getSync_status());
            assertNull(folder.getCreated_at());
            assertNull(folder.getDeleted_at());
            assertNull(folder.getLast_sync());
        }

        @Test
        @DisplayName("Should create folder with all fields")
        void testCompleteFolderCreation() {
            Folder folder = new Folder.Builder(SAMPLE_FOLDER_ID)
                .folderName(SAMPLE_FOLDER_NAME)
                .syncStatus("cloud")
                .createdAt(SAMPLE_DATE)
                .deletedAt(SAMPLE_DATE.plusDays(1))
                .lastSync(SAMPLE_DATE.plusHours(2))
                .build();

            assertEquals(SAMPLE_FOLDER_ID, folder.getFolder_id());
            assertEquals(SAMPLE_FOLDER_NAME, folder.getFolder_name());
            assertEquals("cloud", folder.getSync_status());
            assertEquals(SAMPLE_DATE, folder.getCreated_at());
            assertEquals(SAMPLE_DATE.plusDays(1), folder.getDeleted_at());
            assertEquals(SAMPLE_DATE.plusHours(2), folder.getLast_sync());
        }

        @Test
        @DisplayName("Should support fluent builder pattern")
        void testFluentBuilder() {
            Folder folder = new Folder.Builder(SAMPLE_FOLDER_ID)
                .folderName(SAMPLE_FOLDER_NAME)
                .syncStatus("local")
                .createdAt(SAMPLE_DATE)
                .build();

            assertNotNull(folder);
            assertEquals(SAMPLE_FOLDER_ID, folder.getFolder_id());
            assertEquals("local", folder.getSync_status());
            assertEquals(SAMPLE_DATE, folder.getCreated_at());
        }

        @Test
        @DisplayName("Should handle null values in builder")
        void testBuilderWithNullValues() {
            Folder folder = new Folder.Builder(SAMPLE_FOLDER_ID)
                .folderName(null)
                .syncStatus(null)
                .createdAt(null)
                .deletedAt(null)
                .lastSync(null)
                .build();

            assertEquals(SAMPLE_FOLDER_ID, folder.getFolder_id());
            assertNull(folder.getFolder_name());
            assertNull(folder.getSync_status());
            assertNull(folder.getCreated_at());
            assertNull(folder.getDeleted_at());
            assertNull(folder.getLast_sync());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create folder using default constructor")
        void testDefaultConstructor() {
            Folder folder = new Folder();

            assertNull(folder.getFolder_id());
            assertNull(folder.getFolder_name());
            assertNull(folder.getSync_status());
            assertNull(folder.getCreated_at());
            assertNull(folder.getDeleted_at());
            assertNull(folder.getLast_sync());
        }

        @Test
        @DisplayName("Should create folder using parameterized constructor")
        void testParameterizedConstructor() {
            Folder folder = new Folder(
                SAMPLE_FOLDER_ID,
                SAMPLE_FOLDER_NAME,
                "cloud",
                SAMPLE_DATE,
                SAMPLE_DATE.plusDays(1),
                SAMPLE_DATE.plusHours(2)
            );

            assertEquals(SAMPLE_FOLDER_ID, folder.getFolder_id());
            assertEquals(SAMPLE_FOLDER_NAME, folder.getFolder_name());
            assertEquals("cloud", folder.getSync_status());
            assertEquals(SAMPLE_DATE, folder.getCreated_at());
            assertEquals(SAMPLE_DATE.plusDays(1), folder.getDeleted_at());
            assertEquals(SAMPLE_DATE.plusHours(2), folder.getLast_sync());
        }

        @Test
        @DisplayName("Should handle null values in parameterized constructor")
        void testParameterizedConstructorWithNulls() {
            Folder folder = new Folder(
                SAMPLE_FOLDER_ID,
                null,
                null,
                null,
                null,
                null
            );

            assertEquals(SAMPLE_FOLDER_ID, folder.getFolder_id());
            assertNull(folder.getFolder_name());
            assertNull(folder.getSync_status());
            assertNull(folder.getCreated_at());
            assertNull(folder.getDeleted_at());
            assertNull(folder.getLast_sync());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set folder_id")
        void testFolderIdGetterSetter() {
            Folder folder = new Folder();
            
            folder.setFolder_id(SAMPLE_FOLDER_ID);
            assertEquals(SAMPLE_FOLDER_ID, folder.getFolder_id());
            
            folder.setFolder_id("new-id");
            assertEquals("new-id", folder.getFolder_id());
            
            folder.setFolder_id(null);
            assertNull(folder.getFolder_id());
        }

        @Test
        @DisplayName("Should get and set folder_name")
        void testFolderNameGetterSetter() {
            Folder folder = new Folder();
            
            folder.setFolder_name(SAMPLE_FOLDER_NAME);
            assertEquals(SAMPLE_FOLDER_NAME, folder.getFolder_name());
            
            folder.setFolder_name("Personal");
            assertEquals("Personal", folder.getFolder_name());
            
            folder.setFolder_name(null);
            assertNull(folder.getFolder_name());
        }

        @Test
        @DisplayName("Should get and set sync_status")
        void testSyncStatusGetterSetter() {
            Folder folder = new Folder();
            
            folder.setSync_status("cloud");
            assertEquals("cloud", folder.getSync_status());
            
            folder.setSync_status("local");
            assertEquals("local", folder.getSync_status());
            
            folder.setSync_status(null);
            assertNull(folder.getSync_status());
        }

        @Test
        @DisplayName("Should get and set created_at")
        void testCreatedAtGetterSetter() {
            Folder folder = new Folder();
            
            folder.setCreated_at(SAMPLE_DATE);
            assertEquals(SAMPLE_DATE, folder.getCreated_at());
            
            LocalDateTime newDate = SAMPLE_DATE.plusHours(5);
            folder.setCreated_at(newDate);
            assertEquals(newDate, folder.getCreated_at());
            
            folder.setCreated_at(null);
            assertNull(folder.getCreated_at());
        }

        @Test
        @DisplayName("Should get and set deleted_at")
        void testDeletedAtGetterSetter() {
            Folder folder = new Folder();
            
            folder.setDeleted_at(SAMPLE_DATE);
            assertEquals(SAMPLE_DATE, folder.getDeleted_at());
            
            LocalDateTime newDate = SAMPLE_DATE.plusDays(2);
            folder.setDeleted_at(newDate);
            assertEquals(newDate, folder.getDeleted_at());
            
            folder.setDeleted_at(null);
            assertNull(folder.getDeleted_at());
        }

        @Test
        @DisplayName("Should get and set last_sync")
        void testLastSyncGetterSetter() {
            Folder folder = new Folder();
            
            folder.setLast_sync(SAMPLE_DATE);
            assertEquals(SAMPLE_DATE, folder.getLast_sync());
            
            LocalDateTime newDate = SAMPLE_DATE.plusMinutes(30);
            folder.setLast_sync(newDate);
            assertEquals(newDate, folder.getLast_sync());
            
            folder.setLast_sync(null);
            assertNull(folder.getLast_sync());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should support typical folder lifecycle")
        void testFolderLifecycle() {
            // Create folder
            Folder folder = new Folder.Builder("folder-new")
                .folderName("New Project")
                .syncStatus("new")
                .createdAt(LocalDateTime.now())
                .build();

            assertEquals("folder-new", folder.getFolder_id());
            assertEquals("New Project", folder.getFolder_name());
            assertEquals("new", folder.getSync_status());
            assertNotNull(folder.getCreated_at());
            assertNull(folder.getDeleted_at());

            // Update sync status (simulating sync to cloud)
            folder.setSync_status("cloud");
            folder.setLast_sync(LocalDateTime.now());

            assertEquals("cloud", folder.getSync_status());
            assertNotNull(folder.getLast_sync());

            // Mark as deleted
            LocalDateTime deletionTime = LocalDateTime.now();
            folder.setDeleted_at(deletionTime);

            assertEquals(deletionTime, folder.getDeleted_at());
        }

        @Test
        @DisplayName("Should handle folder renaming")
        void testFolderRenaming() {
            Folder folder = new Folder.Builder(SAMPLE_FOLDER_ID)
                .folderName("Old Name")
                .syncStatus("cloud")
                .build();

            assertEquals("Old Name", folder.getFolder_name());

            // Rename folder
            folder.setFolder_name("New Name");
            folder.setSync_status("local"); // Mark as needing sync

            assertEquals("New Name", folder.getFolder_name());
            assertEquals("local", folder.getSync_status());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty strings")
        void testEmptyStrings() {
            Folder folder = new Folder.Builder("")
                .folderName("")
                .syncStatus("")
                .build();

            assertEquals("", folder.getFolder_id());
            assertEquals("", folder.getFolder_name());
            assertEquals("", folder.getSync_status());
        }

        @Test
        @DisplayName("Should handle whitespace strings")
        void testWhitespaceStrings() {
            Folder folder = new Folder.Builder("   ")
                .folderName("  \t  ")
                .syncStatus("\n")
                .build();

            assertEquals("   ", folder.getFolder_id());
            assertEquals("  \t  ", folder.getFolder_name());
            assertEquals("\n", folder.getSync_status());
        }

        @Test
        @DisplayName("Should handle very long strings")
        void testLongStrings() {
            String longString = "a".repeat(1000);
            
            Folder folder = new Folder.Builder(longString)
                .folderName(longString)
                .syncStatus(longString)
                .build();

            assertEquals(longString, folder.getFolder_id());
            assertEquals(longString, folder.getFolder_name());
            assertEquals(longString, folder.getSync_status());
        }

        @Test
        @DisplayName("Should handle special characters")
        void testSpecialCharacters() {
            String specialChars = "!@#$%^&*()[]{}|;':\",./<>?`~";
            
            Folder folder = new Folder.Builder(specialChars)
                .folderName(specialChars)
                .syncStatus(specialChars)
                .build();

            assertEquals(specialChars, folder.getFolder_id());
            assertEquals(specialChars, folder.getFolder_name());
            assertEquals(specialChars, folder.getSync_status());
        }
    }
}
