# Documentation for Task Management System

## Overview
This documentation provides an overview of the key components of the Task Management System, including the `Task`, `TaskHandler`, `JSONUtils`, and `DBHandler` classes. These classes are responsible for managing tasks, handling JSON serialization/deserialization, database synchronization, and providing utility methods for task operations.

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
- **`last_sync`**: A timestamp of the last synchronization with the database.

### Methods
- **`addTask(String title, String description, String status, String targetDate, String folderName)`**:
  Adds a new task to the `userTasksList` using the `Task.Builder`.

- **`updateTask(Task task, String title, String description, String status, String targetDate, String folderName)`**:
  Updates a task's fields and manages its sync status. The behavior varies based on the task's current sync status:
  - For "new" tasks: Simply updates the fields.
  - For "cloud" tasks: Creates an updated version for syncing and changes original to "local".
  - For "local" tasks: Updates both the local task and its update twin.

- **`prepareSyncJson(String sync_status)`**:
  Filters tasks by their `sync_status` and creates a JSON file for synchronization using `JSONUtils`.

- **`prepareSyncJsonContent(String sync_status)`**:
  Generates a JSON string representation of tasks filtered by sync status.

- **`saveTasksToJson()`**:
  Saves all tasks in `userTasksList` to the local JSON file, to enable offline mode.

- **`loadTasksFromJson()`**:
  Loads tasks from a JSON file. Validates the structure and creates an empty file if needed.

### Helper Methods
- **`updateTaskFields(Task task, String title, String description, String status, String targetDate, String folderName)`**: Updates task fields if the new values are not null.
- **`createTaskFromRow(List<String> columns, List<Object> row)`**: Creates a `Task` object from column names and a row of values.

---

## `JSONUtils.java`
The `JSONUtils` class provides utility methods for JSON file operations, replacing the previous `TaskJsonBuilder` class with a more comprehensive solution.

### Attributes
- **`BASE_DIRECTORY`**: The base directory for storing JSON files.
- **`MAPPER`**: A preconfigured Jackson ObjectMapper for JSON serialization/deserialization.

### Methods
- **`createBaseDirectory()`**:
  Creates the application's base directory and initializes an empty tasks file if needed.

- **`createEmptyJsonFile(String filePath)`**:
  Creates an empty JSON file with the standard structure for tasks.

- **`readJsonFile(File file)`**:
  Reads a JSON file into a Map representation.

- **`readJsonFile(String filePath)`**:
  Reads a JSON file by path into a Map representation.

- **`writeJsonFile(Map<String, Object> data, String filePath)`** and **`writeJsonFile(Map<String, Object> data, File file)`**:
  Writes a data Map to a JSON file.

- **`toJsonString(Object object)`**:
  Converts an object to a JSON string.

- **`fromJsonString(String json)`** and **`fromJsonString(String json, Class<T> valueType)`**:
  Converts a JSON string to a Map or specific object type.

- **`convertValue(Object fromValue, Class<T> toValueType)`**:
  Converts an object to another type using JSON serialization.

- **`updateJsonFile(String filePath, Map<String, Object> newData)`**:
  Updates a JSON file with new data.

- **`readTasksJson()`** and **`writeTasksJson(Map<String, Object> data)`**:
  Convenience methods for reading and writing the standard tasks JSON file.

- **`isValidJsonStructure(File file, String... requiredFields)`**:
  Validates if a file contains valid JSON with required fields.

- **`getTasksData(Map<String, Object> tasksJson)`**:
  Retrieves the data array from a task JSON structure.

- **`updateLastSync(String timestamp)`**:
  Updates the last sync timestamp in the tasks JSON file.

- **`buildJsonStructure(Stream<Task> taskStream)`**:
  Builds a JSON structure from a stream of tasks.

- **`getMapper()`**:
  Returns the Jackson ObjectMapper instance.

### Usage
Used by `TaskHandler` to save tasks locally and prepare them for synchronization, providing a consistent and robust approach to JSON handling throughout the application.

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
  Retrieves all tasks for a user from the database. This method uses the `Task.Builder` pattern to convert JSON results from the database query into Task objects, ensuring consistency with the rest of the application.

#### Helper Methods
- **`getFieldValueAsString(JsonNode dataRow, String fieldName, Map<String, Integer> columnMap)`**:
  Safely extracts string values from JSON data rows with proper null handling and error checking.

- **`parseDateTime(String dateStr)`**:
  Parses date strings from the database into LocalDateTime objects with robust error handling.

- **`updateTaskListFromJson(JsonNode successItem, List<Task> taskList)`**:
  Updates the local task list based on JSON data from database operations.

- **`mergeTasks(List<Task> localTasks, List<Task> cloudTasks)`**:
  Intelligently merges local and cloud task lists, resolving conflicts based on sync status and timestamps.
  
  This method implements a sophisticated conflict resolution strategy that:
  1. Prioritizes tasks marked as "updated"
  2. Handles tasks with different sync statuses appropriately
  3. Uses timestamps to determine which version is newer
  4. Maintains task identity across local and cloud versions

### Data Flow
The `DBHandler` class maintains a consistent data flow pattern:
1. Database data in JSONB format → Parsed into JsonNode objects
2. JsonNode objects → Converted to Task objects using Task.Builder
3. Task operations performed in memory
4. Task objects → Converted to JSON for database operations using JSONUtils

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
taskHandler.addTask("Buy groceries", "Milk, bread, eggs", "pending", "2025-01-10T10:00", "Personal");
taskHandler.addTask("Finish project", "Complete the final report", "completed", "2025-01-15T15:00", "Work");
taskHandler.addTask("Call mom", "Check in and say hi", "completed", "2025-01-12T18:00", "Default");

// Prepare JSON files for sync
taskHandler.prepareSyncJson("new");
taskHandler.prepareSyncJson("update");

// Save all tasks to a local JSON file
taskHandler.saveTasksToJson();

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
````
