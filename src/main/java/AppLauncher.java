import java.util.UUID;

import DBH.NewDBHandler;
import model.TaskHandler;
import javax.swing.SwingUtilities;
import UI.TaskDashboardFrame;
import controller.TaskController;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskHandler taskHandler = new TaskHandler();
            //Tests.testTasks(taskHandler);
            NewDBHandler dbHandler = new NewDBHandler(taskHandler);
            TaskDashboardFrame view = new TaskDashboardFrame("TaskFlow");
            TaskController controller = new TaskController(taskHandler, view, dbHandler);

            view.setController(controller);
            dbHandler.setUserUUID("01959f92-0d81-78ab-9c17-c180be5d9a37");

            view.initialize();
            view.setVisible(true);
        });
    }
}