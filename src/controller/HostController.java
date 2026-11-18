package controller;

import model.Party;
import model.Restaurant;
import model.Table;
import view.TableTimeWindow;

/**
 * Turns the host's clicks and raw text from the view into Restaurant calls,
 * and turns errors into on-screen messages.
 *
 * @author Sieon Lee (package/import fixes by Sathvik Verma)
 * @version 2026.11.18
 */
public class HostController {
    private final Restaurant restaurant;
    private final TableTimeWindow window;

    /**
     * @param restaurant the model
     * @param window the view
     */
    public HostController(Restaurant restaurant, TableTimeWindow window) {
        if (restaurant == null || window == null) {
            throw new IllegalArgumentException(
                "Restaurant and window must not be null.");
        }
        this.restaurant = restaurant;
        this.window = window;
    }

    /**
     * Add button: parses the size and asks the model to add the party.
     *
     * @param nameText raw name text
     * @param sizeText raw size text
     */
    public void handleAdd(String nameText, String sizeText) {
        Integer size = parseWholeNumber(sizeText);
        if (size == null) {
            window.showMessage("Size must be a whole number from 1 to 8.");
            return;
        }

        try {
            Party party = restaurant.addParty(clean(nameText), size);
            window.showMessage("Added " + party.getName() + " (party of "
                + party.getSize() + ").");
            window.clearInputs();
        }
        catch (IllegalArgumentException e) {
            window.showMessage(e.getMessage());
        }
    }

    /**
     * Remove button: removes a party who walked away.
     *
     * @param nameText raw name text
     */
    public void handleRemove(String nameText) {
        String name = clean(nameText);
        try {
            if (restaurant.removeParty(name)) {
                window.showMessage("Removed " + name + " from the waitlist.");
                window.clearInputs();
            }
            else {
                window.showMessage("No waiting party named " + name + ".");
            }
        }
        catch (IllegalArgumentException e) {
            window.showMessage(e.getMessage());
        }
    }

    /**
     * Undo button: removes the most recent add.
     */
    public void handleUndo() {
        Party undone = restaurant.undoLastAdd();
        if (undone == null) {
            window.showMessage("Nothing to undo.");
        }
        else {
            window.showMessage("Undid adding " + undone.getName() + ".");
        }
    }

    /**
     * Combine button: pushes two 4-tops together for a party of 7-8.
     *
     * @param firstText raw text of the first table number
     * @param secondText raw text of the second table number
     */
    public void handleCombine(String firstText, String secondText) {
        Integer first = parseWholeNumber(firstText);
        Integer second = parseWholeNumber(secondText);
        if (first == null || second == null) {
            window.showMessage(
                "Enter two table numbers to combine, like 5 and 6.");
            return;
        }

        try {
            Party seated = restaurant.seatCombined(first, second);
            if (seated == null) {
                window.showMessage("No party of 7-8 is waiting, so tables "
                    + first + " and " + second + " stay apart.");
            }
            else {
                window.showMessage("Combined tables " + first + " + "
                    + second + " for " + seated.getName() + " (party of "
                    + seated.getSize() + ").");
                window.clearCombineInputs();
            }
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            window.showMessage(e.getMessage());
        }
    }

    /**
     * Table click: seats a party at a free table, or frees a taken one.
     *
     * @param tableId the table number
     */
    public void handleTableClick(int tableId) {
        Table table = findTable(tableId);
        if (table == null) {
            window.showMessage("There is no table " + tableId + ".");
            return;
        }

        try {
            if (table.isFree()) {
                Party seated = restaurant.seatAt(tableId);
                if (seated == null) {
                    window.showMessage("No waiting party fits table "
                        + tableId + " (" + table.getCapacity() + " seats).");
                }
                else {
                    window.showMessage("Seated " + seated.getName()
                        + " (party of " + seated.getSize() + ") at table "
                        + tableId + ".");
                }
            }
            else {
                String leaving = table.getSeatedParty().getName();
                Table partner = table.getJoinedWith();
                restaurant.freeTable(tableId);
                if (partner == null) {
                    window.showMessage("Table " + tableId + " is open. "
                        + leaving + " has left.");
                }
                else {
                    window.showMessage("Tables " + tableId + " + "
                        + partner.getNumber() + " are open again. "
                        + leaving + " has left.");
                }
            }
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            window.showMessage(e.getMessage());
        }
    }

    private Table findTable(int tableId) {
        for (Table table : restaurant.getTables()) {
            if (table.getNumber() == tableId) {
                return table;
            }
        }
        return null;
    }

    /**
     * Turns raw text from a box into a whole number. Used for party sizes and
     * table numbers, so bad text is handled the same way everywhere.
     *
     * @param text raw text (may be null, blank, decimal, words, or too big)
     * @return the whole number, or null if the text is not one
     */
    public static Integer parseWholeNumber(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }
}
