package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public class TaskJsonBuilder {

    private static final String BASE_DIRECTORY = System.getProperty("user.home") + File.separator + ".todoapp";

    public static File buildJsonFile(Stream<Task> taskStream, String fileName) {
        File jsonFile = new File(BASE_DIRECTORY + File.separator + fileName);
        try {
            jsonFile.getParentFile().mkdirs(); // Ensure the directory exists
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule for LocalDateTime support
            mapper.writeValue(jsonFile, taskStream.toList()); // Convert the stream to a list and write to the file
        } catch (IOException e) {
            System.err.println("Error creating JSON file: " + e.getMessage());
        }
        return jsonFile;
    }

    public static void createEmptyJsonFile(String filePath) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // Ensure the directory exists
            file.createNewFile();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule for LocalDateTime support
            mapper.writeValue(file, Stream.<Task>empty().toList()); // Initialize with an empty list
        } catch (IOException e) {
            System.err.println("Error creating empty JSON file: " + e.getMessage());
        }
    }
}