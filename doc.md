# Documentation for Task Management System

## Overview
This documentation provides an overview of the key components of the Task Management System, including the `Task`, `TaskHandler`, `TaskJsonBuilder`, and `DBHandler` classes. These classes are responsible for managing tasks, handling JSON serialization/deserialization, database synchronization, and providing utility methods for task operations.

---

## `Task.java`
The `Task` class represents a task in the To-Do List application. It includes attributes, a builder pattern for object creation, and utility methods.

### Attributes
- **`task_id`**: Unique identifier for the task.
- **`task_title`**
- **`description`**
- **`status`**: Current status of the task (e.g., "pending", "completed", "in_progress").
- **`sync_status`**: Sync status of the task (e.g., "new", "update", "sync").
- **`due_date`**
- **`created_at`**: Time when the task was created.
- **`updated_at`**: Time when the task was last updated.
- **`deleted_at`**: Time when the task was deleted.
- **`last_sync`**: Time of the last sync operation.
- **`folder_id`**: Identifier for the folder associated to the task.
- **`folder_name`**

### Builder Pattern

The `Task` class implements a Builder pattern that allows for flexible and dynamic task creation. It provides a clear and readable way to construct objects without needing multiple constructors or static factory methods.

#### Example:
```java
Task task = new Task.Builder("Task Title")
    .description("Task Description")
    .status("pending")
    .dueDate(LocalDateTime.now())
    .build();
```

### Utility Method
- **`viewTaskDesc()`**: Returns a string representation of the task's details.

---

## `TaskHandler.java`
The `TaskHandler` class manages the list of tasks and provides methods for adding, loading, and preparing tasks for synchronization.

### Attributes
- **`userTasksList`**: A list of `Task` objects representing the user's tasks in memory.

### Methods
- **`addTask(String title, String description, String status, String targetDate, String folderName, String sync_status)`**:
  Adds a new task to the `userTasksList` using the `Task.Builder`.

- **`prepareSyncJson(String sync_status)`**:
  Filters tasks by their `sync_status` and creates a JSON file for synchronization.

- **`prepareLocalTasksJson()`**:
  Saves all tasks in `userTasksList` to a local JSON file, to enable a offline mode in the next iteration.

- **`loadTasksFromJson()`**:
  Loads tasks from a JSON file. Ensures the file exists and resets it if the structure is invalid.

### Helper Methods
- **`isInvalidJsonStructure(File file, ObjectMapper mapper)`**: Checks if the JSON file has a valid structure.
- **`ensureJsonFileExists(File file, ObjectMapper mapper)`**: Ensures the JSON file exists and creates it if necessary.
- **`resetJsonFile(File file, ObjectMapper mapper)`**: Resets the JSON file to an empty structure.
- **`parseTasksFromJson(File file, ObjectMapper mapper)`**: Parses tasks from the JSON file.
- **`createTaskFromMap(Map<String, Object> taskMap)`**: Creates a `Task` object from a map of attributes.

---

## `TaskJsonBuilder.java`
The `TaskJsonBuilder` class handles the creation and management of JSON files for tasks.

### Attributes
- **`BASE_DIRECTORY`**: The base directory for storing JSON files.

### Methods
- **`buildJsonFile(Stream<Task> taskStream, String fileName)`**:
  Creates a JSON file from a stream of tasks. Each task is serialized into a map of attributes.

- **`createEmptyJsonFile(String filePath)`**:
  Creates an empty JSON file with a "tasks" wrapper.

### Usage
Used by `TaskHandler` to save tasks locally or prepare them for synchronization.

---

## `DBHandler.java`
The `DBHandler` class manages the interaction between the application and the database, providing methods for task synchronization and data persistence.

### Key Methods

#### Synchronization Methods
- **`syncTasks(String userUuid, String insertJsonContent, String updateJsonContent, List<Task> taskList)`**:
  Coordinates the synchronization of tasks between the local application and the database. It processes new tasks for insertion, updates existing tasks, and refreshes the local task list with the latest data from the database.
  
  This method is critical for maintaining consistency between the local and cloud versions of tasks. It ensures that:
  1. New tasks are inserted into the database
  2. Updated tasks are reflected in the database
  3. Local tasks are refreshed with the latest database state
  4. Task sync statuses are properly maintained

