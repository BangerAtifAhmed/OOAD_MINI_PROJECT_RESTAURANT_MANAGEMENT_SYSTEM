package dao;

import model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Save and retrieve payment records (FR-3, FR-7) */
public class PaymentDAO {

    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public void save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (order_id,amount,payment_method,transaction_id,receipt_number,status) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    payment.getOrderId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getTransactionId());
            ps.setString(5, payment.getReceiptNumber());
            ps.setString(6, payment.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) payment.setId(keys.getInt(1));
            }
        }
    }

    public Payment getByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Payment> getAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY paid_at DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment(
                rs.getInt("order_id"),
                rs.getDouble("amount"),
                rs.getString("payment_method"),
                rs.getString("transaction_id"),
                rs.getString("receipt_number"),
                rs.getString("status")
        );
        p.setId(rs.getInt("id"));
        Timestamp ts = rs.getTimestamp("paid_at");
        if (ts != null) p.setPaidAt(ts.toLocalDateTime());
        return p;
    }
}
