# TrashTDL - Task Management System Documentation

## Overview
TrashTDL is a comprehensive Java desktop application for personal task management with cloud synchronization capabilities. This documentation provides detailed information about the application's architecture, key components, and usage patterns for developers and contributors.

### Key Features
- **Cross-platform Java desktop application** using Swing with modern FlatLaf themes
- **Local-first architecture** with offline functionality and cloud synchronization
- **Multi-user support** with secure authentication and user management
- **Real-time PostgreSQL synchronization** with intelligent conflict resolution
- **Modern UI/UX** with smooth animations, day/night themes, and responsive design
- **Advanced task organization** with folders, filters, and search capabilities

### Architecture Overview
The application follows a clean Model-View-Controller (MVC) architecture:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│       UI        │    │   Controllers   │    │     Models      │
│   (Swing/FlatLaf│◄──►│  TaskController │◄──►│  Task, Folder   │
│    Components)  │    │  UserController │    │  TaskHandler    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                ▼                       ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Data Layer    │    │     Utilities   │
                       │   DBHandler     │    │   JSONUtils     │
                       │   NeonPool      │    │   UserProperties│
                       └─────────────────┘    └─────────────────┘
```

This documentation covers the core components responsible for task management, data persistence, synchronization, and user interface coordination.

---

## `Task.java` - Core Data Model
The `Task` class is the fundamental data model representing a task in the TrashTDL application. It implements a robust builder pattern and provides comprehensive task management capabilities.

### Core Attributes
| Attribute | Type | Description |
|-----------|------|-------------|
| **`task_id`** | String | Unique identifier (UUID) for the task |
| **`task_title`** | String | Human-readable task title |
| **`description`** | String | Detailed task description |
| **`status`** | TaskStatus | Current status: PENDING, IN_PROGRESS, COMPLETED |
| **`sync_status`** | String | Synchronization state: "new", "cloud", "local", "to_update" |
| **`due_date`** | LocalDateTime | Optional due date for the task |
| **`created_at`** | LocalDateTime | Timestamp when the task was created |
| **`updated_at`** | LocalDateTime | Timestamp of last modification |
| **`deleted_at`** | LocalDateTime | Soft deletion timestamp (null if active) |
| **`last_sync`** | LocalDateTime | Last successful synchronization with cloud |
| **`folder_id`** | String | Associated folder identifier |
| **`folder_name`** | String | Human-readable folder name |

### Builder Pattern Implementation
The Task class uses an advanced Builder pattern that ensures immutable object creation and validates data integrity:

```java
// Creating a new task
Task task = new Task.Builder("unique-task-id")
    .taskTitle("Complete project documentation")
    .description("Update all API docs and user guides")
    .status(TaskStatus.IN_PROGRESS)
    .dueDate(LocalDateTime.now().plusDays(3))
    .folderName("Work Projects")
    .folderId("folder-uuid-123")
    .build();

// The builder validates required fields and sets defaults
Task minimalTask = new Task.Builder("task-id")
    .taskTitle("Quick task")
    .build(); // Other fields get sensible defaults
```

### Synchronization States Explained
The `sync_status` field is crucial for the application's offline-first architecture:

- **"new"**: Task created locally, not yet synced to cloud
- **"cloud"**: Task is synchronized and matches cloud version
- **"local"**: Task has local changes not yet synced to cloud
- **"to_update"**: Shadow copy with pending updates for cloud sync

### Utility Methods
- **`viewTaskDesc()`**: Returns formatted string representation for debugging and logging
- **`getModifiedFields()`**: Tracks which fields have been changed for efficient syncing
- **`clearModifiedFields()` / `addModifiedField(String)`**: Manages change tracking

---

## `TaskHandler.java` - Business Logic Controller
The `TaskHandler` class serves as the primary business logic controller for task management. It orchestrates task operations, manages in-memory collections, and coordinates with persistence layers.

### Core Responsibilities
1. **Task Lifecycle Management**: Create, read, update, delete operations
2. **Memory Management**: Maintains active task collections and shadow copies
3. **Synchronization Coordination**: Prepares data for cloud sync operations
4. **Data Persistence**: Handles local JSON file operations
5. **Conflict Resolution**: Manages sync conflicts using shadow update patterns

### Key Attributes
| Attribute | Type | Description |
|-----------|------|-------------|
| **`userTasksList`** | List\<Task\> | Primary in-memory task collection |
| **`userFoldersList`** | List\<Folder\> | Available folders for task organization |
| **`last_sync`** | LocalDateTime | Timestamp of last successful cloud sync |
| **`shadowUpdates`** | Map\<String, Task\> | Shadow copies for conflict-free updates |

### Essential Methods

#### Task Management Operations
```java
// Create new task with all attributes
public void createTask(String title, String description, TaskStatus status, 
                      LocalDateTime dueDate, String folderName);

