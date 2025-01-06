package DBH;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import io.github.cdimascio.dotenv.Dotenv;

public class PSQLtdldbh {
    private static final HikariDataSource dataSource;
    private static Dotenv dotenv;

    // Create a connection pool that reuses the same connection 
    // rather than creating a new one every time a connection is requested
    static{
        HikariConfig config = new HikariConfig();
        dotenv = Dotenv.load();
        if(dotenv.get("DB_URL") != null){
            JOptionPane.showMessageDialog(null, "Using .env file");
            config.setJdbcUrl(dotenv.get("DB_URL"));
            config.setUsername(dotenv.get("DB_USERNAME"));
            config.setPassword(dotenv.get("DB_PASSWORD"));
        }else if(System.getenv("DB_URL") != null){
            JOptionPane.showMessageDialog(null, "Using environment variables");
            config.setJdbcUrl(System.getenv("DB_URL"));
            config.setUsername(System.getenv("DB_USERNAME"));
            config.setPassword(System.getenv("DB_PASSWORD"));
        }else{
            JOptionPane.showMessageDialog(null, "No database configuration found");
            throw new RuntimeException("No database configuration found");
        }
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(1800000);
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException{
        return dataSource.getConnection(); // Get a connection from the pool
    }
    
    public static void closePool(){
            dataSource.close(); // Close the pool when the app shuts down
        }

    //old connection method
    // public static Connection getConnection() throws SQLException {
    //     String url = dotenv.get("DB_URL");
    //     String username = dotenv.get("DB_USERNAME");
    //     String password = dotenv.get("DB_PASSWORD");
    //     return java.sql.DriverManager.getConnection(url, username, password);
    // }
}