package view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Central UI theme — colors, fonts, and factory helpers used by all panels.
 */
public class UITheme {

    // ── Sidebar ───────────────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG    = new Color(15,  23,  42);   // slate-900
    public static final Color SIDEBAR_HOVER = new Color(30,  41,  59);   // slate-800
    public static final Color SIDEBAR_SEL   = new Color(79,  70, 229);   // indigo-600
    public static final Color SIDEBAR_TEXT  = new Color(203, 213, 225);  // slate-300
    public static final Color SIDEBAR_MUTED = new Color(100, 116, 139);  // slate-500

    // ── Page / card ───────────────────────────────────────────────────────────
    public static final Color PAGE_BG   = new Color(241, 245, 249);      // slate-100
    public static final Color CARD_BG   = Color.WHITE;
    public static final Color BORDER    = new Color(226, 232, 240);      // slate-200

    // ── Text ──────────────────────────────────────────────────────────────────
    public static final Color TEXT       = new Color(15,  23,  42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    // ── Semantic button colors ────────────────────────────────────────────────
    public static final Color PRIMARY   = new Color(79,  70, 229);       // indigo-600
    public static final Color SUCCESS   = new Color(22, 163,  74);       // green-600
    public static final Color DANGER    = new Color(220,  38,  38);      // red-600
    public static final Color WARNING   = new Color(217, 119,   6);      // amber-600
    public static final Color SECONDARY = new Color(226, 232, 240);      // slate-200

    // ── Table ─────────────────────────────────────────────────────────────────
    public static final Color TABLE_HEADER = new Color(30,  41,  59);
    public static final Color TABLE_ALT    = new Color(248, 250, 252);
    public static final Color TABLE_SEL    = new Color(224, 231, 255);

    // ── Order-status badge colors ─────────────────────────────────────────────
    public static final Color S_RECEIVED  = new Color(254, 243, 199);
    public static final Color S_COOKING   = new Color(255, 237, 213);
    public static final Color S_READY     = new Color(220, 252, 231);
    public static final Color S_SERVED    = new Color(219, 234, 254);
    public static final Color S_CANCELLED = new Color(254, 226, 226);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD,  17);
    public static final Font F_HEADER = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font F_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_BOLD   = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font F_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_TABLE  = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font F_MONO   = new Font("Consolas", Font.PLAIN, 12);
    public static final Font F_NAV    = new Font("Segoe UI", Font.PLAIN, 13);

    // ── Global UIManager overrides ────────────────────────────────────────────
    public static void apply() {
        UIManager.put("Panel.background",          PAGE_BG);
        UIManager.put("OptionPane.background",     CARD_BG);
        UIManager.put("Button.font",               F_BODY);
        UIManager.put("Label.font",                F_BODY);
        UIManager.put("TextField.font",            F_BODY);
        UIManager.put("TextArea.font",             F_BODY);
        UIManager.put("ComboBox.font",             F_BODY);
        UIManager.put("CheckBox.font",             F_BODY);
        UIManager.put("List.font",                 F_BODY);
        UIManager.put("Table.font",                F_TABLE);
        UIManager.put("TableHeader.font",          F_BOLD);
        UIManager.put("TitledBorder.font",         F_BOLD);
        UIManager.put("TabbedPane.font",           F_BOLD);
        UIManager.put("ScrollPane.border",         new LineBorder(BORDER, 1));
    }

    // ── Button factory ────────────────────────────────────────────────────────

    public enum Btn { PRIMARY, SUCCESS, DANGER, WARNING, SECONDARY }

    public static JButton button(String text, Btn type) {
        Color base;
        Color fg;
        switch (type) {
            case PRIMARY:   base = PRIMARY;   fg = Color.WHITE; break;
            case SUCCESS:   base = SUCCESS;   fg = Color.WHITE; break;
            case DANGER:    base = DANGER;    fg = Color.WHITE; break;
            case WARNING:   base = WARNING;   fg = Color.WHITE; break;
            default:        base = SECONDARY; fg = TEXT;        break;
        }
        Color hover  = blend(base, Color.WHITE, 0.15f);   // 15% lighter
        Color press  = blend(base, Color.BLACK, 0.15f);   // 15% darker

        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(F_BOLD);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);   // we paint ourselves
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        b.setBackground(base);
        b.setForeground(fg);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hover); b.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(base);  b.repaint();
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                b.setBackground(press); b.repaint();
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                b.setBackground(hover); b.repaint();
            }
        });
        return b;
    }

    /** Blend two colours — t=0 → c1, t=1 → c2 */
    private static Color blend(Color c1, Color c2, float t) {
        return new Color(
            (int)(c1.getRed()   + (c2.getRed()   - c1.getRed())   * t),
            (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t),
            (int)(c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * t)
        );
    }

    // ── Table styling ─────────────────────────────────────────────────────────

    public static void styleTable(JTable t) {
        t.setFont(F_TABLE);
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(TABLE_SEL);
        t.setSelectionForeground(TEXT);
        t.setBackground(CARD_BG);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setFont(F_BOLD);
        h.setBackground(TABLE_HEADER);
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(0, 36));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = new JLabel(v == null ? "" : v.toString());
                l.setFont(F_BOLD);
                l.setForeground(Color.WHITE);
                l.setBackground(TABLE_HEADER);
                l.setOpaque(true);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return l;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!sel) { setBackground(r % 2 == 0 ? CARD_BG : TABLE_ALT); setForeground(TEXT); }
                return this;
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** White card panel with a subtle border */
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD_BG);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(12, 14, 12, 14)));
        return p;
    }

    /** Colored page-header strip at the top of each panel */
    public static JPanel pageHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setBackground(CARD_BG);
        p.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(16, 20, 14, 20)));
        JLabel t = new JLabel(title);
        t.setFont(F_TITLE); t.setForeground(TEXT);
        p.add(t, BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel s = new JLabel(subtitle);
            s.setFont(F_SMALL); s.setForeground(TEXT_MUTED);
            p.add(s, BorderLayout.CENTER);
        }
        return p;
    }

    /** Scroll pane with matching border/bg */
    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getViewport().setBackground(CARD_BG);
        return sp;
    }

    /** Text field with modern border */
    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(F_BODY);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    /** Returns the badge background color for an order status string */
    public static Color statusBg(String s) {
        if (s == null) return CARD_BG;
        switch (s) {
            case "RECEIVED":  return S_RECEIVED;
            case "COOKING":   return S_COOKING;
            case "READY":     return S_READY;
            case "SERVED":    return S_SERVED;
            case "CANCELLED": return S_CANCELLED;
            default:          return CARD_BG;
        }
    }

    /** Section label inside a card */
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_HEADER);
        l.setForeground(TEXT);
        return l;
    }
}
