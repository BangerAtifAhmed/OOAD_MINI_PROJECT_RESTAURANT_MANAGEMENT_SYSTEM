package model.decorator;

import model.FoodItem;

/** Adds extra chilli — ₹15 */
public class ExtraChilliDecorator extends ToppingDecorator {

    private static final double PRICE = 15.0;

    public ExtraChilliDecorator(FoodItem item) { super(item); }

    @Override public String getName()        { return wrappedItem.getName() + " + Extra Chilli"; }
    @Override public double getPrice()       { return wrappedItem.getPrice() + PRICE; }
    @Override public String getDescription() { return wrappedItem.getDescription() + ", Extra Chilli (+₹15)"; }
}
