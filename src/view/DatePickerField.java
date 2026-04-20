package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * A text field + calendar-button combo that shows a mini month popup.
 * Usage:
 *   DatePickerField picker = new DatePickerField(LocalDate.now());
 *   LocalDate date = picker.getDate();   // null if empty
 *   String    text = picker.getText();   // "yyyy-MM-dd" or ""
 */
public class DatePickerField extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JTextField textField;
    private LocalDate        selectedDate;

    // Calendar popup state
    private JWindow   popup;
    private YearMonth displayMonth;

    public DatePickerField(LocalDate initial) {
        setLayout(new BorderLayout(2, 0));
        setOpaque(false);

        textField = new JTextField(10);
        textField.setEditable(true);

        JButton calBtn = new JButton("📅");
        calBtn.setFocusable(false);
        calBtn.setMargin(new Insets(1, 3, 1, 3));
        calBtn.addActionListener(e -> togglePopup());

        add(textField, BorderLayout.CENTER);
        add(calBtn,    BorderLayout.EAST);

        if (initial != null) setDate(initial);
    }

    public DatePickerField() { this(null); }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setDate(LocalDate date) {
        this.selectedDate = date;
        textField.setText(date == null ? "" : date.format(FMT));
    }

    /** Returns the selected date, or null if the field is blank / invalid. */
    public LocalDate getDate() {
        String t = textField.getText().trim();
        if (t.isEmpty()) return null;
        try { return LocalDate.parse(t, FMT); }
        catch (Exception e) { return null; }
    }

    /** Returns raw text ("yyyy-MM-dd" or "") — safe to pass straight to SQL. */
    public String getText() { return textField.getText().trim(); }

    // ── Popup calendar ────────────────────────────────────────────────────────

    private void togglePopup() {
        if (popup != null && popup.isVisible()) { popup.dispose(); popup = null; return; }
        displayMonth = selectedDate != null
                ? YearMonth.from(selectedDate)
                : YearMonth.now();
        showPopup();
    }

    private void showPopup() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        popup = new JWindow(owner);
        popup.setFocusableWindowState(false);

        popup.setContentPane(buildCalendarPanel());
        popup.pack();

        // Position below the text field
        Point loc = textField.getLocationOnScreen();
        popup.setLocation(loc.x, loc.y + textField.getHeight() + 2);
        popup.setVisible(true);

        // Close when user clicks outside
        Toolkit.getDefaultToolkit().addAWTEventListener(outsideClickListener,
                AWTEvent.MOUSE_EVENT_MASK);
    }

    private final AWTEventListener outsideClickListener = event -> {
        if (event instanceof MouseEvent && ((MouseEvent) event).getID() == MouseEvent.MOUSE_PRESSED) {
            MouseEvent me = (MouseEvent) event;
            if (popup != null && popup.isVisible()) {
                Point p = me.getLocationOnScreen();
                Rectangle bounds = popup.getBounds();
                if (!bounds.contains(p)) {
                    closePopup();
                }
            }
        }
    };

    private void closePopup() {
        if (popup != null) {
            popup.dispose();
            popup = null;
        }
        Toolkit.getDefaultToolkit().removeAWTEventListener(outsideClickListener);
    }

    // ── Calendar panel ────────────────────────────────────────────────────────

    private JPanel buildCalendarPanel() {
        JPanel root = new JPanel(new BorderLayout(4, 4));
        root.setBorder(new LineBorder(new Color(100, 100, 180), 1));
        root.setBackground(Color.WHITE);

        root.add(buildNavBar(),  BorderLayout.NORTH);
        root.add(buildDayGrid(), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildNavBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(70, 130, 180));
        p.setBorder(new EmptyBorder(4, 6, 4, 6));

        JButton prev = navBtn("◀");
        JButton next = navBtn("▶");

        JLabel monthLabel = new JLabel(
                displayMonth.getMonth().getDisplayName(
                        java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                + "  " + displayMonth.getYear(),
                SwingConstants.CENTER);
        monthLabel.setForeground(Color.WHITE);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 13f));

        prev.addActionListener(e -> { displayMonth = displayMonth.minusMonths(1); refreshPopup(); });
        next.addActionListener(e -> { displayMonth = displayMonth.plusMonths(1);  refreshPopup(); });

        p.add(prev,       BorderLayout.WEST);
        p.add(monthLabel, BorderLayout.CENTER);
        p.add(next,       BorderLayout.EAST);
        return p;
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(70, 130, 180));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        return b;
    }

    private JPanel buildDayGrid() {
        JPanel p = new JPanel(new GridLayout(0, 7, 4, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(6, 8, 8, 8));
        p.setPreferredSize(new Dimension(280, 190));

        // Day-of-week headers
        String[] headers = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
            lbl.setForeground(new Color(100, 100, 180));
            p.add(lbl);
        }

        // Leading empty cells before the 1st of the month
        LocalDate first = displayMonth.atDay(1);
        int startDow = first.getDayOfWeek().getValue() % 7; // Sun=0 .. Sat=6
        for (int i = 0; i < startDow; i++) p.add(new JLabel());

        // Day buttons
        for (int d = 1; d <= displayMonth.lengthOfMonth(); d++) {
            LocalDate date = displayMonth.atDay(d);
            JButton btn = new JButton(String.valueOf(d));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setFont(btn.getFont().deriveFont(12f));
            btn.setMargin(new Insets(2, 2, 2, 2));
            btn.setPreferredSize(new Dimension(34, 30));

            boolean isSelected = date.equals(selectedDate);
            boolean isToday    = date.equals(LocalDate.now());

            if (isSelected) {
                btn.setBackground(new Color(70, 130, 180));
                btn.setForeground(Color.WHITE);
                btn.setOpaque(true);
            } else if (isToday) {
                btn.setBackground(new Color(220, 240, 255));
                btn.setForeground(Color.BLACK);
                btn.setOpaque(true);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
                btn.setOpaque(true);
            }

            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!date.equals(selectedDate))
                        btn.setBackground(new Color(200, 225, 255));
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!date.equals(selectedDate))
                        btn.setBackground(isToday ? new Color(220, 240, 255) : Color.WHITE);
                }
            });

            btn.addActionListener(e -> {
                setDate(date);
                closePopup();
            });
            p.add(btn);
        }
        return p;
    }

    /** Rebuild the popup in-place after month navigation */
    private void refreshPopup() {
        if (popup == null) return;
        popup.setContentPane(buildCalendarPanel());
        popup.pack();
        popup.revalidate();
    }
}
