package DBH;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import io.github.cdimascio.dotenv.Dotenv;

public class PSQLtdldbh {
    private static final HikariDataSource dataSource;

    // Create a connection pool that reuses the same connection 
    // rather than creating a new one every time a connection is requested
    static{
        HikariConfig config = new HikariConfig();
        
        try{
            // First try .env file
            if (new File(".env").exists()){
                System.out.println("Using .env file for database connection details");
                Dotenv dotenv = Dotenv.load();
                config.setJdbcUrl(dotenv.get("DB_URL"));
                config.setUsername(dotenv.get("DB_USERNAME"));
                config.setPassword(dotenv.get("DB_PASSWORD"));
            } 
            // Only if .env not found, try environment variables
            else if (System.getenv("DB_URL") != null){
                config.setJdbcUrl(System.getenv("DB_URL"));
                config.setUsername(System.getenv("DB_USERNAME")); 
                config.setPassword(System.getenv("DB_PASSWORD"));
            }
            else{
                JOptionPane.showMessageDialog(null, "Database connection details not found. Please set the .env" +
                "with the variables DB_URL, DB_USERNAME, and DB_PASSWORD.");
                System.exit(1);
            }
            
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(20000);
            config.setMaxLifetime(600000);
            
            dataSource = new HikariDataSource(config);
            
        }catch (Exception e){
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public static Connection getConnection() throws SQLException{
        return dataSource.getConnection(); // Get a connection from the pool
    }
    
    public static void closePool(){
            dataSource.close(); // Close the pool when the app shuts down
    }

}