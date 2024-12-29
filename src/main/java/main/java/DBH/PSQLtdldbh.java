package main.java.DBH;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;




public class PSQLtdldbh {
    private static final String url = "jdbc:postgresql://localhost:5432/todo_list";
    private static final String user = "postgres";
    private static final String password = "123987";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

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