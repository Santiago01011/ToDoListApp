package COMMON;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import model.Task;

public class JSONUtils {
    
    public static final String BASE_DIRECTORY = System.getProperty("user.home") + File.separator + ".todoapp";
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    
    public static void createBaseDirectory() {
        File baseDir = new File(BASE_DIRECTORY);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        if(!new File(BASE_DIRECTORY + File.separator + "tasks.json").exists())
            createEmptyJsonFile(BASE_DIRECTORY + File.separator + "tasks.json");
    }

    /**
     * Static block to create the base directory and tasks.json file if they do not exist.
     */    
    static {
        createBaseDirectory();
    }
    
    /**
     * Creates an empty JSON file with the specified structure.
     * 
     * @param filePath Path to the file to create
     */
    public static void createEmptyJsonFile(String filePath) {
        File file = new File(filePath);
        try {
            file.getParentFile().mkdirs();
            Map<String, Object> emptyStructure = new HashMap<>();
            emptyStructure.put("data", new ArrayList<List<Object>>());
            emptyStructure.put("columns", List.of("folder_id", "folder_name", "task_id", "task_title", "description", "sync_status", "status", "due_date", "created_at"));
            emptyStructure.put("last_sync", null);
            
            MAPPER.writeValue(file, emptyStructure);
        } catch (IOException e) {
            System.err.println("Error creating empty JSON file: " + e.getMessage());
        }
    }
    
