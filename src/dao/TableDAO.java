package dao;

import model.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Read / status-update operations for restaurant_tables */
public class TableDAO {

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Table> getAllTables() throws SQLException {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM restaurant_tables ORDER BY table_number";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Table> getAvailableTables() throws SQLException {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM restaurant_tables WHERE status='AVAILABLE' ORDER BY table_number";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Table getById(int id) throws SQLException {
        String sql = "SELECT * FROM restaurant_tables WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insertTable(int tableNumber, int capacity) throws SQLException {
        String sql = "INSERT INTO restaurant_tables (table_number, capacity) VALUES (?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, tableNumber);
            ps.setInt(2, capacity);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE restaurant_tables SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Table map(ResultSet rs) throws SQLException {
        return new Table(
                rs.getInt("id"),
                rs.getInt("table_number"),
                rs.getInt("capacity"),
                Table.Status.valueOf(rs.getString("status"))
        );
    }
}