- **`startSyncProcess(TaskHandler taskHandler, String user_id)`**:
  Initiates and manages the entire synchronization process. It handles both first-time syncs (when the local task list is empty) and regular syncs. The method:
  1. Checks if this is a first-time sync
  2. Retrieves tasks from the database
  3. Merges local and cloud tasks intelligently
  4. Prepares JSON content for synchronization
  5. Updates the local task list after sync
  
  This method serves as the main entry point for synchronization operations, orchestrating the entire process.

#### Database Interaction Methods
- **`insertTasksFromJSON(UUID userUUID, String jsonContent, List<Task> taskList)`**:
  Inserts new tasks into the database from a JSON string and updates the local task list with the new database IDs.

- **`updateTasksFromJSON(UUID userUUID, String jsonContent, List<Task> taskList)`**:
  Updates existing tasks in the database with changes from a JSON string.

- **`retrieveTasksFromDB(UUID userUUID)`**:
  Retrieves all tasks for a user from the database. This method handles the parsing of JSON results from the database query into Task objects.

#### Helper Methods
- **`updateTaskListFromJson(JsonNode successItem, List<Task> taskList)`**:
  Updates the local task list based on JSON data from database operations.

- **`setTaskField(Task task, String fieldName, JsonNode dataRow, Map<String, Integer> columnMap)`**:
  Safely sets string field values on a Task object from database results.

- **`setDateField(Task task, String fieldName, JsonNode dataRow, Map<String, Integer> columnMap)`**:
  Safely sets date field values on a Task object from database results.

- **`mergeTasks(List<Task> localTasks, List<Task> cloudTasks)`**:
  Intelligently merges local and cloud task lists, resolving conflicts based on sync status and timestamps.
  
  This method implements a sophisticated conflict resolution strategy that:
  1. Prioritizes tasks marked as "updated"
  2. Handles tasks with different sync statuses appropriately
  3. Uses timestamps to determine which version is newer
  4. Maintains task identity across local and cloud versions

### Importance
The `DBHandler` class is essential for:
1. **Data Persistence**: Ensuring tasks are stored reliably in the database
2. **Synchronization**: Keeping local and cloud data consistent
3. **Conflict Resolution**: Intelligently resolving conflicts between local and cloud versions
4. **Offline Support**: Enabling the application to work offline and sync when connectivity is restored

---

## Example Usage
The following example demonstrates how to use the `TaskHandler` and its methods:

```java
TaskHandler taskHandler = new TaskHandler();

// Add tasks
taskHandler.addTask("Buy groceries", "Milk, bread, eggs", "pending", "2025-01-10T10:00", "Personal", "new");
taskHandler.addTask("Finish project", "Complete the final report", "completed", "2025-01-15T15:00", "Work", "update");
taskHandler.addTask("Call mom", "Check in and say hi", "completed", "2025-01-12T18:00", "Default", "new");

// Prepare JSON files for sync
taskHandler.prepareSyncJson("new");
taskHandler.prepareSyncJson("update");

// Save all tasks to a local JSON file
taskHandler.prepareLocalTasksJson();

// Print tasks in memory for verification
System.out.println("\nTasks in memory:");
taskHandler.userTasksList.forEach(task -> System.out.println(task.viewTaskDesc()));
```

### Synchronization Example
The following example demonstrates how to use the `DBHandler` with `TaskHandler` for synchronization:

```java
// Initialize task handler and load local tasks
TaskHandler taskHandler = new TaskHandler();
taskHandler.loadTasksFromJson();

// Initialize database handler
DBHandler dbHandler = new DBHandler();

// Perform synchronization
dbHandler.startSyncProcess(taskHandler, userUUID.toString());

// After sync, all changes are reflected in taskHandler.userTasksList
// and also saved to the local JSON file
```