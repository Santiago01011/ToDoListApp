package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task {
    private int id;
    private String taskTitle;
    private String description;
    private int userId;
    private LocalDateTime updatedAt;
    private LocalDateTime dateAdded;
    private LocalDateTime targetDate;
    private LocalDateTime deletedAt;
    private boolean isDone;
    private int folderId;
    private String folderName;

    private Task(Builder builder){
        this.id = builder.id;
        this.taskTitle = builder.taskTitle;
        this.description = builder.description;
        this.userId = builder.userId;
        this.dateAdded = builder.dateAdded;
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : LocalDateTime.now();
        this.deletedAt = builder.deletedAt;
        this.targetDate = builder.targetDate;
        this.isDone = builder.isDone;
        this.folderId = builder.folderId;
        this.folderName = builder.folderName;
    }

    // Usage example:
    /*
    Task task = new Task.Builder(userId)
        .taskTitle("Buy groceries")
        .description("Get milk and bread")
        .folderId(1)
        .targetDate(LocalDateTime.now().plusDays(1))
        .build();
    */
    public static class Builder{
        private int id;
        private String taskTitle;
        private String description;
        private int userId;
        private LocalDateTime dateAdded;
        private LocalDateTime updatedAt;
        private LocalDateTime targetDate;
        private LocalDateTime deletedAt;
        private boolean isDone = false;
        private int folderId;
        private String folderName;

        public Builder(int userId){
            this.userId = userId;
        }

        public Builder taskTitle(String taskTitle){
            this.taskTitle = taskTitle;
            return this;
        }

        public Builder id(int id){
            this.id = id;
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

        public Builder folderId(int folderId){
            this.folderId = folderId;
            return this;
        }

        public Builder folderName(String folderName){
            this.folderName = folderName;
            return this;
        }

        public Task build(){
            return new Task(this);
        }
    }

    public int getId(){
        return this.id;
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

    public int getUserId(){
        return this.userId;
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

    public int getFolderId(){
        return this.folderId;
    }

    public String getFolderName(){
        return this.folderName;
    }

    public void setId(int id){
        this.id = id;
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

    public void setTargetDate(LocalDateTime targetDate){
        this.targetDate = targetDate;
    }

    public void setFolderId(int folderId){
        this.folderId = folderId;
    }

    public void setFolderName(String folderName){
        this.folderName = folderName;
    }

    public String viewTaskDesc(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return "Task ID: " + id + "\n" 
        + "Task Title: " + taskTitle + "\n" 
        + "Description: " + description + "\n" 
        + "Date Added: " + dateAdded.format(formatter) + "\n" 
        + "Status: " + (isDone ? "Done" : "Pending") + "\nLast Update: " + updatedAt.format(formatter) +"\n"
        + "Folder ID: " + folderId;
    }
}