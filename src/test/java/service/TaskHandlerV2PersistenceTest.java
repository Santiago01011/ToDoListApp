package service;

import model.Folder;
import model.Task;
import model.TaskHandlerV2;
import COMMON.UserProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TaskHandlerV2PersistenceTest {

    @Test
    public void tasksJsonRoundtrip_preservesFolderNameAndFoldersArray() throws Exception {
        String userId = "testuser-" + UUID.randomUUID().toString();
        UserProperties.setProperty("userUUID", userId);
        // Ensure user dir
        String tasksPath = UserProperties.getUserDataFilePath(userId, "tasks.json");
        File f = new File(tasksPath);
        if (f.exists()) f.delete();

        TaskHandlerV2 handler = new TaskHandlerV2(userId);
        Folder folder = new Folder.Builder("folder-1").folderName("MyFolder").build();
        handler.setFoldersList(List.of(folder));

        handler.createTask("Title", "Desc", null, LocalDateTime.now().plusDays(1), folder.getFolder_id());
        handler.saveTasksToJson();

        assertTrue(f.exists(), "tasks.json should exist after save");

        TaskHandlerV2 loader = new TaskHandlerV2(userId);
        List<Task> tasks = loader.getAllTasks();
        assertFalse(tasks.isEmpty(), "Loaded tasks should not be empty");
        Task loaded = tasks.get(0);
        assertEquals(folder.getFolder_id(), loaded.getFolder_id(), "folder_id should persist");
        // folder_name should be preserved because we persisted folders array and resolved names on save/load
        assertEquals("MyFolder", loaded.getFolder_name(), "folder_name should be available after load");
    }
}
