package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskJsonBuilder {

    private static final String BASE_DIRECTORY = System.getProperty("user.home") + File.separator + ".todoapp";

    public static File buildJsonFile(Stream<Task> taskStream, String fileName) {
        File jsonFile = new File(BASE_DIRECTORY + File.separator + fileName);
        try {
            jsonFile.getParentFile().mkdirs();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            List<Map<String, Object>> tasks = taskStream.map(task -> {
                Map<String, Object> taskMap = new LinkedHashMap<>();
                taskMap.put("task_id", task.getTask_id());
                taskMap.put("task_title", task.getTask_title());
                taskMap.put("description", task.getDescription());
                taskMap.put("status", task.getStatus());
                taskMap.put("sync_status", task.getSync_status());
                taskMap.put("due_date", task.getDue_date() != null ? task.getDue_date().toString() : null);
                taskMap.put("created_at", task.getCreated_at() != null ? task.getCreated_at().toString() : null);
                taskMap.put("updated_at", task.getUpdated_at() != null ? task.getUpdated_at().toString() : null);
                taskMap.put("folder_id", task.getFolder_id());
                taskMap.put("folder_name", task.getFolder_name());
                taskMap.put("deleted_at", task.getDeleted_at() != null ? task.getDeleted_at().toString() : null);
                return taskMap;
            }).collect(Collectors.toList());

            mapper.writeValue(jsonFile, Map.of("tasks", tasks));
        } catch (IOException e) {
            System.err.println("Error creating JSON file: " + e.getMessage());
        }
        return jsonFile;
    }
}