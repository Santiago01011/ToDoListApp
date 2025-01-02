package COMMON;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class UserProperties {
    //private static final String USER_PROPS_FILE = System.getProperty("user.home") + File.separator + ".todolist" + File.separator + "user.yml";
    private static final String USER_PROPS_FILE = "./user.yml";
    private static Map<String, Object> properties;
    
    static {
        initialize();
    }

    private static void initialize() {
        // createDirectory();
        properties = loadProperties();
    }

    // private static void createDirectory() {
    //     try {
    //         Files.createDirectories(Paths.get(System.getProperty("user.home"), ".todolist"));
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    private static Map<String, Object> loadProperties() {
        Yaml yaml = new Yaml();
        try {
            if (Files.exists(Paths.get(USER_PROPS_FILE))) {
                return yaml.load(new FileInputStream(USER_PROPS_FILE));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static void saveProperties() {
        try {
            Yaml yaml = new Yaml();
            yaml.dump(properties, new FileWriter(USER_PROPS_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setProperty(String key, Object value) {
        properties.put(key, value);
        saveProperties();
    }

    public static Object getProperty(String key) {
        return properties.get(key);
    }
}