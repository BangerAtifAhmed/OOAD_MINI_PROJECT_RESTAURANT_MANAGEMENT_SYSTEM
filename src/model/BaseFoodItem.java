package model;

/**
 * Decorator Pattern — Concrete Component (FR-1)
 *
 * Wraps a MenuItem from the database and acts as the root of a decorator chain.
 */
public class BaseFoodItem implements FoodItem {

    private final MenuItem menuItem;

    public BaseFoodItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    @Override public int    getMenuItemId()   { return menuItem.getId(); }
    @Override public String getName()         { return menuItem.getName(); }
    @Override public double getPrice()        { return menuItem.getBasePrice(); }
    @Override public String getDescription()  { return menuItem.getDescription(); }
}
