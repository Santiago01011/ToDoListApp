package DBH;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class H2Manager {

     // SQL for creating tables in H2 database
    public static void createTablesIfNotExist() {
        String CREATE_TABLES_H2 = """       
            CREATE TABLE IF NOT EXISTS users (
                -- id UUID PRIMARY KEY,
                id INT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password TEXT NOT NULL,
                email TEXT
            );

            CREATE TABLE IF NOT EXISTS folders (
                -- id UUID PRIMARY KEY,
                id INT PRIMARY KEY,
                user_id INT NOT NULL,  -- Change this to UUID
                folder_name VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                deleted_at TIMESTAMP,
                sync_status VARCHAR(20) DEFAULT 'LOCAL',
                FOREIGN KEY (user_id) REFERENCES users(id) -- Change this to UUID
            );

            CREATE TABLE IF NOT EXISTS tasks (
                -- id UUID PRIMARY KEY,
                id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                user_id INT NOT NULL,  -- Change this to UUID
                task_title VARCHAR(50) NOT NULL,
                description TEXT,
                is_done BOOLEAN NOT NULL DEFAULT FALSE,
                date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                target_date TIMESTAMP,
                deleted_at TIMESTAMP,
                updated_at TIMESTAMP,
                sync_status VARCHAR(20) DEFAULT 'LOCAL',
                last_sync TIMESTAMP,
                folder_id INT DEFAULT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id), -- Change this to UUID
                FOREIGN KEY (folder_id) REFERENCES folders(id) -- Change this to UUID
            );
        
            """;
        try (Connection conn = PSQLtdldbh.getLocalConnection();
            PreparedStatement pstmt = conn.prepareStatement(CREATE_TABLES_H2)){
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public static void H2dbchanges(){

        createTablesIfNotExist();

        try {
            org.h2.tools.Server.createWebServer("-web").start();
        } catch (SQLException e) {
            System.err.println("Error starting H2 web server: " + e.getMessage());
            e.printStackTrace();
        }


        // String MOD_H2_SCHEMA = """
        //     SELECT * FROM tasks;

        // """;
        // try (Connection conn = PSQLtdldbh.getConnection();
        //     PreparedStatement pstmt = conn.prepareStatement(MOD_H2_SCHEMA)){
        //     ResultSet rs = pstmt.executeQuery();
        //     Task task = new Task(
        //                 rs.getInt("id"),
        //                 rs.getString("task_title"),
        //                 rs.getString("description"),
        //                 rs.getInt("user_id")
        //         );
        //         task.setIsDone(rs.getBoolean("is_done"));
        //         task.setDateAdded(rs.getTimestamp("date_added").toLocalDateTime());
        //         task.setDeleted(rs.getTimestamp("deleted_at") != null);

        //         System.out.println(task.getId());
        //         System.out.println(task.getTaskTitle());
        //         System.out.println(task.getDeleted());
        // } catch (SQLException e){
        //     System.err.println("Error changing database schema: " + e.getMessage());
        //     e.printStackTrace();
        // }
        // System.out.println("");
    }

    


}