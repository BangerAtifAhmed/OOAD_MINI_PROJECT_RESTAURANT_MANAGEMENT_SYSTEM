package model.decorator;

import model.FoodItem;

/** Adds extra cheese — ₹30 */
public class CheeseDecorator extends ToppingDecorator {

    private static final double PRICE = 30.0;

    public CheeseDecorator(FoodItem item) { super(item); }

    @Override public String getName()        { return wrappedItem.getName() + " + Extra Cheese"; }
    @Override public double getPrice()       { return wrappedItem.getPrice() + PRICE; }
    @Override public String getDescription() { return wrappedItem.getDescription() + ", Extra Cheese (+₹30)"; }
}
