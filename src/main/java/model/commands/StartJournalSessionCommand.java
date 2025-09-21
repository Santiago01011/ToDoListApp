package model.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.journal.JournalSession;
import java.time.LocalDateTime;

/**
 * Command for starting a new journal session.
 * This command is local-only and never synced to cloud services.
 */
public record StartJournalSessionCommand(
    @JsonProperty("commandId") String commandId,
    @JsonProperty("entityId") String entityId,
    @JsonProperty("userId") String userId,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("session") JournalSession session
) implements Command {
    
    @Override
    public String getCommandId() { return commandId; }
    
    @Override
    public String getEntityId() { return entityId; }
    
    @Override
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String getUserId() { return userId; }
    
    @Override
    public CommandType getType() { return CommandType.START_JOURNAL_SESSION; }
}