# ToDoList App

A cross-platform Java desktop application for managing personal tasks with both local (JSON file) and cloud (PostgreSQL) synchronization. Supports multiple users, offline mode, share folders between users, and synchronization mechanisms to ensure data consistency.

## Features

- **Multi-user support**: Each user has a private, secure task list.
- **Task management**: Add, view, edit, and delete tasks with title, description, status, due date, and folder.
- **Folders**: Organize tasks into folders.
- **Sync with PostgreSQL (cloud)**: Tasks are stored locally and can be synchronized with a remote PostgreSQL database in the cloud.
- **Offline mode**: Work with your tasks offline, changes are synced when online.
- **Modern Java**: Uses Java 21.
- **User-friendly UI**: Includes day/night mode and modern icons.

## Setup & Installation

1. **Download the latest release**
   - Go to the [Releases](https://github.com/Santiago01011/ToDoListApp/releases) section and download the latest JAR file.
2. **Install Java 21**
   - Make sure you have Java 21 or newer installed.
3. **Run the app**

## Usage

- **Login/Register**: On first launch, register a new user or log in.
- **Task operations**: Use the UI to add, edit, delete, and organize tasks.
- **Sync**: The app automatically syncs tasks with the cloud when possible. Manual sync is also available.
- **Offline**: If the database is unreachable, tasks are saved locally and synced later.

## File Structure

- `src/main/java/` - Java source code
- `src/main/resources/assets/` - UI icons
- `scripts/` - Utility scripts
- `README.md` - This file
- `pom.xml` - Maven build file

## Technologies / Libraries

- Java OpenJDK 21
- PostgreSQL (cloud)
- Jackson (JSON)
- SnakeYAML (user config)
- [Swing DateTime Picker](https://github.com/DJ-Raven/swing-datetime-picker)
- [FlatLaf](https://github.com/JFormDesigner/FlatLaf)
- [MigLayout](https://github.com/mikaelgrev/miglayout)

## Icons Used

Icons are from [Flaticon](https://www.flaticon.com/uicons).

## License

MIT License (see LICENSE file)

---



