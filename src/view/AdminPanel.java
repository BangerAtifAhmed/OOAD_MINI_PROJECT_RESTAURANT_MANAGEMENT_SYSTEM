package view;

import controller.MenuController;
import dao.CustomerDAO;
import dao.TableDAO;
import dao.WaiterDAO;
import model.Customer;
import model.MenuItem;
import model.Table;
import model.Waiter;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminPanel extends JPanel {

    private final MenuController menuController = new MenuController();
    private final WaiterDAO      waiterDAO      = new WaiterDAO();
    private final TableDAO       tableDAO       = new TableDAO();
    private final CustomerDAO    customerDAO    = new CustomerDAO();

    public AdminPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.PAGE_BG);
        add(UITheme.pageHeader("Admin Panel",
                "Manage menu items, waiters, tables and customer profiles."), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.F_BOLD);
        tabs.setBackground(UITheme.PAGE_BG);
        tabs.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));
        tabs.addTab("  Menu",       buildMenuTab());
        tabs.addTab("  Waiters",  buildWaitersTab());
        tabs.addTab("  Tables",     buildTablesTab());
        tabs.addTab("  Customers",  buildCustomersTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 1 — Menu (FR-5)
    // ══════════════════════════════════════════════════════════════════════════

    private DefaultTableModel menuModel;
    private JTable            menuTable;
    private JTextField        nameF, categoryF, priceF, descF, prepF;
    private JCheckBox         availableChk;

    private JPanel buildMenuTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.PAGE_BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        menuModel = new DefaultTableModel(
                new String[]{"ID","Name","Category","Price (₹)","Prep (min)","Available"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(menuModel);
        UITheme.styleTable(menuTable);
        menuTable.getSelectionModel().addListSelectionListener(e -> populateMenuForm());

        JPanel tableCard = tableCard("Menu Items", menuTable);
        p.add(tableCard, BorderLayout.CENTER);
        p.add(buildMenuForm(), BorderLayout.SOUTH);
        refreshMenu();
        return p;
    }

    private JPanel buildMenuForm() {
        JPanel card = UITheme.card(new BorderLayout(0, 8));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        fields.setBackground(UITheme.CARD_BG);

        nameF     = UITheme.field(14); categoryF = UITheme.field(10);
        priceF    = UITheme.field(7);  descF     = UITheme.field(20);
        prepF     = UITheme.field(4);  availableChk = styledCheck("Available", true);

        fields.add(lbl("Name:"));      fields.add(nameF);
        fields.add(lbl("Category:"));  fields.add(categoryF);
        fields.add(lbl("Price (₹):")); fields.add(priceF);
        fields.add(lbl("Desc:"));      fields.add(descF);
        fields.add(lbl("Prep(min):")); fields.add(prepF);
        fields.add(availableChk);
        card.add(fields, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UITheme.CARD_BG);
        JButton addBtn    = UITheme.button("Add",    UITheme.Btn.SUCCESS);
        JButton updateBtn = UITheme.button("Update", UITheme.Btn.PRIMARY);
        JButton deleteBtn = UITheme.button("Delete", UITheme.Btn.DANGER);
        JButton clearBtn  = UITheme.button("Clear",  UITheme.Btn.SECONDARY);
        addBtn.addActionListener(e    -> addMenuItem());
        updateBtn.addActionListener(e -> updateMenuItem());
        deleteBtn.addActionListener(e -> deleteMenuItem());
        clearBtn.addActionListener(e  -> clearMenuForm());
        btns.add(addBtn); btns.add(updateBtn); btns.add(deleteBtn); btns.add(clearBtn);
        card.add(btns, BorderLayout.SOUTH);
        return card;
    }

    private void refreshMenu() {
        menuModel.setRowCount(0);
        try {
            for (MenuItem m : menuController.getAllItems())
                menuModel.addRow(new Object[]{ m.getId(), m.getName(), m.getCategory(),
                        String.format("%.2f", m.getBasePrice()), m.getPrepTimeMinutes(), m.isAvailable() });
        } catch (SQLException ex) { err(ex); }
    }

    private void populateMenuForm() {
        int r = menuTable.getSelectedRow(); if (r < 0) return;
        nameF.setText(menuModel.getValueAt(r,1).toString());
        categoryF.setText(menuModel.getValueAt(r,2).toString());
        priceF.setText(menuModel.getValueAt(r,3).toString());
        prepF.setText(menuModel.getValueAt(r,4).toString());
        availableChk.setSelected(Boolean.parseBoolean(menuModel.getValueAt(r,5).toString()));
    }

    private void clearMenuForm() {
        nameF.setText(""); categoryF.setText(""); priceF.setText("");
        descF.setText(""); prepF.setText(""); availableChk.setSelected(true);
        menuTable.clearSelection();
    }

    private void addMenuItem() {
        try { menuController.addItem(menuFromForm(0)); refreshMenu(); clearMenuForm();
              JOptionPane.showMessageDialog(this, "Item added.");
        } catch (Exception ex) { err(ex); }
    }

    private void updateMenuItem() {
        int r = menuTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        try { menuController.updateItem(menuFromForm((int)menuModel.getValueAt(r,0)));
              refreshMenu(); clearMenuForm();
              JOptionPane.showMessageDialog(this, "Item updated.");
        } catch (Exception ex) { err(ex); }
    }

    private void deleteMenuItem() {
        int r = menuTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
        if (JOptionPane.showConfirmDialog(this,"Delete this item?","Confirm",
                JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        try { menuController.deleteItem((int)menuModel.getValueAt(r,0));
              refreshMenu(); clearMenuForm();
        } catch (SQLException ex) { err(ex); }
    }

    private MenuItem menuFromForm(int id) {
        if (nameF.getText().isBlank()) throw new IllegalArgumentException("Name is required.");
        double price; try { price=Double.parseDouble(priceF.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid price."); }
        int prep=10; try { if(!prepF.getText().isBlank()) prep=Integer.parseInt(prepF.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid prep time."); }
        return new MenuItem(id,nameF.getText().trim(),categoryF.getText().trim(),
                price,descF.getText().trim(),prep,availableChk.isSelected());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 2 — Waiters
    // ══════════════════════════════════════════════════════════════════════════

    private DefaultTableModel waiterModel;
    private JTable            waiterTable;
    private JTextField        wNameF, wPhoneF;
    private JCheckBox         wActiveChk;

    private JPanel buildWaitersTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.PAGE_BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        waiterModel = new DefaultTableModel(
                new String[]{"ID","Name","Phone","Active"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        waiterTable = new JTable(waiterModel);
        UITheme.styleTable(waiterTable);
        waiterTable.getSelectionModel().addListSelectionListener(e -> populateWaiterForm());
        p.add(tableCard("Waiters", waiterTable), BorderLayout.CENTER);
        p.add(buildWaiterForm(), BorderLayout.SOUTH);
        refreshWaiters();
        return p;
    }

    private JPanel buildWaiterForm() {
        JPanel card = UITheme.card(new FlowLayout(FlowLayout.LEFT, 12, 4));
        wNameF   = UITheme.field(14); wPhoneF = UITheme.field(12);
        wActiveChk = styledCheck("Active", true);
        card.add(lbl("Name:")); card.add(wNameF);
        card.add(lbl("Phone:")); card.add(wPhoneF);
        card.add(wActiveChk);

        JButton addBtn    = UITheme.button("Add",    UITheme.Btn.SUCCESS);
        JButton updateBtn = UITheme.button("Update", UITheme.Btn.PRIMARY);
        JButton deleteBtn = UITheme.button("Delete", UITheme.Btn.DANGER);
        JButton clearBtn  = UITheme.button("Clear",  UITheme.Btn.SECONDARY);
        addBtn.addActionListener(e    -> addWaiter());
        updateBtn.addActionListener(e -> updateWaiter());
        deleteBtn.addActionListener(e -> deleteWaiter());
        clearBtn.addActionListener(e  -> clearWaiterForm());
        card.add(addBtn); card.add(updateBtn); card.add(deleteBtn); card.add(clearBtn);
        return card;
    }

    private void refreshWaiters() {
        waiterModel.setRowCount(0);
        try { for (Waiter w : waiterDAO.getAll())
                  waiterModel.addRow(new Object[]{ w.getId(), w.getName(), w.getPhone(), w.isActive() });
        } catch (SQLException ex) { err(ex); }
    }

    private void populateWaiterForm() {
        int r = waiterTable.getSelectedRow(); if (r < 0) return;
        wNameF.setText(waiterModel.getValueAt(r,1).toString());
        Object phone = waiterModel.getValueAt(r,2);
        wPhoneF.setText(phone != null ? phone.toString() : "");
        wActiveChk.setSelected(Boolean.parseBoolean(waiterModel.getValueAt(r,3).toString()));
    }

    private void clearWaiterForm() {
        wNameF.setText(""); wPhoneF.setText(""); wActiveChk.setSelected(true);
        waiterTable.clearSelection();
    }

    private void addWaiter() {
        if (wNameF.getText().isBlank()) { JOptionPane.showMessageDialog(this,"Name required."); return; }
        try { Waiter w=new Waiter(wNameF.getText().trim(),wPhoneF.getText().trim(),wActiveChk.isSelected());
              waiterDAO.insert(w); refreshWaiters(); clearWaiterForm();
              JOptionPane.showMessageDialog(this,"Waiter added.");
        } catch (SQLException ex) { err(ex); }
    }

    private void updateWaiter() {
        int r=waiterTable.getSelectedRow();
        if (r<0){JOptionPane.showMessageDialog(this,"Select a row first.");return;}
        try { Waiter w=new Waiter(wNameF.getText().trim(),wPhoneF.getText().trim(),wActiveChk.isSelected());
              w.setId((int)waiterModel.getValueAt(r,0)); waiterDAO.update(w);
              refreshWaiters(); clearWaiterForm();
              JOptionPane.showMessageDialog(this,"Waiter updated.");
        } catch (SQLException ex) { err(ex); }
    }

    private void deleteWaiter() {
        int r=waiterTable.getSelectedRow();
        if (r<0){JOptionPane.showMessageDialog(this,"Select a row first.");return;}
        if(JOptionPane.showConfirmDialog(this,"Delete this waiter?","Confirm",
                JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        try { waiterDAO.delete((int)waiterModel.getValueAt(r,0));
              refreshWaiters(); clearWaiterForm();
        } catch (SQLException ex) { err(ex); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 3 — Tables
    // ══════════════════════════════════════════════════════════════════════════

    private DefaultTableModel tableModel;
    private JTextField tNumberF, tCapacityF;

    private JPanel buildTablesTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.PAGE_BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        tableModel = new DefaultTableModel(
                new String[]{"ID","Table No.","Capacity","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(tableModel);
        UITheme.styleTable(tbl);
        p.add(tableCard("Restaurant Tables", tbl), BorderLayout.CENTER);
        p.add(buildTableForm(), BorderLayout.SOUTH);
        refreshTables();
        return p;
    }

    private JPanel buildTableForm() {
        JPanel card = UITheme.card(new FlowLayout(FlowLayout.LEFT, 12, 4));
        tNumberF   = UITheme.field(6);
        tCapacityF = UITheme.field(6);
        card.add(lbl("Table Number:"));    card.add(tNumberF);
        card.add(lbl("Capacity (seats):")); card.add(tCapacityF);

        JButton addBtn = UITheme.button("Add Table",  UITheme.Btn.SUCCESS);
        JButton refBtn = UITheme.button("⟳  Refresh", UITheme.Btn.SECONDARY);
        addBtn.addActionListener(e -> addTable());
        refBtn.addActionListener(e -> refreshTables());
        card.add(addBtn); card.add(refBtn);
        return card;
    }

    private void refreshTables() {
        tableModel.setRowCount(0);
        try { for (Table t : tableDAO.getAllTables())
                  tableModel.addRow(new Object[]{ t.getId(), t.getTableNumber(), t.getCapacity(), t.getStatus() });
        } catch (SQLException ex) { err(ex); }
    }

    private void addTable() {
        if (tNumberF.getText().isBlank()||tCapacityF.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,"Table number and capacity are required."); return;
        }
        try {
            int num = Integer.parseInt(tNumberF.getText().trim());
            int cap = Integer.parseInt(tCapacityF.getText().trim());
            tableDAO.insertTable(num, cap);
            refreshTables(); tNumberF.setText(""); tCapacityF.setText("");
            singleton.FloorManager.getInstance().refresh();
            JOptionPane.showMessageDialog(this,"Table " + num + " added.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,"Table number and capacity must be integers.");
        } catch (SQLException ex) { err(ex); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 4 — Customer Profiles (FR-8)
    // ══════════════════════════════════════════════════════════════════════════

    private DefaultTableModel custModel;

    private JPanel buildCustomersTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.PAGE_BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        custModel = new DefaultTableModel(
                new String[]{"ID","Name","Phone","Email","Joined"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable ct = new JTable(custModel);
        UITheme.styleTable(ct);
        p.add(tableCard("Customer Profiles", ct), BorderLayout.CENTER);
        p.add(buildCustomerForm(), BorderLayout.SOUTH);
        refreshCustomers();
        return p;
    }

    private JPanel buildCustomerForm() {
        JPanel card = UITheme.card(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JTextField cName  = UITheme.field(14);
        JTextField cPhone = UITheme.field(11);
        JTextField cEmail = UITheme.field(16);
        card.add(lbl("Name:"));  card.add(cName);
        card.add(lbl("Phone:")); card.add(cPhone);
        card.add(lbl("Email:")); card.add(cEmail);

        JButton saveBtn = UITheme.button("Save Customer", UITheme.Btn.SUCCESS);
        JButton refBtn  = UITheme.button("⟳  Refresh",   UITheme.Btn.SECONDARY);
        saveBtn.addActionListener(e -> {
            if (cName.getText().isBlank()) { JOptionPane.showMessageDialog(this,"Name required."); return; }
            try { Customer c=new Customer(cName.getText().trim(),cPhone.getText().trim(),cEmail.getText().trim());
                  customerDAO.insert(c); cName.setText(""); cPhone.setText(""); cEmail.setText("");
                  refreshCustomers();
                  JOptionPane.showMessageDialog(this,"Customer saved (ID: "+c.getId()+")");
            } catch (SQLException ex) { err(ex); }
        });
        refBtn.addActionListener(e -> refreshCustomers());
        card.add(saveBtn); card.add(refBtn);
        return card;
    }

    private void refreshCustomers() {
        custModel.setRowCount(0);
        try { for (Customer c : customerDAO.getAll()) {
                  String joined = c.getCreatedAt()!=null ? c.getCreatedAt().toLocalDate().toString() : "?";
                  custModel.addRow(new Object[]{ c.getId(),c.getName(),c.getPhone(),c.getEmail(),joined });
              }
        } catch (SQLException ex) { err(ex); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JPanel tableCard(String title, JTable table) {
        JPanel card = new JPanel(new BorderLayout(0,0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new LineBorder(UITheme.BORDER,1));
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT,12,8));
        hdr.setBackground(UITheme.CARD_BG);
        hdr.setBorder(new MatteBorder(0,0,1,0,UITheme.BORDER));
        hdr.add(UITheme.sectionLabel(title));
        card.add(hdr, BorderLayout.NORTH);
        card.add(UITheme.scroll(table), BorderLayout.CENTER);
        return card;
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text); l.setFont(UITheme.F_BOLD); l.setForeground(UITheme.TEXT); return l;
    }

    private JCheckBox styledCheck(String text, boolean selected) {
        JCheckBox c = new JCheckBox(text, selected);
        c.setFont(UITheme.F_BODY); c.setBackground(UITheme.CARD_BG); return c;
    }

    private void err(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