// Smart update with sync-aware conflict resolution
public void updateTask(Task task, String title, String description, 
                      TaskStatus status, LocalDateTime targetDate, 
                      String folderName, LocalDateTime deleted_at);

// Retrieve task by unique identifier
public Task getTaskById(String task_id);

// Toggle task completion status
public void toggleTaskCompletion(String task_id);
```

#### Advanced Update Logic
The `updateTask` method implements sophisticated sync-aware updating:

```java
// Behavior depends on current sync status:
if (task.getSync_status().equals("new")) {
    // Direct update - task not yet synced
    updateTaskFields(task, title, description, status, targetDate, folderName);
} else if (task.getSync_status().equals("cloud")) {
    // Create shadow copy for sync, mark original as "local"
    task.setSync_status("local");
    Task shadow = new Task.Builder(task.getTask_id())
        .sync_status("to_update")
        .updatedAt(LocalDateTime.now())
        ./* only changed fields */
        .build();
    shadowUpdates.put(task.getTask_id(), shadow);
} else if (task.getSync_status().equals("local")) {
    // Update existing shadow or create new one
    updateOrCreateShadow(task, changedFields);
}
```

#### Synchronization Preparation
```java
// Generate JSON content for specific sync operations
public String prepareSyncJsonContent(String sync_status);

// Prepare filtered tasks for database operations
public void prepareSyncJson(String sync_status);

// Get pending updates for cloud synchronization
public List<Task> getShadowUpdatesForSync();

// Clean up after successful sync
public void clearShadowUpdate(String taskId);
```

#### Data Persistence
```java
// Save current state to local JSON file
public void saveTasksToJson();

// Load tasks from local storage on startup
public List<Task> loadTasksFromJson();

// Load shadow updates from persistent storage
private void loadShadowsFromJson();
```

#### Folder Management
```java
// Set available folders from cloud sync
public void setFoldersList(List<Folder> foldersList);

// Get folder names for UI components
public List<String> getFoldersNamesList();

// Find folder ID by name for task assignment
public String getFolderIdByName(String folderName);
```

### Shadow Update Pattern
The TaskHandler implements a sophisticated "shadow update" pattern for conflict-free synchronization:

1. **Cloud Task Update**: When a synced task is modified:
   - Original task marked as "local" 
   - Shadow copy created with "to_update" status
   - Shadow contains only changed fields

2. **Sync Process**: During synchronization:
   - Shadow updates sent to cloud
   - On success: original task updated, shadow deleted
   - On conflict: manual resolution or intelligent merge

3. **Benefits**:
   - Preserves original task state during sync
   - Allows rollback on sync failures
   - Enables optimistic updates with conflict detection

### Usage Patterns
```java
// Typical workflow for task management
TaskHandler handler = new TaskHandler();

// Create and manage tasks
String taskId = UUID.randomUUID().toString();
handler.createTask("Buy groceries", "Milk, bread, eggs", 
                  TaskStatus.PENDING, LocalDateTime.now().plusDays(1), "Personal");

