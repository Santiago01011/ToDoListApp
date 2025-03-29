package DBH;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import COMMON.UserProperties;

public class NeonPool {
    private static String dbUrl;

    static {
        try {
            dbUrl = (String) UserProperties.getProperty("dbUrl");
        } catch (Exception e) {
            System.err.println("Error al configurar la conexión a la base de datos: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dbUrl == null) {
            throw new IllegalStateException("La URL de la base de datos no está configurada.");
        }
        return DriverManager.getConnection(dbUrl);
    }

    public static void closePool() {
        // No pooling mechanism to close
    }
}
