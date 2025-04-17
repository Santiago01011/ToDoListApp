package model;

import COMMON.JSONUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskHandler {
    private static final String TASKS_JSON_FILE = JSONUtils.BASE_DIRECTORY + File.separator + "tasks.json";
    public List<Task> userTasksList;
    private LocalDateTime last_sync = null;

    public LocalDateTime getLastSync() {
        return last_sync;
    }

    public void setLastSync(LocalDateTime last_sync) {
        this.last_sync = last_sync;
    }

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
            task.setTitle(title);
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

    public String prepareSyncJsonContent(String sync_status) {
        var filteredTasks = userTasksList.stream()
            .filter(task -> sync_status.equals(task.getSync_status()))
            .toList();
    
        if (filteredTasks.isEmpty()) return null;

        try {
            Map<String, Object> jsonbStructure = JSONUtils.buildJsonStructure(filteredTasks.stream());  
            jsonbStructure.put("last_sync", getLastSync());        
            return JSONUtils.toJsonString(jsonbStructure);
        } catch (IOException e) {
            System.err.println("Error creating sync JSON content: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Task> loadTasksFromJson() {
        
        File file = new File(TASKS_JSON_FILE);

        try{
            if( !JSONUtils.isValidJsonStructure(file, "columns", "data", "last_sync") ){
                JSONUtils.createEmptyJsonFile(TASKS_JSON_FILE);
                System.err.println("Invalid JSON structure in file: " + TASKS_JSON_FILE + ". Creating a new file and retrieving from cloud.");
                return new ArrayList<>();
            }
            Map<String, Object> wrapper = JSONUtils.readJsonFile(file);
            List<Task> tasks = new ArrayList<>();
            List<String> columns = (List<String>) wrapper.get("columns");
            List<List<Object>> data = (List<List<Object>>) wrapper.get("data");
            setLastSync(LocalDateTime.parse((String) wrapper.get("last_sync")));
            
            if (data != null) {
                for (List<Object> row : data) {
                    tasks.add(createTaskFromRow(columns, row));
                }
            }

            return tasks;
        } catch (IOException e) {
            System.err.println("Error loading tasks from JSON: " + e.getMessage());
            JSONUtils.createEmptyJsonFile(TASKS_JSON_FILE);
            return new ArrayList<>();
        }  
    }

    public Task createTaskFromRow(List<String> columns, List<Object> row) {
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
            .updatedAt(getLastSync())
            .build();
    }

    /**
     * Saves the current tasks to the JSON file.
     */
    public void saveTasksToJson() {
        try {
            Map<String, Object> jsonbStructure = JSONUtils.buildJsonStructure(userTasksList.stream());
            jsonbStructure.put("last_sync", getLastSync());
            JSONUtils.writeJsonFile(jsonbStructure, TASKS_JSON_FILE);
        } catch (IOException e) {
            System.err.println("Error saving tasks to JSON: " + e.getMessage());
        }
    }

}