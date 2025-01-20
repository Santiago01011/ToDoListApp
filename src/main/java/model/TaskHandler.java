package model;

import java.time.LocalDateTime;
import java.util.List;

import DBH.TaskDAO;

public class TaskHandler{

    public void addTask(String title, String description, String targetDate, String folder, int userId){
        Task task = new Task.Builder(userId)
            .taskTitle(title)
            .description(description)
            .targetDate(targetDate.isEmpty() ? null : LocalDateTime.parse(targetDate))
            .folderName(folder)
            .build();


        List<String> folders = TaskDAO.loadFoldersFromDatabase(userId);
        if (!folders.contains(folder)) {
            TaskDAO.saveFolderToDatabase(folder, userId);
        }

        TaskDAO.saveTaskToDatabase(task);

    }
    
}
