package model.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents sentiment analysis data for journal entries.
 * Processing is done locally to preserve privacy.
 */
public final class JournalSentiment {
    
    private final String label;
    private final double score;

    @JsonCreator
    public JournalSentiment(
            @JsonProperty("label") String label,
            @JsonProperty("score") double score) {
        this.label = label;
        this.score = score;
    }

    public String getLabel() { return label; }
    public double getScore() { return score; }

    @Override
    public String toString() {
        return "JournalSentiment{" +
                "label='" + label + '\'' +
                ", score=" + score +
                '}';
    }
}