// Update with automatic sync handling
Task task = handler.getTaskById(taskId);
handler.updateTask(task, "Buy groceries and fruits", null, 
                  TaskStatus.IN_PROGRESS, null, null, null);

// Prepare for synchronization
String newTasksJson = handler.prepareSyncJsonContent("new");
String updatesJson = handler.prepareSyncJsonContent("to_update");

// Persist changes locally
handler.saveTasksToJson();
```

---

## `JSONUtils.java` - Data Serialization & Persistence
The `JSONUtils` class provides a centralized, robust solution for all JSON operations in the application. It replaces the previous `TaskJsonBuilder` with a more comprehensive and type-safe approach to data serialization.

### Architecture & Design
- **Singleton Pattern**: Single ObjectMapper instance with optimized configuration
- **Type Safety**: Generic methods with compile-time type checking
- **Error Handling**: Comprehensive exception handling with graceful degradation
- **Performance**: Optimized Jackson configuration for desktop application use

### Core Attributes
| Attribute | Type | Description |
|-----------|------|-------------|
| **`BASE_DIRECTORY`** | String | User home directory path for app data (`~/.todoapp`) |
| **`MAPPER`** | ObjectMapper | Preconfigured Jackson instance with JSR310 time support |

### ObjectMapper Configuration
```java
private static final ObjectMapper MAPPER = new ObjectMapper()
    .registerModule(new JavaTimeModule())           // Java 8 time support
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
```

### File System Operations
```java
// Directory and file initialization
public static void createBaseDirectory();
public static void createDefaultJsonFile(String filePath);

// Validates and creates standard task JSON structure:
// {
//   "data": [],
//   "columns": ["folder_id", "folder_name", "task_id", ...],
//   "last_sync": null
// }
```

### Core JSON Operations
```java
// File-based operations with automatic error handling
public static Map<String, Object> readJsonFile(String filePath) throws IOException;
public static Map<String, Object> readJsonFile(File file) throws IOException;

public static void writeJsonFile(Map<String, Object> data, String filePath) throws IOException;
public static void writeJsonFile(Map<String, Object> data, File file) throws IOException;

// String-based serialization with type safety
public static String toJsonString(Object object) throws IOException;
public static Map<String, Object> fromJsonString(String json) throws IOException;
public static <T> T fromJsonString(String json, Class<T> valueType) throws IOException;

// Type conversion utilities
public static <T> T convertValue(Object fromValue, Class<T> toValueType);
```

### Task-Specific Operations
```java
// Convenience methods for the standard tasks.json file
public static Map<String, Object> readTasksJson() throws IOException;
public static void writeTasksJson(Map<String, Object> data) throws IOException;

// Task data extraction and manipulation
public static List<List<Object>> getTasksData(Map<String, Object> tasksJson);
public static void updateLastSync(String timestamp);

// Build JSON structure from task streams
public static Map<String, Object> buildJsonStructure(Stream<Task> taskStream);
```

### Data Validation & Integrity
```java
// Validates JSON file structure and required fields
public static boolean isValidJsonStructure(File file, String... requiredFields);

// Example usage:
boolean isValid = JSONUtils.isValidJsonStructure(
    new File("tasks.json"), 
    "data", "columns", "last_sync"
);
```

### Advanced Operations
```java
// Atomic file updates with backup and rollback
public static void updateJsonFile(String filePath, Map<String, Object> newData);

