package service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

import org.junit.jupiter.api.Test;

import model.commands.UpdateTaskCommand;
import service.sync.CommandConverter;
import model.sync.SyncCommand;
import model.TaskStatus;

public class CommandConverterTest {

    @Test
    public void updateTaskProducesChangedFieldsWrapper() {
        // Arrange
        String taskId = "test-task-1";
        String userId = "user-1";

        UpdateTaskCommand.Builder b = new UpdateTaskCommand.Builder(taskId, userId);
        b.title("New title");
        b.status(TaskStatus.completed);
        UpdateTaskCommand cmd = b.build();

        // Act
        SyncCommand sc = CommandConverter.toSyncCommand(cmd);

        // Assert
        assertNotNull(sc, "SyncCommand should not be null");
        assertEquals("UPDATE_TASK", sc.getType());
        
        // changedFields should be at root level, not in data
        Map<String, Object> changed = sc.getChangedFields();
        assertNotNull(changed, "changedFields must not be null");
        assertEquals("New title", changed.get("title"));
        // Enum normalized to string
        assertEquals("completed", changed.get("status"));
        
        // data should be empty for UPDATE_TASK
        Map<String, Object> data = sc.getData();
        assertTrue(data.isEmpty(), "data should be empty for UPDATE_TASK");
    }

    @Test
    public void updateTaskMapsFieldNamesToSnakeCase() {
        // Arrange
        String taskId = "test-task-2";
        String userId = "user-2";

        UpdateTaskCommand.Builder b = new UpdateTaskCommand.Builder(taskId, userId);
        b.folderId("folder-123");
        b.dueDate(java.time.LocalDateTime.of(2025, 1, 1, 12, 0));
        UpdateTaskCommand cmd = b.build();

        // Act
        SyncCommand sc = CommandConverter.toSyncCommand(cmd);

        // Assert
        Map<String, Object> changed = sc.getChangedFields();
        
        // Verify field name mapping
        assertEquals("folder-123", changed.get("folder_id"));
        assertFalse(changed.containsKey("folderId"), "Should not contain camelCase folderId");
        
        assertTrue(changed.containsKey("due_date"));
        assertFalse(changed.containsKey("dueDate"), "Should not contain camelCase dueDate");
    }
}
