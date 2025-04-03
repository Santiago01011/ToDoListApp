package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskHandler {
    private static final String TASKS_JSON_FILE = System.getProperty("user.home") + File.separator + ".todoapp" + File.separator + "tasks.json";
    public List<Task> userTasksList;

    public TaskHandler() {
        this.userTasksList = loadTasksFromJson();
    }

    public void addTask(String title, String description, String status, String targetDate, String folderName) {
        String id = UUID.randomUUID().toString();
        Task task = new Task.Builder(id)
            .taskTitle(title)
            .description(description)
            .dueDate(targetDate.isEmpty() ? null : LocalDateTime.parse(targetDate))
            .folderName(folderName)
            .status(status)
            .sync_status("new")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        userTasksList.add(task);
    }

    public void updateTask(Task task, String title, String description, String status, String targetDate, String folderName) {
        if (task.getSync_status().equals("new")) {
            // Only update the task in the user list - still hasn't been sent to the database
            updateTaskFields(task, title, description, status, targetDate, folderName);
            task.setUpdated_at(LocalDateTime.now());
            return;
        }
        
        if (task.getSync_status().equals("cloud")) {
            // Task is from the cloud, create an updated version to send back
            // First change the original to "local" status
            task.setSync_status("local");
            task.setUpdated_at(LocalDateTime.now());
            
            // Then create a copy with only the necessary fields for update
            Task updatedTask = new Task.Builder(task.getTask_id())
                .folderId(task.getFolder_id())
                .folderName(task.getFolder_name())
                .sync_status("updated")
                .updatedAt(LocalDateTime.now())
                .build();
            
            // Update both tasks with the new field values
            updateTaskFields(task, title, description, status, targetDate, folderName);
            updateTaskFields(updatedTask, title, description, status, targetDate, folderName);
            
            userTasksList.add(updatedTask);
            return;
        }
        
        if (task.getSync_status().equals("local")) {
            // Task is already modified locally, find its update twin if exists
            Task updatedTask = userTasksList.stream()
                .filter(t -> t.getTask_id().equals(task.getTask_id()) && "updated".equals(t.getSync_status()))
                .findFirst()
                .orElse(null);
            
            // If no update twin exists, create one
            if (updatedTask == null) {
                updatedTask = new Task.Builder(task.getTask_id())
                    .folderId(task.getFolder_id())
                    .folderName(task.getFolder_name())
                    .sync_status("updated")
                    .updatedAt(LocalDateTime.now())
                    .build();
                userTasksList.add(updatedTask);
            }
            
            // Update both tasks with the new field values
            updateTaskFields(task, title, description, status, targetDate, folderName);
            updateTaskFields(updatedTask, title, description, status, targetDate, folderName);
            
            task.setUpdated_at(LocalDateTime.now());
            updatedTask.setUpdated_at(LocalDateTime.now());
        }
    }
    
    // Helper method to update task fields
    private void updateTaskFields(Task task, String title, String description, String status, String targetDate, String folderName) {
        if (title != null && !title.isEmpty()) {
            task.setTask_title(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (status != null && !status.isEmpty()) {
            task.setStatus(status);
        }
        if (targetDate != null) {
            task.setDue_date(targetDate.isEmpty() ? null : LocalDateTime.parse(targetDate));
        }
        if (folderName != null) {
            task.setFolder_name(folderName);
        }
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

    public String prepareSyncJsonContent(String sync_status) {
        var filteredTasks = userTasksList.stream()
        .filter(task -> sync_status.equals(task.getSync_status()))
        .toList();
    
        if (filteredTasks.isEmpty()) return null;

        return TaskJsonBuilder.buildJsonContent(filteredTasks.stream());
    }

    public String prepareLocalTasksJsonContent() {
        return TaskJsonBuilder.buildJsonContent(
            userTasksList.stream()
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
            Map<String, Object> wrapper = mapper.readValue(file, 
                    new TypeReference<Map<String, Object>>() {});
            return wrapper == null || !wrapper.containsKey("columns") || !wrapper.containsKey("data");
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
            mapper.writeValue(file, Map.of("columns", new ArrayList<String>(), "data", new ArrayList<List<Object>>()));
        } catch (IOException e) {
            System.err.println("Error resetting JSON file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Task> parseTasksFromJson(File file, ObjectMapper mapper) throws IOException {
        Map<String, Object> wrapper = mapper.readValue(file, new TypeReference<Map<String, Object>>() {});

        List<String> columns = (List<String>) wrapper.get("columns");
        List<List<Object>> data = (List<List<Object>>) wrapper.get("data");
        List<Task> tasks = new ArrayList<>();

        if (data != null) {
            for (List<Object> row : data) {
                tasks.add(createTaskFromRow(columns, row));
            }
        }

        return tasks;
    }

    private Task createTaskFromRow(List<String> columns, List<Object> row) {
        Map<String, Object> taskMap = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            taskMap.put(columns.get(i), row.get(i));
        }

        return new Task.Builder((String) taskMap.get("task_id"))
            .taskTitle((String) taskMap.get("task_title"))
            .folderId((String) taskMap.get("folder_id"))
            .folderName((String) taskMap.get("folder_name"))
            .description((String) taskMap.get("description"))
            .sync_status((String) taskMap.get("sync_status"))
            .status((String) taskMap.get("status"))
            .dueDate(taskMap.get("due_date") != null ? LocalDateTime.parse((String) taskMap.get("due_date")) : null)
            .createdAt(taskMap.get("created_at") != null ? LocalDateTime.parse((String) taskMap.get("created_at")) : null)
            .build();
    }
}
