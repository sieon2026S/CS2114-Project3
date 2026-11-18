package model;

import java.util.ArrayList;
import java.util.List;

/**
 * The model facade: holds the waitlist and tables, applies every rule, and
 * notifies listeners after each real change. Never imports a GUI class.
 *
 * @author Sathvik Verma
 * @version 2026.11.18
 */
public class Restaurant {
    private static final int[] FLOOR = { 2, 2, 2, 2, 4, 4, 4, 6 };

    private final Waitlist waitlist = new Waitlist();
    private final List<Table> tables = new ArrayList<>();
    private int nextArrival = 1;
    private Party lastAdded;
    private final List<RestaurantListener> listeners = new ArrayList<>();

    /**
     * Builds the floor: tables 1-4 seat 2, 5-7 seat 4, 8 seats 6.
     */
    public Restaurant() {
        for (int i = 0; i < FLOOR.length; i++) {
            tables.add(new Table(i + 1, FLOOR[i]));
        }
    }

    /**
     * @param listener told about every change
     */
    public void addListener(RestaurantListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null.");
        }
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (RestaurantListener l : listeners) {
            l.modelChanged();
        }
    }

    private Table table(int tableId) {
        if (tableId < 1 || tableId > tables.size()) {
            throw new IllegalArgumentException("There is no table " + tableId
                + ".");
        }
        return tables.get(tableId - 1);
    }

    private boolean nameInUse(String name) {
        if (waitlist.find(name) != null) {
            return true;
        }
        for (Table t : tables) {
            if (!t.isFree()
                && t.getSeatedParty().getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a walk-in party to the waitlist.
     *
     * @param name party name
     * @param size party size, 1 to 8
     * @return the party that was added
     * @throws IllegalArgumentException on a bad name or size, or a name
     *             already waiting or seated
     */
    public Party addParty(String name, int size) {
        Party party = new Party(name, size, nextArrival);
        if (nameInUse(party.getName())) {
            throw new IllegalArgumentException(party.getName()
                + " is already on the list. Try adding a last name.");
        }
        nextArrival++;
        waitlist.add(party);
        lastAdded = party;
        notifyListeners();
        return party;
    }

    /**
     * Seats the right waiting party at a free table.
     *
     * @param tableId table number 1-8
     * @return who was seated, or null if nobody fits (nothing changes)
     * @throws IllegalArgumentException if the table does not exist
     * @throws IllegalStateException if the table is taken
     */
    public Party seatAt(int tableId) {
        Table t = table(tableId);
        if (!t.isFree()) {
            throw new IllegalStateException("Table " + tableId
                + " is already taken.");
        }
        Party party = waitlist.nextFor(t.getCapacity());
        if (party == null) {
            return null;
        }
        t.seat(party);
        if (party == lastAdded) {
            lastAdded = null;
        }
        notifyListeners();
        return party;
    }

    /**
     * Pushes two free 4-tops together and seats the longest-waiting party of
     * 7-8 there. If nobody of 7-8 is waiting, nothing changes.
     *
     * @param tableA first table number
     * @param tableB second table number
     * @return who was seated, or null if no party of 7-8 is waiting
     * @throws IllegalArgumentException if a table does not exist, both
     *             numbers are the same, or either table is not a 4-top
     * @throws IllegalStateException if either table is taken
     */
    public Party seatCombined(int tableA, int tableB) {
        Table a = table(tableA);
        Table b = table(tableB);
        if (a == b) {
            throw new IllegalArgumentException(
                "Pick two different tables to combine.");
        }
        if (a.getCapacity() != 4 || b.getCapacity() != 4) {
            throw new IllegalArgumentException(
                "Only 4-seat tables (5, 6, 7) can be combined.");
        }
        if (!a.isFree() || !b.isFree()) {
            throw new IllegalStateException("Table "
                + (a.isFree() ? tableB : tableA) + " is already taken.");
        }
        Party party = waitlist.nextFor(8);
        if (party == null) {
            return null;
        }
        a.seat(party);
        b.seat(party);
        a.joinWith(b);
        if (party == lastAdded) {
            lastAdded = null;
        }
        notifyListeners();
        return party;
    }

    /**
     * Frees a table. If it was joined to another table, both are freed and
     * split apart. Freeing a free table changes nothing and sends no
     * notification.
     *
     * @param tableId table number 1-8
     * @return true if someone left
     */
    public boolean freeTable(int tableId) {
        Table t = table(tableId);
        Table partner = t.getJoinedWith();
        if (t.clear() == null) {
            return false;
        }
        if (partner != null) {
            partner.clear();
            t.unjoin();
        }
        notifyListeners();
        return true;
    }

    /**
     * Removes a waiting party who walked away.
     *
     * @param name party name
     * @return true if removed, false if nobody by that name is waiting
     */
    public boolean removeParty(String name) {
        Party party = waitlist.find(name);
        if (party == null) {
            return false;
        }
        waitlist.remove(party);
        if (party == lastAdded) {
            lastAdded = null;
        }
        notifyListeners();
        return true;
    }

    /**
     * Removes the most recently added party if it is still waiting.
     *
     * @return the party removed, or null if there is nothing to undo
     */
    public Party undoLastAdd() {
        if (lastAdded == null || !waitlist.remove(lastAdded)) {
            return null;
        }
        Party undone = lastAdded;
        lastAdded = null;
        notifyListeners();
        return undone;
    }

    /** @return a copy of the waitlist in arrival order */
    public List<Party> getWaitingParties() {
        return waitlist.getWaitingInOrder();
    }

    /** @return a copy of the 8 tables, numbered in order */
    public List<Table> getTables() {
        return new ArrayList<>(tables);
    }
    /**
     * Returns the estimated wait time in minutes for a party currently on the waitlist.
     * If a matching free table is immediately available, returns 0.
     *
     * @param name party name
     * @return estimated wait time in minutes, or -1 if the party is not waiting
     */
    public int getEstimatedWait(String name) {
        Party party = waitlist.find(name);
        if (party == null) {
            return -1;
        }
        if (hasImmediateTableFor(party.getSize())) {
            return 0;
        }
        return waitlist.estimateWaitMinutes(party);
    }

    /**
     * Returns the estimated wait time in minutes for a prospective party of the given size.
     *
     * @param size party size (1 to 8)
     * @return estimated wait time in minutes
     * @throws IllegalArgumentException if size is outside 1..8
     */
    public int getEstimatedWaitForSize(int size) {
        if (size < 1 || size > 8) {
            throw new IllegalArgumentException("Party size must be between 1 and 8.");
        }
        if (hasImmediateTableFor(size)) {
            return 0;
        }
        return waitlist.estimateWaitForSize(size);
    }

    private boolean hasImmediateTableFor(int size) {
        if (size >= 7) {
            int freeFourTops = 0;
            for (Table t : tables) {
                if (t.getCapacity() == 4 && t.isFree()) {
                    freeFourTops++;
                }
            }
            return freeFourTops >= 2;
        }
        for (Table t : tables) {
            if (t.isFree() && t.getCapacity() >= size) {
                return true;
            }
        }
        return false;
    }

}
