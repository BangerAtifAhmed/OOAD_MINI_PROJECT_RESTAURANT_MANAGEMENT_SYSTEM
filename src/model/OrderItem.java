package model;

public class OrderItem {
    private int id;
    private int orderId;
    private int menuItemId;
    private String itemName;           // full decorated name
    private int quantity;
    private double unitPrice;          // decorated (base + toppings) price per unit
    private String toppingsDescription;
    private double toppingsExtraCost;
    private double itemTotal;          // unitPrice * quantity

    public OrderItem() {}

    public OrderItem(int menuItemId, String itemName, int quantity,
                     double unitPrice, String toppingsDescription, double toppingsExtraCost) {
        this.menuItemId          = menuItemId;
        this.itemName            = itemName;
        this.quantity            = quantity;
        this.unitPrice           = unitPrice;
        this.toppingsDescription = toppingsDescription;
        this.toppingsExtraCost   = toppingsExtraCost;
        this.itemTotal           = unitPrice * quantity;
    }

    // Getters
    public int    getId()                    { return id; }
    public int    getOrderId()               { return orderId; }
    public int    getMenuItemId()            { return menuItemId; }
    public String getItemName()              { return itemName; }
    public int    getQuantity()              { return quantity; }
    public double getUnitPrice()             { return unitPrice; }
    public String getToppingsDescription()   { return toppingsDescription; }
    public double getToppingsExtraCost()     { return toppingsExtraCost; }
    public double getItemTotal()             { return itemTotal; }

    // Setters
    public void setId(int id)                              { this.id = id; }
    public void setOrderId(int orderId)                    { this.orderId = orderId; }
    public void setMenuItemId(int menuItemId)              { this.menuItemId = menuItemId; }
    public void setItemName(String itemName)               { this.itemName = itemName; }
    public void setQuantity(int quantity)                  { this.quantity = quantity; this.itemTotal = unitPrice * quantity; }
    public void setUnitPrice(double unitPrice)             { this.unitPrice = unitPrice; this.itemTotal = unitPrice * quantity; }
    public void setToppingsDescription(String td)          { this.toppingsDescription = td; }
    public void setToppingsExtraCost(double tec)           { this.toppingsExtraCost = tec; }
    public void setItemTotal(double itemTotal)             { this.itemTotal = itemTotal; }

    @Override
    public String toString() {
        return String.format("%-30s x%d  ₹%.2f", itemName, quantity, itemTotal);
    }
}
