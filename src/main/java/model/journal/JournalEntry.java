package model.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable JournalEntry representing a single journal entry.
 * All journal data is stored locally only and never sent to cloud services.
 */
public final class JournalEntry {
    
    public enum Source {
        TEXT, VOICE
    }
    
    private final UUID id;
    private final UUID sessionId;
    private final Instant createdAt;
    private final Source source;
    private final String rawText;
    private final Map<String, Object> transcriptMeta;
    private final String summary;
    private final List<String> tags;
    private final JournalEntities entities;
    private final JournalSentiment sentiment;
    private final List<JournalLink> links;
    private final int version;

    @JsonCreator
    public JournalEntry(
            @JsonProperty("id") UUID id,
            @JsonProperty("sessionId") UUID sessionId,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("source") Source source,
            @JsonProperty("rawText") String rawText,
            @JsonProperty("transcriptMeta") Map<String, Object> transcriptMeta,
            @JsonProperty("summary") String summary,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("entities") JournalEntities entities,
            @JsonProperty("sentiment") JournalSentiment sentiment,
            @JsonProperty("links") List<JournalLink> links,
            @JsonProperty("version") int version) {
        this.id = id;
        this.sessionId = sessionId;
        this.createdAt = createdAt;
        this.source = source;
        this.rawText = rawText;
        this.transcriptMeta = transcriptMeta;
        this.summary = summary;
        this.tags = tags;
        this.entities = entities;
        this.sentiment = sentiment;
        this.links = links;
        this.version = version;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getSessionId() { return sessionId; }
    public Instant getCreatedAt() { return createdAt; }
    public Source getSource() { return source; }
    public String getRawText() { return rawText; }
    public Map<String, Object> getTranscriptMeta() { return transcriptMeta; }
    public String getSummary() { return summary; }
    public List<String> getTags() { return tags; }
    public JournalEntities getEntities() { return entities; }
    public JournalSentiment getSentiment() { return sentiment; }
    public List<JournalLink> getLinks() { return links; }
    public int getVersion() { return version; }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .sessionId(this.sessionId)
                .createdAt(this.createdAt)
                .source(this.source)
                .rawText(this.rawText)
                .transcriptMeta(this.transcriptMeta)
                .summary(this.summary)
                .tags(this.tags)
                .entities(this.entities)
                .sentiment(this.sentiment)
                .links(this.links)
                .version(this.version);
    }

    public static class Builder {
        private UUID id;
        private UUID sessionId;
        private Instant createdAt;
        private Source source;
        private String rawText;
        private Map<String, Object> transcriptMeta;
        private String summary;
        private List<String> tags;
        private JournalEntities entities;
        private JournalSentiment sentiment;
        private List<JournalLink> links;
        private int version = 1; // Default version

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder sessionId(UUID sessionId) { this.sessionId = sessionId; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder source(Source source) { this.source = source; return this; }
        public Builder rawText(String rawText) { this.rawText = rawText; return this; }
        public Builder transcriptMeta(Map<String, Object> transcriptMeta) { this.transcriptMeta = transcriptMeta; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder entities(JournalEntities entities) { this.entities = entities; return this; }
        public Builder sentiment(JournalSentiment sentiment) { this.sentiment = sentiment; return this; }
        public Builder links(List<JournalLink> links) { this.links = links; return this; }
        public Builder version(int version) { this.version = version; return this; }

        public JournalEntry build() {
            // Set defaults if not provided
            if (id == null) id = UUID.randomUUID();
            if (createdAt == null) createdAt = Instant.now();
            if (source == null) source = Source.TEXT;
            if (tags == null) tags = List.of();
            if (links == null) links = List.of();
            
            return new JournalEntry(id, sessionId, createdAt, source, rawText, 
                    transcriptMeta, summary, tags, entities, sentiment, links, version);
        }
    }

    @Override
    public String toString() {
        return "JournalEntry{" +
                "id=" + id +
                ", sessionId=" + sessionId +
                ", createdAt=" + createdAt +
                ", source=" + source +
                ", rawText='" + (rawText != null ? rawText.substring(0, Math.min(50, rawText.length())) + "..." : null) + '\'' +
                ", version=" + version +
                '}';
    }
}