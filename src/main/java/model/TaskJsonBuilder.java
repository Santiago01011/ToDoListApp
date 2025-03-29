package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
            Map<String, Object> jsonbStructure = buildJsonStructure(taskStream);

            mapper.writeValue(jsonFile, jsonbStructure);
        } catch (IOException e) {
            System.err.println("Error creating JSONB file: " + e.getMessage());
        }
        return jsonFile;
    }

    public static String buildJsonContent(Stream<Task> taskStream) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            Map<String, Object> jsonbStructure = buildJsonStructure(taskStream);

            return mapper.writeValueAsString(jsonbStructure);
        } catch (IOException e) {
            System.err.println("Error creating JSON content: " + e.getMessage());
            return "";
        }
    }

    private static Map<String, Object> buildJsonStructure(Stream<Task> taskStream) {
        Map<String, Object> jsonbStructure = new LinkedHashMap<>();
        jsonbStructure.put("columns", List.of("folder_id", "folder_name", "task_id", "task_title", "description", "sync_status", "status", "due_date", "created_at"));

        List<List<Object>> data = taskStream.map(task -> {
            List<Object> taskData = new ArrayList<>();
            taskData.add(task.getFolder_id());
            taskData.add(task.getFolder_name());
            taskData.add(task.getTask_id());
            taskData.add(task.getTask_title());
            taskData.add(task.getDescription());
            taskData.add(task.getSync_status());
            taskData.add(task.getStatus());
            taskData.add(task.getDue_date() != null ? task.getDue_date().toString() : null);
            taskData.add(task.getCreated_at() != null ? task.getCreated_at().toString() : null);
            return taskData;
        }).collect(Collectors.toList());

        jsonbStructure.put("data", data);
        return jsonbStructure;
    }
}