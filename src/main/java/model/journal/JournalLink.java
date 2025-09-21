package model.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a link between journal entries and structured items (Tasks/Events).
 * Provides backtracing from generated tasks to their journal source.
 */
public final class JournalLink {
    
    public enum Type {
        TASK, EVENT
    }
    
    private final Type type;
    private final String id;

    @JsonCreator
    public JournalLink(
            @JsonProperty("type") Type type,
            @JsonProperty("id") String id) {
        this.type = type;
        this.id = id;
    }

    public Type getType() { return type; }
    public String getId() { return id; }

    @Override
    public String toString() {
        return "JournalLink{" +
                "type=" + type +
                ", id='" + id + '\'' +
                '}';
    }
}