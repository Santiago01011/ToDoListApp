package service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import model.sync.CommandBatch;
import model.sync.SyncResponse;
import COMMON.JSONUtils;
import COMMON.UserProperties;
import model.Folder;

public class APIService {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = (String) UserProperties.getProperty("authApiUrl");

    /**
    * Performs user login and returns the JSON response as a map.
    */
    public static Map<String, Object> login(String username, String password) {
        Map<String, String> creds = new HashMap<>();
        creds.put("username", username);
        //creds.put("email", "");
        creds.put("password", password);
        try {
            String requestBody = JSONUtils.toJsonString(creds);
            HttpResponse<String> response = post("/api/auth/login", requestBody, false);
            if (response.statusCode() == 200) {
                return JSONUtils.fromJsonString(response.body());
            }
            throw ApiExceptionHandler.fromHttpResponse(response, "login");
        } catch (IOException e) {
            throw ApiExceptionHandler.fromIOException(e, "login");
        } catch (InterruptedException e) {
            throw ApiExceptionHandler.fromInterruptedException(e, "login");
        }
    }

    /**
    * Performs user registration and returns true if the response code is 200.
    */
    public static boolean register(String username, String email, String password) {
        Map<String, String> creds = new HashMap<>();
        creds.put("username", username);
        creds.put("email", email);
        creds.put("password", password);
        try {
            String requestBody = JSONUtils.toJsonString(creds);
            HttpResponse<String> response = post("/api/auth/register", requestBody, false);
            return response.statusCode() == 200;
        } catch (IOException e) {
            throw ApiExceptionHandler.fromIOException(e, "register");
        } catch (InterruptedException e) {
            throw ApiExceptionHandler.fromInterruptedException(e, "register");
        }
        //TODO: Throw exception if not 200
    }

