package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Main application window — dark sidebar + CardLayout content area.
 * Branding: Restaurant Management System
 */
public class MainFrame extends JFrame {

    private final WaiterPanel       waiterPanel  = new WaiterPanel();
    private final KitchenPanel      kitchenPanel = new KitchenPanel();
    private final BillingPanel      billingPanel = new BillingPanel();
    private final OrderHistoryPanel historyPanel = new OrderHistoryPanel();
    private final AdminPanel        adminPanel   = new AdminPanel();

    private final CardLayout cardLayout  = new CardLayout();
    private final JPanel     contentArea = new JPanel(cardLayout);
    private       NavButton  activeBtn   = null;

    public MainFrame() {
        super("Restaurant Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 740));
        setLocationRelativeTo(null);

        contentArea.setBackground(UITheme.PAGE_BG);
        contentArea.add(waiterPanel,  "WAITER");
        contentArea.add(kitchenPanel, "KITCHEN");
        contentArea.add(billingPanel, "BILLING");
        contentArea.add(historyPanel, "HISTORY");
        contentArea.add(adminPanel,   "ADMIN");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(contentArea,    BorderLayout.CENTER);
        pack();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(230, 0));

        sidebar.add(buildLogoArea());
        sidebar.add(spacer(18));
        sidebar.add(sectionLabel("NAVIGATION"));
        sidebar.add(spacer(6));

        // ── Nav items ─────────────────────────────────────────────────────────
        Object[][] items = {
            { NavIcon.WAITER,  "Waiter",  "WAITER"  },
            { NavIcon.KITCHEN, "Kitchen", "KITCHEN" },
            { NavIcon.BILLING, "Billing", "BILLING" },
            { NavIcon.HISTORY, "History", "HISTORY" },
            { NavIcon.ADMIN,   "Admin",   "ADMIN"   },
        };

        NavButton first = null;
        for (Object[] item : items) {
            NavButton btn = new NavButton((NavIcon) item[0], (String) item[1], (String) item[2]);
            sidebar.add(btn);
            if (first == null) first = btn;
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildVersionBadge());

        if (first != null) first.doClick();
        return sidebar;
    }