// Stream processing for large datasets
public static Map<String, Object> buildJsonStructure(Stream<Task> taskStream) {
    List<String> columns = Arrays.asList(
        "folder_id", "folder_name", "task_id", "task_title", 
        "description", "sync_status", "status", "due_date", 
        "created_at", "updated_at", "deleted_at", "last_sync"
    );
    
    List<List<Object>> data = taskStream
        .map(task -> convertTaskToRow(task, columns))
        .collect(Collectors.toList());
    
    return Map.of(
        "data", data,
        "columns", columns,
        "last_sync", LocalDateTime.now().toString()
    );
}
```

### Error Handling Strategy
The JSONUtils class implements a multi-layer error handling approach:

1. **File System Errors**: Automatic directory creation, permission handling
2. **JSON Parsing Errors**: Graceful fallback to default structures
3. **Type Conversion Errors**: Safe casting with informative error messages
4. **Validation Errors**: Schema validation with detailed failure reporting

### Usage Patterns
```java
// Standard file operations
try {
    Map<String, Object> data = JSONUtils.readTasksJson();
    
    // Modify data...
    data.put("last_sync", LocalDateTime.now().toString());
    
    JSONUtils.writeTasksJson(data);
} catch (IOException e) {
    // Handle error with user feedback
    System.err.println("Failed to save tasks: " + e.getMessage());
}

// Type-safe object serialization
Task task = new Task.Builder("id").taskTitle("Test").build();
try {
    String json = JSONUtils.toJsonString(task);
    Task restored = JSONUtils.fromJsonString(json, Task.class);
} catch (IOException e) {
    // Handle serialization error
}

// Bulk operations with streams
Stream<Task> taskStream = taskHandler.userTasksList.stream()
    .filter(task -> "new".equals(task.getSync_status()));
    
Map<String, Object> syncData = JSONUtils.buildJsonStructure(taskStream);
```

### Integration with TaskHandler
The JSONUtils class is tightly integrated with TaskHandler for seamless data persistence:

```java
// In TaskHandler.saveTasksToJson()
public void saveTasksToJson() {
    try {
        Map<String, Object> jsonData = JSONUtils.buildJsonStructure(
            userTasksList.stream()
        );
        JSONUtils.writeTasksJson(jsonData);
    } catch (IOException e) {
        LOGGER.severe("Failed to save tasks: " + e.getMessage());
    }
}
```

---

## `DBHandler.java` - Cloud Synchronization Engine
The `DBHandler` class serves as the sophisticated cloud synchronization engine, managing all interactions between the local application state and the remote PostgreSQL database. It implements intelligent conflict resolution, efficient data transfer, and robust error handling.

### Architecture & Responsibilities
1. **Bidirectional Synchronization**: Upload local changes and download remote updates
2. **Conflict Resolution**: Intelligent merging of local and cloud data states
3. **Connection Management**: Database connection pooling and error recovery
4. **Data Transformation**: Convert between application objects and database formats
5. **Transaction Safety**: Ensure data consistency across sync operations

### Core Attributes
| Attribute | Type | Description |
|-----------|------|-------------|
| **`taskHandler`** | TaskHandler | Reference to business logic controller |
| **`userUUID`** | UUID | Current authenticated user identifier |

### Database Connection Architecture
```java
// Uses NeonPool for connection management
try (Connection conn = NeonPool.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(query)) {
    
    // Database operations with automatic resource cleanup
    // Prepared statements for SQL injection prevention
    // Connection pooling for performance optimization
}
```

### Synchronization Workflow

#### 1. Complete Sync Process
```java
public CompletableFuture<Boolean> startSyncProcess() {
    return CompletableFuture.supplyAsync(() -> {
        try {
            syncTasks(); // Main synchronization logic
            return true;
        } catch (Exception e) {
            System.err.println("Sync failed: " + e.getMessage());
            return false;
        }
    });
}
```

#### 2. Core Sync Logic
```java
private void syncTasks() {
    // Handle first-time sync (empty local tasks)
    if (taskHandler.userTasksList.isEmpty() && 
        taskHandler.getShadowUpdatesForSync().isEmpty()) {
        
        List<Task> cloudTasks = retrieveTasksFromCloud(userUUID, null);
        mergeTasks(cloudTasks);
        taskHandler.setLastSync(LocalDateTime.now());
        return;
    }
    
    // Prepare incremental sync
    OffsetDateTime lastSync = getLastSyncTimestamp();
    taskHandler.setLastSync(LocalDateTime.now());
    
    // Upload local changes
    uploadNewTasks();
    uploadTaskUpdates();
    
    // Download remote changes
    List<Task> cloudTasks = retrieveTasksFromCloud(userUUID, lastSync);
    if (!cloudTasks.isEmpty()) {
        mergeTasks(cloudTasks);
    }
}
```

### Data Upload Operations

#### Insert New Tasks
```java
private void insertTasksFromJSON(UUID userUUID, String jsonContent) {
    String query = "SELECT * FROM todo.insert_tasks_from_jsonb(?, ?::jsonb)";
    
    try (Connection conn = NeonPool.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setObject(1, userUUID);
        pstmt.setString(2, jsonContent);
        
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                // Process success/failure results
                Map<String, Object> result = JSONUtils.fromJsonString(rs.getString(2));
                processInsertResults(result);
            }
        }
    } catch (SQLException | IOException e) {
        System.err.println("Insert failed: " + e.getMessage());
    }
}

