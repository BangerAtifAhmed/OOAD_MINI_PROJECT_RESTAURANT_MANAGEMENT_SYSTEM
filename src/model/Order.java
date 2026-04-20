package model;

import model.state.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int tableId;
    private int customerId;      // 0 = walk-in (no profile)
    private String waiterId;
    private OrderState state;
    private List<OrderItem> items;
    private double totalPrice;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {
        this.state = new ReceivedState();
        this.items = new ArrayList<>();
    }

    // ── State-pattern delegation ─────────────────────────────────────────────
    public void setState(OrderState state)    { this.state = state; }
    public OrderState getState()              { return state; }
    public String getStatus()                 { return state.getStateName(); }

    /** Kitchen presses "advance": RECEIVED→COOKING→READY→SERVED */
    public void advanceState() {
        state.nextState(this);
    }

    /** Waiter / admin cancels an order */
    public void cancel() {
        state.cancel(this);
    }

    public boolean canCancel() { return state.canCancel(); }

    // Helper: restore state from DB string
    public static OrderState stateFromString(String s) {
        switch (s) {
            case "RECEIVED":  return new ReceivedState();
            case "COOKING":   return new CookingState();
            case "READY":     return new ReadyState();
            case "SERVED":    return new ServedState();
            case "CANCELLED": return new CancelledState();
            default:          return new ReceivedState();
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int              getId()          { return id; }
    public int              getTableId()     { return tableId; }
    public int              getCustomerId()  { return customerId; }
    public String           getWaiterId()    { return waiterId; }
    public List<OrderItem>  getItems()       { return items; }
    public double           getTotalPrice()  { return totalPrice; }
    public String           getNotes()       { return notes; }
    public LocalDateTime    getCreatedAt()   { return createdAt; }
    public LocalDateTime    getUpdatedAt()   { return updatedAt; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                        { this.id = id; }
    public void setTableId(int tableId)              { this.tableId = tableId; }
    public void setCustomerId(int customerId)        { this.customerId = customerId; }
    public void setWaiterId(String waiterId)         { this.waiterId = waiterId; }
    public void setItems(List<OrderItem> items)      { this.items = items; }
    public void setTotalPrice(double totalPrice)     { this.totalPrice = totalPrice; }
    public void setNotes(String notes)               { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("Order #%d | Table %d | %s | ₹%.2f", id, tableId, getStatus(), totalPrice);
    }
}
