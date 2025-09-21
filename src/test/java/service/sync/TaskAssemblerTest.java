package service.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskAssemblerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void mergesStatusAndDatesAndFolder() {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("task_id", "11111111-1111-1111-1111-111111111111");
        payload.put("task_title", "Sample");
        payload.put("status", "In Progress");
        payload.put("due_date", LocalDateTime.now().toString());
        payload.put("folder_id", "f1");
        payload.put("folder_name", "Work");

        Task merged = TaskAssembler.mergeFromPayload(null, payload, LocalDateTime.now());
        assertEquals("Sample", merged.getTitle());
        assertEquals(TaskStatus.in_progress, merged.getStatus());
        assertEquals("f1", merged.getFolder_id());
        assertEquals("Work", merged.getFolder_name());
        assertNotNull(merged.getDue_date());
    }

    @Test
    void preservesExistingFieldsOnPartialPayload() {
        Task existing = new Task.Builder("22222222-2222-2222-2222-222222222222")
                .taskTitle("Old")
                .status(TaskStatus.pending)
                .folderId("oldFolder")
                .folderName("Old Name")
                .build();

        ObjectNode payload = mapper.createObjectNode();
        payload.put("task_id", existing.getTask_id());
        payload.put("status", "completed");

        Task merged = TaskAssembler.mergeFromPayload(existing, payload, LocalDateTime.now());
        assertEquals("Old", merged.getTitle());
        assertEquals(TaskStatus.completed, merged.getStatus());
        assertEquals("oldFolder", merged.getFolder_id());
        assertEquals("Old Name", merged.getFolder_name());
    }
}
