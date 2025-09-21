package model.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.journal.ProposedAction;
import java.time.LocalDateTime;

/**
 * Command for recording a proposed action from journal processing.
 * This command is local-only and never synced to cloud services.
 */
public record RecordProposedActionCommand(
    @JsonProperty("commandId") String commandId,
    @JsonProperty("entityId") String entityId,
    @JsonProperty("userId") String userId,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("proposedAction") ProposedAction proposedAction
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
    public CommandType getType() { return CommandType.RECORD_PROPOSED_ACTION; }
}