package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskHandler {
    private static final String TASKS_JSON_FILE = System.getProperty("user.home") + File.separator + ".todoapp" + File.separator + "tasks.json";
    public List<Task> userTasksList;

    public TaskHandler() {
        this.userTasksList = loadTasksFromJson();
    }

    public void addTask(String title, String description, String targetDate, String folder, String userUUID, String status) {
        Task task = new Task.Builder(userUUID)
            .taskTitle(title)
            .description(description)
            .targetDate(targetDate.isEmpty() ? null : LocalDateTime.parse(targetDate))
            .folderName(folder)
            .status(status)
            .build();
        userTasksList.add(task);
    }

    public File prepareSyncJson(String status) {
          return TaskJsonBuilder.buildJsonFile(
               userTasksList.stream().filter(task -> status.equals(task.getStatus())),
               status + "_tasks.json"
          );
     }

    private List<Task> loadTasksFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // Register JavaTimeModule
        File file = new File(TASKS_JSON_FILE);
        if (!file.exists()) {
          TaskJsonBuilder.createEmptyJsonFile(TASKS_JSON_FILE);
          return new ArrayList<>();
        }

        try {
            return mapper.readValue(file, new TypeReference<List<Task>>() {});
        } catch (IOException e) {
            System.err.println("Error loading tasks from JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
