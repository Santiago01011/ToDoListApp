package classes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task{
    private int id;
    private String taskTitle;
    private String description;
    private int userId;
    private LocalDateTime dateAdded;
    private boolean isDone;

    public Task(int id, String taskTitle, String description, int userId){
        this.id = id;
        this.taskTitle = taskTitle;
        this.description = description;
        this.userId = userId;
        this.dateAdded = LocalDateTime.now();
        this.isDone = false;

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

    public void settaskTitle(String taskTitle){
        this.taskTitle = taskTitle;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setIsDone(boolean isDone){
        this.isDone = isDone;
    }

    public void setDateAdded(LocalDateTime dateAdded){
        this.dateAdded = dateAdded;
    }

    public void setId(int id){
        this.id = id;
    }

    public String viewTaskDesc(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return "Task ID: " + id + "\n" 
        + "Task Title: " + taskTitle + "\n" 
        + "Description: " + description + "\n" 
        + "Date Added: " + dateAdded.format(formatter) + "\n" 
        + "Status: " + (isDone ? "Done" : "Pending"); 
    }

}