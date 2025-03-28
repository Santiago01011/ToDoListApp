package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskHandler {
    private static final String TASKS_JSON_FILE = System.getProperty("user.home") + File.separator + ".todoapp" + File.separator + "tasks.json";
    public List<Task> userTasksList;

    public TaskHandler() {
        this.userTasksList = loadTasksFromJson();
    }

    public void addTask(String title, String description, String status, String targetDate, String folderName, String sync_status) {
        Task task = new Task.Builder(title)
            .description(description)
            .dueDate(targetDate.isEmpty() ? null : LocalDateTime.parse(targetDate))
            .folderName(folderName)
            .status(status)
            .sync_status(sync_status)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        userTasksList.add(task);
    }

    public File prepareSyncJson(String sync_status) {
        return TaskJsonBuilder.buildJsonFile(
            userTasksList.stream().filter(task -> sync_status.equals(task.getSync_status())),
            sync_status + "_tasks.json"
        );
    }

    public File prepareLocalTasksJson(){
        return TaskJsonBuilder.buildJsonFile(
            userTasksList.stream(),
            "tasks.json"
        );
    }

    private List<Task> loadTasksFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        File file = new File(TASKS_JSON_FILE);
        
        ensureJsonFileExists(file, mapper);
        
        try {
            if (isInvalidJsonStructure(file, mapper)) {
                resetJsonFile(file, mapper);
                return new ArrayList<>();
            }
            
            return parseTasksFromJson(file, mapper);
        } catch (IOException e) {
            System.err.println("Error loading tasks from JSON: " + e.getMessage());
            resetJsonFile(file, mapper);
            return new ArrayList<>();
        }
    }

    private boolean isInvalidJsonStructure(File file, ObjectMapper mapper) throws IOException {
        if (file.length() == 0) return true;
        
        try {
            Map<String, List<Map<String, Object>>> wrapper = mapper.readValue(file, 
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});
            return wrapper == null || !wrapper.containsKey("tasks");
        } catch (IOException e) {
            System.err.println("Error reading JSON file, will reset: " + e.getMessage());
            return true;
        }
    }

    private void ensureJsonFileExists(File file, ObjectMapper mapper) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                resetJsonFile(file, mapper);
            } catch (IOException e) {
                System.err.println("Error creating JSON file: " + e.getMessage());
            }
        }
    }

    private void resetJsonFile(File file, ObjectMapper mapper) {
        try {
            mapper.writeValue(file, Map.of("tasks", new ArrayList<Task>()));
        } catch (IOException e) {
            System.err.println("Error resetting JSON file: " + e.getMessage());
        }
    }

    private List<Task> parseTasksFromJson(File file, ObjectMapper mapper) throws IOException {
        Map<String, List<Map<String, Object>>> wrapper = mapper.readValue(file, 
                new TypeReference<Map<String, List<Map<String, Object>>>>() {});
        List<Map<String, Object>> taskMaps = wrapper.get("tasks");
        List<Task> tasks = new ArrayList<>();

        if (taskMaps != null) {
            for (Map<String, Object> taskMap : taskMaps) {
                tasks.add(createTaskFromMap(taskMap));
            }
        }
        
        return tasks;
    }

    private Task createTaskFromMap(Map<String, Object> taskMap) {
        return new Task.Builder((String) taskMap.get("task_title"))
            .folderId((String) taskMap.get("folder_id"))
            .folderName((String) taskMap.get("folder_name"))
            .taskId(taskMap.get("task_id") != null ? (String) taskMap.get("task_id") : null)
            .description((String) taskMap.get("description"))
            .status((String) taskMap.get("status"))
            .sync_status((String) taskMap.get("sync_status"))
            .dueDate(taskMap.get("due_date") != null ? LocalDateTime.parse((String) taskMap.get("due_date")) : null)
            .createdAt(taskMap.get("created_at") != null ? LocalDateTime.parse((String) taskMap.get("created_at")) : null)
            .updatedAt(taskMap.get("updated_at") != null ? LocalDateTime.parse((String) taskMap.get("updated_at")) : null)
            .deletedAt(taskMap.get("deleted_at") != null ? LocalDateTime.parse((String) taskMap.get("deleted_at")) : null)
            .build();
    }
}
