package singleton;

import dao.TableDAO;
import model.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Pattern — Centralized Floor Manager (FR-4)
 *
 * A single instance holds the real-time status of every table.
 * Multiple waiter terminals share this one object, preventing double-booking.
 */
public class FloorManager {

    private static FloorManager instance;

    private final TableDAO tableDAO;
    private List<Table> tables;

    // ── Singleton ─────────────────────────────────────────────────────────────
    private FloorManager() {
        this.tableDAO = new TableDAO();
        this.tables   = new ArrayList<>();
        try {
            refresh();
        } catch (SQLException e) {
            System.err.println("FloorManager: could not load tables — " + e.getMessage());
        }
    }

    public static synchronized FloorManager getInstance() {
        if (instance == null) {
            instance = new FloorManager();
        }
        return instance;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Reload table list from DB (call after any status change) */
    public void refresh() throws SQLException {
        tables = tableDAO.getAllTables();
    }

    public List<Table> getAllTables() { return new ArrayList<>(tables); }

    public List<Table> getAvailableTables() {
        List<Table> avail = new ArrayList<>();
        for (Table t : tables)
            if (t.isAvailable()) avail.add(t);
        return avail;
    }

    public Table getTableById(int id) {
        for (Table t : tables)
            if (t.getId() == id) return t;
        return null;
    }

    /**
     * Atomically occupy a table.
     * @return true if the table was available and is now occupied; false if already taken.
     */
    public synchronized boolean occupyTable(int tableId) throws SQLException {
        Table table = getTableById(tableId);
        if (table == null || !table.isAvailable()) return false;
        table.setStatus(Table.Status.OCCUPIED);
        tableDAO.updateStatus(tableId, "OCCUPIED");
        return true;
    }

    /** Release a table back to AVAILABLE (called after payment) */
    public synchronized void releaseTable(int tableId) throws SQLException {
        Table table = getTableById(tableId);
        if (table != null) {
            table.setStatus(Table.Status.AVAILABLE);
            tableDAO.updateStatus(tableId, "AVAILABLE");
        }
    }
}
