import java.util.UUID;

import DBH.NewDBHandler;
import model.TaskHandler;

public class AppLauncher {
    public static void main(String[] args) {
        //JSONUtils jsonUtils = new JSONUtils();
        TaskHandler taskHandler = new TaskHandler();
        NewDBHandler dbHandler = new NewDBHandler(taskHandler);
        //print the userTasksList to see if its correctly loaded
        //Tests.testTasks(taskHandler);
        //taskHandler.addTask("Test Task", "This is a test task", "pending", "", "TestFolder");
        dbHandler.startSyncProcess(UUID.fromString("01959f92-0d81-78ab-9c17-c180be5d9a37"));
        Tests.printUserTasks(taskHandler);
        taskHandler.saveTasksToJson();
    }
}