    /**
    * Sends a POST request to the given API path, optionally including the stored JWT.
    */
    public static HttpResponse<String> post(String path, String jsonBody, boolean withAuth) throws IOException, InterruptedException {
        if (BASE_URL == null || BASE_URL.trim().isEmpty()) {
            throw ApiExceptionHandler.fromInvalidConfig("authApiUrl is not set");
        }
        if (path == null) path = "";
        if (jsonBody == null) jsonBody = "";

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            if (withAuth) {
                String token = (String) UserProperties.getProperty("token");
                if (token == null || token.trim().isEmpty()) {
                    throw new AuthenticationException("Missing authentication token for request to " + path);
                }
                builder.header("Authorization", "Bearer " + token);
            }
            HttpRequest request = builder.build();
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new APIServiceException("Invalid request parameters for path='" + path + "': " + e.getMessage(), e);
        }
    }

    /**
     * Sends a GET request to the given API path, optionally including the stored JWT.
     */
    public static HttpResponse<String> get(String path, boolean withAuth) throws IOException, InterruptedException {
        if (BASE_URL == null || BASE_URL.trim().isEmpty()) {
            throw ApiExceptionHandler.fromInvalidConfig("authApiUrl is not set");
        }
        if (path == null) path = "";

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .GET();
            if (withAuth) {
                String token = (String) UserProperties.getProperty("token");
                if (token == null || token.trim().isEmpty()) {
                    throw new AuthenticationException("Missing authentication token for request to " + path);
                }
                builder.header("Authorization", "Bearer " + token);
            }
            HttpRequest request = builder.build();
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new APIServiceException("Invalid request parameters for path='" + path + "': " + e.getMessage(), e);
        }
    }

    /**
     * Sends a command batch to the API V2 sync endpoint and returns the sync response.
     */
    public static SyncResponse syncCommands(CommandBatch batch) throws IOException, InterruptedException {
        String requestBody = JSONUtils.toJsonString(batch);
        
        // Debug logging for the request
        System.out.println("APIService: Sending request to /api/v2/sync/commands");
        System.out.println("APIService: Request body: " + requestBody);
        
        HttpResponse<String> response = post("/api/v2/sync/commands", requestBody, true);
        
    // Debug logging for the response
    System.out.println("APIService: Response status: " + response.statusCode());
    System.out.println("APIService: Response headers: " + response.headers().map());
    System.out.println("APIService: Response body: " + response.body());
        
        if (response.statusCode() == 200) {
            SyncResponse syncResponse = JSONUtils.fromJsonString(response.body(), SyncResponse.class);
            System.out.println("APIService: Successfully parsed SyncResponse");
            return syncResponse;
        }
        
        throw new RuntimeException("Sync failed with status " + response.statusCode() + ": " + response.body());
    }

    /**
     * Fetches all tasks for the authenticated user from the server.
     */
    public static List<Map<String, Object>> fetchUserTasks() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/api/v2/tasks", true);
        
        if (response.statusCode() == 200) {
            Map<String, Object> responseData = JSONUtils.fromJsonString(response.body());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) responseData.get("tasks");
            return tasks != null ? tasks : new ArrayList<>();
        }
        
        throw new RuntimeException("Fetch tasks failed with status " + response.statusCode() + ": " + response.body());
    }

    /**
     * Fetch pending notifications for the current user.
     * Tries to parse either { notifications: [...] } or a raw array response.
     */
    public static List<Map<String, Object>> fetchPendingNotifications() throws IOException, InterruptedException {
        // System.out.println("APIService: Fetching pending notifications");
        // HttpResponse<String> response = get("/api/v2/notifications/pending", true);
        // System.out.println("APIService: Notifications response status: " + response.statusCode());
        // System.out.println("APIService: Notifications response body: " + response.body());

        // if (response.statusCode() == 200) {
        //     try {
        //         Map<String, Object> responseData = JSONUtils.fromJsonString(response.body());
        //         @SuppressWarnings("unchecked")
        //         List<Map<String, Object>> notifications = (List<Map<String, Object>>) responseData.get("notifications");
        //         if (notifications != null) return notifications;
        //     } catch (Exception ignored) {
        //     }
        //     try {
        //         @SuppressWarnings("unchecked")
        //         List<Map<String, Object>> notifications = (List<Map<String, Object>>) JSONUtils.fromJsonString(response.body());
        //         return notifications != null ? notifications : new ArrayList<>();
        //     } catch (Exception e) {
        //         System.err.println("APIService: Failed to parse notifications: " + e.getMessage());
        //         return new ArrayList<>();
        //     }
        // }
        // throw new RuntimeException("Fetch notifications failed with status " + response.statusCode() + ": " + response.body());
        return null;
    }

    /**
     * Mark a list of notifications as delivered.
     */
    public static boolean markNotificationsDelivered(List<String> notificationIds) throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("notification_ids", notificationIds);
        String json = COMMON.JSONUtils.toJsonString(body);
        System.out.println("APIService: Acking notifications: " + notificationIds);
        HttpResponse<String> response = post("/api/v2/notifications/ack", json, true);
        System.out.println("APIService: Ack response status: " + response.statusCode());
        System.out.println("APIService: Ack response body: " + response.body());
        return response.statusCode() == 200;
    }

    /**
     * Fetch accessible folders for the authenticated user.
     * Accepts either an array response or an object with a 'folders' array.
     * Supports multiple field naming styles: folder_id|id|folderId and folder_name|name|folderName.
     */
    public static List<Folder> fetchUserFolders() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/api/v2/folders", true);
        if (response.statusCode() != 200) {
            throw new RuntimeException("Fetch folders failed with status " + response.statusCode() + ": " + response.body());
        }

        String body = response.body();
        List<Map<String, Object>> raw;
        try {
            // Try as object with 'folders' key
            Map<String, Object> container = JSONUtils.fromJsonString(body);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> folders = (List<Map<String, Object>>) container.get("folders");
            raw = folders != null ? folders : new ArrayList<>();
            if (raw.isEmpty() && container.containsKey("items")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) container.get("items");
                raw = items != null ? items : new ArrayList<>();
            }
        } catch (Exception ignore) {
            try {
                List<Map<String, Object>> arr = JSONUtils.fromJsonListOfMaps(body);
                raw = arr != null ? arr : new ArrayList<>();
            } catch (Exception e) {
                System.err.println("APIService: Failed to parse folders: " + e.getMessage());
                raw = new ArrayList<>();
            }
        }

        return raw.stream()
            .map(APIService::toFolder)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static Folder toFolder(Map<String, Object> m) {
        if (m == null) return null;
        String id = firstNonNullAsString(m, "folder_id", "id", "folderId");
        String name = firstNonNullAsString(m, "folder_name", "name", "folderName");
        if (id == null && name == null) return null;
        return new Folder.Builder(id != null ? id : name)
            .folderName(name)
            .build();
    }

    private static String firstNonNullAsString(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null) return String.valueOf(v);
        }
        return null;
    }
}