package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton — Database connection manager (SRP: owns all JDBC setup).
 *
 * Configure DB_URL / USER / PASSWORD to match your MySQL installation.
 * Place mysql-connector-j-*.jar inside the lib/ folder and add it to the classpath.
 */
public class DatabaseManager {

    // ── Configuration ────────────────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/restaurant_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "atif";   // change to your MySQL password
    // ─────────────────────────────────────────────────────────────────────────

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "MySQL JDBC Driver not found. Add mysql-connector-j-*.jar to lib/", e);
        }
        this.connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
