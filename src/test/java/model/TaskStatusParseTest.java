package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskStatusParseTest {

    @Test
    void parsesEnumNames() {
        assertEquals(TaskStatus.in_progress, TaskStatus.parse("in_progress"));
        assertEquals(TaskStatus.pending, TaskStatus.parse("pending"));
        assertEquals(TaskStatus.completed, TaskStatus.parse("completed"));
        assertEquals(TaskStatus.incoming_due, TaskStatus.parse("incoming_due"));
        assertEquals(TaskStatus.overdue, TaskStatus.parse("overdue"));
        assertEquals(TaskStatus.newest, TaskStatus.parse("newest"));
    }

    @Test
    void parsesDisplayLabels() {
        assertEquals(TaskStatus.in_progress, TaskStatus.parse("In Progress"));
        assertEquals(TaskStatus.pending, TaskStatus.parse("Pending"));
        assertEquals(TaskStatus.completed, TaskStatus.parse("Completed"));
        assertEquals(TaskStatus.incoming_due, TaskStatus.parse("Incoming Due"));
        assertEquals(TaskStatus.overdue, TaskStatus.parse("Overdue"));
        assertEquals(TaskStatus.newest, TaskStatus.parse("Newest"));
    }

    @Test
    void parsesHyphenatedAndCaseVariants() {
        assertEquals(TaskStatus.in_progress, TaskStatus.parse("in-progress"));
        assertEquals(TaskStatus.pending, TaskStatus.parse("PENDING"));
        assertEquals(TaskStatus.completed, TaskStatus.parse("completed"));
        assertEquals(TaskStatus.incoming_due, TaskStatus.parse("Incoming_due"));
    }

    @Test
    void returnsNullForUnknownOrEmpty() {
        assertNull(TaskStatus.parse("unknown"));
        assertNull(TaskStatus.parse(""));
        assertNull(TaskStatus.parse(null));
    }
}
