package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task {
    private String taskUUID;
    private String taskTitle;
    private String description;
    private String userUUID;
    private LocalDateTime updatedAt;
    private LocalDateTime dateAdded;
    private LocalDateTime targetDate;
    private LocalDateTime deletedAt;
    private boolean isDone;
    private String folderUUID;
    private String folderName;
    private String status; // Represents if the task is "local", "cloud", or "new"

    private Task(Builder builder){
        this.taskUUID = builder.taskUUID;
        this.taskTitle = builder.taskTitle;
        this.description = builder.description;
        this.userUUID = builder.userUUID;
        this.dateAdded = builder.dateAdded;
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : LocalDateTime.now();
        this.deletedAt = builder.deletedAt;
        this.targetDate = builder.targetDate;
        this.isDone = builder.isDone;
        this.status = builder.status != null ? builder.status : "new"; // Default to "new" if not set
        this.folderUUID = builder.folderUUID;
        this.folderName = builder.folderName;
    }

    // Usage example:
    /*
    Task task = new Task.Builder(userUUID)
        .taskTitle("Buy groceries")
        .description("Get milk and bread")
        .folderUUID("1")
        .targetDate(LocalDateTime.now().plusDays(1))
        .build();
    */
    public static class Builder{
        private String taskUUID;
        private String taskTitle;
        private String description;
        private String userUUID;
        private LocalDateTime dateAdded;
        private LocalDateTime updatedAt;
        private LocalDateTime targetDate;
        private LocalDateTime deletedAt;
        private boolean isDone = false;
        private String folderUUID;
        private String folderName;
        private String status; // Represents if the task is "local", "cloud", or "new"

        public Builder(String userUUID){
            this.userUUID = userUUID;
        }

        public Builder taskTitle(String taskTitle){
            this.taskTitle = taskTitle;
            return this;
        }

        public Builder taskUUID(String taskUUID){
            this.taskUUID = taskUUID;
            return this;
        }

        public Builder description(String description){
            this.description = description;
            return this;
        }

        public Builder isDone(boolean isDone){
            this.isDone = isDone;
            return this;
        }

        public Builder targetDate(LocalDateTime targetDate){
            this.targetDate = targetDate;
            return this;
        }

        public Builder dateAdded(LocalDateTime dateAdded){
            this.dateAdded = dateAdded;
            return this;
        }

        public Builder deletedAt(LocalDateTime deletedAt){
            this.deletedAt = deletedAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt){
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder folderUUID(String folderUUID){
            this.folderUUID = folderUUID;
            return this;
        }

        public Builder folderName(String folderName){
            this.folderName = folderName;
            return this;
        }

        public Builder status (String status){
            this.status = status;
            return this;
        }

        public Task build(){
            return new Task(this);
        }
    }

    public String getTaskUUID(){
        return this.taskUUID;
    }

    public String getTaskTitle(){
        return this.taskTitle;
    }

    public String getDescription(){
        return this.description;
    }

    public boolean getIsDone(){
        return this.isDone;
    }

    public String getUserUUID(){
        return this.userUUID;
    }

    public LocalDateTime getDateAdded(){
        return this.dateAdded;
    }
    
    public LocalDateTime getTargetDate(){
        return this.targetDate;
    }

    public LocalDateTime getUpdatedAt(){
        return this.updatedAt;
    }

    public LocalDateTime getDeletedAt(){
        return this.deletedAt;
    }

    public String getFolderUUID(){
        return this.folderUUID;
    }

    public String getFolderName(){
        return this.folderName;
    }

    public String getStatus(){
        return this.status;
    }

    public void setTaskUUID(String taskUUID){
        this.taskUUID = taskUUID;
    }

    public void setTaskTitle(String taskTitle){
        this.taskTitle = taskTitle;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setIsDone(boolean isDone){
        this.isDone = isDone;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public void setTargetDate(LocalDateTime targetDate){
        this.targetDate = targetDate;
    }

    public void setFolderUUID(String folderUUID){
        this.folderUUID = folderUUID;
    }

    public void setFolderName(String folderName){
        this.folderName = folderName;
    }

    public String viewTaskDesc(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return "Task UUID: " + taskUUID + "\n" 
        + "Task Title: " + taskTitle + "\n" 
        + "Description: " + description + "\n" 
        + "Date Added: " + dateAdded.format(formatter) + "\n" 
        + "Status: " + status + "\n"
        + "Completion: " + (isDone ? "Done" : "Pending") + "\nLast Update: " + updatedAt.format(formatter) + "\n"
        + "Folder UUID: " + folderUUID;
    }
}