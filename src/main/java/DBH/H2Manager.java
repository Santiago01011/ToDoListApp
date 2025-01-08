package DBH;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class H2Manager {
    
     // SQL for creating tables in H2 database
    public static void createTablesIfNotExist() {
        String CREATE_TABLES_H2 = """       
        CREATE TABLE IF NOT EXISTS users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            password TEXT NOT NULL,
            email TEXT
        );
        
        CREATE TABLE IF NOT EXISTS tasks (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            task_title VARCHAR(50) NOT NULL,
            description TEXT,
            is_done BOOLEAN NOT NULL DEFAULT FALSE,
            date_added TIMESTAMP NOT NULL,
            deleted_at TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            sync_status VARCHAR(20) DEFAULT 'LOCAL',
            last_sync TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id)
            FOREIGN KEY (folder_id) REFERENCES folders(id)
        );


        CREATE TABLE IF NOT EXISTS folders (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            folder_name VARCHAR(100) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id)
        );

        

        """;
        try (Connection conn = PSQLtdldbh.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(CREATE_TABLES_H2)){
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public static void syncWithCloudDatabase(){
        
    }    
}