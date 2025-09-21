package model.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a proposed action derived from a journal entry.
 * Actions require user approval before being converted to commands.
 */
public final class ProposedAction {
    
    public enum Type {
        CREATE_TASK,
        UPDATE_TASK,
        COMPLETE_TASK,
        DELETE_TASK,
        CREATE_EVENT,
        UPDATE_EVENT,
        DELETE_EVENT,
        RESCHEDULE_TASK,
        ADD_TASK_NOTE
    }
    
    public enum Decision {
        PENDING,
        APPROVED,
        REJECTED,
        EDITED
    }
    
    private final UUID id;
    private final UUID entryId;
    private final Type type;
    private final Map<String, Object> payload;
    private final String rationale;
    private final double confidence;
    private final Decision decision;
    private final Map<String, Object> edits;
    private final Instant createdAt;

    @JsonCreator
    public ProposedAction(
            @JsonProperty("id") UUID id,
            @JsonProperty("entryId") UUID entryId,
            @JsonProperty("type") Type type,
            @JsonProperty("payload") Map<String, Object> payload,
            @JsonProperty("rationale") String rationale,
            @JsonProperty("confidence") double confidence,
            @JsonProperty("decision") Decision decision,
            @JsonProperty("edits") Map<String, Object> edits,
            @JsonProperty("createdAt") Instant createdAt) {
        this.id = id;
        this.entryId = entryId;
        this.type = type;
        this.payload = payload;
        this.rationale = rationale;
        this.confidence = confidence;
        this.decision = decision;
        this.edits = edits;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getEntryId() { return entryId; }
    public Type getType() { return type; }
    public Map<String, Object> getPayload() { return payload; }
    public String getRationale() { return rationale; }
    public double getConfidence() { return confidence; }
    public Decision getDecision() { return decision; }
    public Map<String, Object> getEdits() { return edits; }
    public Instant getCreatedAt() { return createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .entryId(this.entryId)
                .type(this.type)
                .payload(this.payload)
                .rationale(this.rationale)
                .confidence(this.confidence)
                .decision(this.decision)
                .edits(this.edits)
                .createdAt(this.createdAt);
    }

    public static class Builder {
        private UUID id;
        private UUID entryId;
        private Type type;
        private Map<String, Object> payload;
        private String rationale;
        private double confidence;
        private Decision decision = Decision.PENDING;
        private Map<String, Object> edits;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder entryId(UUID entryId) { this.entryId = entryId; return this; }
        public Builder type(Type type) { this.type = type; return this; }
        public Builder payload(Map<String, Object> payload) { this.payload = payload; return this; }
        public Builder rationale(String rationale) { this.rationale = rationale; return this; }
        public Builder confidence(double confidence) { this.confidence = confidence; return this; }
        public Builder decision(Decision decision) { this.decision = decision; return this; }
        public Builder edits(Map<String, Object> edits) { this.edits = edits; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ProposedAction build() {
            if (id == null) id = UUID.randomUUID();
            if (createdAt == null) createdAt = Instant.now();
            
            return new ProposedAction(id, entryId, type, payload, rationale, 
                    confidence, decision, edits, createdAt);
        }
    }

    @Override
    public String toString() {
        return "ProposedAction{" +
                "id=" + id +
                ", entryId=" + entryId +
                ", type=" + type +
                ", confidence=" + confidence +
                ", decision=" + decision +
                ", createdAt=" + createdAt +
                '}';
    }
}