package model.decorator;

import model.FoodItem;

/**
 * Decorator Pattern — Abstract Decorator (FR-1)
 *
 * All concrete toppings extend this class. It delegates to the wrapped
 * FoodItem so decorators can be stacked in any order.
 */
public abstract class ToppingDecorator implements FoodItem {

    protected final FoodItem wrappedItem;

    public ToppingDecorator(FoodItem item) {
        this.wrappedItem = item;
    }

    @Override public int    getMenuItemId()  { return wrappedItem.getMenuItemId(); }
    @Override public String getName()        { return wrappedItem.getName(); }
    @Override public double getPrice()       { return wrappedItem.getPrice(); }
    @Override public String getDescription() { return wrappedItem.getDescription(); }
}