private void processInsertResults(Map<String, Object> result) {
    List<Map<String, Object>> successList = 
        (List<Map<String, Object>>) result.get("success");
    
    if (successList != null) {
        for (Map<String, Object> success : successList) {
            String oldUUID = (String) success.get("old");
            String newUUID = (String) success.get("new");
            
            // Update local task with database-generated UUID
            taskHandler.userTasksList.stream()
                .filter(task -> task.getTask_id().equals(oldUUID))
                .findFirst()
                .ifPresent(task -> {
                    task.setTask_id(newUUID);
                    task.setSync_status("cloud");
                    task.setLast_sync(taskHandler.getLastSync());
                });
        }
    }
}
```

#### Update Existing Tasks
```java
private void updateTasksFromJSON(UUID userUUID, String jsonContent) {
    String query = "SELECT * FROM todo.update_tasks_from_jsonb(?, ?::jsonb)";
    
    // Similar pattern to insertTasksFromJSON but handles updates
    // Processes shadow updates and cleans up local state
    // Updates sync status from "local" to "cloud"
}
```

### Data Download Operations

#### Retrieve Cloud Changes
```java
private List<Task> retrieveTasksFromCloud(UUID userUUID, OffsetDateTime lastSync) {
    String query = "SELECT * FROM todo.retrieve_tasks_modified_since_in_jsonb(?, ?)";
    
    try (Connection conn = NeonPool.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setObject(1, userUUID);
        pstmt.setObject(2, lastSync); // null for full sync
        
        List<Task> tasks = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String jsonbResult = rs.getString(2);
                tasks = parseCloudTasks(jsonbResult);
            }
        }
        return tasks;
    } catch (SQLException e) {
        System.err.println("Cloud retrieval failed: " + e.getMessage());
        return new ArrayList<>();
    }
}

private List<Task> parseCloudTasks(String jsonbResult) throws IOException {
    Map<String, Object> resultMap = JSONUtils.fromJsonString(jsonbResult);
    List<String> columns = (List<String>) resultMap.get("columns");
    List<List<Object>> data = (List<List<Object>>) resultMap.get("data");
    
    if (data == null || data.isEmpty()) {
        return new ArrayList<>();
    }
    
    List<Task> tasks = new ArrayList<>();
    for (List<Object> row : data) {
        Task task = buildTaskFromRow(columns, row);
        tasks.add(task);
    }
    return tasks;
}
```

### Intelligent Conflict Resolution

#### Task Merging Strategy
```java
private void mergeTasks(List<Task> cloudTasks) {
    Map<String, Task> localTaskMap = createLocalTaskMap();
    List<String> tasksToRemove = new ArrayList<>();
    
    for (Task cloudTask : cloudTasks) {
        // Handle deleted tasks
        if (cloudTask.getDeleted_at() != null) {
            handleDeletedTask(cloudTask, localTaskMap, tasksToRemove);
            continue;
        }
        
        Task localTask = localTaskMap.get(cloudTask.getTask_id());
        
        if (localTask == null) {
            // New task from cloud - add directly
            taskHandler.userTasksList.add(cloudTask);
        } else {
            // Existing task - resolve conflicts
            resolveTaskConflict(localTask, cloudTask);
        }
    }
    
    // Clean up deleted tasks
    removeDeletedTasks(tasksToRemove);
}

