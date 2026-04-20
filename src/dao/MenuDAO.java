package dao;

import model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for the menu_items table (FR-5) */
public class MenuDAO {

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public List<MenuItem> getAvailableItems() throws SQLException {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE is_available = TRUE ORDER BY category, name";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<MenuItem> getAllItems() throws SQLException {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items ORDER BY category, name";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public MenuItem getById(int id) throws SQLException {
        String sql = "SELECT * FROM menu_items WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // ── Write ────────────────────────────────────────────────────────────────

    public void insert(MenuItem item) throws SQLException {
        String sql = "INSERT INTO menu_items (name,category,base_price,description,prep_time_minutes,is_available) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setDouble(3, item.getBasePrice());
            ps.setString(4, item.getDescription());
            ps.setInt(5,    item.getPrepTimeMinutes());
            ps.setBoolean(6, item.isAvailable());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }
        }
    }

    public void update(MenuItem item) throws SQLException {
        String sql = "UPDATE menu_items SET name=?,category=?,base_price=?,description=?,prep_time_minutes=?,is_available=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setDouble(3, item.getBasePrice());
            ps.setString(4, item.getDescription());
            ps.setInt(5,    item.getPrepTimeMinutes());
            ps.setBoolean(6, item.isAvailable());
            ps.setInt(7,    item.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private MenuItem map(ResultSet rs) throws SQLException {
        return new MenuItem(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("base_price"),
                rs.getString("description"),
                rs.getInt("prep_time_minutes"),
                rs.getBoolean("is_available")
        );
    }
}
