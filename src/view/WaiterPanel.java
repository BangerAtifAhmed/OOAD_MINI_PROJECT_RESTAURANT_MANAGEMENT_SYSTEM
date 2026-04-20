package view;

import controller.MenuController;
import controller.OrderController;
import dao.WaiterDAO;
import model.BaseFoodItem;
import model.FoodItem;
import model.MenuItem;
import model.Order;
import model.OrderItem;
import model.Table;
import model.Waiter;
import model.decorator.*;
import singleton.FloorManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WaiterPanel extends JPanel {

    private final MenuController  menuController  = new MenuController();
    private final OrderController orderController = new OrderController();
    private final WaiterDAO       waiterDAO       = new WaiterDAO();

    private JComboBox<String> tableCombo;
    private JComboBox<Waiter> waiterCombo;
    private JList<String>     menuList;
    private DefaultListModel<String> menuModel;
    private List<MenuItem> menuItems = new ArrayList<>();

    private JCheckBox chkCheese, chkPaneer, chkChilli, chkMushroom;

    private DefaultTableModel orderTableModel;
    private JTable            orderTable;
    private JLabel            totalLabel;
    private JTextField        notesField;

    private List<FoodItem> currentOrderItems = new ArrayList<>();

    public WaiterPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.PAGE_BG);
        add(UITheme.pageHeader("Waiter — Place Order",
                "Select table & waiter, build the order, apply toppings, then place."), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        loadMenu();
    }

    public void refresh() { loadWaiters(); loadTables(); }

    // ── Body ──────────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(UITheme.PAGE_BG);
        body.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        body.add(buildSessionBar(),  BorderLayout.NORTH);
        body.add(buildCenter(),      BorderLayout.CENTER);
        body.add(buildActiveOrders(),BorderLayout.SOUTH);
        return body;
    }

    // ── Session bar ───────────────────────────────────────────────────────────

    private JPanel buildSessionBar() {
        JPanel p = UITheme.card(new FlowLayout(FlowLayout.LEFT, 14, 8));

        waiterCombo = new JComboBox<>();
        waiterCombo.setPreferredSize(new Dimension(160, 30));
        tableCombo  = new JComboBox<>();
        tableCombo.setPreferredSize(new Dimension(165, 30));

        JButton refreshBtn = UITheme.button("⟳  Refresh", UITheme.Btn.SECONDARY);
        refreshBtn.addActionListener(e -> { loadWaiters(); loadTables(); });

        p.add(label("Waiter:")); p.add(waiterCombo);
        p.add(Box.createHorizontalStrut(10));
        p.add(label("Table:"));  p.add(tableCombo);
        p.add(Box.createHorizontalStrut(10));
        p.add(refreshBtn);

        loadWaiters(); loadTables();
        return p;
    }

    // ── Center: menu | order ──────────────────────────────────────────────────

    private JSplitPane buildCenter() {
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildMenuCard(), buildOrderCard());
        sp.setBackground(UITheme.PAGE_BG);
        sp.setBorder(null);
        sp.setDividerSize(8);
        sp.setResizeWeight(0.45);
        return sp;
    }

    private JPanel buildMenuCard() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.PAGE_BG);

        // Menu list
        JPanel listCard = new JPanel(new BorderLayout(0, 0));
        listCard.setBackground(UITheme.CARD_BG);
        listCard.setBorder(new LineBorder(UITheme.BORDER, 1));

        JPanel listHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        listHeader.setBackground(UITheme.CARD_BG);
        listHeader.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        listHeader.add(UITheme.sectionLabel("Menu Items"));
        listCard.add(listHeader, BorderLayout.NORTH);

        menuModel = new DefaultListModel<>();
        menuList  = new JList<>(menuModel);
        menuList.setFont(UITheme.F_BODY);
        menuList.setBackground(UITheme.CARD_BG);
        menuList.setFixedCellHeight(28);
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        listCard.add(UITheme.scroll(menuList), BorderLayout.CENTER);
        p.add(listCard, BorderLayout.CENTER);

        // Toppings card
        JPanel tCard = UITheme.card(new BorderLayout(0, 8));

        JLabel tLbl = UITheme.sectionLabel("Add Toppings  (Decorator Pattern)");
        tCard.add(tLbl, BorderLayout.NORTH);

        JPanel checks = new JPanel(new GridLayout(2, 2, 8, 6));
        checks.setBackground(UITheme.CARD_BG);
        chkCheese   = check("Extra Cheese  +₹30");
        chkPaneer   = check("Paneer        +₹40");
        chkChilli   = check("Extra Chilli  +₹15");
        chkMushroom = check("Mushroom      +₹25");
        checks.add(chkCheese); checks.add(chkPaneer);
        checks.add(chkChilli); checks.add(chkMushroom);
        tCard.add(checks, BorderLayout.CENTER);

        JButton addBtn = UITheme.button("Add to Order  ▶", UITheme.Btn.SUCCESS);
        addBtn.setAlignmentX(CENTER_ALIGNMENT);
        addBtn.addActionListener(e -> addSelectedItemToOrder());
        tCard.add(addBtn, BorderLayout.SOUTH);

        p.add(tCard, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildOrderCard() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.PAGE_BG);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UITheme.CARD_BG);
        tableCard.setBorder(new LineBorder(UITheme.BORDER, 1));

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        hdr.add(UITheme.sectionLabel("Current Order"));
        tableCard.add(hdr, BorderLayout.NORTH);

        orderTableModel = new DefaultTableModel(new String[]{"Item (with toppings)", "Price (₹)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderTable = new JTable(orderTableModel);
        UITheme.styleTable(orderTable);
        tableCard.add(UITheme.scroll(orderTable), BorderLayout.CENTER);
        p.add(tableCard, BorderLayout.CENTER);

        // Footer: total + notes + buttons
        JPanel foot = UITheme.card(new BorderLayout(0, 8));

        totalLabel = new JLabel("Total:  ₹0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalLabel.setForeground(UITheme.PRIMARY);
        foot.add(totalLabel, BorderLayout.NORTH);

        JPanel notesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        notesRow.setBackground(UITheme.CARD_BG);
        notesRow.add(label("Notes:"));
        notesField = UITheme.field(24);
        notesRow.add(notesField);
        foot.add(notesRow, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.CARD_BG);
        JButton clearBtn = UITheme.button("Clear", UITheme.Btn.SECONDARY);
        JButton placeBtn = UITheme.button("Place Order  ✓", UITheme.Btn.PRIMARY);
        clearBtn.addActionListener(e -> clearOrder());
        placeBtn.addActionListener(e -> placeOrder());
        btnRow.add(clearBtn); btnRow.add(placeBtn);
        foot.add(btnRow, BorderLayout.SOUTH);

        p.add(foot, BorderLayout.SOUTH);
        return p;
    }

    // ── Active orders ─────────────────────────────────────────────────────────

    private JPanel buildActiveOrders() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new LineBorder(UITheme.BORDER, 1));
        p.setPreferredSize(new Dimension(0, 155));

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));
        hdr.add(UITheme.sectionLabel("My Active Orders"));
        p.add(hdr, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Order #","Table","Status","Total (₹)","Notes"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable activeTable = new JTable(model);
        UITheme.styleTable(activeTable);
        p.add(UITheme.scroll(activeTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnRow.setBackground(UITheme.CARD_BG);
        JButton refBtn    = UITheme.button("⟳  Refresh",        UITheme.Btn.SECONDARY);
        JButton cancelBtn = UITheme.button("✕  Cancel Selected", UITheme.Btn.DANGER);
        refBtn.addActionListener(e -> refreshActiveOrders(model));
        cancelBtn.addActionListener(e -> {
            int row = activeTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
            int id = (int) model.getValueAt(row, 0);
            try {
                orderController.cancelOrder(id);
                JOptionPane.showMessageDialog(this, "Order #" + id + " cancelled.");
                refreshActiveOrders(model);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        btnRow.add(refBtn); btnRow.add(cancelBtn);
        p.add(btnRow, BorderLayout.SOUTH);

        refreshActiveOrders(model);
        return p;
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    private void loadWaiters() {
        waiterCombo.removeAllItems();
        try {
            List<Waiter> list = waiterDAO.getActiveWaiters();
            for (Waiter w : list) waiterCombo.addItem(w);
            if (list.isEmpty()) JOptionPane.showMessageDialog(this,
                    "No active waiters found.\nAdd waiters in the Admin panel first.");
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void loadTables() {
        tableCombo.removeAllItems();
        List<Table> tables = FloorManager.getInstance().getAvailableTables();
        for (Table t : tables)
            tableCombo.addItem("Table " + t.getTableNumber() + " (id:" + t.getId() + ")");
        if (tables.isEmpty()) tableCombo.addItem("-- No tables available --");
    }

    private void loadMenu() {
        menuModel.clear(); menuItems.clear();
        try {
            for (MenuItem m : menuController.getAvailableItems()) {
                menuItems.add(m);
                menuModel.addElement(String.format("[%s]  %s  —  ₹%.0f", m.getCategory(), m.getName(), m.getBasePrice()));
            }
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void addSelectedItemToOrder() {
        int idx = menuList.getSelectedIndex();
        if (idx < 0) { JOptionPane.showMessageDialog(this, "Select a menu item first."); return; }
        FoodItem food = new BaseFoodItem(menuItems.get(idx));
        if (chkCheese.isSelected())   food = new CheeseDecorator(food);
        if (chkPaneer.isSelected())   food = new PaneerDecorator(food);
        if (chkChilli.isSelected())   food = new ExtraChilliDecorator(food);
        if (chkMushroom.isSelected()) food = new MushroomDecorator(food);
        currentOrderItems.add(food);
        orderTableModel.addRow(new Object[]{ food.getName(), String.format("%.2f", food.getPrice()) });
        updateTotal();
        chkCheese.setSelected(false); chkPaneer.setSelected(false);
        chkChilli.setSelected(false); chkMushroom.setSelected(false);
    }

    private void updateTotal() {
        double t = currentOrderItems.stream().mapToDouble(FoodItem::getPrice).sum();
        totalLabel.setText(String.format("Total:  ₹%.2f", t));
    }

    private void clearOrder() {
        currentOrderItems.clear();
        orderTableModel.setRowCount(0);
        updateTotal();
    }

    private void placeOrder() {
        if (currentOrderItems.isEmpty()) { JOptionPane.showMessageDialog(this, "Add at least one item."); return; }
        Waiter w = (Waiter) waiterCombo.getSelectedItem();
        if (w == null) { JOptionPane.showMessageDialog(this, "Select a waiter first."); return; }
        String tableStr = (String) tableCombo.getSelectedItem();
        if (tableStr == null || tableStr.startsWith("--")) { JOptionPane.showMessageDialog(this, "No table available."); return; }
        int tableId = Integer.parseInt(tableStr.replaceAll(".*id:(\\d+)\\).*", "$1"));
        try {
            Order placed = orderController.placeOrder(tableId, w.getName(), currentOrderItems, notesField.getText());
            JOptionPane.showMessageDialog(this,
                    "Order #" + placed.getId() + " placed!\nTotal: ₹" + String.format("%.2f", placed.getTotalPrice()));
            clearOrder(); notesField.setText(""); loadTables();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void refreshActiveOrders(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            for (Order o : orderController.getActiveOrders())
                model.addRow(new Object[]{ o.getId(), o.getTableId(), o.getStatus(),
                        String.format("%.2f", o.getTotalPrice()), o.getNotes() });
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    // ── Small helpers ─────────────────────────────────────────────────────────

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.F_BOLD);
        l.setForeground(UITheme.TEXT);
        return l;
    }

    private JCheckBox check(String text) {
        JCheckBox c = new JCheckBox(text);
        c.setFont(UITheme.F_BODY);
        c.setBackground(UITheme.CARD_BG);
        return c;
    }
}