private void resolveTaskConflict(Task localTask, Task cloudTask) {
    LocalDateTime localUpdated = localTask.getLast_sync();
    LocalDateTime cloudUpdated = cloudTask.getLast_sync();
    
    // Cloud version is newer - replace local
    if (cloudUpdated != null && 
        (localUpdated == null || cloudUpdated.isAfter(localUpdated))) {
        
        int index = taskHandler.userTasksList.indexOf(localTask);
        taskHandler.userTasksList.set(index, cloudTask);
        
        // Clear any pending shadow updates
        taskHandler.clearShadowUpdate(cloudTask.getTask_id());
    }
    // Otherwise keep local version (it's newer or same)
}
```

### Folder Management Integration
```java
public List<Folder> fetchAccessibleFolders() {
    return getAccessibleFolders(userUUID);
}

private List<Folder> getAccessibleFolders(UUID userUUID) {
    String query = "SELECT * FROM todo.get_accessible_folders(?)";
    
    try (Connection conn = NeonPool.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        List<Folder> folders = new ArrayList<>();
        pstmt.setObject(1, userUUID);
        
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Folder folder = new Folder.Builder(
                    rs.getObject("folder_id", UUID.class).toString())
                    .folderName(rs.getString("folder_name"))
                    .build();
                folders.add(folder);
            }
        }
        return folders;
    } catch (SQLException e) {
        System.err.println("Folder fetch failed: " + e.getMessage());
        return new ArrayList<>();
    }
}
```

### Error Handling & Recovery
The DBHandler implements comprehensive error handling:

1. **Connection Failures**: Graceful degradation to offline mode
2. **SQL Exceptions**: Detailed logging with user-friendly messages
3. **Data Corruption**: Validation and recovery mechanisms
4. **Timeout Handling**: Configurable timeouts with retry logic
5. **Transaction Rollback**: Ensures data consistency on failures

### Performance Optimizations
- **Connection Pooling**: Reuse database connections efficiently
- **Prepared Statements**: Prevent SQL injection and improve performance
- **Batch Operations**: Group multiple operations for network efficiency
- **Incremental Sync**: Only transfer changed data since last sync
- **Lazy Loading**: Load data on-demand to reduce memory usage

### Integration Example
```java
// Complete synchronization workflow
public class SyncService {
    private TaskHandler taskHandler;
    private DBHandler dbHandler;
    
