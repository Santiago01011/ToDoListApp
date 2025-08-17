package service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import COMMON.JSONUtils;
import model.commands.UpdateTaskCommand;
import model.sync.CommandBatch;
import model.sync.SyncCommand;
import model.TaskStatus;
import service.sync.CommandConverter;

public class CommandBatchSerializationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void commandBatchSerializesWithChangedFieldsWrapper() throws Exception {
        String taskId = "task-serial-1";
        String userId = "user-x";

        UpdateTaskCommand.Builder b = new UpdateTaskCommand.Builder(taskId, userId);
        b.title("Batch title");
        b.status(TaskStatus.in_progress);
        UpdateTaskCommand cmd = b.build();

        SyncCommand sc = CommandConverter.toSyncCommand(cmd);
        CommandBatch batch = new CommandBatch(userId, OffsetDateTime.now(), null, List.of(sc));

        String json = JSONUtils.toJsonString(batch);
        assertNotNull(json);
        // The JSON should contain changedFields at root level and the normalized enum value
        assertTrue(json.contains("\"changedFields\""), "serialized JSON must include changedFields at root level");
        assertTrue(json.contains("Batch title"));
        assertTrue(json.contains("in_progress"), "status enum should be serialized as its name");

        // Parse back to ensure structure exists
        Map<String, Object> parsed = JSONUtils.fromJsonString(json);
        assertTrue(parsed.containsKey("commands"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cmds = (List<Map<String, Object>>) parsed.get("commands");
        assertEquals(1, cmds.size());
        
        // changedFields should be at root level of command, not in data
        Map<String, Object> command = cmds.get(0);
        assertTrue(command.containsKey("changedFields"));
        @SuppressWarnings("unchecked")
        Map<String, Object> changedFields = (Map<String, Object>) command.get("changedFields");
        assertNotNull(changedFields);
        assertTrue(changedFields.containsKey("title"));
        assertEquals("Batch title", changedFields.get("title"));
    }
}
