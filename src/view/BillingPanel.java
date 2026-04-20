package view;

import controller.OrderController;
import controller.PaymentController;
import model.Order;
import model.Payment;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BillingPanel extends JPanel {

    private final OrderController   orderController   = new OrderController();
    private final PaymentController paymentController = new PaymentController();

    private DefaultTableModel pendingModel;
    private JTable            pendingTable;
    private DefaultTableModel historyModel;
    private JLabel            orderDetailLabel;

    public BillingPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.PAGE_BG);
        add(UITheme.pageHeader("Billing & Checkout",
                "Select a served order and choose a payment method to process."), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        refresh();
    }

    // ── Body ──────────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(UITheme.PAGE_BG);
        body.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        body.add(buildPendingCard(), BorderLayout.CENTER);
        body.add(buildHistoryCard(), BorderLayout.SOUTH);
        return body;
    }

    // ── Pending orders card ───────────────────────────────────────────────────

    private JPanel buildPendingCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new LineBorder(UITheme.BORDER, 1));

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        hdr.add(UITheme.sectionLabel("Served Orders — Awaiting Payment"));
        card.add(hdr, BorderLayout.NORTH);

        pendingModel = new DefaultTableModel(
                new String[]{"Order #", "Table", "Waiter", "Total (₹)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pendingTable = new JTable(pendingModel);
        UITheme.styleTable(pendingTable);
        pendingTable.getSelectionModel().addListSelectionListener(e -> updateDetail());
        card.add(UITheme.scroll(pendingTable), BorderLayout.CENTER);
        card.add(buildPaymentFooter(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildPaymentFooter() {
        JPanel foot = new JPanel(new BorderLayout(0, 8));
        foot.setBackground(UITheme.CARD_BG);
        foot.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UITheme.BORDER),
                new EmptyBorder(10, 14, 10, 14)));

        orderDetailLabel = new JLabel(" ");
        orderDetailLabel.setFont(UITheme.F_BOLD);
        orderDetailLabel.setForeground(UITheme.TEXT_MUTED);
        foot.add(orderDetailLabel, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        btnRow.setBackground(UITheme.CARD_BG);

        JButton cashBtn   = payBtn("💵  Cash",   UITheme.Btn.SUCCESS);
        JButton stripeBtn = payBtn("💳  Stripe", UITheme.Btn.PRIMARY);
        JButton ppBtn     = payBtn("🅿  PayPal", UITheme.Btn.WARNING);
        JButton refBtn    = UITheme.button("⟳  Refresh", UITheme.Btn.SECONDARY);

        cashBtn.addActionListener(e   -> processPayment("CASH"));
        stripeBtn.addActionListener(e -> processPayment("STRIPE"));
        ppBtn.addActionListener(e     -> processPayment("PAYPAL"));
        refBtn.addActionListener(e    -> refresh());

        btnRow.add(cashBtn); btnRow.add(stripeBtn); btnRow.add(ppBtn); btnRow.add(refBtn);
        foot.add(btnRow, BorderLayout.CENTER);
        return foot;
    }

    private JButton payBtn(String label, UITheme.Btn type) {
        JButton b = UITheme.button(label, type);
        b.setPreferredSize(new Dimension(140, 36));
        return b;
    }

    // ── Payment history card ──────────────────────────────────────────────────

    private JPanel buildHistoryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new LineBorder(UITheme.BORDER, 1));
        card.setPreferredSize(new Dimension(0, 185));

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        hdr.add(UITheme.sectionLabel("Payment History"));
        card.add(hdr, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(
                new String[]{"Receipt No", "Order #", "Method", "Amount (₹)", "Status", "Time"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable histTable = new JTable(historyModel);
        UITheme.styleTable(histTable);
        card.add(UITheme.scroll(histTable), BorderLayout.CENTER);
        return card;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    public void refresh() {
        pendingModel.setRowCount(0);
        historyModel.setRowCount(0);
        try {
            for (Order o : orderController.getAllOrders()) {
                if ("SERVED".equals(o.getStatus()))
                    pendingModel.addRow(new Object[]{
                            o.getId(), "Table " + o.getTableId(),
                            o.getWaiterId(), String.format("%.2f", o.getTotalPrice()) });
            }
            for (Payment p : new dao.PaymentDAO().getAll()) {
                String time = p.getPaidAt() != null
                        ? p.getPaidAt().toLocalTime().toString().substring(0, 8) : "?";
                historyModel.addRow(new Object[]{
                        p.getReceiptNumber(), p.getOrderId(),
                        p.getPaymentMethod(), String.format("%.2f", p.getAmount()),
                        p.getStatus(), time });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDetail() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) { orderDetailLabel.setText(" "); return; }
        double total   = Double.parseDouble(pendingModel.getValueAt(row, 3).toString());
        int    orderId = (int) pendingModel.getValueAt(row, 0);
        orderDetailLabel.setText(
                String.format("  Order #%d   |   Total: ₹%.2f   — choose payment method:", orderId, total));
    }

    private void processPayment(String method) {
        int row = pendingTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
        int orderId = (int) pendingModel.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this,
                "Process " + method + " payment for Order #" + orderId + "?",
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            Payment p = paymentController.processPayment(orderId, method);
            JOptionPane.showMessageDialog(this,
                    "✅  Payment successful!\n\nReceipt:  " + p.getReceiptNumber() +
                    "\nTxn ID:   " + p.getTransactionId() +
                    "\n\nReceipt saved in receipts/ folder.");
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
