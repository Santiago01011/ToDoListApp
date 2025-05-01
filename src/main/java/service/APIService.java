package service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import COMMON.UserProperties;
import COMMON.JSONUtils;

public class APIService {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = (String) UserProperties.getProperty("authApiUrl");

    /**
    * Performs user login and returns the JSON response as a map.
    */
    public static Map<String, Object> login(String username, String password) throws IOException, InterruptedException {
        Map<String, String> creds = new HashMap<>();
        creds.put("username", username);
        creds.put("email", "");
        creds.put("password", password);
        String requestBody = JSONUtils.toJsonString(creds);
        HttpResponse<String> response = post("/api/auth/login", requestBody, false);
        if (response.statusCode() == 200) {
            return JSONUtils.fromJsonString(response.body());
        }
        throw new RuntimeException("Login failed: " + response.body());
    }

    /**
    * Performs user registration and returns true if the response code is 200.
    */
    public static boolean register(String username, String email, String password) throws IOException, InterruptedException {
        Map<String, String> creds = new HashMap<>();
        creds.put("username", username);
        creds.put("email", email);
        creds.put("password", password);
        String requestBody = JSONUtils.toJsonString(creds);
        HttpResponse<String> response = post("/api/auth/register", requestBody, false);
        return response.statusCode() == 200;
        //TODO: Throw exception if not 200
    }

    /**
    * Sends a POST request to the given API path, optionally including the stored JWT.
    */
    public static HttpResponse<String> post(String path, String jsonBody, boolean withAuth) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (withAuth) {
            String token = (String) UserProperties.getProperty("token");
            builder.header("Authorization", "Bearer " + token);
        }
        HttpRequest request = builder.build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
