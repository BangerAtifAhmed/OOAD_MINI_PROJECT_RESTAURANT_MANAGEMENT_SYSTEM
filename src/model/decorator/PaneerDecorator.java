package model.decorator;

import model.FoodItem;

/** Adds paneer topping — ₹40 */
public class PaneerDecorator extends ToppingDecorator {

    private static final double PRICE = 40.0;

    public PaneerDecorator(FoodItem item) { super(item); }

    @Override public String getName()        { return wrappedItem.getName() + " + Paneer"; }
    @Override public double getPrice()       { return wrappedItem.getPrice() + PRICE; }
    @Override public String getDescription() { return wrappedItem.getDescription() + ", Paneer (+₹40)"; }
}
