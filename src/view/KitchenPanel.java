package view;

import controller.OrderController;
import model.Order;
import model.OrderItem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class KitchenPanel extends JPanel {

    private final OrderController orderController = new OrderController();

    private DefaultTableModel queueModel;
    private JTable            queueTable;
    private JTextArea         detailArea;
    private JLabel            statusBar;

    public KitchenPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.PAGE_BG);
        add(UITheme.pageHeader("Kitchen Dashboard",
                "Advance order states: RECEIVED → COOKING → READY → SERVED"), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        startAutoRefresh();
        refresh();
    }

    // ── Body ──────────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(UITheme.PAGE_BG);
        body.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        body.add(buildQueueCard(), BorderLayout.CENTER);
        body.add(buildDetailCard(), BorderLayout.EAST);
        body.add(buildButtonBar(),  BorderLayout.SOUTH);
        return body;
    }

    // ── Queue table ───────────────────────────────────────────────────────────

    private JPanel buildQueueCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new LineBorder(UITheme.BORDER, 1));

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        hdr.add(UITheme.sectionLabel("Active Order Queue  (oldest first)"));
        card.add(hdr, BorderLayout.NORTH);

        String[] cols = {"Order #", "Table", "Status", "Total (₹)", "Time"};
        queueModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        queueTable = new JTable(queueModel);
        UITheme.styleTable(queueTable);
        applyStatusRenderer();
        queueTable.getSelectionModel().addListSelectionListener(e -> showDetail());
        card.add(UITheme.scroll(queueTable), BorderLayout.CENTER);

        statusBar = new JLabel("  Ready");
        statusBar.setFont(UITheme.F_SMALL);
        statusBar.setForeground(UITheme.TEXT_MUTED);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        card.add(statusBar, BorderLayout.SOUTH);
        return card;
    }

    // ── Detail panel ──────────────────────────────────────────────────────────

    private JPanel buildDetailCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new LineBorder(UITheme.BORDER, 1));
        card.setPreferredSize(new Dimension(300, 0));

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        hdr.add(UITheme.sectionLabel("Order Items"));
        card.add(hdr, BorderLayout.NORTH);

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(UITheme.F_MONO);
        detailArea.setBackground(UITheme.CARD_BG);
        detailArea.setForeground(UITheme.TEXT);
        detailArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        card.add(UITheme.scroll(detailArea), BorderLayout.CENTER);
        return card;
    }

    // ── Button bar ────────────────────────────────────────────────────────────

    private JPanel buildButtonBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        p.setBackground(UITheme.PAGE_BG);

        JButton advBtn = UITheme.button("▶   Advance Status", UITheme.Btn.SUCCESS);
        JButton refBtn = UITheme.button("⟳   Refresh",        UITheme.Btn.SECONDARY);
        advBtn.addActionListener(e -> advanceSelected());
        refBtn.addActionListener(e -> refresh());
        p.add(advBtn); p.add(refBtn);
        return p;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    public void refresh() {
        int sel = queueTable.getSelectedRow();
        queueModel.setRowCount(0);
        try {
            List<Order> orders = orderController.getActiveOrders();
            for (Order o : orders) {
                String time = o.getCreatedAt() != null
                        ? o.getCreatedAt().toLocalTime().toString().substring(0, 5) : "?";
                queueModel.addRow(new Object[]{
                        o.getId(), "Table " + o.getTableId(), o.getStatus(),
                        String.format("%.2f", o.getTotalPrice()), time });
            }
            statusBar.setText("  Last refresh: " + java.time.LocalTime.now().toString().substring(0, 8)
                    + "   |   Active orders: " + orders.size());
            if (sel >= 0 && sel < queueTable.getRowCount())
                queueTable.setRowSelectionInterval(sel, sel);
        } catch (SQLException ex) {
            statusBar.setText("  Error: " + ex.getMessage());
        }
    }

    private void showDetail() {
        int row = queueTable.getSelectedRow();
        if (row < 0) return;
        int orderId = (int) queueModel.getValueAt(row, 0);
        try {
            Order o = orderController.getOrderById(orderId);
            if (o == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("Order #").append(o.getId())
              .append("  |  Table ").append(o.getTableId())
              .append("  |  ").append(o.getStatus()).append("\n");
            sb.append("Waiter: ").append(o.getWaiterId()).append("\n");
            if (o.getNotes() != null && !o.getNotes().isBlank())
                sb.append("Notes: ").append(o.getNotes()).append("\n");
            sb.append("─".repeat(32)).append("\n");
            for (OrderItem item : o.getItems()) {
                String name = item.getItemName().length() > 20
                        ? item.getItemName().substring(0, 17) + "..." : item.getItemName();
                sb.append(String.format("%-20s x%d  ₹%.2f%n", name, item.getQuantity(), item.getItemTotal()));
            }
            sb.append("─".repeat(32)).append("\n");
            sb.append(String.format("TOTAL:  ₹%.2f%n", o.getTotalPrice()));
            detailArea.setText(sb.toString());
        } catch (SQLException ex) {
            detailArea.setText("Error: " + ex.getMessage());
        }
    }

    private void advanceSelected() {
        int row = queueTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
        int    orderId = (int) queueModel.getValueAt(row, 0);
        String current = (String) queueModel.getValueAt(row, 2);
        try {
            orderController.advanceOrder(orderId);
            String msg = switch (current) {
                case "RECEIVED" -> "Order #" + orderId + " moved to COOKING.";
                case "COOKING"  -> "Order #" + orderId + " is READY!";
                case "READY"    -> "Order #" + orderId + " marked as SERVED.";
                default         -> "Order #" + orderId + " updated.";
            };
            JOptionPane.showMessageDialog(this, msg);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyStatusRenderer() {
        queueTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(CENTER);
                setFont(UITheme.F_BOLD);
                if (!sel) {
                    setBackground(UITheme.statusBg(v == null ? "" : v.toString()));
                    setForeground(UITheme.TEXT);
                }
                return this;
            }
        });
    }

    private void startAutoRefresh() {
        new Timer(15_000, e -> refresh()).start();
    }
}
