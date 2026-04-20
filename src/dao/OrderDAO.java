package dao;

import model.Order;
import model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Full CRUD for orders + order_items (FR-2, FR-6) */
public class OrderDAO {

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Save new order ───────────────────────────────────────────────────────

    public void save(Order order) throws SQLException {
        String sql = "INSERT INTO orders (table_id,customer_id,waiter_name,status,total_price,notes) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getTableId());
            if (order.getCustomerId() > 0) ps.setInt(2, order.getCustomerId());
            else                           ps.setNull(2, Types.INTEGER);
            ps.setString(3, order.getWaiterId());
            ps.setString(4, order.getStatus());
            ps.setDouble(5, order.getTotalPrice());
            ps.setString(6, order.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) order.setId(keys.getInt(1));
            }
        }
        saveItems(order);
    }

    private void saveItems(Order order) throws SQLException {
        String sql = "INSERT INTO order_items (order_id,menu_item_id,item_name,quantity,unit_price,toppings_description,toppings_extra_cost,item_total) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : order.getItems()) {
                ps.setInt(1,    order.getId());
                ps.setInt(2,    item.getMenuItemId());
                ps.setString(3, item.getItemName());
                ps.setInt(4,    item.getQuantity());
                ps.setDouble(5, item.getUnitPrice());
                ps.setString(6, item.getToppingsDescription());
                ps.setDouble(7, item.getToppingsExtraCost());
                ps.setDouble(8, item.getItemTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── Update status ────────────────────────────────────────────────────────

    public void updateStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Order> getActiveOrders() throws SQLException {
        return queryOrders(
            "SELECT * FROM orders WHERE status IN ('RECEIVED','COOKING','READY') ORDER BY created_at");
    }

    public List<Order> getAllOrders() throws SQLException {
        return queryOrders("SELECT * FROM orders ORDER BY created_at DESC");
    }

    /**
     * Filter orders by optional date range and/or status.
     * Pass null to skip a filter.
     * Dates should be in "yyyy-MM-dd" format.
     */
    public List<Order> getFilteredOrders(String fromDate, String toDate, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE 1=1");
        if (fromDate != null && !fromDate.isBlank())
            sql.append(" AND DATE(created_at) >= '").append(fromDate).append("'");
        if (toDate != null && !toDate.isBlank())
            sql.append(" AND DATE(created_at) <= '").append(toDate).append("'");
        if (status != null && !status.equals("ALL"))
            sql.append(" AND status = '").append(status).append("'");
        sql.append(" ORDER BY created_at DESC");
        return queryOrders(sql.toString());
    }

    public Order getById(int id) throws SQLException {
        List<Order> list = queryOrders("SELECT * FROM orders WHERE id = " + id);
        return list.isEmpty() ? null : list.get(0);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<Order> queryOrders(String sql) throws SQLException {
        List<Order> list = new ArrayList<>();
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Order o = mapOrder(rs);
                o.setItems(getItemsForOrder(o.getId()));
                list.add(o);
            }
        }
        return list;
    }

    private List<OrderItem> getItemsForOrder(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getInt("id"));
                    oi.setOrderId(orderId);
                    oi.setMenuItemId(rs.getInt("menu_item_id"));
                    oi.setItemName(rs.getString("item_name"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setUnitPrice(rs.getDouble("unit_price"));
                    oi.setToppingsDescription(rs.getString("toppings_description"));
                    oi.setToppingsExtraCost(rs.getDouble("toppings_extra_cost"));
                    oi.setItemTotal(rs.getDouble("item_total"));
                    items.add(oi);
                }
            }
        }
        return items;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setTableId(rs.getInt("table_id"));
        int cid = rs.getInt("customer_id"); if (!rs.wasNull()) o.setCustomerId(cid);
        o.setWaiterId(rs.getString("waiter_name"));
        o.setState(Order.stateFromString(rs.getString("status")));
        o.setTotalPrice(rs.getDouble("total_price"));
        o.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) o.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");  if (ua != null) o.setUpdatedAt(ua.toLocalDateTime());
        return o;
    }
}
