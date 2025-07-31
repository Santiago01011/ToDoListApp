package model.commands;

/**
 * Enum of all supported command types in the system.
 * Used for serialization/deserialization and command routing.
 */
public enum CommandType {
    CREATE_TASK,
    UPDATE_TASK,
    DELETE_TASK,
    // Future command types for life assistant features
    CREATE_APPOINTMENT,
    UPDATE_APPOINTMENT,
    DELETE_APPOINTMENT,
    CREATE_FINANCIAL_ENTRY,
    UPDATE_FINANCIAL_ENTRY,
    DELETE_FINANCIAL_ENTRY
}
