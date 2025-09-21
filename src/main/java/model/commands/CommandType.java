package model.commands;

/**
 * Enum of all supported command types in the system.
 * Used for serialization/deserialization and command routing.
 */
public enum CommandType {
    CREATE_TASK,
    UPDATE_TASK,
    DELETE_TASK,
    
    // Journal commands (local-only, never synced)
    ADD_JOURNAL_ENTRY,
    UPDATE_JOURNAL_ENTRY,
    DELETE_JOURNAL_ENTRY,
    START_JOURNAL_SESSION,
    END_JOURNAL_SESSION,
    RECORD_PROPOSED_ACTION,
    SET_PROPOSAL_DECISION,
    
    // Future command types for life assistant features
    CREATE_APPOINTMENT,
    UPDATE_APPOINTMENT,
    DELETE_APPOINTMENT,
    CREATE_FINANCIAL_ENTRY,
    UPDATE_FINANCIAL_ENTRY,
    DELETE_FINANCIAL_ENTRY
}
