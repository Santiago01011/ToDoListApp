package model.commands;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;

/**
 * Utility class for serializing and deserializing commands with proper polymorphic support.
 * Uses Jackson's polymorphic type handling to correctly serialize/deserialize command objects.
 */
public class CommandSerializer {
    
    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    
    /**
     * Mixin interface to add polymorphic type information to Command serialization
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CreateTaskCommand.class, name = "CREATE_TASK"),
        @JsonSubTypes.Type(value = UpdateTaskCommand.class, name = "UPDATE_TASK"),
        @JsonSubTypes.Type(value = DeleteTaskCommand.class, name = "DELETE_TASK")
    })
    public interface CommandMixin {}
    
    static {
        // Register the mixin to enable polymorphic serialization
        mapper.addMixIn(Command.class, CommandMixin.class);
    }
    
    /**
     * Serialize a list of commands to JSON string
     */
    public static String serialize(List<Command> commands) throws Exception {
        return mapper.writeValueAsString(commands);
    }
    
    /**
     * Deserialize a JSON string to a list of commands
     */
    public static List<Command> deserialize(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<List<Command>>() {});
    }
    
    /**
     * Serialize a single command to JSON string
     */
    public static String serialize(Command command) throws Exception {
        return mapper.writeValueAsString(command);
    }
    
    /**
     * Deserialize a JSON string to a single command
     */
    public static Command deserializeCommand(String json) throws Exception {
        return mapper.readValue(json, Command.class);
    }
}
