import java.util.UUID;

import DBH.NewDBHandler;
import model.TaskHandler;
// Import FlatDarkLaf
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import UI.TaskDashboardFrame; // Import the new frame

public class AppLauncher {
    public static void main(String[] args) {
        // Run the rest of the application on the EDT
        SwingUtilities.invokeLater(() -> {
            TaskHandler taskHandler = new TaskHandler();
            NewDBHandler dbHandler = new NewDBHandler(taskHandler);

            // Perform initial sync or load tasks before showing UI
            // Consider doing DB operations in a background thread (SwingWorker)
            // For now, doing it directly:
            dbHandler.startSyncProcess(UUID.fromString("01959f92-0d81-78ab-9c17-c180be5d9a37")); // Example UUID

            // Launch the new Task Dashboard Frame, passing the TaskHandler
            new TaskDashboardFrame("TaskFlow", taskHandler); // Title set to TaskFlow

        });
    }
}