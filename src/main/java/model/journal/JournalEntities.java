package model.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * Represents extracted entities from journal entries.
 * All processing is done locally to preserve privacy.
 */
public final class JournalEntities {
    
    private final List<String> people;
    private final List<String> locations;
    private final List<String> projects;
    private final List<DateTimeEntity> datetimes;
    private final List<DurationEntity> durations;

    @JsonCreator
    public JournalEntities(
            @JsonProperty("people") List<String> people,
            @JsonProperty("locations") List<String> locations,
            @JsonProperty("projects") List<String> projects,
            @JsonProperty("datetimes") List<DateTimeEntity> datetimes,
            @JsonProperty("durations") List<DurationEntity> durations) {
        this.people = people != null ? people : List.of();
        this.locations = locations != null ? locations : List.of();
        this.projects = projects != null ? projects : List.of();
        this.datetimes = datetimes != null ? datetimes : List.of();
        this.durations = durations != null ? durations : List.of();
    }

    public List<String> getPeople() { return people; }
    public List<String> getLocations() { return locations; }
    public List<String> getProjects() { return projects; }
    public List<DateTimeEntity> getDatetimes() { return datetimes; }
    public List<DurationEntity> getDurations() { return durations; }

    public static class DateTimeEntity {
        private final Instant start;
        private final Instant end;
        private final String grain;
        private final String text;

        @JsonCreator
        public DateTimeEntity(
                @JsonProperty("start") Instant start,
                @JsonProperty("end") Instant end,
                @JsonProperty("grain") String grain,
                @JsonProperty("text") String text) {
            this.start = start;
            this.end = end;
            this.grain = grain;
            this.text = text;
        }

        public Instant getStart() { return start; }
        public Instant getEnd() { return end; }
        public String getGrain() { return grain; }
        public String getText() { return text; }
    }

    public static class DurationEntity {
        private final long seconds;
        private final String text;

        @JsonCreator
        public DurationEntity(
                @JsonProperty("seconds") long seconds,
                @JsonProperty("text") String text) {
            this.seconds = seconds;
            this.text = text;
        }

        public long getSeconds() { return seconds; }
        public String getText() { return text; }
    }
}