package model.commands;

import java.time.LocalDateTime;

/**
 * Base interface for all commands in the command queue pattern.
 * Commands represent user intents and are immutable once created.
 * This sealed interface ensures only specific command types can be implemented.
 */
public sealed interface Command 
    permits CreateTaskCommand, UpdateTaskCommand, DeleteTaskCommand {
    
    /**
     * Unique identifier for this command instance
     */
    String getCommandId();
    
    /**
     * The entity (task) this command operates on
     */
    String getEntityId();
    
    /**
     * When this command was created (client-side timestamp)
     */
    LocalDateTime getTimestamp();
    
    /**
     * User who initiated this command
     */
    String getUserId();
    
    /**
     * Type of command for serialization/deserialization
     */
    CommandType getType();
}
