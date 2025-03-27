package DBH;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import COMMON.UserProperties;

public class NeonPool {
    private static String DB_URL;

    static {
        try {
            DB_URL = (String) UserProperties.getProperty("dbUrl");
        } catch (Exception e) {
            System.err.println("Error al cargar la URL de la base de datos: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (DB_URL == null) {
            throw new IllegalStateException("La URL de la base de datos no está configurada.");
        }
        return DriverManager.getConnection(DB_URL);
    }
}
