package COMMON;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class UserProperties {
    private static final String USER_PROPS_FILE = System.getProperty("user.home") + File.separator + ".todoapp" + File.separator + "user.yml";
    private static Map<String, Object> properties = new HashMap<>();

    static {
        initialize();
    }

    private static void initialize() {
        createDirectory();
        properties = loadProperties();
        if (properties.isEmpty()) {
            createDefaultProperties();
        }
    }

    private static void createDirectory() {
        try {
            Files.createDirectories(Paths.get(System.getProperty("user.home"), ".todoapp"));
        } catch (IOException e) {
            handleError("Error creating directory", e);
        }
    }

    private static void createDefaultProperties() {
        properties.put("darkTheme", "false");
        properties.put("rememberMe", "false");
        properties.put("username", "");
        properties.put("password", "");
        properties.put("lastSession", "");
        properties.put("dbUrl", "");
        saveProperties();
    }

    private static Map<String, Object> loadProperties() {
        Yaml yaml = new Yaml();
        try {
            if (Files.exists(Paths.get(USER_PROPS_FILE))) {
                try (InputStream inputStream = new FileInputStream(USER_PROPS_FILE)) {
                    return yaml.load(inputStream);
                }
            }
        } catch (IOException e) {
            handleError("Error loading properties file", e);
        }
        return new HashMap<>();
    }

    public static void saveProperties() {
        try (Writer writer = new FileWriter(USER_PROPS_FILE)) {
            Yaml yaml = new Yaml();
            yaml.dump(properties, writer);
        } catch (IOException e) {
            handleError("Error saving properties file", e);
        }
    }

    public static void setProperty(String key, Object value) {
        properties.put(key, value);
        saveProperties();
    }

    public static Object getProperty(String key) {
        return properties.get(key);
    }

    public static void logOut() {
        setProperty("rememberMe", "false");
        setProperty("username", "");
        setProperty("password", "");
        setProperty("lastSession", "");
    }

    private static void handleError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }
}