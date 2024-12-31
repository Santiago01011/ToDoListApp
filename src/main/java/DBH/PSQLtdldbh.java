package DBH;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class PSQLtdldbh {
    private static final HikariDataSource dataSource;
    private static final Dotenv dotenv = Dotenv.load();


    static{
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dotenv.get("DB_URL"));
        config.setUsername(dotenv.get("DB_USERNAME"));
        config.setPassword(dotenv.get("DB_PASSWORD"));
        config.setMaximumPoolSize(10); // Maximum number of connections
        config.setMinimumIdle(2); // Minimum idle connections
        config.setIdleTimeout(30000); // 30 seconds idle timeout
        config.setMaxLifetime(1800000); // 30 minutes max lifetime for a connection
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // Get a connection from the pool
    }

    public static void closePool() {
        dataSource.close(); // Close the pool when the app shuts down
    }
}