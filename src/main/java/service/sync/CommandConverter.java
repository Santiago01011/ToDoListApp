package service.sync;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import model.commands.Command;
import model.sync.SyncCommand;

public final class CommandConverter {
    private CommandConverter() {}

    public static SyncCommand toSyncCommand(Command command) {
        switch (command.getType()) {
            case CREATE_TASK:
                if (command instanceof model.commands.CreateTaskCommand createCmd) {
                    Map<String, Object> createData = new HashMap<>();
                    createData.put("title", createCmd.title());
                    createData.put("description", createCmd.description());
                    createData.put("status", createCmd.status() != null ? createCmd.status().toString() : null);
                    createData.put("dueDate", createCmd.dueDate());
                    createData.put("folderId", createCmd.folderId());  // Use camelCase to match database expectation
                    createData.put("created_at", LocalDateTime.now());
                    return new SyncCommand(
                        command.getEntityId(),
                        "CREATE_TASK",
                        createData,
                        command.getTimestamp(),
                        command.getCommandId()
                    );
                }
                break;
            case UPDATE_TASK:
                if (command instanceof model.commands.UpdateTaskCommand updateCmd) {
                    // The server expects changedFields at the root level, not inside data.
                    // Map field names to server expectations (database needs to be updated to match)
                    Map<String, Object> changed = updateCmd.changedFields();
                    Map<String, Object> normalized = new HashMap<>();
                    if (changed != null) {
                        for (Map.Entry<String, Object> e : changed.entrySet()) {
                            String key = e.getKey();
                            Object val = e.getValue();
                            
                            // Normalize enum values to strings
                            if (val instanceof Enum) val = val.toString();
                            
                            // Map field names to server expectations
                            switch (key) {
                                case "folderId" -> normalized.put("folder_id", val);  // Map to snake_case for now
                                case "dueDate" -> normalized.put("due_date", val);   // Map to snake_case for now
                                default -> normalized.put(key, val);
                            }
                        }
                    }
                    
                    // Use new constructor that puts changedFields at root level
                    return new SyncCommand(
                        command.getEntityId(),
                        "UPDATE_TASK",
                        new HashMap<>(), // Empty data object
                        command.getTimestamp(),
                        command.getCommandId(),
                        normalized // changedFields at root level
                    );
                }
                break;
            case DELETE_TASK:
                if (command instanceof model.commands.DeleteTaskCommand) {
                    Map<String, Object> deleteData = new HashMap<>();
                    return new SyncCommand(
                        command.getEntityId(),
                        "DELETE_TASK",
                        deleteData,
                        command.getTimestamp(),
                        command.getCommandId()
                    );
                }
                break;
            default:
                return null;
        }
        return null;
    }
}
