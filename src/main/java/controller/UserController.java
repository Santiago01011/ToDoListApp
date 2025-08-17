package controller;

import java.util.Map;

import COMMON.UserProperties;
import UI.LoginFrame;
import model.TaskHandlerV2;
import DBH.DBHandler;
import UI.TaskDashboardFrame;
import service.APIService;

public class UserController {

    private String userUUID;
    private boolean keepLoggedIn = Boolean.valueOf((String) UserProperties.getProperty("rememberMe"));
    private String username = (String) UserProperties.getProperty("username");
    private String password = (String) UserProperties.getProperty("password");
    private String userEmail;

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
        try{
            Map<String, Object> response = APIService.login(username, password);
            if (response != null) {
                String token = (String) response.get("token");
                userUUID = (String) response.get("userId");
                UserProperties.setProperty("username", username);
                UserProperties.setProperty("password", password);
                UserProperties.setProperty("token", token);
                UserProperties.setProperty("userUUID", userUUID);
                return true;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return false;
    }

    public boolean doRegister() {
        try {
            boolean success = APIService.register(username, userEmail, password);
            return success;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public void launchDashboard(LoginFrame loginFrame) {
        // Use TaskHandlerV2 instead of deprecated TaskHandler
        TaskHandlerV2 taskHandlerV2 = new TaskHandlerV2(userUUID);
        DBHandler dbHandler = new DBHandler();
        dbHandler.setUserUUID(userUUID);
        
        TaskDashboardFrame dashboard = new TaskDashboardFrame("TaskFlow");
        TaskController controller = new TaskController(taskHandlerV2, dashboard, dbHandler);
        // Set user UUID for sync service
        controller.setUserUUID(userUUID);

        // Register a JVM shutdown hook to ensure tasks and folder metadata are persisted on exit.
        // This guarantees the authoritative TaskHandlerV2 state is saved even if the app is closed
        // from the OS or the window manager.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                taskHandlerV2.saveTasksToJson();
                System.out.println("TaskHandlerV2: saved tasks on shutdown.");
            } catch (Exception e) {
                System.err.println("Failed to save tasks on shutdown: " + e.getMessage());
            }
        }));
        

        dashboard.setController(controller);
        dashboard.initialize();
        dashboard.setVisible(true);
        dashboard.refreshTaskListDisplay();
        loginFrame.dispose();
    }
}
