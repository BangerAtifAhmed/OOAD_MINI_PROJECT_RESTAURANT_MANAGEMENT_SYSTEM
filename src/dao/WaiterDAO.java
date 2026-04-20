package dao;

import model.Waiter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WaiterDAO {

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Waiter> getActiveWaiters() throws SQLException {
        List<Waiter> list = new ArrayList<>();
        String sql = "SELECT * FROM waiters WHERE active = TRUE ORDER BY name";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Waiter> getAll() throws SQLException {
        List<Waiter> list = new ArrayList<>();
        String sql = "SELECT * FROM waiters ORDER BY name";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void insert(Waiter w) throws SQLException {
        String sql = "INSERT INTO waiters (name, phone, active) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, w.getName());
            ps.setString(2, w.getPhone());
            ps.setBoolean(3, w.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) w.setId(keys.getInt(1));
            }
        }
    }

    public void update(Waiter w) throws SQLException {
        String sql = "UPDATE waiters SET name=?, phone=?, active=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, w.getName());
            ps.setString(2, w.getPhone());
            ps.setBoolean(3, w.isActive());
            ps.setInt(4, w.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM waiters WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Waiter map(ResultSet rs) throws SQLException {
        Waiter w = new Waiter(rs.getString("name"), rs.getString("phone"), rs.getBoolean("active"));
        w.setId(rs.getInt("id"));
        return w;
    }
}
