package model.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a journaling session with multiple entries.
 * Sessions help group related journal entries and track proposed actions.
 */
public final class JournalSession {
    
    public enum Status {
        ACTIVE, PAUSED, ENDED
    }
    
    private final UUID id;
    private final Instant startedAt;
    private final Instant endedAt;
    private final Status status;
    private final List<UUID> entryIds;
    private final List<UUID> proposedActionIds;
    private final Map<String, Object> settingsSnapshot;

    @JsonCreator
    public JournalSession(
            @JsonProperty("id") UUID id,
            @JsonProperty("startedAt") Instant startedAt,
            @JsonProperty("endedAt") Instant endedAt,
            @JsonProperty("status") Status status,
            @JsonProperty("entryIds") List<UUID> entryIds,
            @JsonProperty("proposedActionIds") List<UUID> proposedActionIds,
            @JsonProperty("settingsSnapshot") Map<String, Object> settingsSnapshot) {
        this.id = id;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
        this.entryIds = entryIds != null ? entryIds : List.of();
        this.proposedActionIds = proposedActionIds != null ? proposedActionIds : List.of();
        this.settingsSnapshot = settingsSnapshot;
    }

    public UUID getId() { return id; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public Status getStatus() { return status; }
    public List<UUID> getEntryIds() { return entryIds; }
    public List<UUID> getProposedActionIds() { return proposedActionIds; }
    public Map<String, Object> getSettingsSnapshot() { return settingsSnapshot; }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .startedAt(this.startedAt)
                .endedAt(this.endedAt)
                .status(this.status)
                .entryIds(this.entryIds)
                .proposedActionIds(this.proposedActionIds)
                .settingsSnapshot(this.settingsSnapshot);
    }

    public static class Builder {
        private UUID id;
        private Instant startedAt;
        private Instant endedAt;
        private Status status;
        private List<UUID> entryIds;
        private List<UUID> proposedActionIds;
        private Map<String, Object> settingsSnapshot;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder endedAt(Instant endedAt) { this.endedAt = endedAt; return this; }
        public Builder status(Status status) { this.status = status; return this; }
        public Builder entryIds(List<UUID> entryIds) { this.entryIds = entryIds; return this; }
        public Builder proposedActionIds(List<UUID> proposedActionIds) { this.proposedActionIds = proposedActionIds; return this; }
        public Builder settingsSnapshot(Map<String, Object> settingsSnapshot) { this.settingsSnapshot = settingsSnapshot; return this; }

        public JournalSession build() {
            if (id == null) id = UUID.randomUUID();
            if (startedAt == null) startedAt = Instant.now();
            if (status == null) status = Status.ACTIVE;
            if (entryIds == null) entryIds = List.of();
            if (proposedActionIds == null) proposedActionIds = List.of();
            
            return new JournalSession(id, startedAt, endedAt, status, entryIds, proposedActionIds, settingsSnapshot);
        }
    }

    @Override
    public String toString() {
        return "JournalSession{" +
                "id=" + id +
                ", startedAt=" + startedAt +
                ", endedAt=" + endedAt +
                ", status=" + status +
                ", entryCount=" + entryIds.size() +
                ", proposalCount=" + proposedActionIds.size() +
                '}';
    }
}