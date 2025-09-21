package service;

import model.commands.CreateTaskCommand;
import model.commands.CommandQueue;
import model.TaskHandlerV2;
import COMMON.UserProperties;
import model.sync.SyncResponse;
import model.sync.SyncResponse.CommandResult;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SyncServiceFailureHandlingTest {

    @Test
    public void failedCommandsArePersistedAndRemovedFromQueue() throws Exception {
        String userId = "testuser-" + UUID.randomUUID().toString();
        UserProperties.setProperty("userUUID", userId);

        TaskHandlerV2 handler = new TaskHandlerV2(userId);
        // Ensure empty queue
        CommandQueue q = handler.getCommandQueue();

        // Enqueue a dummy create command
        CreateTaskCommand c1 = CreateTaskCommand.create("cmd-1", userId, "t1", "d1", null, null, null);
        CreateTaskCommand c2 = CreateTaskCommand.create("cmd-2", userId, "t2", "d2", null, null, null);
        q.enqueue(c1);
        q.enqueue(c2);

        assertEquals(2, q.getPendingCommands().size());

    // Obtain actual command IDs assigned by the queue
    List<model.commands.Command> pending = q.getPendingCommands();
    assertEquals(2, pending.size());
    String id1 = pending.get(0).getCommandId();
    String id2 = pending.get(1).getCommandId();

    // Craft a SyncResponse with one success and one failed using real clientIds
    SyncResponse resp = new SyncResponse();
    CommandResult success = new CommandResult();
    success.setClientId(id1);
    success.setCommandType("CREATE_TASK");
    success.setSuccess(true);

    CommandResult failed = new CommandResult();
    failed.setClientId(id2);
    failed.setCommandType("CREATE_TASK");
    failed.setSuccess(false);
    failed.setErrorMessage("duplicate key");

    resp.setProcessedCommands(List.of(success));
    resp.setFailedCommands(List.of(failed));
        resp.setServerTimestamp(OffsetDateTime.now());

        SyncService svc = new SyncService(handler);
        svc.setUserUUID(userId);

        svc.handleSyncResponse(resp, OffsetDateTime.now());

        // After handling, queue should no longer contain the two commands
        assertTrue(q.getPendingCommands().isEmpty(), "Queue should have removed processed/failed commands");

        File failedReport = new File(COMMON.UserProperties.getUserDataFilePath(userId, "failed_commands.json"));
        assertTrue(failedReport.exists(), "failed_commands.json should be created");

    }
}
