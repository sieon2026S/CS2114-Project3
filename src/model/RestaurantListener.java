package model;

/**
 * The one-method contract the model uses to announce changes without knowing
 * who is listening.
 *
 * @author Zolo
 * @version 2026.11.11
 */
public interface RestaurantListener {
    /**
     * Called after every real change to the restaurant.
     */
    void modelChanged();
}
