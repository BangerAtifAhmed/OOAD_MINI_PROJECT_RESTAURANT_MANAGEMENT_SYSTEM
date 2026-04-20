package controller;

import dao.OrderDAO;
import model.FoodItem;
import model.Order;
import model.OrderItem;
import singleton.FloorManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MVC Controller — handles all order-related business logic.
 * The view layer calls methods here; it never touches DAO or FloorManager directly.
 */
public class OrderController {

    private final OrderDAO     orderDAO;
    private final FloorManager floorManager;

    public OrderController() {
        this.orderDAO     = new OrderDAO();
        this.floorManager = FloorManager.getInstance();
    }

    // ── Place Order (FR-1 + FR-4) ─────────────────────────────────────────────

    /**
     * Place a new order. Uses FloorManager to check and occupy the table,
     * then persists the order with its decorated items.
     *
     * @param tableId    ID of the selected table
     * @param waiterId   Name / ID of the waiter
     * @param foodItems  List of FoodItem objects (possibly with decorators applied)
     * @param notes      Special instructions
     * @return the saved Order with its generated DB id
     */
    public Order placeOrder(int tableId, String waiterId,
                            List<FoodItem> foodItems, String notes) throws SQLException {

        if (!floorManager.occupyTable(tableId)) {
            throw new IllegalStateException("Table " + tableId + " is not available.");
        }

        Order order = new Order();
        order.setTableId(tableId);
        order.setWaiterId(waiterId);
        order.setNotes(notes);

        List<OrderItem> items = new ArrayList<>();
        double total = 0;
        for (FoodItem fi : foodItems) {
            // base price of the raw menu item
            double basePrice   = fi.getPrice();
            // The decorator description carries topping info
            double toppingCost = 0;
            String baseDesc    = fi.getDescription();

            OrderItem oi = new OrderItem(
                    fi.getMenuItemId(),
                    fi.getName(),
                    1,                    // quantity — caller stacks decorators per item
                    basePrice,
                    baseDesc,
                    toppingCost
            );
            items.add(oi);
            total += basePrice;
        }
        order.setItems(items);
        order.setTotalPrice(total);
        orderDAO.save(order);
        return order;
    }

    // ── State transitions (FR-2) ──────────────────────────────────────────────

    /** Kitchen advances order: RECEIVED→COOKING→READY→SERVED */
    public void advanceOrder(int orderId) throws SQLException {
        Order order = orderDAO.getById(orderId);
        if (order == null) throw new IllegalArgumentException("Order #" + orderId + " not found.");
        order.advanceState();
        orderDAO.updateStatus(orderId, order.getStatus());
    }

    /** Waiter cancels an order (only if RECEIVED) */
    public void cancelOrder(int orderId) throws SQLException {
        Order order = orderDAO.getById(orderId);
        if (order == null) throw new IllegalArgumentException("Order #" + orderId + " not found.");
        order.cancel();   // throws IllegalStateException if not cancellable
        orderDAO.updateStatus(orderId, order.getStatus());
        floorManager.releaseTable(order.getTableId());
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Order> getActiveOrders() throws SQLException {
        return orderDAO.getActiveOrders();
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.getAllOrders();
    }

    public Order getOrderById(int id) throws SQLException {
        return orderDAO.getById(id);
    }
}
