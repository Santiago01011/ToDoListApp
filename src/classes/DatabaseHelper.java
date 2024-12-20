package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper{
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/todo_list";
    //private static final String DB_NAME = "todo_list";  //fix createDatabase method
    private static final String USER = "postgres";
    private static final String PASSWORD = "123987";

    public static Connection connect(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("Connected to the database");
        } catch(SQLException e){
            System.out.println("Error connecting to the database");
            e.printStackTrace();
        }
        return conn;
    }

    //fix createDatabase method
    // public static void createDatabase(){
    //     //create the database
    //     try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    //         Statement stmt = conn.createStatement()){
    //         String createDB = "CREATE DATABASE " + DB_NAME;
    //         stmt.executeUpdate(createDB);
    //         System.out.println("Database created successfully");
    //     } catch(SQLException e){
    //         System.out.println("Error creating database");
    //         e.printStackTrace();
    //     }
    // }
    public static void createTables(){
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
        + "id SERIAL PRIMARY KEY,"
        + "username VARCHAR(50) NOT NULL,"
        + "password VARCHAR(50) NOT NULL"  //password should be hashed
        + ");";

        String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks("
        + "id SERIAL PRIMARY KEY,"
        + "task_title VARCHAR(50) NOT NULL,"
        + "description TEXT,"
        + "date_added TIMESTAMP NOT NULL,"
        + "is_done BOOLEAN NOT NULL,"
        + "user_id INTEGER NOT NULL,"
        + "FOREIGN KEY (user_id) REFERENCES users(id)"
        + ");";

        try(Connection conn = connect();
            Statement stmt = conn.createStatement()){
            stmt.execute(createUsersTable);
            stmt.execute(createTasksTable);
            System.out.println("Tables created successfully");
        } catch(SQLException e){
            System.out.println("Error creating tables");
            e.printStackTrace();
        }

    }
}
