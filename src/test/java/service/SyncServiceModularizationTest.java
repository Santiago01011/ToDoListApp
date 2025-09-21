package service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import model.TaskHandlerV2;
import model.commands.CommandQueue;
import model.commands.CreateTaskCommand;
import model.sync.SyncCommand;
import service.sync.CommandConverter;

@DisplayName("SyncService modularization smoke tests")
class SyncServiceModularizationTest {

    @Test
    @DisplayName("CommandConverter creates SyncCommand from CreateTaskCommand")
    void testCommandConverterCreate() {
    String userId = UUID.randomUUID().toString();
        TaskHandlerV2 v2 = new TaskHandlerV2(userId);
        CommandQueue q = v2.getCommandQueue();
    // Ensure a clean slate in case a previous run persisted commands
    q.clearCommands();

        var cmd = CreateTaskCommand.create(
            "task-1", userId, "Title", "Desc", model.TaskStatus.pending, null, "folder-1"
        );
        q.enqueue(cmd);

        List<model.commands.Command> pending = q.getPendingCommands();
        assertEquals(1, pending.size());

        SyncCommand sc = CommandConverter.toSyncCommand(pending.get(0));
        assertNotNull(sc);
        assertEquals("CREATE_TASK", sc.getType());
        assertEquals("task-1", sc.getEntityId());
        assertNotNull(sc.getData());
        assertEquals("Title", sc.getData().get("title"));
    }
}
