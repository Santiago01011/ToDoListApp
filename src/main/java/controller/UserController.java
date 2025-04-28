package controller;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import COMMON.JSONUtils;
import COMMON.UserProperties;
import UI.LoginFrame;
import model.TaskHandler;
import DBH.NewDBHandler;
import UI.TaskDashboardFrame;
import controller.TaskController;

public class UserController {

    private String userUUID;
    private boolean keepLoggedIn = Boolean.valueOf((String) UserProperties.getProperty("rememberMe"));
    private String username = (String) UserProperties.getProperty("username");
    private String password = (String) UserProperties.getProperty("password");
    private String authUrl = (String) UserProperties.getProperty("authApiUrl");
    private String userEmail;

    public UserController() {

    }

    public boolean getKeepLoggedIn() {
        return keepLoggedIn;
    }

    public void setKeepLoggedIn(boolean keepLoggedIn) {
        this.keepLoggedIn = keepLoggedIn;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }
    
    // Allow setting password from login UI
    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean doLogin() {
        try {
            Map<String, String> creds = new HashMap<>();
            creds.put("username", username);
            creds.put("email", "");
            creds.put("password", password);
            String requestBody = JSONUtils.toJsonString(creds);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Map<String, Object> respMap = JSONUtils.fromJsonString(response.body());
                userUUID = (String) respMap.get("userId");
                String token = (String) respMap.get("token");
                UserProperties.setProperty("token", token);
                UserProperties.setProperty("userUUID", userUUID);
                UserProperties.setProperty("username", username);
                UserProperties.setProperty("password", password);
                return true;
            } else {
                System.err.println("Login failed: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return false;
    }

    public void launchDashboard(LoginFrame loginFrame) {
        TaskHandler taskHandler = new TaskHandler();
        NewDBHandler dbHandler = new NewDBHandler(taskHandler);
        dbHandler.setUserUUID(userUUID);
        dbHandler.startSyncProcess();
        TaskDashboardFrame dashboard = new TaskDashboardFrame("TaskFlow");
        TaskController controller = new TaskController(taskHandler, dashboard, dbHandler);
        dashboard.setController(controller);
        dashboard.initialize();
        dashboard.setVisible(true);
        dashboard.refreshTaskListDisplay(taskHandler.userTasksList);
        loginFrame.dispose();
    }
}
