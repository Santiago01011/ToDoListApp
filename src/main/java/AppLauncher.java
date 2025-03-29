import model.TaskHandler;
import DBH.DBHandler;



public class AppLauncher {
    public static void main(String[] args) {

        TaskHandler taskHandler = new TaskHandler();
        DBHandler dbHandler = new DBHandler();

        /* Test the task creation and JSON preparation */
        //testTasks(taskHandler);
        /* Test sync process */
        dbHandler.startSyncProcess(taskHandler);
    }

    public static void testTasks(TaskHandler taskHandler) {
                // create some tasks with delay between each to get different creation timestamps
                taskHandler.addTask("Complete project documentation", "Write final docs for Java project", "pending", "2023-12-15T17:00:00", "Work");
                try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
                
                taskHandler.addTask("Buy groceries", "Milk, bread, eggs", "pending", "2023-11-30T10:00:00", "Personal");
                try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
                
                taskHandler.addTask("Schedule dentist appointment", "Call Dr. Smith's office", "pending", "", "Health");
                try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
                
                taskHandler.addTask("Prepare presentation", "Create slides for team meeting", "in_progress", "2023-12-05T09:00:00", "Work");
                try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
                
                taskHandler.addTask("Pay utility bills", "Electricity and water", "fail", "2023-11-28T23:59:59", "Finance");
        
    }
}