    public void performSync() {
        // Prepare local changes
        String newTasks = taskHandler.prepareSyncJsonContent("new");
        String updates = taskHandler.prepareSyncJsonContent("to_update");
        
        // Execute synchronization
        CompletableFuture<Boolean> syncFuture = dbHandler.startSyncProcess();
        
        syncFuture.thenAccept(success -> {
            if (success) {
                // Update UI, refresh folder list
                updateUI();
                refreshFolders();
            } else {
                // Handle sync failure
                showSyncError();
            }
        });
    }
}
```

## UI Architecture & Components

### Overview
TrashTDL uses a modern Swing-based UI architecture with FlatLaf theming, smooth animations, and responsive design patterns. The UI follows a component-based approach with clear separation of concerns.

### Core UI Components

#### `TaskDashboardFrame.java` - Main Application Window
The central hub of the application that orchestrates all UI interactions:

```java
// Key features:
- Sliding panel animations (150ms smooth transitions)
- Card-based task display with staggered loading
- Responsive layout that adapts to window resizing
- Theme switching with live preview
- Context-sensitive toolbar and actions
```

#### `TaskCardPanel.java` - Individual Task Display
Represents a single task with interactive elements:
- **Visual Status Indicators**: Color-coded status badges
- **Completion Toggle**: Checkbox with visual feedback
- **Action Buttons**: View, Edit, Delete with hover effects
- **Due Date Warnings**: Red highlighting for overdue tasks
- **Smooth Animations**: Expand/collapse with height transitions

#### `NewTaskPanel.java` & `EditTaskPanel.java` - Task Forms
Slide-in panels for task creation and modification:
- **Date/Time Pickers**: Enhanced datetime selection components
- **Folder Assignment**: Dropdown with dynamic folder loading
- **Form Validation**: Real-time validation with user feedback
- **Focus Management**: Automatic focus progression and restoration

#### `TopBarPanel.java` - Navigation & Filtering
Main navigation and filtering interface:
- **Folder Filter**: Dynamic dropdown with "All Folders" option
- **Status Filters**: Multi-select checkboxes for task status
- **Sort Options**: Radio buttons for different sorting criteria
- **User Actions**: Profile menu with logout, edit account options
- **Theme Toggle**: Day/night mode switching

#### `HistoryPanel.java` - Completed Tasks View
Specialized panel for managing task history:
- **Completed Task List**: Chronological display of finished tasks
- **Restore Actions**: One-click task restoration to active state
- **Delete Actions**: Permanent task removal from history
- **Slide Animation**: Smooth slide-in from left side

### Animation System
TrashTDL implements a sophisticated animation system for enhanced user experience:

```java
// Staggered card loading
private static final int STAGGER_DELAY = 50;
private static final int FIRST_RUN_DELAY = 200;

// Each task card animates in with a slight delay
for (int i = 0; i < tasks.size(); i++) {
    Timer starter = new Timer(CARD_TIMER_DELAY, null);
    starter.setInitialDelay(baseDelay + (i * STAGGER_DELAY));
    starter.addActionListener(e -> animateCardHeight(card));
    starter.start();
}
```

### Theme System
The application supports dynamic theming with two built-in themes:

#### Day Theme (CoffeYellow)
- Warm, light color palette
- High contrast for readability
- Professional appearance

#### Night Theme (NightBlue)
- Dark background with accent colors
- Reduced eye strain for low-light use
- Modern, sleek appearance

```java
// Theme switching implementation
public void onToggleTheme() {
    common.toggleColorMode();
    UserProperties.setProperty("darkTheme", String.valueOf(common.useNightMode));
    refreshTheme();
    rebuildUI(); // Applies new theme across all components
}
```

---

## Controller Architecture

### `TaskController.java` - Primary Business Logic Controller
Coordinates between UI events and data operations:

#### Core Responsibilities
1. **Task Lifecycle Management**: Handle create, read, update, delete operations
2. **Filter Coordination**: Apply complex filtering criteria to task lists
3. **Sync Orchestration**: Manage background synchronization with visual feedback
4. **User Action Processing**: Handle user interactions from UI components
5. **State Management**: Maintain application state and coordinate updates

#### Key Methods
```java
// Task management operations
public void handleCreateTask(String title, String description, String folderName, 
                           LocalDateTime dueDate, TaskStatus status);
public void handleEditTaskRequest(String taskId, String title, String desc, ...);
public void handleDeleteTaskRequest(String taskId);
public void handleTaskCompletionToggle(Task task);

// Filtering and organization
public List<Task> getTasksByFilters(FiltersCriteria criteria);
public List<Task> getTasksByFolder(List<Task> sourceList, String selectedFolder);
public List<Task> getTasksByStatus(List<Task> sourceList, TaskStatus status);

// Synchronization coordination
public void handleSyncRequest();
public LocalDateTime getLastSyncTime();

// User management
public void handleLogoutRequest();
public void handleChangeUsernameRequest();
public void handleDeleteAccountRequest();
```

### `UserController.java` - Authentication & User Management
Handles user authentication and session management:

```java
// Authentication flow
public boolean doLogin();           // Authenticate with API
public boolean doRegister();        // Create new user account
public void launchDashboard();      // Initialize main application