    private JPanel buildLogoArea() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // bottom border line
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(9, 14, 29));
        p.setBorder(BorderFactory.createEmptyBorder(22, 8, 20, 8));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMinimumSize(new Dimension(230, 135));
        p.setPreferredSize(new Dimension(230, 135));
        p.setMaximumSize(new Dimension(230, 135));

        // Logo icon (painted fork+knife on a circle)
        JComponent icon = new JComponent() {
            { setPreferredSize(new Dimension(44, 44)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Circle background
                g2.setColor(UITheme.SIDEBAR_SEL);
                g2.fillOval(0, 0, 42, 42);
                // Fork (left)
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(14, 10, 14, 32);
                g2.drawLine(12, 10, 12, 17);
                g2.drawLine(16, 10, 16, 17);
                g2.drawArc(11, 14, 6, 5, 180, 180);
                // Knife (right)
                g2.drawLine(28, 10, 28, 32);
                g2.draw(new QuadCurve2D.Float(28, 10, 32, 14, 28, 20));
                g2.dispose();
            }
        };
        icon.setAlignmentX(LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel("Restaurant");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("Management System");
        subLabel.setFont(UITheme.F_SMALL);
        subLabel.setForeground(UITheme.SIDEBAR_MUTED);
        subLabel.setAlignmentX(LEFT_ALIGNMENT);

        p.add(icon);
        p.add(spacer(10));
        p.add(nameLabel);
        p.add(spacer(2));
        p.add(subLabel);
        return p;
    }

    private JPanel buildVersionBadge() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        p.setBackground(UITheme.SIDEBAR_BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMinimumSize(new Dimension(230, 42));
        p.setPreferredSize(new Dimension(230, 42));
        p.setMaximumSize(new Dimension(230, 42));
        JLabel v = new JLabel("v1.0  —  CS352B Project");
        v.setFont(UITheme.F_SMALL);
        v.setForeground(UITheme.SIDEBAR_MUTED);
        p.add(v);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Component spacer(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(UITheme.SIDEBAR_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(230, 20));
        l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        return l;
    }

    // ── NavButton ─────────────────────────────────────────────────────────────

    private class NavButton extends JButton {
        private final NavIcon icon;
        private       boolean isActive = false;

        NavButton(NavIcon icon, String label, String card) {
            super(label);
            this.icon = icon;
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(UITheme.SIDEBAR_TEXT);
            setBackground(UITheme.SIDEBAR_BG);
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setMargin(new Insets(0, 0, 0, 0));
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            setMaximumSize(new Dimension(230, 50));
            setMinimumSize(new Dimension(230, 50));
            setPreferredSize(new Dimension(230, 50));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!isActive) setBackground(UITheme.SIDEBAR_HOVER);
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!isActive) setBackground(UITheme.SIDEBAR_BG);
                }
                @Override public void mousePressed(MouseEvent e) {
                    setBackground(new Color(20, 30, 60));
                }
                @Override public void mouseReleased(MouseEvent e) {
                    setBackground(isActive ? new Color(30, 41, 80) : UITheme.SIDEBAR_HOVER);
                }
            });

            addActionListener(e -> {
                cardLayout.show(contentArea, card);
                if (activeBtn != null) activeBtn.setActive(false);
                setActive(true);
                activeBtn = this;
                switch (card) {
                    case "WAITER":  waiterPanel.refresh();  break;
                    case "KITCHEN": kitchenPanel.refresh(); break;
                    case "BILLING": billingPanel.refresh(); break;
                    case "HISTORY": historyPanel.refresh(); break;
                }
            });
        }

        void setActive(boolean active) {
            this.isActive = active;
            setBackground(active ? new Color(30, 41, 80) : UITheme.SIDEBAR_BG);
            setForeground(active ? Color.WHITE : UITheme.SIDEBAR_TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Background
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Left selection bar
            if (isActive) {
                g2.setColor(UITheme.SIDEBAR_SEL);
                g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
            }

            // Icon at x=6, vertically centred
            int cy = getHeight() / 2;
            icon.draw(g2, 6, cy, isActive);
            g2.dispose();

            // Text at x=24 — right after icon (6+15=21, +3px gap)
            g.setColor(getForeground());
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(getText(), 24, ty);
        }
    }

    // ── NavIcon (painted icons) ───────────────────────────────────────────────

    enum NavIcon {
        WAITER, KITCHEN, BILLING, HISTORY, ADMIN;

        void draw(Graphics2D g2, int x, int cy, boolean active) {
            Color c = active ? Color.WHITE : new Color(148, 163, 184);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (this) {
                case WAITER -> {   // person
                    g2.fillOval(x + 3, cy - 10, 9, 9);            // head
                    g2.drawArc(x, cy, 15, 10, 0, 180);            // shoulders
                }
                case KITCHEN -> {  // chef hat
                    g2.fillRoundRect(x + 1, cy - 5, 13, 10, 4, 4);  // hat body
                    g2.fillOval(x + 3, cy - 10, 9, 8);              // top puff
                    g2.setColor(active ? new Color(30,41,80) : UITheme.SIDEBAR_BG);
                    g2.drawLine(x + 3, cy - 2, x + 12, cy - 2);     // line detail
                    g2.setColor(c);
                    g2.fillRect(x + 2, cy + 4, 11, 3);              // brim
                }
                case BILLING -> {  // credit card
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.drawRoundRect(x, cy - 8, 15, 11, 3, 3);
                    g2.fillRect(x, cy - 4, 15, 4);                  // magnetic stripe
                    g2.setColor(active ? new Color(30,41,80) : UITheme.SIDEBAR_BG);
                    g2.fillRect(x + 2, cy - 2, 5, 2);               // chip
                    g2.setColor(c);
                    g2.fillRect(x + 2, cy + 5, 4, 2);               // number dots
                    g2.fillRect(x + 8, cy + 5, 4, 2);
                }
                case HISTORY -> {  // clock
                    g2.drawOval(x, cy - 8, 15, 15);
                    g2.drawLine(x + 7, cy - 1, x + 7, cy + 4);     // hour hand
                    g2.drawLine(x + 7, cy - 1, x + 11, cy - 1);    // minute hand
                    g2.fillOval(x + 6, cy - 2, 3, 3);              // center dot
                }
                case ADMIN -> {    // gear
                    int cx = x + 7; int r = 4;
                    g2.fillOval(cx - r, cy - r, r * 2, r * 2);    // inner circle
                    g2.setColor(active ? new Color(30,41,80) : UITheme.SIDEBAR_BG);
                    g2.fillOval(cx - 2, cy - 2, 4, 4);             // hole
                    g2.setColor(c);
                    // teeth
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.toRadians(i * 45);
                        int tx = (int)(cx + Math.cos(angle) * 7);
                        int ty = (int)(cy + Math.sin(angle) * 7);
                        g2.fillOval(tx - 2, ty - 2, 4, 4);
                    }
                }
            }
        }
    }
}
