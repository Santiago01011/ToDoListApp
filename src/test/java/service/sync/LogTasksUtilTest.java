package service.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LogTasksUtilTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void normalizesColumnsAndDataToObject() {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode columns = mapper.createArrayNode();
        columns.add("task_id");
        columns.add("title");
        root.set("columns", columns);

        ArrayNode row = mapper.createArrayNode();
        row.add("abc");
        row.add("Hello");

        JsonNode normalized = LogTasksUtil.normalizeRow(root, row, mapper);
        assertTrue(normalized.isObject());
        assertEquals("abc", normalized.get("task_id").asText());
        assertEquals("Hello", normalized.get("title").asText());
    }

    @Test
    void parsesOffsetDateTimeWithTimezone() {
        ObjectNode node = mapper.createObjectNode();
        node.put("updated_at", "2024-09-10T12:34:56Z");
        LocalDateTime dt = LogTasksUtil.timeOf(node, "updated_at");
        assertNotNull(dt);
    }
}
