package model;

public class MenuItem {
    private int id;
    private String name;
    private String category;
    private double basePrice;
    private String description;
    private int prepTimeMinutes;
    private boolean available;

    public MenuItem() {}

    public MenuItem(int id, String name, String category, double basePrice,
                    String description, int prepTimeMinutes, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.basePrice = basePrice;
        this.description = description;
        this.prepTimeMinutes = prepTimeMinutes;
        this.available = available;
    }

    // Getters
    public int getId()              { return id; }
    public String getName()         { return name; }
    public String getCategory()     { return category; }
    public double getBasePrice()    { return basePrice; }
    public String getDescription()  { return description; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public boolean isAvailable()    { return available; }

    // Setters
    public void setId(int id)                       { this.id = id; }
    public void setName(String name)                { this.name = name; }
    public void setCategory(String category)        { this.category = category; }
    public void setBasePrice(double basePrice)      { this.basePrice = basePrice; }
    public void setDescription(String description)  { this.description = description; }
    public void setPrepTimeMinutes(int m)           { this.prepTimeMinutes = m; }
    public void setAvailable(boolean available)     { this.available = available; }

    @Override
    public String toString() {
        return String.format("[%s] %s  —  ₹%.2f  (%d min)", category, name, basePrice, prepTimeMinutes);
    }
}
