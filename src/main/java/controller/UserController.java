package controller;

import java.util.Map;

import COMMON.UserProperties;
import UI.LoginFrame;
import model.TaskHandler;
import model.TaskHandlerV2;
import DBH.DBHandler;
import UI.TaskDashboardFrame;
import controller.TaskController;
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
    }    public void launchDashboard(LoginFrame loginFrame) {
        TaskHandlerV2 taskHandlerV2 = new TaskHandlerV2(userUUID, true); // Enable command queue by default
        TaskHandler taskHandler = taskHandlerV2.getLegacyHandler(); // Get legacy handler for backward compatibility
        DBHandler dbHandler = new DBHandler(taskHandler);
        dbHandler.setUserUUID(userUUID);
        dbHandler.startSyncProcess();
        TaskDashboardFrame dashboard = new TaskDashboardFrame("TaskFlow");
        TaskController controller = new TaskController(taskHandler, dashboard, dbHandler);
        dashboard.setController(controller);
        dashboard.initialize();
        dashboard.setVisible(true);
        dashboard.refreshTaskListDisplay();
        loginFrame.dispose();
    }
}
