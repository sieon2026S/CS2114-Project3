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
 * @version 2026.11.11
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
        int size;
        try {
            size = Integer.parseInt(clean(sizeText));
        }
        catch (NumberFormatException e) {
            window.showMessage("Size must be a whole number from 1 to 6.");
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
                restaurant.freeTable(tableId);
                window.showMessage("Table " + tableId + " is open. "
                    + leaving + " has left.");
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

    private String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }
}