// Session management
private boolean keepLoggedIn;       // Remember me functionality
private String userUUID;           // Current user identifier
private String username, password; // Encrypted credentials
```

---

## Example Usage & Integration Patterns

### Complete Application Workflow
```java
// 1. Application Startup
public static void main(String[] args) {
    // Initialize themes and UI
    FlatLaf.registerCustomDefaultsSource("themes");
    
    // Check for existing session
    UserController userController = new UserController();
    if (userController.getKeepLoggedIn()) {
        // Auto-login and launch dashboard
        userController.launchDashboard(null);
    } else {
        // Show login screen
        new LoginFrame("TrashTDL Login").setVisible(true);
    }
}

// 2. Task Management Workflow
TaskHandler taskHandler = new TaskHandler();
DBHandler dbHandler = new DBHandler(taskHandler);
TaskController controller = new TaskController(taskHandler, view, dbHandler);

// Create new task
controller.handleCreateTask(
    "Complete documentation", 
    "Update all user guides and API docs",
    "Work Projects",
    LocalDateTime.now().plusDays(2),
    TaskStatus.IN_PROGRESS
);

// Apply filters
FiltersCriteria criteria = new FiltersCriteria(
    "Work Projects",                    // folder filter
    Set.of(TaskStatus.IN_PROGRESS)     // status filter
);
List<Task> filteredTasks = controller.getTasksByFilters(criteria);

// Synchronize with cloud
controller.handleSyncRequest();
```

### Synchronization Workflow Example
```java
// Complete sync operation with error handling
public void performBackgroundSync() {
    CompletableFuture<Boolean> syncFuture = dbHandler.startSyncProcess();
    
    syncFuture.thenAcceptAsync(success -> {
        if (success) {
            // Update UI on successful sync
            SwingUtilities.invokeLater(() -> {
                view.updateLastSyncLabel(taskHandler.getLastSync());
                view.refreshTaskListDisplay();
                view.updateFolderList(taskHandler.getFoldersNamesList());
            });
        } else {
            // Handle sync failure
            SwingUtilities.invokeLater(() -> {
                showSyncErrorDialog();
            });
        }
    }).exceptionally(throwable -> {
        System.err.println("Sync error: " + throwable.getMessage());
        return null;
    });
}
```

### UI Component Integration
```java
// TaskDashboardFrame initialization
public void initialize() {
    taskController.loadInitialTasks();        // Load saved tasks
    taskController.loadInitialFolderList();   // Sync folder list
    taskController.loadInitialSyncTime();     // Display last sync
    
    refreshTaskListDisplay();                 // Populate UI
    updateFolderList(getFolderList());       // Update dropdowns
    
    setVisible(true);                         // Show main window
}

// Real-time filtering
topBarPanel.setListener(new TopBarPanel.Listener() {
    @Override
    public void onFolderFilterChanged(String folder) {
        filterCriteria = new FiltersCriteria(folder, filterCriteria.statuses());
        refreshTaskListDisplay(); // Apply filter immediately
    }
    
    @Override
    public void onStatusFilterChanged(Set<TaskStatus> statuses) {
        filterCriteria = new FiltersCriteria(filterCriteria.folderName(), statuses);
        refreshTaskListDisplay(); // Update task list
    }
});
```

### Error Handling Patterns
```java
// Graceful error handling with user feedback
public void handleSyncRequest() {
    try {
        CompletableFuture<Boolean> syncFuture = dbHandler.startSyncProcess();
        syncFuture.thenAccept(success -> {
            if (success) {
                showSuccessMessage("Sync completed successfully");
            } else {
                showWarningMessage("Sync completed with some issues");
            }
        });
    } catch (Exception e) {
        showErrorMessage("Sync failed: " + e.getMessage());
        // Fallback to offline mode
        enableOfflineMode();
    }
}
```

This comprehensive documentation provides developers with a complete understanding of TrashTDL's architecture, component relationships, and usage patterns. The modular design ensures maintainability while the rich feature set provides a professional-grade task management experience.
````
