package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ensures all required tables exist in the database.
 * Called once at application startup — safe to run every time (IF NOT EXISTS).
 */
public class DatabaseInitializer {

    public static void init() throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (Statement st = conn.createStatement()) {

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS restaurant_tables (" +
                "  id           INT AUTO_INCREMENT PRIMARY KEY," +
                "  table_number INT  NOT NULL UNIQUE," +
                "  capacity     INT  NOT NULL," +
                "  status       ENUM('AVAILABLE','OCCUPIED') NOT NULL DEFAULT 'AVAILABLE'" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS waiters (" +
                "  id     INT AUTO_INCREMENT PRIMARY KEY," +
                "  name   VARCHAR(100) NOT NULL," +
                "  phone  VARCHAR(20)," +
                "  active BOOLEAN NOT NULL DEFAULT TRUE" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS menu_items (" +
                "  id                INT AUTO_INCREMENT PRIMARY KEY," +
                "  name              VARCHAR(100)  NOT NULL," +
                "  category          VARCHAR(50)   NOT NULL," +
                "  base_price        DECIMAL(10,2) NOT NULL," +
                "  description       TEXT," +
                "  prep_time_minutes INT  NOT NULL DEFAULT 10," +
                "  is_available      BOOLEAN NOT NULL DEFAULT TRUE" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS customers (" +
                "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  name       VARCHAR(100) NOT NULL," +
                "  phone      VARCHAR(20)," +
                "  email      VARCHAR(100)," +
                "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS orders (" +
                "  id          INT AUTO_INCREMENT PRIMARY KEY," +
                "  table_id    INT NOT NULL," +
                "  customer_id INT," +
                "  waiter_name VARCHAR(100)," +
                "  status      ENUM('RECEIVED','COOKING','READY','SERVED','CANCELLED') NOT NULL DEFAULT 'RECEIVED'," +
                "  total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                "  notes       TEXT," +
                "  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (table_id)    REFERENCES restaurant_tables(id)," +
                "  FOREIGN KEY (customer_id) REFERENCES customers(id)" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS order_items (" +
                "  id                   INT AUTO_INCREMENT PRIMARY KEY," +
                "  order_id             INT           NOT NULL," +
                "  menu_item_id         INT           NOT NULL," +
                "  item_name            VARCHAR(200)  NOT NULL," +
                "  quantity             INT           NOT NULL DEFAULT 1," +
                "  unit_price           DECIMAL(10,2) NOT NULL," +
                "  toppings_description VARCHAR(500)," +
                "  toppings_extra_cost  DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                "  item_total           DECIMAL(10,2) NOT NULL," +
                "  FOREIGN KEY (order_id)     REFERENCES orders(id) ON DELETE CASCADE," +
                "  FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS payments (" +
                "  id             INT AUTO_INCREMENT PRIMARY KEY," +
                "  order_id       INT           NOT NULL UNIQUE," +
                "  amount         DECIMAL(10,2) NOT NULL," +
                "  payment_method ENUM('CASH','STRIPE','PAYPAL') NOT NULL," +
                "  transaction_id VARCHAR(100)," +
                "  receipt_number VARCHAR(50)   NOT NULL UNIQUE," +
                "  status         ENUM('SUCCESS','FAILED') NOT NULL DEFAULT 'SUCCESS'," +
                "  paid_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (order_id) REFERENCES payments(id)" +
                ")"
            );

            System.out.println("Database tables verified.");
        }
    }
}
