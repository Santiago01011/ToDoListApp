package DBH;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class PSQLtdldbh {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String JDBC_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static{
        JDBC_URL = dotenv.get("DB_URL");
        DB_USER = dotenv.get("DB_USERNAME");
        DB_PASSWORD = dotenv.get("DB_PASSWORD");

        if (JDBC_URL == null || DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Environment variables for database connection are not set properly.");
        }
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }
}