import dao.DatabaseInitializer;
import dao.DatabaseManager;
import view.MainFrame;

import javax.swing.*;

/**
 * FoodieFlow — Restaurant Management System
 *
 * Entry point. Verifies the DB connection, then launches the Swing UI.
 *
 * Prerequisites:
 *   1. MySQL running on localhost:3306
 *   2. Run schema.sql once to create the database and seed data
 *   3. Update DatabaseManager.PASSWORD if needed
 *   4. Add mysql-connector-j-*.jar to the classpath (lib/ folder)
 *
 * Compile:
 *   javac -cp "lib/*" -sourcepath src -d out src/Main.java
 * Run:
 *   java  -cp "out;lib/*" Main       (Windows)
 *   java  -cp "out:lib/*" Main       (Linux/Mac)
 */
public class Main {
    public static void main(String[] args) {

        // Test DB connection and auto-create any missing tables
        try {
            DatabaseManager.getInstance();
            System.out.println("Database connection successful.");
            DatabaseInitializer.init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the database.\n\n" + e.getMessage() +
                    "\n\nPlease:\n" +
                    "  1. Start MySQL server\n" +
                    "  2. Run schema.sql\n" +
                    "  3. Check credentials in DatabaseManager.java",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            view.UITheme.apply();
            new MainFrame().setVisible(true);
        });
    }
}
