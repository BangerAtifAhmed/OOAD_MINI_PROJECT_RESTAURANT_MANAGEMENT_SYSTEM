package model;

/**
 * Decorator Pattern — Component Interface (FR-1)
 *
 * Both BaseFoodItem (concrete component) and ToppingDecorator (abstract decorator)
 * implement this interface, allowing toppings to be stacked transparently.
 */
public interface FoodItem {
    /** Full display name (grows as decorators are added) */
    String getName();

    /** Cumulative price including all toppings */
    double getPrice();

    /** Human-readable description listing all toppings */
    String getDescription();

    /** ID of the root menu item in the DB */
    int getMenuItemId();
}
