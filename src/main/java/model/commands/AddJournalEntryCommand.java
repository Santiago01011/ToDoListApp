package model.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.journal.JournalEntry;
import java.time.LocalDateTime;

/**
 * Command for adding a new journal entry.
 * This command is local-only and never synced to cloud services.
 */
public record AddJournalEntryCommand(
    @JsonProperty("commandId") String commandId,
    @JsonProperty("entityId") String entityId,
    @JsonProperty("userId") String userId,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("entry") JournalEntry entry
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
    public CommandType getType() { return CommandType.ADD_JOURNAL_ENTRY; }
}