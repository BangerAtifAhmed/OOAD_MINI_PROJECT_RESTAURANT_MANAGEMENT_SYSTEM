package view;

import dao.OrderDAO;
import model.Order;
import model.OrderItem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderHistoryPanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();

    private DefaultTableModel historyModel;
    private JTable            historyTable;
    private JTextArea         detailArea;

    private DatePickerField   fromDateF, toDateF;
    private JComboBox<String> statusFilter;
    private JLabel            summaryLabel;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy  HH:mm");

    public OrderHistoryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.PAGE_BG);
        add(UITheme.pageHeader("Order History",
                "Filter by date range and status — newest orders shown first."), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        refresh();
    }

    // ── Body ──────────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(UITheme.PAGE_BG);
        body.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        body.add(buildFilterBar(),   BorderLayout.NORTH);
        body.add(buildMainArea(),    BorderLayout.CENTER);
        body.add(buildSummaryBar(),  BorderLayout.SOUTH);
        return body;
    }

    // ── Filter bar ────────────────────────────────────────────────────────────

    private JPanel buildFilterBar() {
        JPanel card = UITheme.card(new FlowLayout(FlowLayout.LEFT, 12, 6));

        fromDateF = new DatePickerField(LocalDate.now().withDayOfMonth(1));
        toDateF   = new DatePickerField(LocalDate.now());

        statusFilter = new JComboBox<>(
                new String[]{"ALL","RECEIVED","COOKING","READY","SERVED","CANCELLED"});
        statusFilter.setFont(UITheme.F_BODY);
        statusFilter.setPreferredSize(new Dimension(130, 30));

        JButton searchBtn = UITheme.button("🔍  Search",       UITheme.Btn.PRIMARY);
        JButton clearBtn  = UITheme.button("✕  Clear Filters", UITheme.Btn.SECONDARY);

        searchBtn.addActionListener(e -> refresh());
        clearBtn.addActionListener(e  -> { fromDateF.setDate(null); toDateF.setDate(null);
                                           statusFilter.setSelectedIndex(0); refresh(); });

        card.add(lbl("From:")); card.add(fromDateF);
        card.add(lbl("To:"));   card.add(toDateF);
        card.add(lbl("Status:")); card.add(statusFilter);
        card.add(searchBtn); card.add(clearBtn);
        return card;
    }

    // ── Main split ────────────────────────────────────────────────────────────

    private JSplitPane buildMainArea() {
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildHistoryCard(), buildDetailCard());
        sp.setBackground(UITheme.PAGE_BG);
        sp.setBorder(null);
        sp.setDividerSize(8);
        sp.setResizeWeight(0.65);
        return sp;
    }

    private JPanel buildHistoryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new LineBorder(UITheme.BORDER, 1));

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        hdr.add(UITheme.sectionLabel("Orders  (newest first)"));
        card.add(hdr, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(
                new String[]{"Order #","Table","Waiter","Status","Total (₹)","Date & Time"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        UITheme.styleTable(historyTable);
        applyStatusRenderer();
        historyTable.getSelectionModel().addListSelectionListener(e -> showDetail());
        card.add(UITheme.scroll(historyTable), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDetailCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new LineBorder(UITheme.BORDER, 1));
        card.setPreferredSize(new Dimension(290, 0));

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        hdr.add(UITheme.sectionLabel("Order Details"));
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

    private JPanel buildSummaryBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        p.setBackground(UITheme.PAGE_BG);
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(UITheme.F_BOLD);
        summaryLabel.setForeground(UITheme.TEXT_MUTED);
        p.add(summaryLabel);
        return p;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    public void refresh() {
        historyModel.setRowCount(0);
        detailArea.setText("");
        try {
            List<Order> orders = orderDAO.getFilteredOrders(
                    fromDateF.getText(), toDateF.getText(),
                    (String) statusFilter.getSelectedItem());

            double revenue = 0; int served = 0;
            for (Order o : orders) {
                String date = o.getCreatedAt() != null ? o.getCreatedAt().format(DISPLAY_FMT) : "?";
                historyModel.addRow(new Object[]{
                        o.getId(), "Table " + o.getTableId(),
                        o.getWaiterId() != null ? o.getWaiterId() : "—",
                        o.getStatus(), String.format("%.2f", o.getTotalPrice()), date });
                if ("SERVED".equals(o.getStatus())) { revenue += o.getTotalPrice(); served++; }
            }
            summaryLabel.setText(String.format(
                    "  Found: %d orders   |   Served: %d   |   Revenue: ₹%.2f",
                    orders.size(), served, revenue));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetail() {
        int row = historyTable.getSelectedRow(); if (row < 0) return;
        int orderId = (int) historyModel.getValueAt(row, 0);
        try {
            Order o = orderDAO.getById(orderId); if (o == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("═".repeat(32)).append("\n");
            sb.append(String.format("Order  #%d%n", o.getId()));
            sb.append(String.format("Table  : %d%n", o.getTableId()));
            sb.append(String.format("Waiter : %s%n", o.getWaiterId() != null ? o.getWaiterId() : "—"));
            sb.append(String.format("Status : %s%n", o.getStatus()));
            if (o.getCreatedAt() != null)
                sb.append(String.format("Date   : %s%n", o.getCreatedAt().format(DISPLAY_FMT)));
            if (o.getNotes() != null && !o.getNotes().isBlank())
                sb.append(String.format("Notes  : %s%n", o.getNotes()));
            sb.append("─".repeat(32)).append("\n");
            sb.append(String.format("%-22s %8s%n","Item","₹"));
            sb.append("─".repeat(32)).append("\n");
            for (OrderItem item : o.getItems()) {
                String name = item.getItemName();
                if (name.length() > 22) name = name.substring(0, 19) + "...";
                sb.append(String.format("%-22s %8.2f%n", name, item.getItemTotal()));
                if (item.getToppingsExtraCost() > 0)
                    sb.append(String.format("  toppings  +₹%.2f%n", item.getToppingsExtraCost()));
            }
            sb.append("─".repeat(32)).append("\n");
            sb.append(String.format("%-22s %8.2f%n","TOTAL", o.getTotalPrice()));
            sb.append("═".repeat(32)).append("\n");
            detailArea.setText(sb.toString());
            detailArea.setCaretPosition(0);
        } catch (SQLException ex) { detailArea.setText("Error: " + ex.getMessage()); }
    }

    private void applyStatusRenderer() {
        historyTable.getColumnModel().getColumn(3).setCellRenderer(
                new DefaultTableCellRenderer() {
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

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text); l.setFont(UITheme.F_BOLD); l.setForeground(UITheme.TEXT); return l;
    }
}
