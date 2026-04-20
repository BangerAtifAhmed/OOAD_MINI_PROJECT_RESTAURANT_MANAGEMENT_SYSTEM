package model.decorator;

import model.FoodItem;

/** Adds mushroom topping — ₹25 */
public class MushroomDecorator extends ToppingDecorator {

    private static final double PRICE = 25.0;

    public MushroomDecorator(FoodItem item) { super(item); }

    @Override public String getName()        { return wrappedItem.getName() + " + Mushroom"; }
    @Override public double getPrice()       { return wrappedItem.getPrice() + PRICE; }
    @Override public String getDescription() { return wrappedItem.getDescription() + ", Mushroom (+₹25)"; }
}
