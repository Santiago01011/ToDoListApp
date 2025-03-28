### New `IMPROVEMENTS.md`

```markdown
## ToDo List App Improvements

This file tracks the expected improvements and steps for each day.

### Day 1

- Set up the project structure. ✔
- Create the `Task` class to represent a task. ✔
- Create the `AppFrame` class to set up the main application window. ✔
- Add functionality to add tasks with optional descriptions. ✔
- Display tasks in the center panel. ✔
- Add functionality to the "View" and "Update" buttons. ✔
- Add a panel to display the task details. ✔

### Day 2

- Add the button and functionality to delete tasks. ✔
- Change the 'Update' button to look like a checkbox. ✔
- Change the 'Title' button to a label. ✔
- Improve the UI with additional styling and layout adjustments. ✔
- Implement task persistence using a database. ✔
- Implement functionality to update the database after closing the window. ✔
- Implement user profiles with private tasks. ✔
- Implement a history of completed tasks by user. ✔
- Implement functionality to restore tasks from history. ✔

### Day 3

- Re-code the entire project using the MVC pattern. ✔
- Divide the project into three packages: model, UI, and DBHandler. ✔
- Implement a toggle button to switch between day and night mode. ✔
- Improve the UI with additional styling and layout adjustments. ✔
- Add a day and night mode switch to the application. ✔
- Improve application performance. ✔
- Update the TaskFrame when a task is restored from history. ✔
- Make the update function more efficient. ✔
- Ensure the database handler works with the MVC pattern. ✔
- Implement a login and register screen. ✔

### Day 4

- Implement a file handler to save user data without a database.
- Implement a way to use the app without a database.

### Day 5

- Host the database on a server. ✔
- Implement the app to work with the server database. ✔
- Create a JAR file to run the app on any computer. ✔
- Improve the structure and security of the database. ✔
- Decrease the number of connections to the database. ✔

### Day 6

- Resolve all issues with the modify statement functions. ✔
- Connect to the database only when the app closes or the update button is pressed. ✔
- Finish day 4 and 5 improvements.
- Implement the edit task frame with all functionalities. ✔
- Add a delete trigger to the database. ✔
- Add `dump.sql` to the project. ✔
- Improve UI settings. ✔

### Days 7, 8, 9, 10

- Change the app structure to work with a local embedded database. ✔
- Refactor a lot of the code to work with the embedded database. ✔
- Refactor the `EditTaskFrame` and the button that invokes it. ✔
- Implement folders; each task can be assigned to a folder, and tasks without a folder will be auto-assigned to the default user's folder. ✔
- Implement a way to create, delete, and rename folders.
- Refactor all the UI code for `HistoryFrame` to show completed and deleted tasks. ✔
- Implement a way to restore deleted tasks. ✔
- Implement a way to delete tasks permanently. ✔
- Implement a way to edit completed tasks in `HistoryFrame`. ✔

### Additional Improvements

- Migration to auto-generated UUID keys, which will help in:
    - **Synchronization:**
        - **Consistency:** Time-ordered UUIDs can help maintain consistent order and timestamps across different databases, useful for synchronization.
        - **Conflict Resolution:** Helps in identifying and resolving conflicts more efficiently.
    - **Performance:**
        - **Indexing:** Time-ordered UUIDs can lead to more efficient indexing, improving performance when querying and sorting records.
        - **Insertions:** Reduces fragmentation in the database, leading to more efficient insertions and updates.
    - **Scalability:**
        - **Distributed Systems:** Well-suited for distributed systems where multiple clients or servers generate UUIDs independently, ensuring a high level of uniqueness and consistency.

