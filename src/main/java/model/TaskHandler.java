package model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import DBH.DBHandler;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskHandler {
    private static final String TASKS_JSON_FILE = System.getProperty("user.home") + File.separator + ".todoapp" + File.separator + "tasks.json";
    private List<Task> tasks;
    private final DBHandler dbHandler;

    public TaskHandler(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
        this.tasks = loadTasksFromJson();
    }

    // Agregar una tarea a la lista en memoria
    public void addTask(String title, String description, String targetDate, String folder, int userId) {
        Task task = new Task.Builder(userId)
            .taskTitle(title)
            .description(description)
            .targetDate(targetDate.isEmpty() ? null : LocalDateTime.parse(targetDate))
            .folderName(folder)
            .build();
        tasks.add(task);
        saveTasksToJson(); // Guarda las tareas locales
    }

    // Sincronizar tareas locales con la base de datos
    public void sendTasksToDatabase() {
        List<Task> tasksToSend = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getId() == 0) { // Tareas sin UUID
                tasksToSend.add(task);
            }
        }

        if (!tasksToSend.isEmpty()) {
            String jsonToSend = tasksToJson(tasksToSend);
            dbHandler.insertTasksFromJSON(String.valueOf(tasksToSend.get(0).getUserId()), jsonToSend);

            // Actualizar las tareas con UUIDs generados por la base de datos
            for (Task task : tasksToSend) {
                task.setId(generateUUID()); // Simula la asignación de un UUID
            }
            saveTasksToJson(); // Guarda los cambios en el archivo JSON
        }
    }

    // Cargar tareas desde el archivo JSON
    private List<Task> loadTasksFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(TASKS_JSON_FILE);
        if (!file.exists()) {
            createEmptyJsonFile();
            return new ArrayList<>();
        }

        try {
            return mapper.readValue(file, new TypeReference<List<Task>>() {});
        } catch (IOException e) {
            System.err.println("Error al cargar tareas desde JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Guardar tareas en el archivo JSON
    private void saveTasksToJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(TASKS_JSON_FILE), tasks);
        } catch (IOException e) {
            System.err.println("Error al guardar tareas en JSON: " + e.getMessage());
        }
    }

    // Crear un archivo JSON vacío si no existe
    private void createEmptyJsonFile() {
        try {
            File file = new File(TASKS_JSON_FILE);
            file.getParentFile().mkdirs();
            file.createNewFile();
            saveTasksToJson();
        } catch (IOException e) {
            System.err.println("Error al crear el archivo JSON: " + e.getMessage());
        }
    }

    // Convertir una lista de tareas a JSON
    private String tasksToJson(List<Task> tasks) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(tasks);
        } catch (IOException e) {
            System.err.println("Error al convertir tareas a JSON: " + e.getMessage());
            return "[]";
        }
    }

    // Simular la generación de un UUID
    private int generateUUID() {
        return (int) (Math.random() * 100000); // Simula un UUID único
    }
}
