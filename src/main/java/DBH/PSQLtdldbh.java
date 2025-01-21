package DBH;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import io.github.cdimascio.dotenv.Dotenv;

public class PSQLtdldbh {
    private static HikariDataSource localDataSource;
    private static HikariDataSource cloudDataSource;
    private static final boolean USE_CLOUD = false;
    // Create a connection pool that reuses the same connection 
    // rather than creating a new one every time a connection is requested
    
    static{
        init();
        H2Manager.H2dbchanges();
    }

    public static void init(){        
        try{
           
            // H2 embedded database - init the local connection
            HikariConfig localConfig = new HikariConfig();
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + "/.todoapp/taskdb";
            localConfig.setJdbcUrl("jdbc:h2:file:" + dbPath);
            localConfig.setUsername("sa");
            localConfig.setPassword("sa");
            configurePoolSettings(localConfig);
            localDataSource = new HikariDataSource(localConfig);
            
            HikariConfig cloudConfig = new HikariConfig();
            if (new File(".env").exists()){
                System.out.println("Using .env file for database connection details");
                Dotenv dotenv = Dotenv.load();
                cloudConfig.setJdbcUrl(dotenv.get("DB_URL"));
                cloudConfig.setUsername(dotenv.get("DB_USERNAME"));
                cloudConfig.setPassword(dotenv.get("DB_PASSWORD"));
                configurePoolSettings(cloudConfig);
                cloudDataSource = new HikariDataSource(cloudConfig);
            }
            else if (System.getenv("DB_URL") != null){
                cloudConfig.setJdbcUrl(System.getenv("DB_URL"));
                cloudConfig.setUsername(System.getenv("DB_USERNAME")); 
                cloudConfig.setPassword(System.getenv("DB_PASSWORD"));
                configurePoolSettings(cloudConfig);
                cloudDataSource = new HikariDataSource(cloudConfig);
            }
            else{
                JOptionPane.showMessageDialog(null, "Cloud database connection details not found. Please set the .env" +
                    "with the variables DB_URL, DB_USERNAME, and DB_PASSWORD.");
            }     
        }catch (Exception e){
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    // Method to configure the connection pool settings
    private static void configurePoolSettings(HikariConfig config){
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(600000);
    }

    public static Connection getCloudConnection() throws SQLException{
        if (cloudDataSource == null) {
            throw new SQLException("Cloud database connection not configured");
        }
        return cloudDataSource.getConnection();
    }

    public static Connection getLocalConnection() throws SQLException{
        return localDataSource.getConnection();
    }

    public static Connection getConnection() throws SQLException{
        return USE_CLOUD ? getCloudConnection() : getLocalConnection();        
    }
    
    // Method to close the connection pool
    public static void closePool(){
        if (cloudDataSource != null){
            cloudDataSource.close();
        }
    }

     // Method to check if cloud sync is available
     public static boolean isCloudAvailable() {
        return cloudDataSource != null;
    }

    
}