package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/db_tiw?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "0830";

    static {
        try {
            // Ensure the JDBC driver is loaded by explicitly loading it
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find JDBC driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
