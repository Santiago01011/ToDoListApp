import model.Task;
import model.TaskHandler;

    public class Tests {
        public static void testTasks(TaskHandler taskHandler) {
            // create some tasks with delay between each to get different creation timestamps
            taskHandler.addTask("Complete project documentation", "Write final docs for Java project", "pending", "2023-12-15T17:00:00", "Work");
            try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
            
            taskHandler.addTask("Buy groceries", "Milk, bread, eggs", "pending", "2025-11-30T10:00:00", "Personal");
            try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
            
            taskHandler.addTask("Schedule dentist appointment", "Call Dr. Smith's office", "pending", "", "Health");
            try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
            
            taskHandler.addTask("Prepare presentation", "Create slides for team meeting", "in_progress", "2025-12-05T09:00:00", "Work");
            try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
            
            taskHandler.addTask("Pay utility bills", "Electricity and water", "fail", "2025-11-28T23:59:59", "Finance");

            // get the last task to update it before sync
            Task lastTask = taskHandler.userTasksList.get(taskHandler.userTasksList.size() - 1);
            taskHandler.updateTask(lastTask, null, null, "pending", null, null, null);
        }


        public static void testUpdateTask(TaskHandler taskHandler) {
            // get an existing task to update, for example, the last one in the list
            Task taskToUpdate = taskHandler.userTasksList.get(taskHandler.userTasksList.size() - 1);
            // update the task with new values
            taskHandler.updateTask(taskToUpdate, "Test update 2", null, null, null, null, null);
        }


        public static void printUserTasks(TaskHandler taskHandler) {
            System.out.println("User Tasks:");
            for (Task task : taskHandler.userTasksList) {
                System.out.println(task.viewTaskDesc() + "\n");
            }
        }
}