    /**
     * Reads a JSON file into a Map.
     * 
     * @param file The file to read
     * @return A Map representation of the JSON content
     * @throws IOException If there is an error reading the file
     */
    public static Map<String, Object> readJsonFile(File file) throws IOException {
        return MAPPER.readValue(file, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * Reads a JSON file into a Map using the file path.
     * 
     * @param filePath Path to the file to read
     * @return A Map representation of the JSON content
     * @throws IOException If there is an error reading the file
     */
    public static Map<String, Object> readJsonFile(String filePath) throws IOException {
        return readJsonFile(new File(filePath));
    }
    
    /**
     * Writes a Map to a JSON file.
     * 
     * @param data The data to write
     * @param filePath Path to the file to write to
     * @throws IOException If there is an error writing the file
     */
    public static void writeJsonFile(Map<String, Object> data, String filePath) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), data);
    }
    
    /**
     * Writes a Map to a JSON file.
     * 
     * @param data The data to write
     * @param file The file to write to
     * @throws IOException If there is an error writing the file
     */
    public static void writeJsonFile(Map<String, Object> data, File file) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, data);
    }
    
    /**
     * Converts an object to a JSON string.
     * 
     * @param object The object to convert
     * @return A JSON string representation of the object
     * @throws IOException If there is an error converting the object
     */
    public static String toJsonString(Object object) throws IOException {
        return MAPPER.writeValueAsString(object);
    }
    
    /**
     * Converts a JSON string to an object of the specified type.
     * 
     * @param <T> The type to convert to
     * @param json The JSON string to convert
     * @param valueType The class of the type to convert to
     * @return An object of the specified type
     * @throws IOException If there is an error converting the JSON
     */
    public static <T> T fromJsonString(String json, Class<T> valueType) throws IOException {
        return MAPPER.readValue(json, valueType);
    }
    
    /**
     * Converts a JSON string to a Map.
     * 
     * @param json The JSON string to convert
     * @return A Map representation of the JSON content
     * @throws IOException If there is an error converting the JSON
     */
    public static Map<String, Object> fromJsonString(String json) throws IOException {
        return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * Converts an object to the specified type.
     * 
     * @param <T> The type to convert to
     * @param fromValue The object to convert
     * @param toValueType The class of the type to convert to
     * @return An object of the specified type
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return MAPPER.convertValue(fromValue, toValueType);
    }
    
    /**
     * Updates a JSON file with new data.
     * 
     * @param filePath Path to the file to update
     * @param newData The new data to write
     * @throws IOException If there is an error updating the file
     */
    public static void updateJsonFile(String filePath, Map<String, Object> newData) throws IOException {
        writeJsonFile(newData, filePath);
    }
    
    /**
     * Reads the default tasks JSON file.
     * 
     * @return A Map representation of the tasks JSON content
     * @throws IOException If there is an error reading the file
     */
    public static Map<String, Object> readTasksJson() throws IOException {
        return readJsonFile(BASE_DIRECTORY + File.separator + "tasks.json");
    }
    
    /**
     * Writes to the default tasks JSON file.
     * 
     * @param data The data to write
     * @throws IOException If there is an error writing the file
     */
    public static void writeTasksJson(Map<String, Object> data) throws IOException {
        writeJsonFile(data, BASE_DIRECTORY + File.separator + "tasks.json");
    }

    /**
     * Validates if a file contains valid JSON structure with required fields.
     * 
     * @param file The file to validate
     * @param requiredFields The fields that must be present in the JSON
     * @return true if the structure is valid, false otherwise
     */
    public static boolean isValidJsonStructure(File file, String... requiredFields) {
        if (!file.exists() || file.length() == 0) {
            return false;
        }
        
        try {
            Map<String, Object> content = readJsonFile(file);
            for (String field : requiredFields) {
                if (!content.containsKey(field)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validates if a String contains valid JSON structure with required fields.
     * 
     * @param jsonContent The file to validate
     * @param requiredFields The fields that must be present in the JSON
     * @return true if the structure is valid, false otherwise
     */
    public static boolean isValidJsonStructure(String jsonContent, String... requiredFields) {
        if (jsonContent.isEmpty() || jsonContent.length() == 0) {
            return false;
        }
        
        try {
            Map<String, Object> content = fromJsonString(jsonContent);
            for (String field : requiredFields) {
                if (!content.containsKey(field)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Retrieves the data array from a task JSON structure.
     * 
     * @param tasksJson The tasks JSON structure
     * @return The data array as a List of Lists
     */
    @SuppressWarnings("unchecked")
    public static List<List<Object>> getTasksData(Map<String, Object> tasksJson) {
        return (List<List<Object>>) tasksJson.get("data");
    }
    
    /**
     * Updates the last sync timestamp in the tasks JSON file.
     * 
     * @param timestamp The new timestamp
     * @throws IOException If there is an error updating the file
     */
    public static void updateLastSync(String timestamp) throws IOException {
        Map<String, Object> tasksJson = readTasksJson();
        tasksJson.put("last_sync", timestamp);
        writeTasksJson(tasksJson);
    }
    
    /**
     * Gets the ObjectMapper instance for custom configurations if needed.
     * 
     * @return The ObjectMapper instance
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * Builds a JSON structure for tasks.
     * 
     * @param taskStream The stream of tasks to include in the JSON structure
     * @return A Map representing the JSON structure
     * @throws IOException If there is an error building the structure
     */
    public static Map<String, Object> buildJsonStructure(Stream<Task> taskStream) {
        Map<String, Object> jsonbStructure = new LinkedHashMap<>();
        jsonbStructure.put("columns", List.of("folder_id", "folder_name", "task_id", "task_title", "description", "sync_status", "status", "due_date", "created_at"));

        List<List<Object>> data = taskStream.map(task -> {
            List<Object> taskData = new ArrayList<>();
            taskData.add(task.getFolder_id());
            taskData.add(task.getFolder_name());
            taskData.add(task.getTask_id());
            taskData.add(task.getTitle());
            taskData.add(task.getDescription());
            taskData.add(task.getSync_status());
            taskData.add(task.getStatus());
            taskData.add(task.getDue_date() != null ? task.getDue_date().toString() : null);
            taskData.add(task.getCreated_at() != null ? task.getCreated_at().toString() : null);
            return taskData;
        }).collect(Collectors.toList());

        jsonbStructure.put("data", data);
        jsonbStructure.put("last_sync", null);
        return jsonbStructure;
    }
}