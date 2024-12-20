package classes;

public class ToDoListApp {

    public static void main(String[] args) {
        //DatabaseHelper.createDatabase();  //fix createDatabase method
        DatabaseHelper.createTables();
        new AppFrame();
    }
}